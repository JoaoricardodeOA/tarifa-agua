package com.desafio_tecnico.tarifa_agua.service;

import com.desafio_tecnico.tarifa_agua.dto.CategoriaDTO;
import com.desafio_tecnico.tarifa_agua.dto.CategoriaResponse;
import com.desafio_tecnico.tarifa_agua.dto.FaixaDTO;
import com.desafio_tecnico.tarifa_agua.dto.FaixaResponse;
import com.desafio_tecnico.tarifa_agua.dto.TabelaTarifariaCriadaResponse;
import com.desafio_tecnico.tarifa_agua.dto.TabelaTarifariaExclusaoResponse;
import com.desafio_tecnico.tarifa_agua.dto.TabelaTarifariaRequest;
import com.desafio_tecnico.tarifa_agua.dto.TabelaTarifariaResponse;
import com.desafio_tecnico.tarifa_agua.entity.CategoriaConsumidor;
import com.desafio_tecnico.tarifa_agua.entity.FaixaConsumo;
import com.desafio_tecnico.tarifa_agua.entity.TabelaTarifaria;
import com.desafio_tecnico.tarifa_agua.entity.TarifaCategoria;
import com.desafio_tecnico.tarifa_agua.exception.BusinessException;
import com.desafio_tecnico.tarifa_agua.repository.FaixaConsumoRepository;
import com.desafio_tecnico.tarifa_agua.repository.TabelaTarifariaRepository;
import com.desafio_tecnico.tarifa_agua.repository.TarifaCategoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TabelaTarifariaService {

    private final TabelaTarifariaRepository tabelaRepo;
    private final TarifaCategoriaRepository tarifaCatRepo;
    private final FaixaConsumoRepository faixaRepo;

    @Transactional
    public TabelaTarifariaCriadaResponse criarTabela(TabelaTarifariaRequest request) {
        Optional<TabelaTarifaria> tabelaAtiva = tabelaRepo.findFirstByAtivaTrue();

        if (tabelaAtiva.isPresent()) {
            throw new BusinessException(
                    "Já existe uma tabela ativa com id: " +
                            tabelaAtiva.get().getId() +
                            ". Desative-a antes de criar uma nova."
            );
        }

        Map<CategoriaConsumidor, CategoriaDTO> categoriaMap = validarCategoriasObrigatorias(request);

        for (CategoriaDTO cat : categoriaMap.values()) {
            validarFaixas(cat.faixas());
        }

        TabelaTarifaria tabela = new TabelaTarifaria();
        tabela.setNome(request.nome());
        try {
            tabelaRepo.save(tabela);
            tabelaRepo.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(
                    "Não foi possível criar a tabela: já existe uma tabela ativa."
            );
        }

        for (Map.Entry<CategoriaConsumidor, CategoriaDTO> entry : categoriaMap.entrySet()) {
            CategoriaConsumidor categoriaEnum = entry.getKey();
            CategoriaDTO cat = entry.getValue();

            TarifaCategoria tarifaCat = new TarifaCategoria();
            tarifaCat.setTabelaTarifaria(tabela);
            tarifaCat.setCategoria(categoriaEnum);
            tarifaCatRepo.save(tarifaCat);

            for (FaixaDTO faixa : cat.faixas()) {
                FaixaConsumo fc = new FaixaConsumo();
                fc.setTarifaCategoria(tarifaCat);
                fc.setInicio(faixa.inicio());
                fc.setFim(faixa.fim());
                fc.setValorUnitario(faixa.valorUnitario());
                faixaRepo.save(fc);
            }
        }

        return new TabelaTarifariaCriadaResponse(
                tabela.getId(),
                tabela.getDataVigencia(),
                "Tabela criada com sucesso"
        );
    }

    private void validarFaixas(List<FaixaDTO> faixas) {
        if (faixas == null || faixas.isEmpty()) {
            throw new BusinessException("Categoria deve possuir ao menos uma faixa.");
        }

        List<FaixaDTO> ordenadas = faixas.stream()
                .sorted(Comparator.comparing(FaixaDTO::inicio))
                .toList();

        FaixaDTO primeira = ordenadas.get(0);

        if (primeira.inicio() == null) {
            throw new BusinessException("Faixa inicial não pode ser nula.");
        }

        if (primeira.inicio() != 0) {
            throw new BusinessException("As faixas devem iniciar em 0.");
        }

        for (int i = 0; i < ordenadas.size(); i++) {

            FaixaDTO atual = ordenadas.get(i);

            if (atual.inicio() == null)
                throw new BusinessException("Início da faixa não pode ser nulo.");

            if (atual.valorUnitario() == null)
                throw new BusinessException("Valor unitário não pode ser nulo.");

            if (atual.valorUnitario().compareTo(BigDecimal.ZERO) < 0)
                throw new BusinessException("Valor unitário não pode ser negativo.");

            if (atual.fim() != null && atual.fim() < atual.inicio())
                throw new BusinessException("Fim da faixa não pode ser menor que o início.");

            if (i < ordenadas.size() - 1) {

                FaixaDTO proxima = ordenadas.get(i + 1);

                if (atual.fim() == null)
                    throw new BusinessException("Somente a última faixa pode ter fim nulo.");

                if (proxima.inicio() == null)
                    throw new BusinessException("Início da próxima faixa não pode ser nulo.");

                if (atual.fim() >= proxima.inicio())
                    throw new BusinessException("Faixas possuem sobreposição.");

                if (atual.fim() + 1 != proxima.inicio())
                    throw new BusinessException("Existem lacunas entre as faixas.");
            }
        }

        FaixaDTO ultima = ordenadas.get(ordenadas.size() - 1);

        if (ultima.fim() != null) {
            throw new BusinessException(
                    "A última faixa deve ter fim nulo para cobrir consumo ilimitado."
            );
        }
    }

    private Map<CategoriaConsumidor, CategoriaDTO> validarCategoriasObrigatorias(TabelaTarifariaRequest request) {
        Set<CategoriaConsumidor> obrigatorias = Set.of(
                CategoriaConsumidor.COMERCIAL,
                CategoriaConsumidor.INDUSTRIAL,
                CategoriaConsumidor.PARTICULAR,
                CategoriaConsumidor.PUBLICO
        );

        Map<CategoriaConsumidor, CategoriaDTO> categoriaMap = new LinkedHashMap<>();

        for (CategoriaDTO catDTO : request.categorias()) {
            CategoriaConsumidor categoriaEnum;
            try {
                categoriaEnum = CategoriaConsumidor.valueOf(catDTO.categoria().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Categoria inválida: " + catDTO.categoria());
            }

            if (categoriaMap.containsKey(categoriaEnum)) {
                throw new BusinessException("Não é permitido duplicar categorias na tabela.");
            }

            categoriaMap.put(categoriaEnum, catDTO);
        }

        if (!categoriaMap.keySet().containsAll(obrigatorias)) {
            throw new BusinessException(
                    "Tabela deve conter todas as categorias obrigatórias: " + obrigatorias
            );
        }

        if (categoriaMap.size() != obrigatorias.size()) {
            throw new BusinessException(
                    "Tabela deve conter apenas as categorias obrigatórias: " + obrigatorias
            );
        }

        return categoriaMap;
    }

    private TabelaTarifariaResponse toResponse(TabelaTarifaria tabela) {
        List<CategoriaResponse> categorias = tabela.getCategorias().stream()
                .sorted(Comparator.comparing(c -> c.getCategoria().name()))
                .map(cat -> new CategoriaResponse(
                        cat.getCategoria(),
                        cat.getFaixas().stream()
                                .sorted(Comparator.comparing(FaixaConsumo::getInicio))
                                .map(f -> new FaixaResponse(f.getInicio(), f.getFim(), f.getValorUnitario()))
                                .toList()
                ))
                .toList();

        return new TabelaTarifariaResponse(
                tabela.getId(),
                tabela.getNome(),
                tabela.getDataVigencia(),
                tabela.isAtiva(),
                categorias
        );
    }

    @Transactional
    public TabelaTarifariaExclusaoResponse excluir(Long id) {
        TabelaTarifaria tabela = tabelaRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Tabela não encontrada"));

        if (!tabela.isAtiva()) {
            throw new BusinessException("Tabela já está inativa.");
        }
        tabela.setAtiva(false);
        tabelaRepo.save(tabela);
        return new TabelaTarifariaExclusaoResponse(
                tabela.getId(),
                tabela.isAtiva(),
                "Tabela desativada com sucesso"
        );
    }

    @Transactional(readOnly = true)
    public Page<TabelaTarifariaResponse> listar(Pageable pageable) {
        return tabelaRepo.findAll(pageable).map(this::toResponse);
    }
}
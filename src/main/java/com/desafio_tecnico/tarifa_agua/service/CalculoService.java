package com.desafio_tecnico.tarifa_agua.service;

import com.desafio_tecnico.tarifa_agua.dto.CalculoRequest;
import com.desafio_tecnico.tarifa_agua.dto.CalculoResponse;

import com.desafio_tecnico.tarifa_agua.dto.DetalhamentoResponse;
import com.desafio_tecnico.tarifa_agua.dto.FaixaInfo;
import com.desafio_tecnico.tarifa_agua.entity.CategoriaConsumidor;
import com.desafio_tecnico.tarifa_agua.entity.FaixaConsumo;
import com.desafio_tecnico.tarifa_agua.entity.TabelaTarifaria;
import com.desafio_tecnico.tarifa_agua.entity.TarifaCategoria;
import com.desafio_tecnico.tarifa_agua.exception.BusinessException;
import com.desafio_tecnico.tarifa_agua.repository.TabelaTarifariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculoService {

    private final TabelaTarifariaRepository tabelaRepo;

    @Transactional(readOnly = true)
    public CalculoResponse calcular(CalculoRequest request) {

        TabelaTarifaria tabela = tabelaRepo.findAtivaComRelacionamentos()
                .orElseThrow(() -> new BusinessException("Não existe tabela ativa."));

        CategoriaConsumidor categoriaEnum;
        try {
            categoriaEnum = CategoriaConsumidor.valueOf(request.categoria().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Categoria inválida: " + request.categoria());
        }

        TarifaCategoria tarifaCategoria = tabela.getCategorias().stream()
                .filter(c -> c.getCategoria() == categoriaEnum)
                .findFirst()
                .orElseThrow(() -> new BusinessException("Categoria não encontrada na tabela ativa."));

        List<FaixaConsumo> faixasOrdenadas = tarifaCategoria.getFaixas().stream()
                .sorted(Comparator.comparing(FaixaConsumo::getInicio))
                .toList();

        int consumoRestante = request.consumo();
        BigDecimal valorTotal = BigDecimal.ZERO;

        List<DetalhamentoResponse> detalhamento = new ArrayList<>();

        for (FaixaConsumo faixa : faixasOrdenadas) {

            if (consumoRestante <= 0) break;

            int inicio = faixa.getInicio();
            Integer fim = faixa.getFim();
            int limiteFaixa = (fim != null) ? (fim - inicio ) : consumoRestante;

            int m3NaFaixa = Math.min(consumoRestante, limiteFaixa);

            BigDecimal subtotal = faixa.getValorUnitario()
                    .multiply(BigDecimal.valueOf(m3NaFaixa));

            valorTotal = valorTotal.add(subtotal);

            detalhamento.add(
                    new DetalhamentoResponse(
                            new FaixaInfo(inicio, fim),
                            m3NaFaixa,
                            faixa.getValorUnitario(),
                            subtotal
                    )
            );

            consumoRestante -= m3NaFaixa;
        }

        return new CalculoResponse(
                categoriaEnum.name(),
                request.consumo(),
                valorTotal,
                detalhamento
        );
    }

}

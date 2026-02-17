package com.desafio_tecnico.tarifa_agua.service;

import com.desafio_tecnico.tarifa_agua.dto.CategoriaDTO;
import com.desafio_tecnico.tarifa_agua.dto.FaixaDTO;
import com.desafio_tecnico.tarifa_agua.dto.TabelaTarifariaCriadaResponse;
import com.desafio_tecnico.tarifa_agua.dto.TabelaTarifariaExclusaoResponse;
import com.desafio_tecnico.tarifa_agua.dto.TabelaTarifariaRequest;
import com.desafio_tecnico.tarifa_agua.entity.TabelaTarifaria;
import com.desafio_tecnico.tarifa_agua.exception.BusinessException;
import com.desafio_tecnico.tarifa_agua.repository.FaixaConsumoRepository;
import com.desafio_tecnico.tarifa_agua.repository.TabelaTarifariaRepository;
import com.desafio_tecnico.tarifa_agua.repository.TarifaCategoriaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TabelaTarifariaServiceTest {

    @Mock
    private TabelaTarifariaRepository tabelaRepo;

    @Mock
    private TarifaCategoriaRepository tarifaCatRepo;

    @Mock
    private FaixaConsumoRepository faixaRepo;

    @InjectMocks
    private TabelaTarifariaService service;

    // ======================= criarTabela() =======================

    @Nested
    @DisplayName("criarTabela()")
    class CriarTabela {

        @Test
        @DisplayName("deve lançar exceção quando já existe tabela ativa")
        void deveLancarExcecaoComTabelaAtiva() {
            TabelaTarifaria existente = new TabelaTarifaria();
            existente.setId(99L);
            when(tabelaRepo.findFirstByAtivaTrue()).thenReturn(Optional.of(existente));

            TabelaTarifariaRequest request = criarRequestValida();

            assertThatThrownBy(() -> service.criarTabela(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Já existe uma tabela ativa com id: 99");
        }

        @Test
        @DisplayName("deve criar tabela com sucesso quando não existe tabela ativa")
        void deveCriarTabelaComSucesso() {
            when(tabelaRepo.findFirstByAtivaTrue()).thenReturn(Optional.empty());
            when(tabelaRepo.save(any(TabelaTarifaria.class))).thenAnswer(invocation -> {
                TabelaTarifaria t = invocation.getArgument(0);
                t.setId(1L);
                return t;
            });

            TabelaTarifariaRequest request = criarRequestValida();
            TabelaTarifariaCriadaResponse response = service.criarTabela(request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.mensagem()).isEqualTo("Tabela criada com sucesso");
            verify(tabelaRepo).save(any(TabelaTarifaria.class));
        }

        @Test
        @DisplayName("deve lançar exceção para categoria inválida")
        void deveLancarExcecaoCategoriaInvalida() {
            when(tabelaRepo.findFirstByAtivaTrue()).thenReturn(Optional.empty());

            List<FaixaDTO> faixas = List.of(
                    new FaixaDTO(0, null, new BigDecimal("5.00"))
            );
            TabelaTarifariaRequest request = new TabelaTarifariaRequest("Tabela", List.of(
                    new CategoriaDTO("INVALIDA", faixas),
                    new CategoriaDTO("COMERCIAL", faixas),
                    new CategoriaDTO("INDUSTRIAL", faixas),
                    new CategoriaDTO("PUBLICO", faixas)
            ));

            assertThatThrownBy(() -> service.criarTabela(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Categoria inválida");
        }

        @Test
        @DisplayName("deve lançar exceção quando faltam categorias obrigatórias")
        void deveLancarExcecaoFaltandoCategorias() {
            when(tabelaRepo.findFirstByAtivaTrue()).thenReturn(Optional.empty());

            List<FaixaDTO> faixas = List.of(
                    new FaixaDTO(0, null, new BigDecimal("5.00"))
            );
            TabelaTarifariaRequest request = new TabelaTarifariaRequest("Tabela", List.of(
                    new CategoriaDTO("COMERCIAL", faixas),
                    new CategoriaDTO("INDUSTRIAL", faixas)
            ));

            assertThatThrownBy(() -> service.criarTabela(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("categorias obrigatórias");
        }

        @Test
        @DisplayName("deve lançar exceção quando há categorias duplicadas")
        void deveLancarExcecaoCategoriasDuplicadas() {
            when(tabelaRepo.findFirstByAtivaTrue()).thenReturn(Optional.empty());

            List<FaixaDTO> faixas = List.of(
                    new FaixaDTO(0, null, new BigDecimal("5.00"))
            );
            TabelaTarifariaRequest request = new TabelaTarifariaRequest("Tabela", List.of(
                    new CategoriaDTO("COMERCIAL", faixas),
                    new CategoriaDTO("COMERCIAL", faixas),
                    new CategoriaDTO("INDUSTRIAL", faixas),
                    new CategoriaDTO("PARTICULAR", faixas),
                    new CategoriaDTO("PUBLICO", faixas)
            ));

            assertThatThrownBy(() -> service.criarTabela(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("duplicar categorias");
        }
    }

    // ======================= validarFaixas() =======================

    @Nested
    @DisplayName("validarFaixas() — via criarTabela()")
    class ValidarFaixas {

        @Test
        @DisplayName("deve lançar exceção quando faixas não iniciam em 0")
        void deveLancarExcecaoFaixaNaoIniciaEmZero() {
            when(tabelaRepo.findFirstByAtivaTrue()).thenReturn(Optional.empty());

            TabelaTarifariaRequest request = criarRequestComFaixas(List.of(
                    new FaixaDTO(5, null, new BigDecimal("5.00"))
            ));

            assertThatThrownBy(() -> service.criarTabela(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("As faixas devem iniciar em 0.");
        }

        @Test
        @DisplayName("deve lançar exceção quando última faixa tem fim não-nulo")
        void deveLancarExcecaoUltimaFaixaComFim() {
            when(tabelaRepo.findFirstByAtivaTrue()).thenReturn(Optional.empty());

            TabelaTarifariaRequest request = criarRequestComFaixas(List.of(
                    new FaixaDTO(0, 10, new BigDecimal("5.00"))
            ));

            assertThatThrownBy(() -> service.criarTabela(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("A última faixa deve ter fim nulo para cobrir consumo ilimitado.");
        }

        @Test
        @DisplayName("deve lançar exceção quando há sobreposição entre faixas")
        void deveLancarExcecaoSobreposicao() {
            when(tabelaRepo.findFirstByAtivaTrue()).thenReturn(Optional.empty());

            TabelaTarifariaRequest request = criarRequestComFaixas(List.of(
                    new FaixaDTO(0, 10, new BigDecimal("5.00")),
                    new FaixaDTO(8, null, new BigDecimal("10.00"))
            ));

            assertThatThrownBy(() -> service.criarTabela(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Faixas possuem sobreposição.");
        }

        @Test
        @DisplayName("deve lançar exceção quando há lacunas entre faixas")
        void deveLancarExcecaoLacuna() {
            when(tabelaRepo.findFirstByAtivaTrue()).thenReturn(Optional.empty());

            TabelaTarifariaRequest request = criarRequestComFaixas(List.of(
                    new FaixaDTO(0, 10, new BigDecimal("5.00")),
                    new FaixaDTO(15, null, new BigDecimal("10.00"))
            ));

            assertThatThrownBy(() -> service.criarTabela(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Existem lacunas entre as faixas.");
        }

        @Test
        @DisplayName("deve lançar exceção quando fim é menor que início")
        void deveLancarExcecaoFimMenorQueInicio() {
            when(tabelaRepo.findFirstByAtivaTrue()).thenReturn(Optional.empty());

            TabelaTarifariaRequest request = criarRequestComFaixas(List.of(
                    new FaixaDTO(0, -1, new BigDecimal("5.00")),
                    new FaixaDTO(10, null, new BigDecimal("10.00"))
            ));

            assertThatThrownBy(() -> service.criarTabela(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Fim da faixa não pode ser menor que o início.");
        }

        @Test
        @DisplayName("deve lançar exceção quando valor unitário é negativo")
        void deveLancarExcecaoValorNegativo() {
            when(tabelaRepo.findFirstByAtivaTrue()).thenReturn(Optional.empty());

            TabelaTarifariaRequest request = criarRequestComFaixas(List.of(
                    new FaixaDTO(0, null, new BigDecimal("-1.00"))
            ));

            assertThatThrownBy(() -> service.criarTabela(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Valor unitário não pode ser negativo.");
        }

        @Test
        @DisplayName("deve lançar exceção quando faixa intermediária tem fim nulo")
        void deveLancarExcecaoFaixaIntermediariaFimNulo() {
            when(tabelaRepo.findFirstByAtivaTrue()).thenReturn(Optional.empty());

            TabelaTarifariaRequest request = criarRequestComFaixas(List.of(
                    new FaixaDTO(0, null, new BigDecimal("5.00")),
                    new FaixaDTO(10, null, new BigDecimal("10.00"))
            ));

            assertThatThrownBy(() -> service.criarTabela(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Somente a última faixa pode ter fim nulo.");
        }

        @Test
        @DisplayName("deve aceitar faixas válidas contíguas")
        void deveAceitarFaixasValidas() {
            when(tabelaRepo.findFirstByAtivaTrue()).thenReturn(Optional.empty());
            when(tabelaRepo.save(any(TabelaTarifaria.class))).thenAnswer(invocation -> {
                TabelaTarifaria t = invocation.getArgument(0);
                t.setId(1L);
                return t;
            });

            TabelaTarifariaRequest request = criarRequestComFaixas(List.of(
                    new FaixaDTO(0, 10, new BigDecimal("5.00")),
                    new FaixaDTO(11, 20, new BigDecimal("8.00")),
                    new FaixaDTO(21, null, new BigDecimal("12.00"))
            ));

            TabelaTarifariaCriadaResponse response = service.criarTabela(request);

            assertThat(response.id()).isEqualTo(1L);
        }
    }

    // ======================= excluir() =======================

    @Nested
    @DisplayName("excluir()")
    class Excluir {

        @Test
        @DisplayName("deve lançar exceção quando tabela não existe")
        void deveLancarExcecaoTabelaNaoEncontrada() {
            when(tabelaRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.excluir(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Tabela não encontrada");
        }

        @Test
        @DisplayName("deve lançar exceção quando tabela já está inativa")
        void deveLancarExcecaoTabelaJaInativa() {
            TabelaTarifaria tabela = new TabelaTarifaria();
            tabela.setId(1L);
            tabela.setAtiva(false);
            when(tabelaRepo.findById(1L)).thenReturn(Optional.of(tabela));

            assertThatThrownBy(() -> service.excluir(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Tabela já está inativa.");
        }

        @Test
        @DisplayName("deve desativar tabela ativa com sucesso")
        void deveDesativarTabelaComSucesso() {
            TabelaTarifaria tabela = new TabelaTarifaria();
            tabela.setId(1L);
            tabela.setNome("Tabela Teste");
            tabela.setAtiva(true);
            when(tabelaRepo.findById(1L)).thenReturn(Optional.of(tabela));
            when(tabelaRepo.save(tabela)).thenReturn(tabela);

            TabelaTarifariaExclusaoResponse response = service.excluir(1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.ativa()).isFalse();
            assertThat(response.mensagem()).isEqualTo("Tabela desativada com sucesso");
            verify(tabelaRepo).save(tabela);
        }

        @Test
        @DisplayName("deve chamar save() explicitamente ao desativar")
        void deveChamarSaveExplicitamente() {
            TabelaTarifaria tabela = new TabelaTarifaria();
            tabela.setId(1L);
            tabela.setAtiva(true);
            when(tabelaRepo.findById(1L)).thenReturn(Optional.of(tabela));
            when(tabelaRepo.save(tabela)).thenReturn(tabela);

            service.excluir(1L);

            verify(tabelaRepo).save(tabela);
        }
    }

    // ======================= Helpers =======================

    private TabelaTarifariaRequest criarRequestValida() {
        List<FaixaDTO> faixas = List.of(
                new FaixaDTO(0, 10, new BigDecimal("5.00")),
                new FaixaDTO(11, null, new BigDecimal("10.00"))
        );
        return new TabelaTarifariaRequest("Tabela Teste", List.of(
                new CategoriaDTO("COMERCIAL", faixas),
                new CategoriaDTO("INDUSTRIAL", faixas),
                new CategoriaDTO("PARTICULAR", faixas),
                new CategoriaDTO("PUBLICO", faixas)
        ));
    }

    /**
     * Cria um request com as 4 categorias obrigatórias, todas usando as mesmas faixas fornecidas.
     */
    private TabelaTarifariaRequest criarRequestComFaixas(List<FaixaDTO> faixas) {
        return new TabelaTarifariaRequest("Tabela Teste", List.of(
                new CategoriaDTO("COMERCIAL", faixas),
                new CategoriaDTO("INDUSTRIAL", faixas),
                new CategoriaDTO("PARTICULAR", faixas),
                new CategoriaDTO("PUBLICO", faixas)
        ));
    }
}

package com.desafio_tecnico.tarifa_agua.service;

import com.desafio_tecnico.tarifa_agua.dto.CalculoRequest;
import com.desafio_tecnico.tarifa_agua.dto.CalculoResponse;
import com.desafio_tecnico.tarifa_agua.entity.CategoriaConsumidor;
import com.desafio_tecnico.tarifa_agua.entity.FaixaConsumo;
import com.desafio_tecnico.tarifa_agua.entity.TabelaTarifaria;
import com.desafio_tecnico.tarifa_agua.entity.TarifaCategoria;
import com.desafio_tecnico.tarifa_agua.exception.BusinessException;
import com.desafio_tecnico.tarifa_agua.repository.TabelaTarifariaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculoServiceTest {

    @Mock
    private TabelaTarifariaRepository tabelaRepo;

    @InjectMocks
    private CalculoService calculoService;

    @Nested
    @DisplayName("calcular()")
    class Calcular {

        @Test
        @DisplayName("deve lançar exceção quando não existe tabela ativa")
        void deveLancarExcecaoSemTabelaAtiva() {
            when(tabelaRepo.findAtivaComRelacionamentos()).thenReturn(Optional.empty());

            assertThatThrownBy(() -> calculoService.calcular(new CalculoRequest("COMERCIAL", 10)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Não existe tabela ativa.");
        }

        @Test
        @DisplayName("deve lançar exceção para categoria inválida")
        void deveLancarExcecaoCategoriaInvalida() {
            TabelaTarifaria tabela = criarTabelaComFaixas();
            when(tabelaRepo.findAtivaComRelacionamentos()).thenReturn(Optional.of(tabela));

            assertThatThrownBy(() -> calculoService.calcular(new CalculoRequest("INVALIDA", 10)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Categoria inválida: INVALIDA");
        }

        @Test
        @DisplayName("deve lançar exceção quando categoria não existe na tabela ativa")
        void deveLancarExcecaoCategoriaNaoEncontrada() {
            TabelaTarifaria tabela = criarTabelaComFaixas();
            when(tabelaRepo.findAtivaComRelacionamentos()).thenReturn(Optional.of(tabela));

            assertThatThrownBy(() -> calculoService.calcular(new CalculoRequest("PUBLICO", 10)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Categoria não encontrada na tabela ativa.");
        }

        @Test
        @DisplayName("deve calcular consumo zero corretamente")
        void deveCalcularConsumoZero() {
            TabelaTarifaria tabela = criarTabelaComFaixas();
            when(tabelaRepo.findAtivaComRelacionamentos()).thenReturn(Optional.of(tabela));

            CalculoResponse response = calculoService.calcular(new CalculoRequest("COMERCIAL", 0));

            assertThat(response.valorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.consumoTotal()).isZero();
            assertThat(response.detalhamento()).isEmpty();
        }

        @Test
        @DisplayName("deve calcular consumo dentro de uma única faixa")
        void deveCalcularConsumoUmaFaixa() {
            // Faixa: [0-10) @ R$5.00
            TabelaTarifaria tabela = criarTabelaComFaixas();
            when(tabelaRepo.findAtivaComRelacionamentos()).thenReturn(Optional.of(tabela));

            CalculoResponse response = calculoService.calcular(new CalculoRequest("COMERCIAL", 5));

            // 5 m³ × R$5.00 = R$25.00
            assertThat(response.valorTotal()).isEqualByComparingTo(new BigDecimal("25.00"));
            assertThat(response.categoria()).isEqualTo("COMERCIAL");
            assertThat(response.consumoTotal()).isEqualTo(5);
            assertThat(response.detalhamento()).hasSize(1);
            assertThat(response.detalhamento().get(0).m3Cobrados()).isEqualTo(5);
        }

        @Test
        @DisplayName("deve calcular consumo com múltiplas faixas")
        void deveCalcularConsumoMultiplasFaixas() {
            // Faixas: [0-10) @ R$5.00, [10-null) @ R$10.00
            TabelaTarifaria tabela = criarTabelaComFaixas();
            when(tabelaRepo.findAtivaComRelacionamentos()).thenReturn(Optional.of(tabela));

            CalculoResponse response = calculoService.calcular(new CalculoRequest("COMERCIAL", 15));

            // Faixa 1: 10 m³ × R$5.00 = R$50.00 (limiteFaixa = fim - inicio = 10 - 0 = 10)
            // Faixa 2: 5 m³ × R$10.00 = R$50.00
            // Total: R$100.00
            assertThat(response.valorTotal()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(response.detalhamento()).hasSize(2);
            assertThat(response.detalhamento().get(0).m3Cobrados()).isEqualTo(10);
            assertThat(response.detalhamento().get(1).m3Cobrados()).isEqualTo(5);
        }

        @Test
        @DisplayName("deve aceitar categoria em lowercase")
        void deveAceitarCategoriaLowercase() {
            TabelaTarifaria tabela = criarTabelaComFaixas();
            when(tabelaRepo.findAtivaComRelacionamentos()).thenReturn(Optional.of(tabela));

            CalculoResponse response = calculoService.calcular(new CalculoRequest("comercial", 5));

            assertThat(response.categoria()).isEqualTo("COMERCIAL");
        }

        @Test
        @DisplayName("deve retornar detalhamento com faixa info corretos")
        void deveRetornarDetalhamentoComFaixaInfo() {
            TabelaTarifaria tabela = criarTabelaComFaixas();
            when(tabelaRepo.findAtivaComRelacionamentos()).thenReturn(Optional.of(tabela));

            CalculoResponse response = calculoService.calcular(new CalculoRequest("COMERCIAL", 15));

            assertThat(response.detalhamento().get(0).faixa().inicio()).isZero();
            assertThat(response.detalhamento().get(0).faixa().fim()).isEqualTo(10);
            assertThat(response.detalhamento().get(0).valorUnitario()).isEqualByComparingTo(new BigDecimal("5.00"));

            assertThat(response.detalhamento().get(1).faixa().inicio()).isEqualTo(10);
            assertThat(response.detalhamento().get(1).faixa().fim()).isNull();
            assertThat(response.detalhamento().get(1).valorUnitario()).isEqualByComparingTo(new BigDecimal("10.00"));
        }
    }

    /**
     * Cria uma TabelaTarifaria com uma categoria COMERCIAL contendo duas faixas:
     * - [0, 10) @ R$5.00
     * - [10, null) @ R$10.00
     */
    private TabelaTarifaria criarTabelaComFaixas() {
        TabelaTarifaria tabela = new TabelaTarifaria();
        tabela.setId(1L);
        tabela.setNome("Tabela Teste");
        tabela.setAtiva(true);

        TarifaCategoria categoria = new TarifaCategoria();
        categoria.setId(1L);
        categoria.setTabelaTarifaria(tabela);
        categoria.setCategoria(CategoriaConsumidor.COMERCIAL);

        FaixaConsumo faixa1 = new FaixaConsumo();
        faixa1.setId(1L);
        faixa1.setTarifaCategoria(categoria);
        faixa1.setInicio(0);
        faixa1.setFim(10);
        faixa1.setValorUnitario(new BigDecimal("5.00"));

        FaixaConsumo faixa2 = new FaixaConsumo();
        faixa2.setId(2L);
        faixa2.setTarifaCategoria(categoria);
        faixa2.setInicio(10);
        faixa2.setFim(null);
        faixa2.setValorUnitario(new BigDecimal("10.00"));

        categoria.setFaixas(Set.of(faixa1, faixa2));
        tabela.setCategorias(Set.of(categoria));

        return tabela;
    }
}

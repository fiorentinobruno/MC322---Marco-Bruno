package br.unicamp.ic.mc322.strategy;

import br.unicamp.ic.mc322.model.Demanda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Critérios de escolha das implementações de {@link EstrategiaProducao} (padrão Strategy).
 */
class EstrategiasTest {

    private Demanda simples;
    private Demanda dupla;
    private Demanda cancelada;
    private List<Demanda> demandas;

    @BeforeEach
    void prepararDemandas() {
        cancelada = new Demanda("Placa Multi-camada", 50, 30.0);
        cancelada.cancelar();
        simples = new Demanda("Placa Simples", 2, 30.0);
        dupla = new Demanda("Placa Dupla", 7, 30.0);
        demandas = List.of(cancelada, simples, dupla);
    }

    @Test
    @DisplayName("Ordem de Chegada escolhe a primeira demanda elegível")
    void ordemDeChegada() {
        assertSame(simples, new EstrategiaOrdemChegada().selecionarDemanda(demandas, 1000.0));
    }

    @Test
    @DisplayName("Maior Demanda escolhe a que tem mais unidades pendentes, ignorando canceladas")
    void maiorDemanda() {
        assertSame(dupla, new EstrategiaMaiorDemanda().selecionarDemanda(demandas, 1000.0));
    }

    @Test
    @DisplayName("Maximizar Produção considera quantas unidades cabem no orçamento")
    void maximizarProducao() {
        assertSame(dupla, new EstrategiaMaximoProdutos().selecionarDemanda(demandas, 1000.0));
    }

    @Test
    @DisplayName("Sem orçamento para uma unidade, nenhuma demanda é elegível")
    void semOrcamento() {
        assertNull(new EstrategiaOrdemChegada().selecionarDemanda(demandas, 10.0));
        assertNull(new EstrategiaMaiorDemanda().selecionarDemanda(demandas, 10.0));
        assertNull(new EstrategiaMaximoProdutos().selecionarDemanda(demandas, 10.0));
    }
}

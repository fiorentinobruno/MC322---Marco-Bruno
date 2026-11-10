package br.unicamp.ic.mc322.model;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.EntradaInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regras de negócio da {@link Demanda}: criação, transições de {@link StatusDemanda} e restauração.
 */
class DemandaTest {

    @Test
    @DisplayName("Demanda nova começa pendente com a quantidade informada")
    void novaDemandaComecaPendente() {
        Demanda d = new Demanda("Placa Simples", 3, 30.0);
        assertEquals(StatusDemanda.PENDENTE, d.getStatus());
        assertEquals(3, d.getQuantidadeProdutos());
        assertEquals(90.0, d.getCustoTotalEstimado(), 1e-9);
    }

    @Test
    @DisplayName("Quantidade não positiva é rejeitada na criação")
    void quantidadeInvalidaLancaExcecao() {
        assertThrows(EntradaInvalidaException.class, () -> new Demanda("Placa Simples", 0, 30.0));
        assertThrows(EntradaInvalidaException.class, () -> new Demanda("Placa Simples", -2, 30.0));
        assertThrows(EntradaInvalidaException.class, () -> new Demanda(" ", 1, 30.0));
    }

    @Test
    @DisplayName("Produzir todas as unidades conclui a demanda")
    void registrarProducaoAteZerarConclui() {
        Demanda d = new Demanda("Placa Dupla", 2, 30.0);
        d.iniciarProducao();
        assertEquals(StatusDemanda.EM_PRODUCAO, d.getStatus());
        d.registrarProducao(1);
        assertEquals(1, d.getQuantidadeProdutos());
        d.registrarProducao(1);
        assertEquals(0, d.getQuantidadeProdutos());
        assertEquals(StatusDemanda.CONCLUIDA, d.getStatus());
    }

    @Test
    @DisplayName("Demanda cancelada não pode ser alterada nem receber produção")
    void demandaCanceladaNaoPodeSerAlterada() {
        Demanda d = new Demanda("Placa Simples", 2, 30.0);
        d.cancelar();
        assertThrows(DemandaInvalidaException.class, () -> d.atualizarQuantidade(5));
        assertThrows(DemandaInvalidaException.class, () -> d.registrarProducao(1));
        assertEquals(2, d.getQuantidadeProdutos());
        assertEquals(StatusDemanda.CANCELADA, d.getStatus());
    }

    @Test
    @DisplayName("Demanda concluída não pode ser cancelada, mas é reaberta ao receber novas unidades")
    void demandaConcluida() {
        Demanda d = new Demanda("Placa Simples", 1, 30.0);
        d.registrarProducao(1);
        assertThrows(DemandaInvalidaException.class, d::cancelar);
        d.atualizarQuantidade(4);
        assertEquals(StatusDemanda.PENDENTE, d.getStatus());
        assertEquals(4, d.getQuantidadeProdutos());
    }

    @Test
    @DisplayName("Restaurar mantém o status e rejeita combinações que violam as invariantes")
    void restaurarValidaInvariantes() {
        Demanda d = Demanda.restaurar("Placa Dupla", 5, 30.0, StatusDemanda.EM_PRODUCAO);
        assertEquals(StatusDemanda.EM_PRODUCAO, d.getStatus());
        assertEquals(5, d.getQuantidadeProdutos());

        Demanda concluida = Demanda.restaurar("Placa Dupla", 0, 30.0, StatusDemanda.CONCLUIDA);
        assertEquals(0, concluida.getQuantidadeProdutos());

        assertThrows(EntradaInvalidaException.class, () -> Demanda.restaurar("Placa Dupla", 3, 30.0, StatusDemanda.CONCLUIDA));
        assertThrows(EntradaInvalidaException.class, () -> Demanda.restaurar("Placa Dupla", 0, 30.0, StatusDemanda.PENDENTE));
        assertThrows(EntradaInvalidaException.class, () -> Demanda.restaurar("Placa Dupla", -1, 30.0, StatusDemanda.CANCELADA));
        assertThrows(EntradaInvalidaException.class, () -> Demanda.restaurar("Placa Dupla", 1, -3.0, StatusDemanda.PENDENTE));
    }
}

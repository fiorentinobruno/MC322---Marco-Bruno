package br.unicamp.ic.mc322.model;

import br.unicamp.ic.mc322.exception.EntradaInvalidaException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Controle de estoque da {@link MateriaPrima}: consumo, reposição e estoque mínimo.
 */
class MateriaPrimaTest {

    private MateriaPrima laminado;

    @BeforeEach
    void prepararEstoque() {
        laminado = new MateriaPrima("MP1", "Laminado", 50.0, "dm2", 20.0, 5.0);
    }

    @Test
    @DisplayName("Consumir e repor alteram o estoque")
    void consumirEReporAlteramEstoque() {
        laminado.consumir(12.0);
        assertEquals(38.0, laminado.getQuantidade(), 1e-9);
        laminado.adicionarEstoque(2.0);
        assertEquals(40.0, laminado.getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("Consumir mais do que o disponível lança RecursoInsuficienteException sem alterar o estoque")
    void consumoAlemDoDisponivel() {
        RecursoInsuficienteException e = assertThrows(RecursoInsuficienteException.class, () -> laminado.consumir(60.0));
        assertTrue(e.getMessage().contains("insuficiente"));
        assertEquals(50.0, laminado.getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("Estoque abaixo do mínimo operacional impede o consumo")
    void estoqueAbaixoDoMinimo() {
        MateriaPrima pouco = new MateriaPrima("MP1", "Laminado", 15.0, "dm2", 20.0, 5.0);
        assertThrows(RecursoInsuficienteException.class, () -> pouco.consumir(4.0));
        assertEquals(15.0, pouco.getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("Valores inválidos são rejeitados")
    void valoresInvalidos() {
        assertThrows(EntradaInvalidaException.class, () -> laminado.adicionarEstoque(-1.0));
        assertThrows(EntradaInvalidaException.class, () -> laminado.adicionarEstoque(Double.NaN));
        assertThrows(EntradaInvalidaException.class, () -> new MateriaPrima("MP1", "Laminado", -1.0, "dm2", 0, 1));
        assertThrows(EntradaInvalidaException.class, () -> new MateriaPrima("", "Laminado", 1.0, "dm2", 0, 1));
    }
}

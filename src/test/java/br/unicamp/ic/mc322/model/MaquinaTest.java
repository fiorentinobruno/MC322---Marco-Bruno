package br.unicamp.ic.mc322.model;

import br.unicamp.ic.mc322.exception.EntradaInvalidaException;
import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Composição da {@link Maquina} (motor, sensor e ventoinhas), restauração de estado e regras que
 * impedem a operação, além da agregação na {@link LinhaDeProducao}.
 */
class MaquinaTest {

    private Corrosora corrosora;

    @BeforeEach
    void criarMaquina() {
        corrosora = new Corrosora("Corrosora CU-1", 60.0, 10.0, 0.3, 2);
    }

    @Test
    @DisplayName("Máquina nova é criada com suas partes em estado perfeito")
    void maquinaNovaComPartes() {
        assertEquals(100.0, corrosora.getSaude(), 1e-9);
        assertEquals(100.0, corrosora.getSaudeMotor(), 1e-9);
        assertEquals(2, corrosora.getQuantidadeVentoinhas());
        assertEquals(2, corrosora.contarVentoinhasOperantes());
        assertEquals(25.0, corrosora.lerTemperaturaAtual(), 1e-9);
        assertFalse(corrosora.estaLigada());
        assertFalse(corrosora.precisaManutencao());
    }

    @Test
    @DisplayName("Configuração inválida é rejeitada no construtor")
    void configuracaoInvalida() {
        assertThrows(EntradaInvalidaException.class, () -> new Corrosora("C", 60.0, 10.0, 0.3, 0));
        assertThrows(EntradaInvalidaException.class, () -> new Corrosora("C", -1.0, 10.0, 0.3, 1));
        assertThrows(EntradaInvalidaException.class, () -> new Corrosora("C", 60.0, 10.0, 1.5, 1));
    }

    @Test
    @DisplayName("Restaurar estado aplica os valores à máquina e às partes")
    void restaurarEstado() {
        corrosora.restaurarEstado(45.5, 20.0, 50.0, new double[]{80.0, 0.0});
        assertEquals(45.5, corrosora.getSaude(), 1e-9);
        assertEquals(20.0, corrosora.getSaudeMotor(), 1e-9);
        assertEquals(50.0, corrosora.lerTemperaturaAtual(), 1e-9);
        assertEquals(80.0, corrosora.getSaudeVentoinha(0), 1e-9);
        assertEquals(1, corrosora.contarVentoinhasOperantes());
        assertTrue(corrosora.precisaManutencao());
    }

    @Test
    @DisplayName("Restaurar com valor inválido lança exceção e não altera nada")
    void restaurarInvalidoNaoAltera() {
        assertThrows(EntradaInvalidaException.class,
                () -> corrosora.restaurarEstado(150.0, 50.0, 30.0, new double[]{50.0, 50.0}));
        assertThrows(EntradaInvalidaException.class,
                () -> corrosora.restaurarEstado(50.0, 50.0, 30.0, new double[]{50.0}));
        assertThrows(EntradaInvalidaException.class,
                () -> corrosora.restaurarEstado(50.0, 50.0, 10.0, new double[]{50.0, 50.0}));
        assertEquals(100.0, corrosora.getSaude(), 1e-9);
        assertEquals(100.0, corrosora.getSaudeMotor(), 1e-9);
        assertEquals(100.0, corrosora.getSaudeVentoinha(1), 1e-9);
    }

    @Test
    @DisplayName("Máquina quebrada ou superaquecida não liga até ser reparada")
    void maquinaIndisponivelNaoLiga() {
        corrosora.restaurarEstado(0.0, 100.0, 25.0, new double[]{100.0, 100.0});
        assertTrue(corrosora.estaQuebrada());
        assertThrows(MaquinaIndisponivelException.class, corrosora::ligar);

        corrosora.restaurarEstado(100.0, 100.0, corrosora.getTemperaturaMaxima(), new double[]{100.0, 100.0});
        assertThrows(MaquinaIndisponivelException.class, corrosora::ligar);

        corrosora.reparar();
        assertDoesNotThrow(corrosora::ligar);
        assertTrue(corrosora.estaLigada());
        assertEquals(25.0, corrosora.lerTemperaturaAtual(), 1e-9);
    }

    @Test
    @DisplayName("Máquina desligada não processa")
    void maquinaDesligadaNaoProcessa() {
        MateriaPrima laminado = new MateriaPrima("MP1", "Laminado", 100.0, "dm2", 0.0, 1.0);
        Produto placa = FabricaDePlacas.criar("Placa Simples", "PCB-1");
        assertThrows(MaquinaIndisponivelException.class, () -> corrosora.processar(laminado, placa));
        assertEquals(100.0, laminado.getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("A linha agrega máquinas sem duplicá-las e não expõe a lista para alteração")
    void linhaDeProducaoAgregaMaquinas() {
        LinhaDeProducao linha = new LinhaDeProducao("Linha");
        linha.adicionarMaquina(corrosora);
        assertThrows(EntradaInvalidaException.class, () -> linha.adicionarMaquina(corrosora));
        assertThrows(EntradaInvalidaException.class, () -> linha.adicionarMaquina(null));
        List<Maquina> maquinas = linha.getMaquinas();
        assertEquals(1, maquinas.size());
        assertThrows(UnsupportedOperationException.class, () -> maquinas.add(corrosora));
    }
}

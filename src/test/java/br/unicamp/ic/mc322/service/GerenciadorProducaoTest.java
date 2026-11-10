package br.unicamp.ic.mc322.service;

import br.unicamp.ic.mc322.GeradorFixo;
import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.EntradaInvalidaException;
import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.exception.OrcamentoInsuficienteException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Corrosora;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.Embaladora;
import br.unicamp.ic.mc322.model.EstacaoInspecao;
import br.unicamp.ic.mc322.model.LinhaDeProducao;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.strategy.EstrategiaMaiorDemanda;
import br.unicamp.ic.mc322.strategy.EstrategiaOrdemChegada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regras de negócio e operações de produção do {@link GerenciadorProducao}, incluindo a
 * consistência do estado quando uma produção falha.
 */
class GerenciadorProducaoTest {

    private static final double CUSTO_POR_UNIDADE = 10.0 + 8.0 + 12.0;

    private GerenciadorProducao planta;

    @BeforeEach
    void criarPlanta() {
        planta = PlantaPadrao.criar(Cenario.IDEAL);
        semFalhas(planta);
    }

    /** Controla os sorteios de todas as máquinas: nenhuma falha e toda placa aprovada. */
    private static void semFalhas(GerenciadorProducao g) {
        for (Maquina m : g.getMaquinas()) {
            m.definirGeradorAleatorio(new GeradorFixo(GeradorFixo.SEM_FALHAS));
        }
    }

    @Test
    @DisplayName("A planta padrão começa com o orçamento do cenário e sem demandas")
    void estadoInicial() {
        assertEquals(Cenario.IDEAL.getBudgetInicial(), planta.getBudget(), 1e-9);
        assertTrue(planta.getDemandas().isEmpty());
        assertTrue(planta.getProdutosFabricados().isEmpty());
        assertEquals(3, planta.getMaquinas().size());
        assertEquals(CUSTO_POR_UNIDADE, planta.consultarCustoOperacao(), 1e-9);
        assertEquals(Cenario.APOCALIPTICO.getBudgetInicial(), PlantaPadrao.criar(Cenario.APOCALIPTICO).getBudget(), 1e-9);
    }

    @Test
    @DisplayName("Registrar demanda cria ou acumula unidades do mesmo tipo")
    void registrarDemanda() {
        planta.registrarDemanda("Placa Simples", 2);
        planta.registrarDemanda("placa simples", 3);
        assertEquals(1, planta.getDemandas().size());
        assertEquals(5, planta.getDemandas().get(0).getQuantidadeProdutos());
    }

    @Test
    @DisplayName("Demanda com quantidade inválida ou tipo desconhecido é rejeitada")
    void demandaInvalida() {
        assertThrows(EntradaInvalidaException.class, () -> planta.registrarDemanda("Placa Simples", 0));
        assertThrows(DemandaInvalidaException.class, () -> planta.registrarDemanda("Placa Quântica", 1));
        assertThrows(DemandaInvalidaException.class, () -> planta.fabricarDemanda("Placa Dupla"));
        assertTrue(planta.getDemandas().isEmpty());
    }

    @Test
    @DisplayName("Fabricar consome matéria-prima de cada etapa, debita o custo e atualiza demanda e armazém")
    void fabricarComSucesso() {
        planta.registrarDemanda("Placa Simples", 2);
        planta.fabricarDemanda("Placa Simples");

        assertEquals(196.0, planta.getMateriaPrimaPrincipal().getQuantidade(), 1e-9);
        assertEquals(99.0, planta.getMateriaPrimaEmbalagem().getQuantidade(), 1e-9);
        assertEquals(99.0, planta.getMateriaPrimaSelo().getQuantidade(), 1e-9);
        assertEquals(6000.0 - CUSTO_POR_UNIDADE, planta.getBudget(), 1e-9);

        Demanda d = planta.getDemandas().get(0);
        assertEquals(1, d.getQuantidadeProdutos());
        assertEquals(StatusDemanda.EM_PRODUCAO, d.getStatus());
        assertEquals(1, planta.getProdutosFabricados().size());
        assertEquals("PCB-1", planta.getProdutosFabricados().get(0).getId());
        assertEquals("LOTE-1", planta.getProdutosFabricados().get(0).getLote());
        assertEquals(1, planta.getContadorPlacas());
        assertTrue(planta.getMaquinas().get(0).getSaude() < 100.0, "o uso deve desgastar a máquina");
    }

    @Test
    @DisplayName("A estratégia ativa decide qual demanda é produzida")
    void producaoPelaEstrategia() {
        planta.registrarDemanda("Placa Simples", 1);
        planta.registrarDemanda("Placa Dupla", 2);
        planta.setEstrategia(new EstrategiaMaiorDemanda());
        planta.executarProximaProducao();   // Maior Demanda: 2 placas duplas contra 1 simples

        assertEquals(StatusDemanda.EM_PRODUCAO, planta.getDemandas().get(1).getStatus());
        assertEquals(1, planta.getDemandas().get(1).getQuantidadeProdutos());
        assertEquals(StatusDemanda.PENDENTE, planta.getDemandas().get(0).getStatus());
        assertEquals("Placa Dupla", planta.getProdutosFabricados().get(0).getTipo());

        planta.setEstrategia(new EstrategiaOrdemChegada());
        planta.executarProximaProducao();   // Ordem de Chegada: a de placa simples veio primeiro
        assertEquals(StatusDemanda.CONCLUIDA, planta.getDemandas().get(0).getStatus());
    }

    @Test
    @DisplayName("Sem demanda elegível, executar a próxima produção lança DemandaInvalidaException")
    void semDemandaElegivel() {
        assertThrows(DemandaInvalidaException.class, planta::executarProximaProducao);
    }

    @Test
    @DisplayName("Estoque insuficiente lança RecursoInsuficienteException sem alterar o estado")
    void estoqueInsuficiente() {
        MateriaPrima laminado = new MateriaPrima("MP1", "Laminado", 25.0, "dm2", 20.0, 5.0);
        MateriaPrima esd = new MateriaPrima("MP2", "ESD", 100.0, "un", 10.0, 2.0);
        MateriaPrima cera = new MateriaPrima("MP3", "Cera", 100.0, "un", 10.0, 1.0);
        LinhaDeProducao linha = new LinhaDeProducao("Linha de teste");
        linha.adicionarMaquina(new Corrosora("Corrosora", 60.0, 10.0, 0.3, 2));
        linha.adicionarMaquina(new Embaladora("Embaladora", 60.0, 8.0, 0.2, 1.0, 2));
        linha.adicionarMaquina(new EstacaoInspecao("Bancada", 60.0, 12.0, 0.1, 0.3, 1.0, 1));
        GerenciadorProducao g = new GerenciadorProducao(laminado, esd, cera, linha, Cenario.IDEAL, new EstrategiaOrdemChegada());
        semFalhas(g);

        g.registrarDemanda("Placa Multi-camada", 1); // exige 30 dm2 de laminado
        assertThrows(RecursoInsuficienteException.class, () -> g.fabricarDemanda("Placa Multi-camada"));

        assertEquals(25.0, laminado.getQuantidade(), 1e-9);
        assertEquals(100.0, esd.getQuantidade(), 1e-9);
        assertEquals(6000.0, g.getBudget(), 1e-9);
        assertEquals(StatusDemanda.PENDENTE, g.getDemandas().get(0).getStatus());
        assertTrue(g.getProdutosFabricados().isEmpty());
    }

    @Test
    @DisplayName("Falha técnica na última etapa devolve a matéria-prima já consumida e não cobra a produção")
    void falhaNoMeioDaLinhaRestauraEstoque() {
        planta.getMaquinas().get(2).definirGeradorAleatorio(new GeradorFixo(GeradorFixo.SEMPRE_FALHA));
        planta.registrarDemanda("Placa Dupla", 1);

        assertThrows(MaquinaIndisponivelException.class, () -> planta.fabricarDemanda("Placa Dupla"));

        assertEquals(200.0, planta.getMateriaPrimaPrincipal().getQuantidade(), 1e-9);
        assertEquals(100.0, planta.getMateriaPrimaEmbalagem().getQuantidade(), 1e-9);
        assertEquals(100.0, planta.getMateriaPrimaSelo().getQuantidade(), 1e-9);
        assertEquals(6000.0, planta.getBudget(), 1e-9);
        assertEquals(0, planta.getContadorPlacas());
        assertEquals(1, planta.getDemandas().get(0).getQuantidadeProdutos());
        assertFalse(planta.getMaquinas().get(0).estaLigada(), "as máquinas devem ser desligadas após a falha");
    }

    @Test
    @DisplayName("Sem orçamento para produzir, a demanda é cancelada e OrcamentoInsuficienteException é lançada")
    void orcamentoInsuficienteCancelaDemanda() {
        planta.registrarDemanda("Placa Simples", 1);
        planta.restaurarEstado(10.0, 0, 0, new ArrayList<>(planta.getDemandas()), List.of());

        assertThrows(OrcamentoInsuficienteException.class, () -> planta.fabricarDemanda("Placa Simples"));
        assertEquals(StatusDemanda.CANCELADA, planta.getDemandas().get(0).getStatus());
        assertEquals(10.0, planta.getBudget(), 1e-9);
    }

    @Test
    @DisplayName("Comprar matéria-prima aumenta o estoque e debita o orçamento; compra acima do orçamento é recusada")
    void comprarMateriaPrima() {
        planta.comprarMateriaPrima("laminado", 10.0);
        assertEquals(210.0, planta.getMateriaPrimaPrincipal().getQuantidade(), 1e-9);
        assertEquals(6000.0 - 50.0, planta.getBudget(), 1e-9);

        assertThrows(OrcamentoInsuficienteException.class, () -> planta.comprarMateriaPrima("cera", 1_000_000.0));
        assertThrows(EntradaInvalidaException.class, () -> planta.comprarMateriaPrima("ouro", 1.0));
        assertThrows(EntradaInvalidaException.class, () -> planta.comprarMateriaPrima("esd", -3.0));
        assertEquals(100.0, planta.getMateriaPrimaSelo().getQuantidade(), 1e-9);
        assertEquals(6000.0 - 50.0, planta.getBudget(), 1e-9);
    }

    @Test
    @DisplayName("Restaurar estado inválido é rejeitado sem alterar o gerenciador")
    void restaurarEstadoInvalido() {
        planta.registrarDemanda("Placa Simples", 1);
        List<Demanda> duplicadas = List.of(new Demanda("Placa Dupla", 1, 30.0), new Demanda("Placa Dupla", 2, 30.0));

        assertThrows(EntradaInvalidaException.class, () -> planta.restaurarEstado(-1.0, 0, 0, List.of(), List.of()));
        assertThrows(EntradaInvalidaException.class, () -> planta.restaurarEstado(100.0, 0, 0, duplicadas, List.of()));
        assertEquals(6000.0, planta.getBudget(), 1e-9);
        assertEquals(1, planta.getDemandas().size());
    }
}

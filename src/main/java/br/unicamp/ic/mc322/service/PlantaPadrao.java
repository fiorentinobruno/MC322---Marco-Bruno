package br.unicamp.ic.mc322.service;

import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Corrosora;
import br.unicamp.ic.mc322.model.Embaladora;
import br.unicamp.ic.mc322.model.EstacaoInspecao;
import br.unicamp.ic.mc322.model.LinhaDeProducao;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.strategy.EstrategiaOrdemChegada;

/**
 * Monta a planta com os dados padrão do projeto: as três matérias-primas, as três máquinas (com
 * suas partes internas) e a linha de produção que as agrupa.
 * <p>
 * É usada na primeira execução (quando ainda não existe estado salvo), quando o usuário escolhe
 * começar uma planta nova e pelos testes automatizados, que precisam de uma planta conhecida sem
 * depender do menu interativo.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public final class PlantaPadrao {

    /** Nome da linha de produção padrão. */
    public static final String NOME_LINHA = "Linha de Produção PCB-1";

    /**
     * Classe utilitária: não deve ser instanciada.
     */
    private PlantaPadrao() {
    }

    /**
     * Cria uma planta nova, com estoque inicial, máquinas novas, nenhuma demanda e a estratégia
     * "Ordem de Chegada".
     *
     * @param cenario cenário de operação, que define o orçamento inicial e o desgaste
     * @return o gerenciador da planta montada
     */
    public static GerenciadorProducao criar(Cenario cenario) {
        MateriaPrima laminado = new MateriaPrima("MP1", "Laminado FR-4 (cobre 35 um)", 200.0, "dm2", 20.0, 5.0);
        MateriaPrima esd      = new MateriaPrima("MP2", "Saco ESD antiestático", 100.0, "un", 10.0, 2.0);
        MateriaPrima cera     = new MateriaPrima("MP3", "Selo de cera", 100.0, "un", 10.0, 1.0);

        // O último argumento de cada máquina é o número de ventoinhas (relação 1:N com a máquina).
        Corrosora corrosora = new Corrosora("Corrosora CU-1", 60.0, 10.0, 0.3, 2);
        Embaladora embaladora = new Embaladora("Embaladora ESD-2", 60.0, 8.0, 0.2, 1.0, 2);
        EstacaoInspecao inspecao = new EstacaoInspecao("Bancada de Teste", 60.0, 12.0, 0.1, 0.3, 1.0, 1);

        // Agrupador: a linha recebe máquinas que já existem (agregação), na ordem das etapas.
        LinhaDeProducao linha = new LinhaDeProducao(NOME_LINHA);
        linha.adicionarMaquina(corrosora);
        linha.adicionarMaquina(embaladora);
        linha.adicionarMaquina(inspecao);

        return new GerenciadorProducao(laminado, esd, cera, linha, cenario, new EstrategiaOrdemChegada());
    }
}

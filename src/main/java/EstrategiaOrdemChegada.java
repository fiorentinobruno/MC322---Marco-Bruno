import java.util.List;

/**
 * Estratégia que atende a primeira demanda elegível da fila, na ordem em que foram registradas.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class EstrategiaOrdemChegada implements EstrategiaProducao {

    /**
     * Cria a estratégia (sem estado interno).
     */
    public EstrategiaOrdemChegada() {
    }

    /**
     * Retorna a primeira demanda elegível da lista.
     *
     * @param demandas demandas registradas, em ordem de chegada
     * @param orcamento orçamento atual, em reais
     * @return a primeira demanda elegível, ou {@code null} se não houver nenhuma
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamento) {
        for (Demanda demanda : demandas) {
            if (ehElegivel(demanda, orcamento)) {
                return demanda;
            }
        }

        return null;
    }

    /**
     * Retorna o nome da estratégia.
     *
     * @return {@code "Ordem de Chegada"}
     */
    @Override
    public String getNomeEstrategia() {
        return "Ordem de Chegada";
    }
}

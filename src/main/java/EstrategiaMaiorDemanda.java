import java.util.List;

/**
 * Estratégia que atende a demanda elegível com o maior número de unidades pendentes.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class EstrategiaMaiorDemanda implements EstrategiaProducao {

    /**
     * Cria a estratégia (sem estado interno).
     */
    public EstrategiaMaiorDemanda() {
    }

    /**
     * Retorna a demanda elegível com mais unidades pendentes; em empate, vale a primeira da lista.
     *
     * @param demandas demandas registradas
     * @param orcamento orçamento atual, em reais
     * @return a demanda escolhida, ou {@code null} se não houver nenhuma elegível
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamento) {
        Demanda maiorDemanda = null;

        for (Demanda demanda : demandas) {
            if (!ehElegivel(demanda, orcamento)) {
                continue;
            }

            if (maiorDemanda == null
                    || demanda.getQuantidadeProdutos() > maiorDemanda.getQuantidadeProdutos()) {
                maiorDemanda = demanda;
            }
        }

        return maiorDemanda;
    }

    /**
     * Retorna o nome da estratégia.
     *
     * @return {@code "Maior Demanda"}
     */
    @Override
    public String getNomeEstrategia() {
        return "Maior Demanda";
    }
}

import java.util.List;

/**
 * Estratégia que escolhe a demanda que rende mais placas dentro do orçamento atual, considerando
 * o menor entre as unidades pendentes e as unidades que o orçamento permite fabricar.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class EstrategiaMaximoProdutos implements EstrategiaProducao {

    /**
     * Cria a estratégia (sem estado interno).
     */
    public EstrategiaMaximoProdutos() {
    }

    /**
     * Retorna a demanda elegível com o maior número de unidades viáveis; em empate, vale a primeira.
     *
     * @param demandas demandas registradas
     * @param orcamento orçamento atual, em reais
     * @return a demanda escolhida, ou {@code null} se não houver nenhuma elegível
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamento) {
        Demanda melhor = null;
        int maxUnidades = 0;

        for (Demanda d : demandas) {
            if (!ehElegivel(d, orcamento)) continue;
            int viaveis = (int) Math.min(d.getQuantidadeProdutos(), orcamento / d.getCustoUnitarioEstimado());
            if (viaveis > maxUnidades) {
                maxUnidades = viaveis;
                melhor = d;
            }
        }
        return melhor;
    }

    /**
     * Retorna o nome da estratégia.
     *
     * @return {@code "Maximizar Produção"}
     */
    @Override
    public String getNomeEstrategia() {
        return "Maximizar Produção";
    }
}

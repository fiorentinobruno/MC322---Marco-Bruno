import java.util.List;

public class EstrategiaMaximoProdutos implements EstrategiaProducao {

    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        if (orcamentoDisponivel <= 0) {
            return null;
        }
        Demanda melhor = null;
        for (Demanda d : demandas) {
            if (d.estaProduzivel()
                    && (melhor == null || d.getQuantidadeProdutos() < melhor.getQuantidadeProdutos())) {
                melhor = d;
            }
        }
        return melhor;
    }

    @Override
    public String getNomeEstrategia() {
        return "Máximo de Demandas Concluídas";
    }
}

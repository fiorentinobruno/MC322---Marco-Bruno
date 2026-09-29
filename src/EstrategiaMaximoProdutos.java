import java.util.List;

public class EstrategiaMaximoProdutos implements EstrategiaProducao {

    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamento) {
        Demanda melhor = null;
        int maxUnidades = 0;
        
        for (Demanda d : demandas) {
            if (!ehElegivel(d, orcamento)) continue;
            int viaveis = Math.min(d.getQuantidadeProdutos(), (int) (orcamento / d.getCustoUnitarioEstimado()));
            if (viaveis > maxUnidades) {
                maxUnidades = viaveis;
                melhor = d;
            }
        }
        return melhor;
    }

    @Override
    public String getNomeEstrategia() {
        return "Maximizar Produção";
    }
}
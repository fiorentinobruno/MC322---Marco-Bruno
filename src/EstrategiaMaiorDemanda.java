import java.util.List;

public class EstrategiaMaiorDemanda implements EstrategiaProducao {

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

    @Override
    public String getNomeEstrategia() {
        return "Maior Demanda";
    }
}
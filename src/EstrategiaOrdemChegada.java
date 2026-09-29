import java.util.List;

public class EstrategiaOrdemChegada implements EstrategiaProducao {

    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamento) {
        for (Demanda demanda : demandas) {
            if (ehElegivel(demanda, orcamento)) {
                return demanda;
            }
        }

        return null;
    }

    @Override
    public String getNomeEstrategia() {
        return "Ordem de Chegada";
    }
}
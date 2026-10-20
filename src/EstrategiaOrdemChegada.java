import java.util.List;

public class EstrategiaOrdemChegada implements EstrategiaProducao{

    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel){
        for (Demanda d : demandas){
            if (d.getStatus() == StatusDemanda.PENDENTE){
                return d;
            }         
    }
    return null;
    }

    @Override
    public String getNomeEstrategia(){
        return "Ordem de Chegada";
    }
}
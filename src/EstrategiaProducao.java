import java.util.List;

public interface EstrategiaProducao{
     String selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);
     String getNomeEstrategia();
}
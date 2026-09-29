import java.util.List;

public interface EstrategiaProducao {

    Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);

    String getNomeEstrategia();

    default boolean ehElegivel(Demanda d, double orcamento) {
        StatusDemanda s = d.getStatus();
        return (s == StatusDemanda.PENDENTE || s == StatusDemanda.EM_PRODUCAO)
                && d.getQuantidadeProdutos() > 0
                && d.ehViavel(orcamento);
    }
}
import java.util.List;

public class EstrategiaMaiorDemanda implements EstrategiaProducao {

    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        if (orcamentoDisponivel <= 0) {
            return null;
        }
        Demanda maior = null;
        for (Demanda d : demandas) {
            if (d.estaProduzivel()
                    && (maior == null || d.getQuantidadeProdutos() > maior.getQuantidadeProdutos())) {
                maior = d;
            }
        }
        return maior;
    }

    @Override
    public String getNomeEstrategia() {
        return "Maior Demanda de Produtos";
    }
}

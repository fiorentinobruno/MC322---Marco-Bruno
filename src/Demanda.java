public class Demanda {
    private String tipoProduto;
    private int quantidadeProdutos;
    private StatusDemanda status;
    private final double custoUnitarioEstimado;


    public Demanda(String tipoProduto, int quantidadeProdutos, double custoUnitarioEstimado) {
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = quantidadeProdutos;
        this.custoUnitarioEstimado = custoUnitarioEstimado;
        this.status = StatusDemanda.PENDENTE;
    }

    public boolean atualizarQuantidade(int quantidadeAdicional) {
        if (status == StatusDemanda.CANCELADA) {
            return false;
        }
        quantidadeProdutos += quantidadeAdicional;

        if (status == StatusDemanda.CONCLUIDA){
           status = StatusDemanda.PENDENTE;  
        } 

        return true;
    }

    public double calcularMateriaPrimaNecessaria(double demandaMateriaPrimaPorUnidade) {
        return this.quantidadeProdutos * demandaMateriaPrimaPorUnidade;
    }

    public void registrarProducao(int unidadesFabricadas) {
        if (status != StatusDemanda.PENDENTE && status != StatusDemanda.EM_PRODUCAO) return;
        quantidadeProdutos -= unidadesFabricadas;
        if (quantidadeProdutos <= 0) {
            quantidadeProdutos = 0;
            atender();
        }
    }

    public boolean atender() {
        if (status == StatusDemanda.CANCELADA){
            return false;
        } 
        status = StatusDemanda.CONCLUIDA;
        return true;
    }

    public void iniciarProducao() {
        if (status == StatusDemanda.PENDENTE) {
            status = StatusDemanda.EM_PRODUCAO;
        }
    }

    public boolean cancelar() {
        if (status == StatusDemanda.CONCLUIDA) {
           return false; 
        }
        status = StatusDemanda.CANCELADA;
        return true;
    }

    public String getTipoProduto() {
        return tipoProduto;
    }

    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    public StatusDemanda getStatus() {
        return status;
    }

    public double getCustoTotalEstimado() {
        return quantidadeProdutos * custoUnitarioEstimado;
    }

    public boolean ehViavel(double orcamento) {
        return custoUnitarioEstimado > 0 && custoUnitarioEstimado <= orcamento;
    }

    public double getCustoUnitarioEstimado() { 
        return custoUnitarioEstimado; 
    }

}
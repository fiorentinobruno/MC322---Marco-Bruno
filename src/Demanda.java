public class Demanda {

    private String tipoProduto;
    private int quantidadeProdutos;
    private StatusDemanda status;

    public Demanda(String tipoProduto, int quantidadeProdutos) {
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = quantidadeProdutos;
        status = StatusDemanda.PENDENTE;
    }

    public void atualizarQuantidade(int quantidadeAdicional) {
        if (status == StatusDemanda.CANCELADA) {
            throw new DemandaInvalidaException(
                    "A demanda de " + tipoProduto + " foi cancelada e não aceita novas unidades.");
        }
        this.quantidadeProdutos += quantidadeAdicional;
        status = StatusDemanda.PENDENTE;
    }

    public double calcularMateriaPrimaNecessaria(double demandaMateriaPrimaPorUnidade) {
        return this.quantidadeProdutos * demandaMateriaPrimaPorUnidade;
    }

    public void registrarProducao(int unidadesFabricadas) {
        this.quantidadeProdutos -= unidadesFabricadas;
        if (this.quantidadeProdutos <= 0) {
            this.quantidadeProdutos = 0;
            atender();
        }
    }

    public boolean atender() {
        if (status == StatusDemanda.CANCELADA) {
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

    public void cancelar() {
        if (status == StatusDemanda.CONCLUIDA) {
            throw new DemandaInvalidaException(
                    "A demanda de " + tipoProduto + " já foi concluída e não pode ser cancelada.");
        }
        status = StatusDemanda.CANCELADA;
    }

    public boolean estaProduzivel() {
        return quantidadeProdutos > 0
                && status != StatusDemanda.CONCLUIDA
                && status != StatusDemanda.CANCELADA;
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
}

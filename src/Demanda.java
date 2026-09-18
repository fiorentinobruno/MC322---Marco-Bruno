public class Demanda {
    private String tipoProduto;
    private int quantidadeProdutos;
    private boolean atendida;

    public Demanda(String tipoProduto, int quantidadeProdutos) {
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = quantidadeProdutos;
        this.atendida = false;
    }

    public void atualizarQuantidade(int quantidadeAdicional) {
        this.quantidadeProdutos += quantidadeAdicional;
        this.atendida = false; // se chegou mais pedido, não está mais "totalmente atendida"
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

    public void atender() {
        this.atendida = true;
    }

    public String getTipoProduto() {
        return tipoProduto;
    }

    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    public boolean isAtendida() {
        return atendida;
    }
}
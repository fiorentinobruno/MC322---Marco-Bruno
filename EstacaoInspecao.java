public class EstacaoInspecao {
    private boolean ativa;
    private int produtosInspecionados;

    public EstacaoInspecao() {
        this.ativa = false;
        this.produtosInspecionados = 0;
    }

    public void ativar() {
        this.ativa = true;

    }

    public void desativar() {
        this.ativa = false;

    }

    public void inspecionar(Produto produto) {
        if (!this.ativa) {
            System.out.println("Estação desativada. Ative-a para inspecionar o produto.");
            return;
        }
        if (!produto.getStatus().equals("processado")) {
            System.out.println("Produto ainda não foi processado pela máquina.");
            return;
        }
        produto.aprovar();
        produtosInspecionados++;
        System.out.println("Produto inspecionado com sucesso");

    }

    public int getTotalInspecionados() {
        return produtosInspecionados;

    }

}
public class EstacaoInspecao{
    private boolean ativa;
    private int produtosInspecionados;

    public EstacaoInspecao(boolean ativa, int produtosInspencionados){
        this.ativa = ativa;
        this.produtosInspecionados = produtosInspencionados;
    }

    public void ativar(){
        this.ativa = true;

    }
    public void desativar(){
        this.ativa = false;

    }

    public void inspecionar(Produto produto){
         if (!this.ativa){
            System.out.printf("Bancada desativada. Ative-a antes de inspecionar.%n");
            return;
        }
        if (!produto.getStatus().equals("processado")){
            System.out.printf("A placa ainda não passou pela corrosora.%n");
            return;
        }
        produto.aprovar();
        produtosInspecionados++;
        System.out.printf("Produto inspecionado com sucesso%n");
    }

    public int getTotalInspecionados(){
        return produtosInspecionados;

    }

}
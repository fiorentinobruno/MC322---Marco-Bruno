public class EstacaoInspecao{
    private boolean ativa;
    private int produtosInspencionados;

    public EstacaoInspecao(boolean ativa, int produtosInspencionados){
        this.ativa = ativa;
        this.produtosInspencionados = produtosInspencionados;
    }

    public void ativar(){
        this.ativa = true;

    }
    public void desativar(){
        this.ativa = false;

    }

    public void inspecionar(){
        if (this.ativa){
            produtosInspencionados ++;
            System.out.printf( "Produto inspecionado com sucesso%n");
        }
        else{
            System.out.printf( "Produto reprovado na fase de inspeção%n");
        }
        

    }

    public int getTotalInspecionados(){
        return produtosInspencionados;

    }

}
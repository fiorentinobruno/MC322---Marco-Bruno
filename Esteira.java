public class Esteira{
    private String item;
    private boolean emMovimento;
    private double capacidade_max;

    public Esteira (String item, boolean emMovimento,double capacidade_max){
        this.item = item;
        this.emMovimento = emMovimento;
        this.capacidade_max = capacidade_max;
    }

    public void ligar(){
        this.emMovimento = true;
    }

    public void desligar(){
        this.emMovimento = false;
    }

    public void adicionarItem( String novoItem){ 
        if (this.item == null){
            this.item = novoItem;
        
        }
        else {
            System.out.printf("Esteira em funcionamento, tente mais tarde%n");
        }

    }

    public String removerItem(){
        String itemRemovido = this.item;
        this.item = null;
        return itemRemovido;
        

    }

    public boolean verificarCapacidade(double cm_de_cobre){
        return cm_de_cobre <= capacidade_max;
    }


    
}

public class Maquina {
    private String nome;
    private boolean ligada;
    private double capacidade_max;

    public  Maquina(String nome, boolean ligada, double capacidade_max ){
        this.nome = nome;
        this.ligada = ligada;
        this.capacidade_max = capacidade_max;
    }

    public void ligar(){
        this.ligada = true;
    }
    public void desligar(){
        this.ligada = false;
    }
    public void processar(MateriaPrima mp, double demanda){
        if (this.ligada  ){
            System.out.printf(" ligada%n");
            if(this.capacidade_max < demanda){
                System.out.printf("A demanda excede a capacidade máxima da máquna%n");
            }
            else{
                if(mp.verificar_quant(demanda)){
                    mp.consumir(demanda);
                    System.out.printf("imprimindo PCB");
                }
                else{
                    System.out.printf("Estoque insuficiente");
                }
            }
        }
        else {
           System.out.printf(" desligada%n");
        }
    }
     public String getNome (){
        return nome;
    }
    public boolean ligada(){
            return this.ligada;
    }

}

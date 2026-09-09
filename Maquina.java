
public class Maquina {
    private String nome;
    private boolean ligada;
    private double capacidadeMax;

    public  Maquina(String nome, boolean ligada, double capacidadeMax ){
        this.nome = nome;
        this.ligada = ligada;
        this.capacidadeMax = capacidadeMax;
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
            if(this.capacidadeMax < demanda){
                System.out.printf("A demanda excede a capacidade máxima da máquna%n");
            }
            else{
                if(mp.verificarQuant(demanda)){
                    mp.consumir(demanda);
                    System.out.printf("Imprimindo PCB");
                }
                else{
                    System.out.printf("Estoque insuficiente de cobre");
                }
            }
        }
        else {
           System.out.printf("Desligada%n");
        }
    }
     public String getNome (){
        return nome;
    }
    public boolean ligada(){
            return this.ligada;
    }

}

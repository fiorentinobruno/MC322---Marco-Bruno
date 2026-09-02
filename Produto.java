
public class Produto {
    private String id;
    private String nome;
    private String status;
    private double quant_disponivel;
    private double quant_necessaria;

    public Produto(String id, String nome, String status, double quant_necessaria){
        this.id = id;
        this.nome = nome;
        this.status = status;
        this.quant_necessaria = quant_necessaria;
        this.quant_disponivel = quant_disponivel;
    }

    public void processar( ){
        this.status ="processado";
    }
    public void defDemanda(double demanda){
        this.quant_necessaria = demanda; 
    }
    public double getDemanda( ){
        return quant_necessaria;
    }
    public String getID (){
        return id;
    }

    public String getNome (){
        return nome;
    }
     public double getQuant (){
        return quant_disponivel;
    }
    public String getStatus (){
        return status;
    }

}

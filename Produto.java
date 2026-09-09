
public class Produto {
    private String id;
    private String nome;
    private String status;
    private double quantNecessaria;

    public Produto(String id, String nome, String status, double quantNecessaria){
        this.id = id;
        this.nome = nome;
        this.status = status;
        this.quantNecessaria = quantNecessaria;
    }

    public void processar(String idMateriaPrima){
        this.status = "processado";
        this.materiaPrimaUsada = idMateriaPrima;
    }

     public void aprovar(){
        this.status = "inspecionado";
    }

    public void defDemanda(double demanda){
        this.quantNecessaria = demanda; 
    }
    public double getDemanda( ){
        return quantNecessaria;
    }
    public String getId (){
        return id;
    }

    public String getNome (){
        return nome;
    }

    public String getStatus (){
        return status;
    }

     public String getMateriaPrimaUsada (){
        return materiaPrimaUsada;
    }

}

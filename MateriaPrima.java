
public class MateriaPrima {
    private String id;
    private String nome;
    private double quant;
    private String uni;
    private double quant_min;

    public MateriaPrima(String id, String nome, double quant, String uni, double quant_min){
        this.id = id;
        this.nome = nome;
        this.quant = quant;
        this.uni  = uni;
        this.quant_min = quant_min;
    
    }

    public boolean verificar_quant(double consumido){
        return quant >= consumido;

    }
    public double add_estoque(double adicionado){
        return quant = quant + adicionado;
    }

    public double consumir (double consumido){
        return quant = quant - consumido;
    }

    public String getID (){
        return id;
    }

    public String getName (){
        return nome;
    }
    public double getQuant (){
        return quant;
    }
    
    
}

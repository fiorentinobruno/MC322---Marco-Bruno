
public class MateriaPrima {
    private String id;
    private String nome;
    private double quant;
    private String unidade;
    private double quantidadeMinima;

    public MateriaPrima(String id, String nome, double quant, String unidade, double quantidadeMinima){
        this.id = id;
        this.nome = nome;
        this.quant = quant;
        this.unidade  = unidade;
        this.quantidadeMinima = quantidadeMinima;
    
    }

    public boolean verificarQuant(double consumido){
        return quant >= consumido && quant >= quantidadeMinima;

    }
    public void addEstoque(double adicionado){
         quant = quant + adicionado;
    }

    public void consumir (double consumido){
         quant = quant - consumido;
    }

    public String getId (){
        return id;
    }

    public String getNome (){
        return nome;
    }

    public String getUnidade (){
        return unidade;
    }

    public double getQuant (){
        return quant;
    }
    
    
}

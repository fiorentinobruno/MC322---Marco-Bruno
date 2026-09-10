
public class Produto {
    private String id;
    private String nome;
    private String status;
    private double quantidadeMateriaPrimaNecessaria;
    private String materiaPrimaUsada;

    public Produto(String id, String nome, double quantidadeMateriaPrimaNecessaria) {
        this.id = id;
        this.nome = nome;
        this.status = "aguardando processamento";
        this.quantidadeMateriaPrimaNecessaria = quantidadeMateriaPrimaNecessaria;
    }

    public void processar(String idMateriaPrima) {
        this.status = "processado";
        this.materiaPrimaUsada = idMateriaPrima;
    }

    public void definirDemandaMateriaPrima(double demanda) {
        this.quantidadeMateriaPrimaNecessaria = demanda;
    }

    public double getDemandaMateriaPrima() {
        return quantidadeMateriaPrimaNecessaria;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getStatus() {
        return status;
    }

    public void aprovar() {
        this.status = "inspecionado";
    }

    public String getMateriaPrimaUsada() {
    return materiaPrimaUsada;
}
}

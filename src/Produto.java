
public abstract class Produto implements Auditavel{
    private String id;
    private String nome;
    private String status;
    private double quantidadeMateriaPrimaNecessaria;
    private String materiaPrimaUsada;
    private double qualidade;
    private double probabilidadeFalhaAcumulada;
    private static int totalProdutosFabricados;

    public Produto(String id, String nome, double quantidadeMateriaPrimaNecessaria, double qualidade) {
        this.id = id;
        this.nome = nome;
        this.status = "aguardando processamento";
        this.quantidadeMateriaPrimaNecessaria = quantidadeMateriaPrimaNecessaria;
        this.qualidade = qualidade;
        totalProdutosFabricados++;
    }

    public abstract void processar(String idMateriaPrima);
    public abstract double calcularTempoProducao();
    public abstract String getTipo();

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

    public void setMateriaPrimaUsada(String idMateriaPrima) {
        materiaPrimaUsada = idMateriaPrima;
    }

    public double getQualidade() {
        return qualidade;
    }

    public double getProbabilidadeFalhaAcumulada(){
        return probabilidadeFalhaAcumulada;
    }

    public void aumentarProbabilidadeFalha(double incremento){
        probabilidadeFalhaAcumulada += incremento;
    }

    public double getQuantidadeMateriaPrimaPorUnidade(){
        return quantidadeMateriaPrimaNecessaria;
    }

    public void setStatus(String newstatus){
        status = newstatus;
    }

    public static int getTotalProdutosFabricados(){
        return totalProdutosFabricados;
    }
}

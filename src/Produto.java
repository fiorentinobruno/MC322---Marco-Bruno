
public abstract class Produto implements Auditavel{
    private String id;
    private String nome;
    private String status;
    private double quantidadeMateriaPrimaNecessaria;
    private String materiaPrimaUsada;
    private double qualidade;
    private double probabilidadeFalhaAcumulada;
    private static int totalProdutosFabricados;
    private String lote;

    public Produto(String id, String nome, double quantidadeMateriaPrimaNecessaria, double qualidade) {
        this.id = id;
        this.nome = nome;
        this.status = "aguardando processamento";
        this.quantidadeMateriaPrimaNecessaria = quantidadeMateriaPrimaNecessaria;
        this.qualidade = qualidade;
        totalProdutosFabricados++;
        this.lote = "-";
    }

    public abstract void processar(String idMateriaPrima);
    public abstract double calcularTempoProducao();
    public abstract String getTipo();

    

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

    public String getLote() { 
        return lote; 
    }  

    public void aprovar() {
        this.status = "inspecionado";
    }

    public String getMateriaPrimaUsada() {
        return materiaPrimaUsada;
    }

    public double getQualidade() {
        return qualidade;
    }

    public double getProbabilidadeFalhaAcumulada(){
        return probabilidadeFalhaAcumulada;
    }

    public double getQuantidadeMateriaPrimaPorUnidade(){
        return quantidadeMateriaPrimaNecessaria;
    }

    public static int getTotalProdutosFabricados(){
        return totalProdutosFabricados;
    }

    public void setMateriaPrimaUsada(String idMateriaPrima) {
        materiaPrimaUsada = idMateriaPrima;
    }
    
    public void definirDemandaMateriaPrima(double demanda) {
        this.quantidadeMateriaPrimaNecessaria = demanda;
    }

    public void aumentarProbabilidadeFalha(double incremento){
        probabilidadeFalhaAcumulada += incremento;
    }

    public void setStatus(String newstatus){
        status = newstatus;
    }

    public void setLote(String lote) { 
        this.lote = lote; 
    }

    

    @Override
    public boolean precisaManutencao() {
        return "defeituoso".equalsIgnoreCase(this.status) || this.probabilidadeFalhaAcumulada > 0.25;
    }

    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format(
            "ID: %s | Tipo: %s | Qualidade: %.2f | Risco Acumulado: %.1f%% | Status: %s | Alerta: %s",
            getId(),
            getTipo(),
            getQualidade(),
            this.probabilidadeFalhaAcumulada * 100.0,
            getStatus(),
            precisaManutencao() ? "DESCARTE" : "Aprovado no Controle de Qualidade"
        );
    }
}

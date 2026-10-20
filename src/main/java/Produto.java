/**
 * Produto fabricado pela planta: uma placa de circuito impresso (PCB).
 * <p>
 * Cada subclasse representa um nível de complexidade (simples, dupla, multi-camada) e define a
 * qualidade exigida, o tempo de produção e como o produto é processado. O {@code status} acompanha
 * a posição do produto na linha: {@code "aguardando processamento"}, {@code "processado"},
 * {@code "embalado"}, {@code "inspecionado"} ou {@code "defeituoso"}.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public abstract class Produto implements Auditavel {
    private String id;
    private String nome;
    private String status;
    private double quantidadeMateriaPrimaNecessaria;
    private String materiaPrimaUsada;
    private double qualidade;
    private double probabilidadeFalhaAcumulada;
    private static int totalProdutosFabricados;
    private String lote;

    /**
     * Cria um produto aguardando processamento.
     *
     * @param id                              identificador único (ex.: {@code PCB-1})
     * @param nome                            nome de exibição
     * @param quantidadeMateriaPrimaNecessaria quantidade de laminado consumida pela corrosão
     * @param qualidade                       nível de qualidade exigido (0 a 1); quanto maior, mais rigorosa a inspeção
     */
    public Produto(String id, String nome, double quantidadeMateriaPrimaNecessaria, double qualidade) {
        this.id = id;
        this.nome = nome;
        this.status = "aguardando processamento";
        this.quantidadeMateriaPrimaNecessaria = quantidadeMateriaPrimaNecessaria;
        this.qualidade = qualidade;
        totalProdutosFabricados++;
        this.lote = "-";
    }

    /**
     * Aplica ao produto o efeito do processamento principal (corrosão).
     *
     * @param idMateriaPrima identificador da matéria-prima utilizada
     */
    public abstract void processar(String idMateriaPrima);

    /**
     * Calcula o tempo de produção do tipo de produto.
     *
     * @return o tempo de produção, em minutos
     */
    public abstract double calcularTempoProducao();

    /**
     * Retorna o nome do tipo do produto (ex.: {@code "Placa Simples"}).
     *
     * @return o tipo do produto
     */
    public abstract String getTipo();

    /**
     * Retorna a demanda de laminado para fabricar uma unidade.
     *
     * @return a quantidade de matéria-prima necessária
     */
    public double getDemandaMateriaPrima() {
        return quantidadeMateriaPrimaNecessaria;
    }

    /**
     * Retorna o identificador do produto.
     *
     * @return o id (ex.: {@code PCB-1})
     */
    public String getId() {
        return id;
    }

    /**
     * Retorna o nome de exibição.
     *
     * @return o nome do produto
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o estado atual do produto na linha de produção.
     *
     * @return o status textual
     */
    public String getStatus() {
        return status;
    }

    /**
     * Retorna o lote ao qual o produto pertence.
     *
     * @return o lote, ou {@code "-"} se ainda não foi atribuído
     */
    public String getLote() {
        return lote;
    }

    /**
     * Marca o produto como aprovado na inspeção.
     */
    public void aprovar() {
        this.status = "inspecionado";
    }

    /**
     * Retorna a matéria-prima usada no processamento principal.
     *
     * @return o id da matéria-prima, ou {@code null} se ainda não foi processado
     */
    public String getMateriaPrimaUsada() {
        return materiaPrimaUsada;
    }

    /**
     * Retorna o nível de qualidade exigido do produto.
     *
     * @return a qualidade, entre 0 e 1
     */
    public double getQualidade() {
        return qualidade;
    }

    /**
     * Retorna a probabilidade de defeito acumulada pelos problemas ocorridos na linha.
     *
     * @return a probabilidade acumulada (0.1 equivale a 10%)
     */
    public double getProbabilidadeFalhaAcumulada() {
        return probabilidadeFalhaAcumulada;
    }

    /**
     * Retorna a quantidade de laminado necessária por unidade deste produto.
     *
     * @return a quantidade de matéria-prima por unidade
     */
    public double getQuantidadeMateriaPrimaPorUnidade() {
        return quantidadeMateriaPrimaNecessaria;
    }

    /**
     * Retorna quantos produtos já foram instanciados desde o início do programa.
     *
     * @return o total de produtos criados
     */
    public static int getTotalProdutosFabricados() {
        return totalProdutosFabricados;
    }

    /**
     * Registra a matéria-prima usada no processamento principal.
     *
     * @param idMateriaPrima identificador da matéria-prima
     */
    public void setMateriaPrimaUsada(String idMateriaPrima) {
        materiaPrimaUsada = idMateriaPrima;
    }

    /**
     * Redefine a quantidade de matéria-prima necessária por unidade.
     *
     * @param demanda nova quantidade necessária
     */
    public void definirDemandaMateriaPrima(double demanda) {
        this.quantidadeMateriaPrimaNecessaria = demanda;
    }

    /**
     * Soma um incremento à probabilidade de defeito do produto.
     *
     * @param incremento valor a somar (0.1 equivale a 10%)
     */
    public void aumentarProbabilidadeFalha(double incremento) {
        probabilidadeFalhaAcumulada += incremento;
    }

    /**
     * Atualiza o estado do produto na linha de produção.
     *
     * @param newstatus novo status textual
     */
    public void setStatus(String newstatus) {
        status = newstatus;
    }

    /**
     * Atribui o produto a um lote.
     *
     * @param lote identificador do lote (ex.: {@code LOTE-1})
     */
    public void setLote(String lote) {
        this.lote = lote;
    }

    /**
     * Indica se o produto deve ser descartado: está defeituoso ou o risco acumulado passa de 25%.
     *
     * @return {@code true} se o produto deve ser descartado
     */
    @Override
    public boolean precisaManutencao() {
        return "defeituoso".equalsIgnoreCase(this.status) || this.probabilidadeFalhaAcumulada > 0.25;
    }

    /**
     * Gera o diagnóstico do produto com id, tipo, qualidade, risco acumulado, status e alerta.
     *
     * @return a linha de diagnóstico
     */
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

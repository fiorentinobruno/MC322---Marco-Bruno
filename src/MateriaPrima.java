
public class MateriaPrima {
    private String id;
    private String nome;
    private double quantidade;
    private String unidade;
    private double quantidadeMinima;
    private double custoPorUnidade;

    public MateriaPrima(String id, String nome, double quantidade, String unidade, double quantidadeMinima, double custoPorUnidade) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
        this.custoPorUnidade = custoPorUnidade;

    }

    public boolean verificarDisponibilidade(double consumido) {
        return quantidade >= consumido && quantidade >= quantidadeMinima;

    }

    public void adicionarEstoque(double adicionado) {
        quantidade += adicionado;
    }

    public void consumir(double consumido) {
        quantidade -= consumido;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getCustoPorUnidade(){
        return custoPorUnidade;
    }
    
}

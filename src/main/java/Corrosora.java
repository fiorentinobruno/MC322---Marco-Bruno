import java.util.Random;

/**
 * Primeira etapa da linha: corrói o laminado de cobre para revelar as trilhas da placa.
 * Consome laminado na quantidade exigida pelo produto e pode introduzir um defeito aleatório.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class Corrosora extends Maquina {
    private double chanceAumentarFalha;
    private Random random;

    /**
     * Cria uma corrosora (probabilidade-base de falha 2% e reparo de R$ 250).
     *
     * @param nome                nome de exibição
     * @param capacidadeMax       maior quantidade de laminado que cabe em uma corrosão
     * @param custoOperacao       custo, em reais, de cada corrosão
     * @param chanceAumentarFalha chance (0 a 1) de a corrosão introduzir 10% de risco de defeito na placa
     */
    public Corrosora(String nome, double capacidadeMax, double custoOperacao, double chanceAumentarFalha) {
        super(nome, capacidadeMax, 0.02, custoOperacao, 250);
        this.chanceAumentarFalha = chanceAumentarFalha;
        this.random = new Random();
    }

    /**
     * Retorna o tipo da máquina.
     *
     * @return {@code "Corrosora"}
     */
    @Override
    public String getTipo() {
        return "Corrosora";
    }

    /**
     * Verifica se a placa cabe na corrosora e se há laminado suficiente. Não altera nada.
     *
     * @param mp      laminado a ser consumido
     * @param produto placa a ser corroída
     * @throws MaquinaIndisponivelException se a quantidade de laminado excede a capacidade da corrosora
     * @throws RecursoInsuficienteException se o estoque de laminado é insuficiente
     */
    @Override
    public void verificarRecursos(MateriaPrima mp, Produto produto) {
        exigirCapacidade(produto.getQuantidadeMateriaPrimaPorUnidade());
        mp.exigirDisponibilidade(produto.getQuantidadeMateriaPrimaPorUnidade());
    }

    /**
     * Corrói a placa: consome laminado, aplica desgaste e pode adicionar risco de defeito.
     * Todas as verificações ocorrem antes de qualquer consumo.
     *
     * @param mp      laminado a ser consumido
     * @param produto placa a ser corroída
     * @throws MaquinaIndisponivelException se a corrosora está desligada, sofre falha técnica ou a capacidade é excedida
     * @throws RecursoInsuficienteException se o estoque de laminado é insuficiente
     */
    @Override
    public void processar(MateriaPrima mp, Produto produto) {
        exigirLigada();
        exigirSemFalhaTecnica(produto, "O processamento");
        verificarRecursos(mp, produto);

        aplicarDesgaste();

        mp.consumir(produto.getQuantidadeMateriaPrimaPorUnidade());
        produto.processar(mp.getId());
        System.out.println("Corroendo " + produto.getNome() + " (" + produto.getId() + "). Trilhas de cobre reveladas.");

        if (random.nextDouble() < chanceAumentarFalha) {
            produto.aumentarProbabilidadeFalha(0.1);
            System.out.println("Problema na corrosão de " + produto.getId() + ". Probabilidade de falha aumentada em 10%.");
        }
    }
}

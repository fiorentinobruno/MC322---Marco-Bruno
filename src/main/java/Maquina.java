import java.util.Random;

/**
 * Máquina da linha de produção. Cada etapa (corrosão, embalagem, inspeção) é uma subclasse.
 * <p>
 * Regras de negócio principais:
 * <ul>
 *   <li>Cada processamento reduz a saúde da máquina de forma aleatória (multiplicada pelo cenário).</li>
 *   <li>Quanto menor a saúde, maior a chance de falha técnica.</li>
 *   <li>Com saúde zero a máquina está quebrada e só volta a operar após um reparo.</li>
 *   <li>Abaixo de 30 de saúde a máquina é considerada em necessidade de manutenção.</li>
 * </ul>
 * Situações que impedem a operação são sinalizadas com {@link MaquinaIndisponivelException} e
 * {@link RecursoInsuficienteException}; a máquina apenas imprime avisos informativos (como a
 * quebra por desgaste), que não interrompem a etapa em andamento.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public abstract class Maquina implements Auditavel {
    private String nome;
    private boolean ligada;
    private double capacidadeMax;
    private double probabilidadeFalha;
    private double custoOperacao;
    private double saude;
    private double desgasteMinimo;
    private double desgasteMaximo;
    private double custoReparo;
    private double multiplicadorDesgaste;
    private double multiplicadorFalha;

    private Random random;

    /**
     * Cria uma máquina desligada, com saúde máxima (100).
     *
     * @param nome               nome de exibição
     * @param capacidadeMax      capacidade máxima de processamento por operação
     * @param probabilidadeFalha probabilidade-base de falha técnica (0 a 1)
     * @param custoOperacao      custo, em reais, de cada operação
     * @param custoReparo        custo, em reais, de um reparo completo
     */
    public Maquina(String nome, double capacidadeMax, double probabilidadeFalha, double custoOperacao, double custoReparo) {
        this.nome = nome;
        this.ligada = false;
        this.capacidadeMax = capacidadeMax;
        this.probabilidadeFalha = probabilidadeFalha;
        this.custoOperacao = custoOperacao;
        this.random = new Random();
        this.saude = 100.0;
        this.desgasteMaximo = 3.0;
        this.desgasteMinimo = 0.5;
        this.custoReparo = custoReparo;
        this.multiplicadorDesgaste = 1.0;
        this.multiplicadorFalha = 1.0;
    }

    /**
     * Verifica, <b>sem alterar nada</b>, se há capacidade e matéria-prima para processar o produto.
     * Deve ser chamado antes de iniciar uma produção para evitar estados parciais.
     *
     * @param mp      matéria-prima que a etapa consumirá
     * @param produto produto a ser processado
     * @throws MaquinaIndisponivelException se o lote excede a capacidade máxima da máquina
     * @throws RecursoInsuficienteException se a matéria-prima não cobre o consumo da etapa
     */
    public abstract void verificarRecursos(MateriaPrima mp, Produto produto);

    /**
     * Executa a etapa da máquina sobre o produto, consumindo matéria-prima e aplicando desgaste.
     * Nenhum recurso é consumido se a operação falhar.
     *
     * @param mp      matéria-prima que a etapa consome
     * @param produto produto a ser processado
     * @throws MaquinaIndisponivelException se a máquina está desligada, sofre falha técnica ou o lote excede a capacidade
     * @throws RecursoInsuficienteException se a matéria-prima não é suficiente
     */
    public abstract void processar(MateriaPrima mp, Produto produto);

    /**
     * Retorna o tipo da máquina (ex.: {@code "Corrosora"}).
     *
     * @return o tipo da máquina
     */
    public abstract String getTipo();

    /**
     * Aplica os multiplicadores de desgaste e de falha do cenário escolhido.
     *
     * @param c cenário de operação
     */
    public void aplicarCenario(Cenario c) {
        this.multiplicadorDesgaste = c.getMultiplicadorDesgaste();
        this.multiplicadorFalha = c.getMultiplicadorFalha();
    }

    /**
     * Liga a máquina.
     *
     * @throws MaquinaIndisponivelException se a máquina está quebrada e precisa de reparo
     */
    public void ligar() {
        if (estaQuebrada()) {
            throw new MaquinaIndisponivelException("A " + nome + " está quebrada e precisa de reparo. "
                    + "Use 'Auditoria e manutenção' para repará-la.");
        }
        this.ligada = true;
    }

    /**
     * Desliga a máquina. Sempre é seguro chamar este método.
     */
    public void desligar() {
        this.ligada = false;
    }

    /**
     * Retorna o nome de exibição.
     *
     * @return o nome da máquina
     */
    public String getNome() {
        return nome;
    }

    /**
     * Informa se a máquina está ligada.
     *
     * @return {@code true} se está ligada
     */
    public boolean estaLigada() {
        return this.ligada;
    }

    /**
     * Informa se a máquina quebrou por desgaste (saúde zerada).
     *
     * @return {@code true} se a saúde chegou a zero
     */
    public boolean estaQuebrada() {
        return saude <= 0;
    }

    /**
     * Retorna o custo de cada operação.
     *
     * @return o custo de operação, em reais
     */
    public double getCustoOperacao() {
        return custoOperacao;
    }

    /**
     * Exige que a máquina esteja ligada antes de processar.
     *
     * @throws MaquinaIndisponivelException se a máquina está desligada
     */
    protected void exigirLigada() {
        if (!estaLigada()) {
            throw new MaquinaIndisponivelException("A " + nome + " está desligada.");
        }
    }

    /**
     * Sorteia uma falha técnica considerando a saúde atual; se ocorrer, desliga a máquina.
     *
     * @param produto produto que estava sendo processado
     * @param etapa   descrição da etapa interrompida (ex.: {@code "O processamento"})
     * @throws MaquinaIndisponivelException se o sorteio indicar falha técnica
     */
    protected void exigirSemFalhaTecnica(Produto produto, String etapa) {
        if (verificarFalha()) {
            desligar();
            throw new MaquinaIndisponivelException("Falha técnica na " + nome + "! " + etapa + " de "
                    + produto.getId() + " foi interrompido.");
        }
    }

    /**
     * Exige que o consumo caiba na capacidade máxima da máquina.
     *
     * @param quantidade quantidade que seria processada de uma vez
     * @throws MaquinaIndisponivelException se a quantidade ultrapassa a capacidade máxima
     */
    protected void exigirCapacidade(double quantidade) {
        if (quantidade > capacidadeMax) {
            throw new MaquinaIndisponivelException(String.format(
                    "A capacidade máxima da %s (%.1f) foi ultrapassada: a operação exige %.1f.",
                    nome, capacidadeMax, quantidade));
        }
    }

    /**
     * Sorteia se ocorre falha técnica, usando a probabilidade atual.
     *
     * @return {@code true} se a falha ocorreu
     */
    protected boolean verificarFalha() {
        return random.nextDouble() < calcularProbabilidadeFalhaAtual();
    }

    /**
     * Retorna a capacidade máxima da máquina.
     *
     * @return a capacidade máxima por operação
     */
    public double getCapacidadeMax() {
        return capacidadeMax;
    }

    /**
     * Reduz a saúde da máquina em um valor aleatório. Se a saúde zerar, a máquina quebra e desliga.
     */
    public void aplicarDesgaste() {
        if (saude <= 0) return;

        double perda = desgasteMinimo + random.nextDouble() * (desgasteMaximo - desgasteMinimo);
        saude -= perda * multiplicadorDesgaste;

        if (saude <= 0) {
            saude = 0;
            desligar();
            System.out.println("A máquina " + nome + " quebrou totalmente por desgaste. É necessária manutenção.");
        }
    }

    /**
     * Calcula a probabilidade de falha atual: a base do cenário somada à parcela causada pelo desgaste.
     *
     * @return a probabilidade de falha, entre 0 e 1
     */
    public double calcularProbabilidadeFalhaAtual() {
        double base = Math.min(1.0, probabilidadeFalha * multiplicadorFalha);
        double desgasteProporcional = (100.0 - saude) / 100.0;
        return base + desgasteProporcional * (1.0 - base);
    }

    /**
     * Retorna a saúde atual.
     *
     * @return a saúde, de 0 (quebrada) a 100 (perfeita)
     */
    public double getSaude() {
        return saude;
    }

    /**
     * Restaura a saúde da máquina para 100.
     */
    public void reparar() {
        this.saude = 100;
        System.out.println("A máquina " + nome + " foi consertada. Sua saúde agora é 100.\n");
    }

    /**
     * Retorna o custo de um reparo completo.
     *
     * @return o custo de reparo, em reais
     */
    public double getCustoReparo() {
        return custoReparo;
    }

    /**
     * Indica se a máquina precisa de manutenção (saúde abaixo de 30).
     *
     * @return {@code true} se a saúde é menor que 30
     */
    @Override
    public boolean precisaManutencao() {
        return this.saude < 30.0;
    }

    /**
     * Gera o diagnóstico da máquina com saúde, probabilidade de falha, custo de reparo e situação.
     *
     * @return a linha de diagnóstico
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format(
            "%s (%s) | Saúde: %.1f%% | Probabilidade falha: %.1f%% | Custo reparo: R$%.2f | Status: %s",
            getNome(),
            getTipo(),
            this.saude,
            calcularProbabilidadeFalhaAtual() * 100.0,
            this.custoReparo,
            precisaManutencao() ? "REQUER MANUTENÇÃO" : "OPERACIONAL"
        );
    }
}

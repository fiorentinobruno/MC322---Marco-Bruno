import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Máquina da linha de produção. Cada etapa (corrosão, embalagem, inspeção) é uma subclasse.
 * <p>
 * <b>Composição.</b> A máquina é o Todo e é formada por três tipos de Partes, todas criadas dentro
 * do seu próprio construtor a partir de configurações primitivas:
 * <ul>
 *   <li>1 {@link MotorDeAcionamento} (1:1), que gera calor e se desgasta;</li>
 *   <li>1 {@link SensorDeTemperatura} (1:1), que mede o aquecimento da máquina;</li>
 *   <li>1..* {@link VentoinhaDeResfriamento} (1:N), que retiram calor da máquina.</li>
 * </ul>
 * As partes não são recebidas de fora, não são compartilhadas com outras máquinas e nunca são
 * devolvidas por getters: o restante do sistema conversa apenas com a máquina, que delega o
 * trabalho às partes (Lei de Deméter). Quando a máquina deixa de existir, suas partes também.
 * <p>
 * Regras de negócio principais:
 * <ul>
 *   <li>Cada processamento reduz a saúde da máquina e de suas partes de forma aleatória
 *       (multiplicada pelo cenário).</li>
 *   <li>Quanto menor a saúde da máquina e do motor, maior a chance de falha técnica.</li>
 *   <li>A cada ciclo o motor aquece a máquina e as ventoinhas a resfriam; ventoinhas desgastadas
 *       ou paradas e motores desgastados fazem a temperatura subir.</li>
 *   <li>Em alerta de temperatura a máquina falha mais e as placas processadas ganham risco de
 *       defeito; superaquecida, ela não liga.</li>
 *   <li>Com saúde zero ou motor queimado a máquina não liga até passar por um reparo.</li>
 *   <li>A máquina precisa de manutenção quando sua saúde fica abaixo de 30 ou quando alguma parte
 *       precisa de manutenção.</li>
 * </ul>
 * Situações que impedem a operação são sinalizadas com {@link MaquinaIndisponivelException} e
 * {@link RecursoInsuficienteException}; a máquina apenas imprime avisos informativos (como a
 * quebra por desgaste), que não interrompem a etapa em andamento.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public abstract class Maquina implements Auditavel {
    private static final double PESO_DESGASTE_MOTOR = 0.3;
    private static final double RISCO_FALHA_TEMPERATURA = 0.1;
    private static final double RISCO_PRODUTO_TEMPERATURA = 0.05;

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

    private final MotorDeAcionamento motor;
    private final SensorDeTemperatura sensor;
    private final List<VentoinhaDeResfriamento> ventoinhas;

    /**
     * Cria uma máquina desligada, com saúde máxima (100), e instancia suas partes internas: um
     * motor, um sensor de temperatura e a quantidade informada de ventoinhas.
     *
     * @param nome                 nome de exibição
     * @param capacidadeMax        capacidade máxima de processamento por operação
     * @param probabilidadeFalha   probabilidade-base de falha técnica (0 a 1)
     * @param custoOperacao        custo, em reais, de cada operação
     * @param custoReparo          custo, em reais, de um reparo completo
     * @param potenciaMotor        potência do motor de acionamento, em kW
     * @param temperaturaMaxima    temperatura, em graus Celsius, a partir da qual a máquina superaquece
     * @param quantidadeVentoinhas número de ventoinhas de resfriamento; deve ser pelo menos 1
     * @throws EntradaInvalidaException se a quantidade de ventoinhas for menor que 1, a potência
     *                                  não for positiva ou a temperatura máxima não for maior que a ambiente
     */
    public Maquina(String nome, double capacidadeMax, double probabilidadeFalha, double custoOperacao, double custoReparo,
                   double potenciaMotor, double temperaturaMaxima, int quantidadeVentoinhas) {
        if (quantidadeVentoinhas < 1) {
            throw new EntradaInvalidaException("A " + nome + " precisa de pelo menos uma ventoinha de resfriamento.");
        }
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

        // Composição: a própria máquina cria suas partes; elas não existem fora dela.
        this.motor = new MotorDeAcionamento(potenciaMotor);
        this.sensor = new SensorDeTemperatura(temperaturaMaxima);
        this.ventoinhas = new ArrayList<>();
        for (int i = 1; i <= quantidadeVentoinhas; i++) {
            this.ventoinhas.add(new VentoinhaDeResfriamento(i));
        }
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
     * Aplica os multiplicadores de desgaste e de falha do cenário escolhido. O multiplicador de
     * desgaste também vale para as partes internas, que o recebem a cada ciclo de operação.
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
     * @throws MaquinaIndisponivelException se a máquina está quebrada, com o motor queimado ou
     *                                      superaquecida e precisa de reparo
     */
    public void ligar() {
        if (estaQuebrada()) {
            throw new MaquinaIndisponivelException("A " + nome + " está quebrada e precisa de reparo. "
                    + "Use 'Auditoria e manutenção' para repará-la.");
        }
        if (!motor.estaOperante()) {
            throw new MaquinaIndisponivelException("O motor de acionamento da " + nome + " queimou. "
                    + "Use 'Auditoria e manutenção' para repará-la.");
        }
        if (sensor.estaSuperaquecido()) {
            throw new MaquinaIndisponivelException(String.format(
                    "A %s está superaquecida (%.1f °C, máximo de %.1f °C). "
                            + "Use 'Auditoria e manutenção' para repará-la e normalizar a temperatura.",
                    nome, sensor.lerTemperatura(), sensor.getTemperaturaMaxima()));
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
     * Executa um ciclo de operação completo, propagando o uso pela estrutura composta: desgasta a
     * máquina, o motor e as ventoinhas, atualiza a temperatura e, se a máquina estiver em alerta
     * de temperatura, aumenta o risco de defeito do produto em processamento.
     *
     * @param produto produto que está sendo processado neste ciclo
     */
    protected void executarCicloOperacao(Produto produto) {
        aplicarDesgaste();
        desgastarComponentes();
        regularTemperatura();

        if (sensor.estaEmAlerta()) {
            produto.aumentarProbabilidadeFalha(RISCO_PRODUTO_TEMPERATURA);
            System.out.printf("Temperatura elevada na %s (%.1f °C). Risco de defeito de %s aumentado em 5%%.%n",
                    nome, sensor.lerTemperatura(), produto.getId());
        }
    }

    /**
     * Desgasta o motor e cada ventoinha. Se o motor queimar neste ciclo, a máquina é desligada; se
     * uma ventoinha parar, o usuário é avisado de que o resfriamento caiu.
     */
    private void desgastarComponentes() {
        boolean motorFuncionava = motor.estaOperante();
        motor.aplicarDesgaste(multiplicadorDesgaste);
        if (motorFuncionava && !motor.estaOperante()) {
            desligar();
            System.out.println("O motor de acionamento da " + nome + " queimou por desgaste. É necessária manutenção.");
        }

        for (VentoinhaDeResfriamento v : ventoinhas) {
            boolean girava = v.estaOperante();
            v.aplicarDesgaste(multiplicadorDesgaste);
            if (girava && !v.estaOperante()) {
                System.out.println("A ventoinha " + v.getNumero() + " da " + nome + " parou. O resfriamento da máquina caiu.");
            }
        }
    }

    /**
     * Aplica o balanço térmico do ciclo: o motor aquece a máquina e as ventoinhas a resfriam. Se a
     * temperatura atingir o máximo, a máquina é desligada.
     */
    private void regularTemperatura() {
        sensor.aquecer(motor.calcularCalorGerado());
        sensor.resfriar(calcularResfriamentoTotal());

        if (sensor.estaSuperaquecido()) {
            desligar();
            System.out.printf("A %s superaqueceu (%.1f °C). Ela não liga até passar por manutenção.%n",
                    nome, sensor.lerTemperatura());
        }
    }

    /**
     * Soma o resfriamento oferecido pelas ventoinhas em funcionamento.
     *
     * @return o resfriamento total por ciclo, em graus Celsius
     */
    private double calcularResfriamentoTotal() {
        double total = 0;
        for (VentoinhaDeResfriamento v : ventoinhas) total += v.calcularResfriamento();
        return total;
    }

    /**
     * Calcula a probabilidade de falha atual: a base do cenário somada à parcela causada pelo
     * desgaste da máquina, pelo desgaste do motor e pela temperatura em alerta.
     *
     * @return a probabilidade de falha, entre 0 e 1
     */
    public double calcularProbabilidadeFalhaAtual() {
        double base = Math.min(1.0, probabilidadeFalha * multiplicadorFalha);
        double desgasteProporcional = (100.0 - saude) / 100.0;
        double riscoComponentes = motor.calcularFatorDesgaste() * PESO_DESGASTE_MOTOR
                + (sensor.estaEmAlerta() ? RISCO_FALHA_TEMPERATURA : 0.0);
        double risco = Math.min(1.0, desgasteProporcional + riscoComponentes);
        return base + risco * (1.0 - base);
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
     * Repara a máquina por completo: restaura a saúde da estrutura, do motor e das ventoinhas para
     * 100 e normaliza a temperatura.
     */
    public void reparar() {
        this.saude = 100;
        motor.reparar();
        for (VentoinhaDeResfriamento v : ventoinhas) v.reparar();
        sensor.resfriarAteAmbiente();
        System.out.println("A máquina " + nome + " foi consertada (estrutura, motor e ventoinhas) e a temperatura "
                + "foi normalizada. Sua saúde agora é 100.\n");
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
     * Lê a temperatura atual da máquina (repassa a leitura do sensor).
     *
     * @return a temperatura, em graus Celsius
     */
    public double lerTemperaturaAtual() {
        return sensor.lerTemperatura();
    }

    /**
     * Retorna a temperatura a partir da qual a máquina superaquece (repassa o limite do sensor).
     *
     * @return a temperatura máxima, em graus Celsius
     */
    public double getTemperaturaMaxima() {
        return sensor.getTemperaturaMaxima();
    }

    /**
     * Conta quantas ventoinhas ainda giram.
     *
     * @return o número de ventoinhas operantes
     */
    public int contarVentoinhasOperantes() {
        int operantes = 0;
        for (VentoinhaDeResfriamento v : ventoinhas) {
            if (v.estaOperante()) operantes++;
        }
        return operantes;
    }

    /**
     * Retorna quantas ventoinhas a máquina possui.
     *
     * @return o número total de ventoinhas
     */
    public int getQuantidadeVentoinhas() {
        return ventoinhas.size();
    }

    /**
     * Retorna a saúde do motor de acionamento (repassa a leitura do motor).
     *
     * @return a saúde do motor, de 0 (queimado) a 100 (novo)
     */
    public double getSaudeMotor() {
        return motor.getSaude();
    }

    /**
     * Descreve os componentes que formam a máquina, com suas quantidades.
     *
     * @return uma linha por tipo de componente
     */
    public String descreverComponentes() {
        return String.format("1x Motor de acionamento (%.2f kW)", motor.getPotencia())
                + String.format("\n1x Sensor de temperatura (máx. %.1f °C)", sensor.getTemperaturaMaxima())
                + String.format("\n%dx Ventoinha de resfriamento", ventoinhas.size());
    }

    /**
     * Indica se a máquina precisa de manutenção: saúde abaixo de 30 ou alguma parte (motor,
     * sensor ou ventoinha) precisando de manutenção.
     *
     * @return {@code true} se a máquina ou alguma de suas partes precisa de manutenção
     */
    @Override
    public boolean precisaManutencao() {
        if (this.saude < 30.0 || motor.precisaManutencao() || sensor.precisaManutencao()) {
            return true;
        }
        for (VentoinhaDeResfriamento v : ventoinhas) {
            if (v.precisaManutencao()) return true;
        }
        return false;
    }

    /**
     * Gera o resumo da máquina em uma linha, sem as partes internas: saúde, probabilidade de falha,
     * temperatura, custo de reparo e situação.
     *
     * @return a linha de resumo
     */
    public String gerarResumo() {
        return String.format(
            "%s (%s) | Saúde: %.1f%% | Probabilidade falha: %.1f%% | Temperatura: %.1f °C | Custo reparo: R$%.2f | Status: %s",
            getNome(),
            getTipo(),
            this.saude,
            calcularProbabilidadeFalhaAtual() * 100.0,
            sensor.lerTemperatura(),
            this.custoReparo,
            precisaManutencao() ? "REQUER MANUTENÇÃO" : "OPERACIONAL"
        );
    }

    /**
     * Gera o diagnóstico da máquina em cascata: o resumo da máquina e, indentado abaixo dele, o
     * diagnóstico de cada parte (motor, sensor e ventoinhas).
     *
     * @return o relatório com várias linhas
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        StringBuilder sb = new StringBuilder(gerarResumo());
        sb.append("\n    > ").append(motor.gerarRelatorioDiagnostico());
        sb.append("\n    > ").append(sensor.gerarRelatorioDiagnostico());
        for (VentoinhaDeResfriamento v : ventoinhas) {
            sb.append("\n    > ").append(v.gerarRelatorioDiagnostico());
        }
        return sb.toString();
    }
}

import java.util.Random;

/**
 * Motor elétrico que aciona uma {@link Maquina}. É uma Parte da máquina (composição 1:1): é criado
 * dentro do construtor da máquina, pertence a uma única máquina e não é exposto ao restante do
 * sistema.
 * <p>
 * Regras de funcionamento:
 * <ul>
 *   <li>Cada ciclo de operação desgasta o motor de forma aleatória (multiplicada pelo cenário).</li>
 *   <li>Um motor desgastado gera mais calor (até o dobro) e aumenta a chance de falha da máquina.</li>
 *   <li>Com saúde zero o motor queima, e a máquina não liga até ser reparada.</li>
 *   <li>Abaixo de 30 de saúde o motor é considerado em necessidade de manutenção.</li>
 * </ul>
 * O motor apenas mantém o próprio estado; quem decide o que fazer com ele (desligar a máquina,
 * avisar o usuário) é a {@link Maquina}.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class MotorDeAcionamento implements Auditavel {
    private static final double SAUDE_MAXIMA = 100.0;
    private static final double LIMITE_MANUTENCAO = 30.0;
    private static final double CALOR_POR_KW = 2.0;

    private final double potencia;
    private double saude;
    private double desgasteMinimo;
    private double desgasteMaximo;
    private Random random;

    /**
     * Cria um motor novo, com saúde máxima (100).
     *
     * @param potencia potência do motor, em kW; deve ser um número positivo
     * @throws EntradaInvalidaException se a potência não for um número positivo e finito
     */
    public MotorDeAcionamento(double potencia) {
        if (!(potencia > 0) || Double.isInfinite(potencia)) {
            throw new EntradaInvalidaException("A potência do motor deve ser um número maior que zero.");
        }
        this.potencia = potencia;
        this.saude = SAUDE_MAXIMA;
        this.desgasteMinimo = 0.2;
        this.desgasteMaximo = 1.2;
        this.random = new Random();
    }

    /**
     * Reduz a saúde do motor em um valor aleatório. A saúde nunca fica abaixo de zero.
     *
     * @param multiplicador fator do cenário aplicado ao desgaste (1.0 significa desgaste normal)
     */
    public void aplicarDesgaste(double multiplicador) {
        if (saude <= 0) return;

        double perda = desgasteMinimo + random.nextDouble() * (desgasteMaximo - desgasteMinimo);
        saude = Math.max(0, saude - perda * multiplicador);
    }

    /**
     * Calcula o calor gerado pelo motor em um ciclo de operação. Quanto mais desgastado, mais
     * calor ele gera (até o dobro do calor de um motor novo).
     *
     * @return o aquecimento provocado, em graus Celsius
     */
    public double calcularCalorGerado() {
        return potencia * CALOR_POR_KW * (1.0 + calcularFatorDesgaste());
    }

    /**
     * Calcula o quanto o motor já se desgastou.
     *
     * @return 0 para um motor novo e 1 para um motor queimado
     */
    public double calcularFatorDesgaste() {
        return (SAUDE_MAXIMA - saude) / SAUDE_MAXIMA;
    }

    /**
     * Informa se o motor ainda funciona.
     *
     * @return {@code true} se a saúde é maior que zero
     */
    public boolean estaOperante() {
        return saude > 0;
    }

    /**
     * Restaura a saúde do motor para 100.
     */
    public void reparar() {
        this.saude = SAUDE_MAXIMA;
    }

    /**
     * Retorna a saúde atual.
     *
     * @return a saúde, de 0 (queimado) a 100 (novo)
     */
    public double getSaude() {
        return saude;
    }

    /**
     * Retorna a potência do motor.
     *
     * @return a potência, em kW
     */
    public double getPotencia() {
        return potencia;
    }

    /**
     * Indica se o motor precisa de manutenção (saúde abaixo de 30).
     *
     * @return {@code true} se a saúde é menor que 30
     */
    @Override
    public boolean precisaManutencao() {
        return saude < LIMITE_MANUTENCAO;
    }

    /**
     * Gera o diagnóstico do motor com potência, saúde e situação.
     *
     * @return a linha de diagnóstico
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        String situacao;
        if (!estaOperante()) {
            situacao = "QUEIMADO";
        } else if (precisaManutencao()) {
            situacao = "DESGASTADO";
        } else {
            situacao = "OK";
        }
        return String.format("Motor de acionamento (%.2f kW) | Saúde: %.1f%% | %s", potencia, saude, situacao);
    }
}

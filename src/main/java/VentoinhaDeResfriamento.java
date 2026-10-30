import java.util.Random;

/**
 * Ventoinha que retira calor de uma {@link Maquina}. É uma Parte da máquina (composição 1:N): cada
 * máquina cria, no próprio construtor, uma ou mais ventoinhas numeradas, que não são
 * compartilhadas nem expostas.
 * <p>
 * Regras de funcionamento:
 * <ul>
 *   <li>Cada ciclo de operação desgasta a ventoinha de forma aleatória (multiplicada pelo cenário).</li>
 *   <li>O resfriamento que ela oferece é proporcional à sua saúde; parada, ela não resfria nada.</li>
 *   <li>Abaixo de 30 de saúde a ventoinha é considerada em necessidade de manutenção.</li>
 * </ul>
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class VentoinhaDeResfriamento implements Auditavel {
    private static final double SAUDE_MAXIMA = 100.0;
    private static final double LIMITE_MANUTENCAO = 30.0;
    private static final double CAPACIDADE_RESFRIAMENTO = 3.2;

    private final int numero;
    private double saude;
    private double desgasteMinimo;
    private double desgasteMaximo;
    private Random random;

    /**
     * Cria uma ventoinha nova, com saúde máxima (100).
     *
     * @param numero posição da ventoinha dentro da máquina, a partir de 1
     * @throws EntradaInvalidaException se o número for menor que 1
     */
    public VentoinhaDeResfriamento(int numero) {
        if (numero < 1) {
            throw new EntradaInvalidaException("O número da ventoinha deve ser maior ou igual a 1.");
        }
        this.numero = numero;
        this.saude = SAUDE_MAXIMA;
        this.desgasteMinimo = 0.3;
        this.desgasteMaximo = 1.5;
        this.random = new Random();
    }

    /**
     * Reduz a saúde da ventoinha em um valor aleatório. A saúde nunca fica abaixo de zero.
     *
     * @param multiplicador fator do cenário aplicado ao desgaste (1.0 significa desgaste normal)
     */
    public void aplicarDesgaste(double multiplicador) {
        if (saude <= 0) return;

        double perda = desgasteMinimo + random.nextDouble() * (desgasteMaximo - desgasteMinimo);
        saude = Math.max(0, saude - perda * multiplicador);
    }

    /**
     * Calcula quanto a ventoinha resfria a máquina em um ciclo de operação.
     *
     * @return o resfriamento, em graus Celsius (zero se a ventoinha está parada)
     */
    public double calcularResfriamento() {
        if (!estaOperante()) return 0;
        return CAPACIDADE_RESFRIAMENTO * (saude / SAUDE_MAXIMA);
    }

    /**
     * Informa se a ventoinha ainda gira.
     *
     * @return {@code true} se a saúde é maior que zero
     */
    public boolean estaOperante() {
        return saude > 0;
    }

    /**
     * Restaura a saúde da ventoinha para 100.
     */
    public void reparar() {
        this.saude = SAUDE_MAXIMA;
    }

    /**
     * Retorna a posição da ventoinha dentro da máquina.
     *
     * @return o número da ventoinha, a partir de 1
     */
    public int getNumero() {
        return numero;
    }

    /**
     * Retorna a saúde atual.
     *
     * @return a saúde, de 0 (parada) a 100 (nova)
     */
    public double getSaude() {
        return saude;
    }

    /**
     * Indica se a ventoinha precisa de manutenção (saúde abaixo de 30).
     *
     * @return {@code true} se a saúde é menor que 30
     */
    @Override
    public boolean precisaManutencao() {
        return saude < LIMITE_MANUTENCAO;
    }

    /**
     * Gera o diagnóstico da ventoinha com número, saúde e situação.
     *
     * @return a linha de diagnóstico
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        String situacao;
        if (!estaOperante()) {
            situacao = "PARADA";
        } else if (precisaManutencao()) {
            situacao = "DESGASTADA";
        } else {
            situacao = "OK";
        }
        return String.format("Ventoinha %d | Saúde: %.1f%% | %s", numero, saude, situacao);
    }
}

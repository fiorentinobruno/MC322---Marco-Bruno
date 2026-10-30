/**
 * Sensor que mede a temperatura interna de uma {@link Maquina}. É uma Parte da máquina
 * (composição 1:1): é criado dentro do construtor da máquina e só é lido por ela.
 * <p>
 * Regras de funcionamento:
 * <ul>
 *   <li>A temperatura parte da temperatura ambiente (25 °C) e nunca fica abaixo dela.</li>
 *   <li>A partir de 85% da temperatura máxima o sensor entra em alerta: a máquina falha mais e as
 *       placas processadas ganham risco de defeito.</li>
 *   <li>Ao atingir a temperatura máxima a máquina está superaquecida e não liga até a
 *       manutenção.</li>
 * </ul>
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class SensorDeTemperatura implements Auditavel {
    private static final double TEMPERATURA_AMBIENTE = 25.0;
    private static final double FRACAO_ALERTA = 0.85;

    private final double temperaturaMaxima;
    private double temperaturaAtual;

    /**
     * Cria um sensor marcando a temperatura ambiente.
     *
     * @param temperaturaMaxima temperatura, em graus Celsius, a partir da qual a máquina está
     *                          superaquecida; deve ser maior que a temperatura ambiente (25 °C)
     * @throws EntradaInvalidaException se a temperatura máxima não for maior que a ambiente
     */
    public SensorDeTemperatura(double temperaturaMaxima) {
        if (!(temperaturaMaxima > TEMPERATURA_AMBIENTE) || Double.isInfinite(temperaturaMaxima)) {
            throw new EntradaInvalidaException(String.format(
                    "A temperatura máxima do sensor deve ser maior que a ambiente (%.1f °C).", TEMPERATURA_AMBIENTE));
        }
        this.temperaturaMaxima = temperaturaMaxima;
        this.temperaturaAtual = TEMPERATURA_AMBIENTE;
    }

    /**
     * Registra um aumento de temperatura.
     *
     * @param graus aquecimento, em graus Celsius; valores negativos são ignorados
     */
    public void aquecer(double graus) {
        if (graus > 0) {
            temperaturaAtual += graus;
        }
    }

    /**
     * Registra uma queda de temperatura, sem nunca ficar abaixo da temperatura ambiente.
     *
     * @param graus resfriamento, em graus Celsius; valores negativos são ignorados
     */
    public void resfriar(double graus) {
        if (graus > 0) {
            temperaturaAtual = Math.max(TEMPERATURA_AMBIENTE, temperaturaAtual - graus);
        }
    }

    /**
     * Volta a temperatura para a ambiente (usado na manutenção da máquina).
     */
    public void resfriarAteAmbiente() {
        temperaturaAtual = TEMPERATURA_AMBIENTE;
    }

    /**
     * Retorna a temperatura medida.
     *
     * @return a temperatura atual, em graus Celsius
     */
    public double lerTemperatura() {
        return temperaturaAtual;
    }

    /**
     * Retorna o limite de superaquecimento.
     *
     * @return a temperatura máxima, em graus Celsius
     */
    public double getTemperaturaMaxima() {
        return temperaturaMaxima;
    }

    /**
     * Informa se a temperatura chegou à faixa de alerta (85% da máxima ou mais).
     *
     * @return {@code true} se o sensor está em alerta
     */
    public boolean estaEmAlerta() {
        return temperaturaAtual >= temperaturaMaxima * FRACAO_ALERTA;
    }

    /**
     * Informa se a temperatura atingiu o máximo permitido.
     *
     * @return {@code true} se a máquina está superaquecida
     */
    public boolean estaSuperaquecido() {
        return temperaturaAtual >= temperaturaMaxima;
    }

    /**
     * Indica se a temperatura exige intervenção (faixa de alerta ou superaquecimento).
     *
     * @return {@code true} se o sensor está em alerta
     */
    @Override
    public boolean precisaManutencao() {
        return estaEmAlerta();
    }

    /**
     * Gera o diagnóstico do sensor com a temperatura atual, o limite e a situação.
     *
     * @return a linha de diagnóstico
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        String situacao;
        if (estaSuperaquecido()) {
            situacao = "SUPERAQUECIDO";
        } else if (estaEmAlerta()) {
            situacao = "ALERTA";
        } else {
            situacao = "OK";
        }
        return String.format("Sensor de temperatura | %.1f °C (máx. %.1f °C) | %s",
                temperaturaAtual, temperaturaMaxima, situacao);
    }
}

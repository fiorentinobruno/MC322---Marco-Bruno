package br.unicamp.ic.mc322;

import java.util.Random;

/**
 * Gerador "aleatório" que sempre devolve o mesmo valor, usado para tornar os testes
 * determinísticos. Com {@link #SEM_FALHAS} nenhuma falha técnica ocorre e toda placa é aprovada na
 * inspeção; com {@link #SEMPRE_FALHA} a máquina sempre sofre falha técnica.
 */
public class GeradorFixo extends Random {

    /** Valor alto: nenhum sorteio de falha ou rejeição acontece. */
    public static final double SEM_FALHAS = 0.999;
    /** Valor zero: todo sorteio de falha acontece. */
    public static final double SEMPRE_FALHA = 0.0;

    private final double valor;

    /**
     * Cria o gerador com o valor fixo.
     *
     * @param valor valor devolvido por {@link #nextDouble()}
     */
    public GeradorFixo(double valor) {
        this.valor = valor;
    }

    @Override
    public double nextDouble() {
        return valor;
    }
}

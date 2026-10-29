/**
 * Contrato de qualquer elemento da planta que possa ser inspecionado em uma auditoria, seja ele
 * uma {@link Maquina} ou um {@link Produto}.
 * <p>
 * Permite que o {@link GerenciadorProducao} percorra, de forma polimórfica, uma única lista com
 * itens de hierarquias diferentes.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public interface Auditavel {

    /**
     * Gera uma linha de texto com o diagnóstico atual do item.
     *
     * @return o relatório de diagnóstico, pronto para ser exibido
     */
    String gerarRelatorioDiagnostico();

    /**
     * Informa se o item exige intervenção (reparo de máquina ou descarte de produto).
     *
     * @return {@code true} se o item precisa de manutenção ou descarte
     */
    boolean precisaManutencao();
}

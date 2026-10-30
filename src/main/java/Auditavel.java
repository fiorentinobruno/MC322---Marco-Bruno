/**
 * Contrato de qualquer elemento da planta que possa ser inspecionado em uma auditoria: a
 * {@link LinhaDeProducao}, uma {@link Maquina}, um componente interno da máquina ou um
 * {@link Produto}.
 * <p>
 * Permite que o {@link GerenciadorProducao} percorra, de forma polimórfica, uma única lista com
 * itens de hierarquias diferentes, e que a auditoria se propague em cascata
 * (linha, máquina, componentes): cada nível pede o diagnóstico do nível abaixo.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public interface Auditavel {

    /**
     * Gera o diagnóstico atual do item. Itens compostos (linha e máquina) devolvem um texto com
     * várias linhas indentadas, incluindo o diagnóstico de suas partes.
     *
     * @return o relatório de diagnóstico, pronto para ser exibido
     */
    String gerarRelatorioDiagnostico();

    /**
     * Informa se o item exige intervenção (reparo de máquina ou componente, ou descarte de produto).
     *
     * @return {@code true} se o item precisa de manutenção ou descarte
     */
    boolean precisaManutencao();
}

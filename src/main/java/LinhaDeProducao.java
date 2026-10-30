import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Agrupador das máquinas da planta: uma linha de produção que executa suas máquinas em sequência,
 * na ordem em que foram adicionadas (corrosão, embalagem e inspeção).
 * <p>
 * <b>Agregação, não composição.</b> As máquinas são criadas fora da linha (no {@link Main}) e
 * entregues a ela por {@link #adicionarMaquina(Maquina)}. Uma máquina existe independentemente da
 * linha e poderia ser transferida para outra, por isso a relação é de agregação (1:N). Já as partes
 * internas de cada máquina (motor, sensor e ventoinhas) pertencem a ela por composição.
 * <p>
 * <b>Encapsulamento (Lei de Deméter).</b> Quem usa a linha ({@link GerenciadorProducao}) não acessa
 * as máquinas nem suas partes: pede à linha que produza, calcule custos, repare ou audite, e a
 * linha delega o trabalho a cada máquina. A coleção interna nunca é devolvida diretamente.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class LinhaDeProducao implements Auditavel {
    private final String nome;
    private final List<Maquina> maquinas;

    /**
     * Cria uma linha de produção vazia.
     *
     * @param nome nome de exibição da linha
     * @throws EntradaInvalidaException se o nome for vazio
     */
    public LinhaDeProducao(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new EntradaInvalidaException("O nome da linha de produção não pode ser vazio.");
        }
        this.nome = nome;
        this.maquinas = new ArrayList<>();
    }

    /**
     * Adiciona uma máquina ao final da linha. A ordem de adição define a ordem das etapas.
     *
     * @param maquina máquina a adicionar
     * @throws EntradaInvalidaException se a máquina for nula ou já fizer parte da linha
     */
    public void adicionarMaquina(Maquina maquina) {
        if (maquina == null) {
            throw new EntradaInvalidaException("Não é possível adicionar uma máquina inexistente à " + nome + ".");
        }
        if (maquinas.contains(maquina)) {
            throw new EntradaInvalidaException("A " + maquina.getNome() + " já faz parte da " + nome + ".");
        }
        maquinas.add(maquina);
    }

    /**
     * Retorna uma visão somente leitura das máquinas da linha. Tentativas de adicionar ou remover
     * itens por meio dela lançam {@link UnsupportedOperationException}; alterações na estrutura só
     * podem ser feitas pelos métodos da própria linha.
     *
     * @return a lista imutável de máquinas, na ordem das etapas
     */
    public List<Maquina> getMaquinas() {
        return Collections.unmodifiableList(maquinas);
    }

    /**
     * Retorna o número de máquinas (etapas) da linha.
     *
     * @return a quantidade de máquinas
     */
    public int getQuantidadeMaquinas() {
        return maquinas.size();
    }

    /**
     * Retorna o nome de exibição.
     *
     * @return o nome da linha
     */
    public String getNome() {
        return nome;
    }

    /**
     * Aplica o cenário de operação a todas as máquinas da linha.
     *
     * @param c cenário de operação
     */
    public void aplicarCenario(Cenario c) {
        for (Maquina m : maquinas) m.aplicarCenario(c);
    }

    /**
     * Verifica, <b>sem alterar nada</b>, se todas as etapas têm capacidade e matéria-prima para
     * processar o produto.
     *
     * @param produto produto a ser fabricado
     * @param insumos matéria-prima de cada etapa, na mesma ordem das máquinas
     * @throws EntradaInvalidaException     se o número de insumos não corresponde ao número de etapas
     * @throws MaquinaIndisponivelException se o lote excede a capacidade de alguma máquina
     * @throws RecursoInsuficienteException se falta matéria-prima em alguma etapa
     */
    public void verificarRecursos(Produto produto, MateriaPrima... insumos) {
        exigirInsumoPorEtapa(insumos);
        for (int i = 0; i < maquinas.size(); i++) {
            maquinas.get(i).verificarRecursos(insumos[i], produto);
        }
    }

    /**
     * Fabrica o produto passando-o por todas as máquinas, em ordem. As máquinas são ligadas antes
     * da primeira etapa e sempre desligadas ao final, com sucesso ou não.
     * <p>
     * A linha não desfaz consumo de matéria-prima: se uma etapa falhar, a exceção é repassada para
     * que o {@link GerenciadorProducao} restaure o estoque.
     *
     * @param produto produto a ser fabricado
     * @param insumos matéria-prima de cada etapa, na mesma ordem das máquinas
     * @throws EntradaInvalidaException     se o número de insumos não corresponde ao número de etapas
     * @throws MaquinaIndisponivelException se a linha está vazia ou alguma máquina está quebrada,
     *                                      superaquecida, desligada ou sofre falha técnica
     * @throws RecursoInsuficienteException se falta matéria-prima em alguma etapa
     */
    public void produzir(Produto produto, MateriaPrima... insumos) {
        if (maquinas.isEmpty()) {
            throw new MaquinaIndisponivelException("A " + nome + " não possui máquinas para produzir.");
        }
        exigirInsumoPorEtapa(insumos);

        try {
            ligarMaquinas();

            System.out.println("\n--- Iniciando produção de " + produto.getTipo() + " (" + produto.getLote()
                    + ") na " + nome + " ---");

            for (int i = 0; i < maquinas.size(); i++) {
                maquinas.get(i).processar(insumos[i], produto);
            }
        } finally {
            desligarMaquinas();
        }
    }

    /**
     * Soma o custo de operação de todas as máquinas, ou seja, o custo de produzir uma unidade.
     *
     * @return o custo por unidade produzida, em reais
     */
    public double calcularCustoPorUnidade() {
        double total = 0;
        for (Maquina m : maquinas) total += m.getCustoOperacao();
        return total;
    }

    /**
     * Retorna o custo de reparo de uma máquina da linha.
     *
     * @param indice posição da máquina na linha (começando em zero)
     * @return o custo de reparo, em reais
     * @throws EntradaInvalidaException se o índice não corresponde a nenhuma máquina
     */
    public double consultarCustoReparo(int indice) {
        exigirIndiceValido(indice);
        return maquinas.get(indice).getCustoReparo();
    }

    /**
     * Repara uma máquina da linha (estrutura e todas as suas partes). O pagamento do reparo é
     * responsabilidade de quem chama.
     *
     * @param indice posição da máquina na linha (começando em zero)
     * @throws EntradaInvalidaException se o índice não corresponde a nenhuma máquina
     */
    public void repararMaquina(int indice) {
        exigirIndiceValido(indice);
        maquinas.get(indice).reparar();
    }

    /**
     * Monta a lista numerada das máquinas com o resumo de cada uma (sem as partes internas).
     *
     * @return uma linha por máquina, numeradas a partir de 1
     */
    public String listarMaquinas() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maquinas.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(i + 1).append(" - ").append(maquinas.get(i).gerarResumo());
        }
        return sb.toString();
    }

    /**
     * Descreve a estrutura da linha: as máquinas, na ordem das etapas, e os componentes que
     * compõem cada uma, com suas quantidades.
     *
     * @return a descrição hierárquica, pronta para ser exibida
     */
    public String descreverEstrutura() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s (%d máquina(s), executadas na ordem abaixo)", nome, maquinas.size()));
        for (int i = 0; i < maquinas.size(); i++) {
            Maquina m = maquinas.get(i);
            sb.append(String.format("\n  %d. %s (%s)", i + 1, m.getNome(), m.getTipo()));
            for (String componente : m.descreverComponentes().split("\n")) {
                sb.append("\n       ").append(componente);
            }
        }
        return sb.toString();
    }

    /**
     * Monta as leituras atuais de temperatura, ventilação e motor de cada máquina. Os valores são
     * obtidos pelos métodos repassadores da máquina, sem acessar suas partes.
     *
     * @return uma linha por máquina, numeradas a partir de 1
     */
    public String gerarLeiturasSensores() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maquinas.size(); i++) {
            Maquina m = maquinas.get(i);
            if (i > 0) sb.append('\n');
            sb.append(String.format("%d - %s | Temperatura: %.1f °C (máx. %.1f °C) | Ventoinhas operantes: %d/%d | Motor: %.1f%%",
                    i + 1, m.getNome(), m.lerTemperaturaAtual(), m.getTemperaturaMaxima(),
                    m.contarVentoinhasOperantes(), m.getQuantidadeVentoinhas(), m.getSaudeMotor()));
        }
        return sb.toString();
    }

    /**
     * Indica se alguma máquina da linha (ou alguma de suas partes) precisa de manutenção.
     *
     * @return {@code true} se ao menos uma máquina precisa de manutenção
     */
    @Override
    public boolean precisaManutencao() {
        for (Maquina m : maquinas) {
            if (m.precisaManutencao()) return true;
        }
        return false;
    }

    /**
     * Gera a auditoria em cascata da linha: o cabeçalho da linha e, indentado abaixo dele, o
     * diagnóstico de cada máquina, que por sua vez inclui o diagnóstico das suas partes.
     *
     * @return o relatório hierárquico, com várias linhas
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s | Máquinas: %d | Status: %s",
                nome, maquinas.size(), precisaManutencao() ? "REQUER ATENÇÃO" : "OPERACIONAL"));
        for (Maquina m : maquinas) {
            String bloco = m.gerarRelatorioDiagnostico().replace("\n", "\n  ");
            sb.append("\n  > ").append(bloco);
        }
        return sb.toString();
    }

    /**
     * Liga todas as máquinas da linha.
     *
     * @throws MaquinaIndisponivelException se alguma máquina está quebrada ou superaquecida
     */
    private void ligarMaquinas() {
        for (Maquina m : maquinas) m.ligar();
    }

    /**
     * Desliga todas as máquinas da linha.
     */
    private void desligarMaquinas() {
        for (Maquina m : maquinas) m.desligar();
    }

    /**
     * Exige que o índice corresponda a uma máquina da linha.
     *
     * @param indice posição informada (começando em zero)
     * @throws EntradaInvalidaException se o índice está fora do intervalo
     */
    private void exigirIndiceValido(int indice) {
        if (indice < 0 || indice >= maquinas.size()) {
            throw new EntradaInvalidaException("Máquina inválida. Escolha um número entre 1 e " + maquinas.size() + ".");
        }
    }

    /**
     * Exige uma matéria-prima para cada etapa da linha.
     *
     * @param insumos matérias-primas informadas
     * @throws EntradaInvalidaException se a quantidade não corresponde ao número de máquinas
     */
    private void exigirInsumoPorEtapa(MateriaPrima[] insumos) {
        if (insumos == null || insumos.length != maquinas.size()) {
            throw new EntradaInvalidaException(String.format(
                    "A %s tem %d etapa(s), mas foram informadas %d matéria(s)-prima(s).",
                    nome, maquinas.size(), insumos == null ? 0 : insumos.length));
        }
    }
}

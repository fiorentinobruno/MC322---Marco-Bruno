# MC322 - Planta Industrial de PCBs

Simulação de uma planta industrial de placas de circuito impresso (PCBs), desenvolvida em Java para a disciplina
MC322 - Programação Orientada a Objetos (UNICAMP) por **Marco Correa** e **Bruno Fiorentino**.

A linha de produção tem três etapas: **Corrosora → Embaladora → Bancada de Teste (inspeção)**. O usuário registra
demandas, escolhe uma estratégia de produção, compra matéria-prima, fabrica placas e audita/repara as máquinas, tudo
por um menu no terminal. O estado da planta pode ser salvo em XML e recuperado em execuções posteriores.

## Requisitos

- JDK 17 ou superior (o Gradle Wrapper baixa o Gradle e o JUnit automaticamente; não é preciso instalá-los).

## Como usar

Linux / macOS:

```bash
./gradlew build      # compila o projeto e executa os testes automatizados
./gradlew run        # executa a aplicação (menu interativo)
./gradlew test       # executa apenas os testes JUnit
./gradlew javadoc    # gera a documentação em build/docs/javadoc/index.html
```

Windows:

```bat
gradlew.bat build
gradlew.bat run
gradlew.bat test
gradlew.bat javadoc
```

> Dica: para um terminal mais limpo, use `./gradlew run --console=plain`.

O relatório HTML dos testes é gerado em `build/reports/tests/test/index.html` (o resultado de cada teste também
aparece no terminal, com `PASSED`/`FAILED`). Os testes não abrem o menu interativo e não pedem nada pelo teclado.

## Estrutura do projeto

```
build.gradle / settings.gradle / gradlew / gradlew.bat / gradle/wrapper/
README.md
Justificativa.txt
src/main/java/br/unicamp/ic/mc322/
    Main.java                 menu, tratamento de erros na interface, quando salvar/carregar
    model/                    entidades e componentes do domínio
        Maquina, Corrosora, Embaladora, EstacaoInspecao
        MotorDeAcionamento, SensorDeTemperatura, VentoinhaDeResfriamento   (partes da máquina)
        LinhaDeProducao        (agrupador das máquinas)
        Produto, PlacaSimples, PlacaDupla, PlacaMultiCamada, FabricaDePlacas
        Demanda, StatusDemanda, MateriaPrima, Cenario, Auditavel, Esteira
    service/                  regras de negócio da planta
        GerenciadorProducao    (demandas, produção, estoque, orçamento)
        PlantaPadrao           (monta a planta inicial do projeto)
    strategy/                 padrão Strategy
        EstrategiaProducao, EstrategiaOrdemChegada, EstrategiaMaiorDemanda, EstrategiaMaximoProdutos
    persistence/              leitura e escrita em XML
        PersistenciaPlanta
    exception/                exceções de domínio
        PlantaException e subclasses, PersistenciaException
src/test/java/br/unicamp/ic/mc322/
    GeradorFixo.java          gerador "aleatório" fixo, para testes determinísticos
    model/                    DemandaTest, MateriaPrimaTest, MaquinaTest
    service/                  GerenciadorProducaoTest
    strategy/                 EstrategiasTest
    persistence/              PersistenciaPlantaTest
```

## Persistência (salvar e carregar)

O menu principal ganhou duas opções:

```
8 - Salvar estado da planta
9 - Carregar estado da planta
```

- **Arquivo:** `dados/planta.xml`, relativo à pasta em que a aplicação é executada (com `./gradlew run`, a raiz do
  projeto). A pasta `dados/` é criada automaticamente e está no `.gitignore`.
- **Primeira execução:** a ausência do arquivo é tratada como situação **normal**: a aplicação informa que não há estado
  salvo, pergunta o cenário e monta a planta padrão. Escolhemos assim porque é o caso esperado de quem acabou de clonar
  o repositório, e não um erro.
- **Execuções seguintes:** se o arquivo existir, a aplicação pergunta se o usuário quer continuar o estado salvo ou
  começar uma planta nova. Começar uma planta nova **não** apaga nem sobrescreve o arquivo; ele só muda quando o usuário
  salva.
- **Atualizar:** salvar de novo substitui o estado anterior. Ao sair, a aplicação pergunta se o usuário quer salvar;
  responder "não" mantém intacto o último estado salvo.
- **Carregar:** a planta carregada **substitui por completo** a planta em uso (demandas, estoque, máquinas, armazém),
  sem combinar dados das duas.

Trecho do arquivo gerado:

```xml
<planta versao="1">
    <configuracao cenario="IDEAL" estrategia="ORDEM_CHEGADA">
        <budget>5970.0</budget>
        <contadorPlacas>1</contadorPlacas>
        <contadorLotes>1</contadorLotes>
    </configuracao>
    <materiasPrimas>
        <materiaPrima id="MP1" papel="laminado">
            <nome>Laminado FR-4 (cobre 35 um)</nome>
            <quantidade>188.0</quantidade>
            <unidade>dm2</unidade>
            <quantidadeMinima>20.0</quantidadeMinima>
            <custoPorUnidade>5.0</custoPorUnidade>
        </materiaPrima>
        ...
    </materiasPrimas>
    <linhaDeProducao nome="Linha de Produção PCB-1">
        <maquina ordem="1" tipo="corrosora">
            <nome>Corrosora CU-1</nome>
            <configuracao>
                <capacidadeMax>60.0</capacidadeMax>
                <custoOperacao>10.0</custoOperacao>
                <chanceAumentarFalha>0.3</chanceAumentarFalha>
            </configuracao>
            <estado>
                <saude>97.8</saude>
            </estado>
            <componentes>
                <motor potenciaKw="3.0" saude="99.1"/>
                <sensorTemperatura temperaturaAtual="25.0" temperaturaMaxima="70.0"/>
                <ventoinhas>
                    <ventoinha numero="1" saude="99.2"/>
                    <ventoinha numero="2" saude="98.7"/>
                </ventoinhas>
            </componentes>
        </maquina>
        ...
    </linhaDeProducao>
    <demandas>
        <demanda status="EM_PRODUCAO" tipoProduto="Placa Dupla">
            <quantidadePendente>3</quantidadePendente>
            <custoUnitarioEstimado>30.0</custoUnitarioEstimado>
        </demanda>
    </demandas>
    <armazem>
        <placa id="PCB-2" tipo="Placa Dupla">
            <lote>LOTE-2</lote>
            <status>inspecionado</status>
            <materiaPrimaUsada>MP1</materiaPrimaUsada>
            <riscoAcumulado>0.0</riscoAcumulado>
        </placa>
    </armazem>
</planta>
```

### Organização das responsabilidades

- `PersistenciaPlanta` (pacote `persistence`) é a **única** classe que conhece XML e arquivos. Ela usa a API DOM do
  próprio JDK (`javax.xml`), sem bibliotecas externas.
- As entidades de domínio não sabem que são persistidas. Elas só ganharam o necessário para expor e **restaurar** o
  próprio estado com validação: `Maquina.restaurarEstado(...)` (que delega às partes), `Demanda.restaurar(...)` e
  `GerenciadorProducao.restaurarEstado(...)`. Assim, um objeto recuperado respeita as mesmas invariantes de um objeto
  criado normalmente.
- A criação das placas pelo tipo ficou em `FabricaDePlacas`, usada tanto pelo gerenciador quanto pela persistência, para
  não duplicar essa regra. A montagem da planta inicial saiu do `Main` e foi para `PlantaPadrao`.
- O `Main` só decide **quando** salvar/carregar e troca a planta em uso pela carregada.

### Tratamento de erros de persistência

Todas as falhas viram uma `PersistenciaException` (subclasse de `PlantaException`, a mesma hierarquia da Tarefa 4) com
um `Motivo` específico:

| Motivo                | Quando ocorre                                                                    |
|-----------------------|----------------------------------------------------------------------------------|
| `ARQUIVO_INEXISTENTE` | não há arquivo salvo (na inicialização isso não é erro: vira planta padrão)       |
| `ARQUIVO_VAZIO`       | o arquivo existe mas está vazio                                                   |
| `FORMATO_INVALIDO`    | XML mal formado (corrompido) ou com estrutura diferente da esperada               |
| `DADOS_INVALIDOS`     | XML válido, mas com valores que violam as regras (estoque negativo, saúde > 100, demanda concluída com unidades pendentes, cenário desconhecido, número inválido...) |
| `FALHA_LEITURA`       | erro do sistema ao ler o arquivo                                                  |
| `FALHA_ESCRITA`       | erro do sistema ao gravar (sem permissão, caminho inválido...)                    |

Garantias de consistência:

- **Carregar valida antes de alterar:** a persistência monta uma planta nova em memória e só a devolve depois que tudo
  foi lido e validado. Se algo falhar, a planta em uso continua exatamente como estava.
- **Salvar não corrompe o estado anterior:** o XML é gravado primeiro em um arquivo temporário na mesma pasta e só então
  movido para o lugar de `planta.xml`. Se a escrita falhar no meio, o último estado salvo continua válido e o temporário
  é apagado.
- Na inicialização, se o arquivo estiver corrompido, a aplicação mostra o erro, inicia a planta padrão e **não**
  sobrescreve o arquivo (o usuário pode inspecioná-lo antes de salvar de novo).

## Escopo e Limitações

### Dados persistidos vs. não persistidos

**Persistidos (obrigatórios pelo enunciado):**

- **Estoque de matérias-primas:** identificador, nome, quantidade, unidade, estoque mínimo e custo por unidade das três
  matérias-primas, com o papel de cada uma (laminado, embalagem, selo), porque cada etapa da linha consome uma delas.
- **Máquinas e seus subcomponentes (composição da Tarefa 5):** para cada máquina, na ordem da linha, salvamos a
  configuração (nome, capacidade, custo de operação e os parâmetros próprios de cada tipo: chance de problema, consumo de
  ESD/selo, rigor da inspeção) e o estado (saúde da estrutura). Para as partes: saúde do motor, temperatura atual do
  sensor e saúde de cada ventoinha. Potência do motor e temperatura máxima são fixas por tipo de máquina; elas são
  gravadas e, no carregamento, conferidas com as do modelo. As partes continuam sendo criadas pelo construtor da máquina
  (composição) e só têm o estado restaurado depois, então a relação Todo-Parte é preservada.
- **Demandas e seus `StatusDemanda`:** tipo de produto, quantidade pendente, custo unitário estimado e status
  (`PENDENTE`, `EM_PRODUCAO`, `CONCLUIDA`, `CANCELADA`).

**Persistidos (opcionais, escolhidos pela dupla):**

- **Orçamento atual e cenário:** sem eles a planta recuperada não seria comparável (o cenário define desgaste e falhas, e
  o orçamento decide quais demandas são viáveis).
- **Estratégia de produção ativa:** é uma escolha do usuário que ele esperaria reencontrar.
- **Contadores de placas e de lotes:** garantem que, depois de carregar, as novas placas continuem a numeração
  (`PCB-3`, `LOTE-3`...) sem repetir identificadores do armazém.
- **Armazém (placas aprovadas):** identificador, tipo, lote, status, matéria-prima usada e risco acumulado, para que o
  relatório do armazém e a auditoria continuem iguais após recarregar.

**Mantidos apenas em memória (não persistidos):**

- **Se a máquina está ligada:** as máquinas só ficam ligadas durante uma produção e sempre são desligadas no final
  (`finally`); entre duas ações do menu estão sempre desligadas, então não há o que salvar.
- **Geradores aleatórios (`Random`):** são ferramenta de simulação, não estado do negócio. A planta recuperada continua
  com o mesmo cenário e o mesmo estado de desgaste, mas os próximos sorteios são novos.
- **Placas rejeitadas na inspeção:** elas não vão para o armazém (regra das tarefas anteriores), então não existem em
  nenhuma coleção da planta. O efeito delas fica registrado no contador de placas e no estoque consumido.
- **Valores constantes do modelo** (custo de reparo de cada tipo de máquina, limites de manutenção, taxas de desgaste,
  qualidade de cada tipo de placa): fazem parte do código, não do estado; a qualidade de uma placa, por exemplo, é
  derivada do tipo salvo.
- **`Esteira`:** a classe existe desde as primeiras tarefas, mas não participa da linha atual nem guarda estado usado
  pela planta.
- **Mensagens já exibidas no terminal:** são saída da interface, não dados da planta.

Limitações conhecidas: há um único arquivo de estado (não há vários "saves"); o formato tem `versao="1"` e um arquivo de
outra versão é recusado em vez de convertido; editar o XML à mão é possível, mas qualquer valor fora das regras faz o
carregamento ser recusado inteiro (não carregamos "metade" de uma planta).

### Explicação da suíte de testes

Os testes usam **JUnit 5** e rodam com `./gradlew test` (e automaticamente no `./gradlew build`). Princípios seguidos:

- **Independência:** cada classe de teste prepara seus dados em um `@BeforeEach`, a partir de uma planta padrão nova;
  nenhum teste depende de outro, da ordem de execução ou de uma execução manual anterior.
- **Isolamento dos arquivos:** os testes de persistência usam `@TempDir`, uma pasta temporária criada e apagada pelo
  JUnit para cada teste. O arquivo real `dados/planta.xml` nunca é usado.
- **Determinismo:** as máquinas sorteiam falhas e rejeições. Os testes trocam o gerador aleatório de cada máquina por um
  `GeradorFixo` (`Maquina.definirGeradorAleatorio`), que força "nunca falha" ou "sempre falha", então o resultado é
  sempre o mesmo.
- **Erros verificados explicitamente:** quando uma operação deve falhar, o teste usa `assertThrows` com a exceção
  específica (e, na persistência, confere o `Motivo`) e verifica também que o estado não foi alterado.

O que cada classe avalia:

| Classe de teste | Categoria | O que verifica |
|-----------------|-----------|----------------|
| `model/DemandaTest` | regras de negócio + erros | criação, transições de status (pendente → em produção → concluída), demanda cancelada não pode ser alterada nem produzida, concluída não pode ser cancelada e é reaberta ao receber unidades, `restaurar` rejeita combinações inválidas |
| `model/MateriaPrimaTest` | regras de negócio + erros | consumo e reposição, `RecursoInsuficienteException` ao consumir além do disponível ou abaixo do mínimo (sem alterar o estoque), valores inválidos no construtor |
| `model/MaquinaTest` | criação/composição + estado + erros | máquina nova com motor, sensor e ventoinhas; configuração inválida; `restaurarEstado` aplica valores às partes e, se inválido, não altera nada; máquina quebrada ou superaquecida não liga até o reparo; desligada não processa; a linha não aceita máquina duplicada e não expõe a lista para alteração |
| `strategy/EstrategiasTest` | regras de negócio | o critério de cada estratégia (ordem de chegada, maior demanda, maximizar produção), ignorando demandas canceladas e sem orçamento |
| `service/GerenciadorProducaoTest` | produção + regras de negócio + erros | estado inicial por cenário; registro e acúmulo de demandas; produção completa (consumo das 3 matérias-primas, custo debitado, demanda, armazém, lote, desgaste); escolha pela estratégia ativa; estoque insuficiente; falha técnica no meio da linha devolve a matéria-prima e não cobra; falta de orçamento cancela a demanda; compra de matéria-prima; restauração inválida não altera o gerenciador |
| `persistence/PersistenciaPlantaTest` | persistência + erros | salvar cria o XML (e a pasta); salvar e carregar preserva configuração, orçamento, contadores, estoque, máquinas e cada parte, demandas com todos os status e o armazém; a planta carregada é independente e continua produzindo sem repetir IDs; salvar de novo substitui o estado; arquivo inexistente, vazio, corrompido, com estrutura incompatível ou com dados fora das regras; falha de escrita sem deixar temporários; carregamento com falha não altera a planta em uso |

## Tratamento de erros

Os erros previstos são representados por uma hierarquia de exceções de domínio:

| Exceção                        | Quando ocorre                                                              |
|--------------------------------|----------------------------------------------------------------------------|
| `PlantaException`              | Raiz da hierarquia                                                         |
| `EntradaInvalidaException`     | Valor digitado inválido (quantidade, índice, matéria-prima desconhecida)  |
| `DemandaInvalidaException`     | Demanda inexistente, cancelada, sem unidades pendentes ou tipo desconhecido |
| `RecursoInsuficienteException` | Falta de matéria-prima                                                     |
| `OrcamentoInsuficienteException` | Orçamento não cobre produção, compra ou reparo                           |
| `MaquinaIndisponivelException` | Máquina quebrada, desligada, com falha técnica ou sem capacidade           |
| `PersistenciaException`        | Falha ao salvar ou carregar o estado (com `Motivo` específico)            |

O `Main` captura essas exceções, mostra uma caixa de erro com mensagem clara, aguarda ENTER e devolve o usuário ao menu.
Uma produção que falha não deixa a planta pela metade: os recursos são conferidos antes de qualquer consumo e, se algo
falhar durante a linha, estoque e orçamento voltam ao valor anterior (o desgaste das máquinas que chegaram a operar é
mantido, pois representa uso físico real).

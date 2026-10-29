# MC322 - Planta Industrial de PCBs

Simulação de uma planta industrial de placas de circuito impresso (PCBs), desenvolvida em Java para a disciplina
MC322 - Programação Orientada a Objetos (UNICAMP) por **Marco Correa** e **Bruno Fiorentino**.

A linha de produção tem três etapas: **Corrosora → Embaladora → Bancada de Teste (inspeção)**. O usuário registra
demandas, escolhe uma estratégia de produção, compra matéria-prima, fabrica placas e audita/repara as máquinas, tudo
por um menu no terminal.

## Requisitos

- JDK 17 ou superior (o Gradle Wrapper baixa o Gradle automaticamente; não é preciso instalá-lo).

## Como usar

Linux / macOS:

```bash
./gradlew build      # compila o projeto
./gradlew run        # executa a aplicação (menu interativo)
./gradlew javadoc    # gera a documentação em build/docs/javadoc/index.html
```

Windows:

```bat
gradlew.bat build
gradlew.bat run
gradlew.bat javadoc
```

> Dica: para um terminal mais limpo, use `./gradlew run --console=plain`.

O projeto compila a partir de um ambiente limpo, sem depender de arquivos compilados nem de comandos manuais.

## Estrutura do projeto

```
build.gradle
settings.gradle
gradlew / gradlew.bat
gradle/wrapper/
README.md
justificativa.txt
src/main/java/
    Main.java                       menu e tratamento de erros na interface
    GerenciadorProducao.java        regras da planta (demandas, produção, estoque, orçamento)
    Maquina, Corrosora, Embaladora, EstacaoInspecao
    Produto, PlacaSimples, PlacaDupla, PlacaMultiCamada
    Demanda, StatusDemanda, Cenario, MateriaPrima, Esteira
    EstrategiaProducao + 3 estratégias, Auditavel
    PlantaException e subclasses    exceções de domínio
```

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

O `Main` captura essas exceções, mostra uma caixa de erro com mensagem clara, aguarda ENTER e devolve o usuário ao menu.
Uma produção que falha não deixa a planta pela metade: os recursos são conferidos antes de qualquer consumo e, se algo
falhar durante a linha, estoque e orçamento voltam ao valor anterior (o desgaste das máquinas que chegaram a operar é
mantido, pois representa uso físico real).

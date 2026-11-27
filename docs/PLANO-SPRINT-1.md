# Plano da Sprint 1 — Delivery KMP

Plano único e cronológico para as duas disciplinas (DIM0547 Web II e DIM0524 Mobile),
porque o monorepo, o CI e a branch principal são compartilhados. Cada item tem uma
etiqueta de trilha: **[W]** Web II, **[M]** Mobile, **[WM]** afeta as duas.

## Datas que mandam

| Data | O que |
|---|---|
| **28/09 (seg)** | **Apresentação da Coorte B (online, Google Meet) — nas duas disciplinas** |
| 02/10 (sex) 23:59 | Entrega final: estado da branch `master` (hash do último commit) |

> A data que realmente aperta é **28/09**, não 02/10: o código precisa estar demonstrável
> ao vivo no dia 28. O intervalo 29/09–02/10 é buffer de polimento, não tempo de construção.
>
> Os **vídeos**, porém, só vencem em 02/10 (o que é avaliado é o link no `README.md`).
> Grave-os no buffer, não na véspera da apresentação. São **dois vídeos de 5 minutos**
> (um por disciplina), cada um com os dois integrantes falando. Os roteiros são diferentes.

## Índice

| Seção | O que tem lá |
|---|---|
| [Estado atual](#estado-atual-ponto-de-partida-real) | O que existe hoje no repositório, verificado no código |
| [D1 — Camadas e teste de arquitetura](#d1--onde-vivem-as-camadas-e-o-que-o-teste-de-arquitetura-vigia-w) | Regra de dependência, inversão, e por que o `shared/` não serve de palco |
| [D2 — Modelo de dados](#d2--modelo-de-dados-w) | Centavos, invariantes, chave estrangeira, UUID |
| [D3 — Ordem no Mobile](#d3--ordem-no-mobile-tema-e-componentes-antes-das-telas-m) | Por que tema e componentes vêm antes das telas |
| [D4 — Fluxo no Git](#d4--fluxo-de-trabalho-no-git-wm) | CI como portão, revisão cruzada |
| [Dia 0 (20–21/09)](#dia-0--hojeamanhã-202109-destravar-o-terreno) | Quadro, version catalog, source sets de teste, CI |
| [Dias 1–3 (22–24/09)](#dias-13-222409-o-grosso-da-construção) | Camadas, migrações, compose, repositórios, CRUD · tema, telas, formulário |
| [Dias 4–5 (25–26/09)](#dias-45-252609-navegação-contratos-e-o-que-aparece-no-vídeo) | Validação, OpenAPI, ArchUnit · navegação, deep link, larguras |
| [Dia 6 (27/09)](#dia-6-2709-testes-acessibilidade-e-ci-verde) | Testes, acessibilidade, CI verde |
| [Dia 7 (28/09)](#dia-7-2809-apresentação-da-coorte-b) | Apresentação e os dois roteiros de vídeo |
| [Buffer (29/09–02/10)](#dias-811-29090210-buffer) | Gravação dos vídeos e polimento |
| [Checklist de armadilhas](#checklist-de-armadilhas-específicas-deste-repositório) | O que quebra neste repo especificamente |
| [Riscos de versão](#riscos-de-versão-confirmar-antes-de-fixar) | O que confirmar antes de fixar |

Cada tarefa do cronograma traz um bloco **> Por quê** com o conceito de engenharia por trás
da decisão. As decisões D1–D4 trazem a fundamentação completa.

## Estado atual (ponto de partida real)

Confirmado lendo o repositório hoje:

- `shared/` tem o domínio puro (`Restaurant`, `MenuItem`, `Dinheiro`, ids) — **sem testes**.
- `api/` tem só `/health`. As dependências de Exposed, Koin e Postgres já estão declaradas,
  mas **não há nenhuma linha de código de banco, nem source set de teste**.
- `app/` tem uma tela (`RestaurantListScreen`) com dados locais, `MaterialTheme {}` padrão,
  **sem navegação, sem tema próprio, sem testes, sem source set de teste**.
- `docker-compose.yml` sobe Postgres, mas **nada consome a URL**.
- CI verde, roda ktlint, detekt, build, `:shared:jvmTest`, APK e jar desktop, Go.
- `mise run test` roda `./gradlew test` — **isso não executa nada do módulo KMP `app`**.
- ktlint e detekt são aplicados via `subprojects {}` na raiz: **todo código novo passa por eles**.
- Falta no `libs.versions.toml`: Flyway, Testcontainers, ArchUnit/Konsist, navigation-compose,
  material3 adaptive/window-size-class, `ktor-server-test-host`, ui-test do Compose.

Ou seja: das 20 tarefas das duas disciplinas, praticamente nenhuma tem base pronta além
do domínio e do CI. Planeje como construção, não como ajuste.

---

## Decisões a tomar ANTES de escrever código

### D1 — Onde vivem as camadas e o que o teste de arquitetura vigia [W]

#### O conceito: regra de dependência

Um sistema tem partes que mudam por motivos diferentes. As regras de restaurante e cardápio
mudam quando o **produto** muda — é a parte estável, que justifica o software existir. Ktor,
Exposed e o próprio PostgreSQL mudam quando a **infraestrutura** muda: são detalhes
substituíveis, escolhidos por conveniência.

A regra de dependência diz que a seta aponta sempre do volátil para o estável, nunca o
contrário. O adaptador HTTP conhece o caso de uso; o caso de uso não sabe que HTTP existe.
Se inverter, uma troca de framework vira uma reescrita do domínio — e, no dia a dia, o
domínio deixa de ser testável sem subir meia infraestrutura junto.

A proposta do grupo já assume isso: `docs/proposta.md` §4 diz que o domínio fica em
`shared/src/commonMain` "sem dependência de Compose, Ktor, banco de dados ou outra tecnologia
de infraestrutura". D1 é essa frase virando estrutura de pastas e teste.

#### O mecanismo: inversão de dependência

O caso de uso precisa gravar no banco, mas não pode conhecer o banco. A saída é o caso de uso
**declarar a porta** e a infraestrutura implementá-la:

```
aplicacao/   interface RepositorioDeRestaurantes { fun salvar(r: Restaurant) }   ← declara
                      ▲
infra/       class RepositorioExposed : RepositorioDeRestaurantes                 ← implementa
```

O controle flui do caso de uso para o banco; a **dependência de código** aponta ao contrário.
É isso que permite, em T4, manter uma implementação em memória para os testes rápidos: quem
depende da interface não percebe a diferença.

#### Por que isso vira teste, e não só combinado

Regra que mora na cabeça dos dois integrantes é violada no primeiro PR com pressa. O teste de
arquitetura é a regra escrita como verificação executável, rodando no CI a cada push — o
combinado passa a ter quem o cobre.

Por isso a rubrica não pede só "ter camadas": pede **ver o teste falhar**. Um teste que nunca
reprovou nada não é prova de nada — pode estar apontando para o pacote errado, ou com a regra
invertida, e ninguém saberia. Quebrar de propósito uma vez é como se verifica o verificador.

#### Por que `shared/commonMain` não serve de palco

Esta é a parte que o plano anterior só afirmava. A cadeia completa:

1. `shared/build.gradle.kts` tem `commonMain.dependencies { }` **literalmente vazio**. Nenhum
   framework está no classpath desse source set.
2. Logo, escrever `import io.ktor.server.application.Application` numa classe de domínio dá
   **erro de compilação** — *unresolved reference*. Não é violação de arquitetura detectada:
   é código que não existe para o compilador.
3. Erro de compilação ⇒ o Gradle nunca chega na task de teste ⇒ o ArchUnit não roda, não
   imprime nada, **não fica vermelho**. No vídeo você mostraria uma mensagem do compilador,
   que prova menos: não demonstra que *você* escreveu uma verificação.

Um mal-entendido a evitar na apresentação: **não** é que "Ktor não é multiplataforma" — o
`ktor-server-core` do Ktor 3 tem alvos além da JVM. É só o classpath: nada foi declarado ali.

A consequência é contraintuitiva e vale dizer em voz alta na arguição: **o KMP já impõe a
regra por construção no `shared/`**. Ótimo para o produto, inútil para a demonstração — e o
critério pede a demonstração.

O risco real mora em `api/`, que **tem** Ktor e Exposed no classpath, onde escrever
`import io.ktor...` dentro de um caso de uso compila normalmente e ninguém percebe na revisão.
É exatamente onde uma regra não verificada seria de fato violada — e, portanto, onde o teste
tem valor.

#### A decisão

```
shared/src/commonMain/.../dominio/            entidades e objetos de valor (já existe)
api/src/main/kotlin/.../aplicacao/            casos de uso + portas (interfaces de repositório)
api/src/main/kotlin/.../infra/persistencia/   tabelas e repositórios Exposed
api/src/main/kotlin/.../infra/http/           rotas Ktor, DTOs, mapeamento de erro
api/src/test/kotlin/.../ArquiteturaTest.kt    ArchUnit
```

Regra vigiada: `..aplicacao..` e o domínio **não** podem depender de `io.ktor..`,
`org.jetbrains.exposed..`, `java.sql..`, `org.postgresql..`.

**ArchUnit, não Konsist:** uma ferramenta só, e o ArchUnit lê bytecode JVM — exatamente o que
`api/` produz. Como `api` depende de `project(":shared")`, a mesma regra pode incluir os
pacotes do domínio no escopo. A regra cobre os dois módulos; só a **encenação** da falha
acontece do lado do `api`.

Mensagem que você deve ver na tela ao quebrar de propósito (item 18) — é isso que vai no vídeo:

```
Architecture Violation [Priority: MEDIUM] - Rule 'classes that reside in a package
'..aplicacao..' should not depend on classes that reside in a package 'io.ktor..''
was violated (1 times)
```

### D2 — Modelo de dados [W]

`Restaurant` 1:N `MenuItem`. Rota aninhada `/restaurantes/{id}/itens` **e** filtro.

**Dinheiro em centavos (`BIGINT`), jamais ponto flutuante.** Em binário, `0.1 + 0.2 != 0.3`:
`float`/`double` não representam frações decimais exatamente, e o erro acumula a cada soma.
Dinheiro exige aritmética exata, então se guarda a menor unidade indivisível — o centavo — num
inteiro. O domínio já acerta nisso (`Dinheiro(val centavos: Long)`); o banco precisa acompanhar,
senão a corretude se perde justamente na borda da persistência.

**A mesma regra em dois lugares, de propósito.** São duas invariantes distintas, em escopos
distintos: `Dinheiro` exige `centavos >= 0` (zero é um valor monetário legítimo — um total, um
desconto integral), enquanto `adicionarItem` exige preço **positivo**, porque item de cardápio
de graça não é item de cardápio. O `CHECK (preco_centavos > 0)` no SQL espelha a segunda, a de
`adicionarItem` — e a repetição é deliberada, não descuido: a regra de domínio protege
o caminho da aplicação, a constraint protege contra **qualquer** escrita que não passe por ele
(um script, uma migração futura, um `psql` aberto às duas da manhã). O banco é a última
fronteira onde a invariante ainda pode ser garantida.

**Chave estrangeira com `ON DELETE CASCADE`.** O 1:N da proposta expresso onde o banco
consegue fazer valer: item de cardápio não existe sem restaurante. Integridade referencial é a
regra de negócio que você *não* precisa lembrar de aplicar no código.

**UUID em vez de `serial`.** O argumento genérico (id gerável fora do banco, não vaza volume de
negócio) importa menos aqui que o específico: `RestaurantId.novo()` gera o id **dentro do
domínio**, antes de qualquer persistência. Com `serial`, o id só passaria a existir depois que
o `INSERT` retornasse — o objeto de domínio não poderia ser construído por inteiro sem tocar o
banco, e `Restaurant` deixaria de ser testável isoladamente. É a regra de dependência de D1
reaparecendo na escolha do esquema.

**Índice em `restaurante_id`** porque a rota aninhada `/restaurantes/{id}/itens` filtra por ele.
Índice serve consulta que existe; índice "por precaução" só custa escrita mais lenta.

### D3 — Ordem no Mobile: tema e componentes ANTES das telas [M]

**O conceito: preocupação transversal.** Tema e componentes reutilizáveis atravessam todas as
telas — não são uma feature ao lado das outras, são uma decisão que cada tela herda. Decisão
transversal tomada tarde custa retrabalho multiplicado pelo número de telas; tomada cedo, é
custo pago uma vez.

É o mesmo raciocínio que manda separar as camadas antes de escrever os repositórios, do lado
Web (item 8): **ordene o trabalho pelo custo de mudar de ideia depois**, não pelo que parece
mais urgente.

**Neste repositório.** `DeliveryApp.kt` usa `MaterialTheme {}` cru, sem esquema de cor próprio,
e `RestaurantListScreen.kt` tem espaçamentos e decisões visuais soltos no corpo da tela. Cada
tela nova escrita antes do tema é uma tela a reescrever depois.

**Fazer certo significa:** cor por **papel semântico** (`colorScheme.surface`, `onSurface`,
`primary`) em vez de valor fixo. É isso — e só isso — que faz o mesmo componente funcionar nos
modos claro e escuro sem nenhum `if (escuro)` espalhado pelo código.

### D4 — Fluxo de trabalho no Git [WM]

**O conceito: o CI é portão, não relatório.** Verificação que roda *depois* do merge informa
que já quebrou; verificação que roda *antes* impede que quebre. É por isso que a revisão de PR
é exigência e não formalidade burocrática — e por isso vale 30% da nota.

Com duas pessoas, a revisão cruzada compra duas coisas concretas: conhecimento distribuído
(nenhum trecho do sistema com um dono único, que vira ponto cego quando ele some na véspera) e
um segundo par de olhos antes de `master`, que é o que será avaliado pelo hash do commit.

As duas rubricas exigem: ≥1 PR integrado por integrante, PRs revisados **pelo outro**,
cartões ligados a PRs, CI verde. Na prática:

- **Nada de push direto em `master`** (o histórico recente tem commits `Update README.md`
  que parecem edição direta — pare com isso agora).
- Divisão natural: um integrante puxa a trilha Web, o outro a trilha Mobile; cada um revisa
  os PRs do outro. Isso satisfaz as duas exigências de uma vez.
- Commits distribuídos pelas semanas — não faça tudo em 27/09.

---

## Cronograma

### Dia 0 — hoje/amanhã (20–21/09): destravar o terreno

Tudo aqui é bloqueante para o resto. Faça primeiro, num PR só ou dois pequenos.

> **Por quê o Dia 0 existe:** antecipar o risco. O que tem mais chance de quebrar neste
> projeto não é o CRUD nem as telas — é a infraestrutura de teste (Docker no runner do
> Actions, Skia no CI, tasks do Gradle que não rodam o que prometem). Erro de infraestrutura
> descoberto no dia 0 custa uma tarde; descoberto em 27/09 custa a entrega. Você paga o risco
> quando ele é barato.

1. **[WM] T1 — Quadro.** Mover as histórias P1 para a coluna da sprint, criar os cartões
   T2–T10 das duas disciplinas, atribuir dono a cada um. É barato e vale nota de
   "atividade no repositório" desde o primeiro dia.
2. **[WM] Estender o `libs.versions.toml`.** Adicionar, num PR só:
   - Web: `flyway-core`, `flyway-database-postgresql`, `hikaricp`, `archunit-junit5`,
     `ktor-server-test-host`, `testcontainers-bom` + `testcontainers-postgresql`, JUnit 5.
   - Mobile: `org.jetbrains.androidx.navigation:navigation-compose`,
     `org.jetbrains.compose.material3:material3-window-size-class:1.7.3`,
     `org.jetbrains.compose.material3.adaptive:adaptive:1.0.0-alpha03`.
   - **Confirmar as versões** antes de fixar (veja "Riscos de versão" no fim). As duas
     últimas linhas são as confirmadas para Compose MP 1.7.3.
   > **Por quê:** o version catalog é a fonte única de versões do monorepo. Versão declarada
   > em dois lugares diverge, e divergência de versão entre módulos é um bug de build que se
   > manifesta longe da causa — o tipo mais caro de depurar sob prazo.
3. **[WM] Consertar `mise run test`.** Hoje é `./gradlew test`, que não roda nada do `app`
   (módulo KMP). Trocar por algo como:
   `./gradlew :shared:allTests :api:test :app:desktopTest` (ou `check`).
   As duas rubricas cobram explicitamente que `mise run test` roda a suíte inteira.
   > **Por quê:** um comando documentado que não executa o que promete é pior que comando
   > nenhum — produz falsa segurança. Hoje `mise run test` termina com sucesso sem rodar um
   > único teste do `app`, e é esse "verde" que você levaria para a entrega.
4. **[M] Criar os source sets de teste do `app`** (`commonTest` com `kotlin("test")` e
   `compose.ui:ui-test`; `desktopTest` com `compose.desktop.currentOs`) e **[W] o
   `api/src/test`** com JUnit 5 — incluindo `tasks.test { useJUnitPlatform() }` no
   `api/build.gradle.kts`: sem essa linha o módulo Kotlin/JVM **não executa** os testes
   JUnit 5 (ArchUnit e Ktor) e o CI fica verde sem rodar nada.
   Só o esqueleto, com um teste trivial, para provar que roda.
   > **Por quê o teste trivial:** ele não testa o produto, testa o **encanamento**. Você quer
   > ver a contagem de testes executados subir de zero antes de escrever o primeiro teste que
   > importa. Mesmo motivo de quebrar o ArchUnit de propósito: verificar o verificador.
5. **[WM] CI: acrescentar os passos de teste**, com a instalação das libs do Skia **antes**
   do teste de desktop:
   ```yaml
   - run: sudo apt-get update && sudo apt-get install -y libgl1 libegl1 libfontconfig1
   - run: ./gradlew :app:desktopTest --no-daemon
   - run: ./gradlew :api:test --no-daemon
   ```
   > Atenção: o módulo aqui se chama **`:app`**, não `:composeApp` como nos enunciados.
   > Sem essas três bibliotecas o teste falha com `UnsatisfiedLinkError: libGL.so.1`.
   Suba esse passo **hoje**, mesmo com testes vazios: é o ponto mais provável de CI vermelho,
   e CI vermelho no prazo custa 30% **nas duas disciplinas**.
   > **Por quê essas três bibliotecas:** o Compose desenha no desktop através do Skia, que é
   > código nativo e precisa do OpenGL do sistema. O runner Linux do Actions vem sem elas. O
   > teste de interface não precisa de tela, mas precisa da biblioteca gráfica.
6. **[W] Um teste descartável com Testcontainers, já no CI.** Sobe um Postgres e verifica
   que conecta. Mesmo raciocínio do `libGL`: se o Docker do runner der problema, você quer
   saber em 21/09, não em 27/09 com os testes de integração prontos.
7. **[WM] Higiene:** adicionar `.kotlin/` ao `.gitignore`; decidir se
   `docs/comentarios-extraidos.md` entra no repositório; atualizar `docs/uso-de-ia.md`
   para citar também o Claude Code (a rubrica de Mobile exige esse registro atualizado).

---

### Dias 1–3 (22–24/09): o grosso da construção

Duas frentes em paralelo, uma por integrante.

#### Trilha Web II [W]

8. **T6 (primeira metade) — Separar as camadas.** Mover/criar os pacotes de D1 **antes**
   de escrever repositório. Se fizer depois, você move código duas vezes.
   > **Por quê agora:** a estrutura de pacotes define onde cada coisa nasce. Escrever primeiro
   > e organizar depois significa mover código, refazer imports e refazer PRs já revisados —
   > e o teste de arquitetura, quando chegar, vai reprovar tudo de uma vez. Ver D1.
9. **T2 — Migrações.** `api/src/main/resources/db/migration/V1__esquema_inicial.sql`:
   `restaurante(id uuid pk, nome text not null)`,
   `item_cardapio(id uuid pk, restaurante_id uuid not null references restaurante(id) on delete cascade, nome text not null, descricao text, preco_centavos bigint not null check (preco_centavos > 0), disponivel boolean not null default true)`,
   índice em `restaurante_id`. Chamar `Flyway.migrate()` na subida da aplicação.
   **Nunca** `SchemaUtils.create` — o esquema vem só da migração.
   > **Por quê:** o esquema é código versionado, igual ao resto. A migração avança só para
   > frente (nunca se edita uma já aplicada — cria-se a próxima), porque o banco de produção
   > já rodou a antiga e não há como voltar no tempo. E o resultado é repetível: um banco
   > vazio chega sempre ao mesmo esquema, e rodar de novo não muda nada. `SchemaUtils.create`
   > e `ddl-auto` quebram isso, porque derivam o esquema do código — aí você nunca sabe qual
   > versão está lá, nem como chegou.
10. **T3 — `docker compose` de verdade.** O compose já sobe o Postgres; falta a API **ler**
    `DATABASE_URL`/`DATABASE_USER`/`DATABASE_PASSWORD` do ambiente (com padrão de
    desenvolvimento) e conectar. Documentar no README e criar `mise run up`.
    > **Por quê a URL vem do ambiente:** configuração não pode morar dentro do artefato. O
    > mesmo JAR precisa rodar contra o Postgres do seu Docker, o do CI e o de produção — muda
    > o ambiente, não o código. É também o que impede credencial de ir parar no Git. E o
    > `docker compose` existe para que a máquina de cada integrante e a do CI sejam o mesmo
    > ambiente: "na minha máquina funciona" é um sintoma de ambiente divergente.
11. **T4 — Repositórios Exposed.** Implementar as portas de `aplicacao`; conversão
    linha↔domínio num lugar só; `suspendTransaction` em toda operação; **manter a
    implementação em memória** para os testes sem banco.
    > **Por quê manter as duas implementações:** é a inversão de dependência de D1 rendendo
    > juros. Como o caso de uso depende da *interface*, trocar Exposed por uma lista em
    > memória é invisível para ele — e é isso que torna o teste de caso de uso rápido
    > (milissegundos, sem Docker). A implementação em memória não é código jogado fora: é a
    > costura que deixa a pirâmide de testes existir.
12. **T5 — CRUD completo.** Listar / buscar / criar / atualizar / remover para as duas
    entidades; `/restaurantes/{id}/itens` aninhada; paginação `?pagina=&tamanho=` com
    teto (ex.: 100) aplicada no SQL (`limit`/`offset`, **não** em memória); filtro por nome;
    `201` + `Location`, `204`, `404`, `409`.
    > **Por quê paginar no SQL:** empurre o trabalho para onde os dados estão. Carregar a
    > tabela inteira para descartar 99% dela na JVM gasta rede, memória e tempo — e funciona
    > bem com 12 restaurantes de teste, exatamente até não funcionar mais. Resposta sem teto
    > é uma falha esperando escala; o `tamanho` máximo é o que impede um cliente de pedir
    > tudo.
    > **Por quê os status importam:** verbo e código são contrato, não decoração. `201` +
    > `Location` diz "criei, está aqui"; `204` diz "fiz, não há o que devolver"; `404`
    > distingue "não existe" de "deu erro"; `409` marca conflito com o estado atual. Um
    > cliente escrito contra esse contrato funciona sem ler a sua documentação.

#### Trilha Mobile [M]

13. **T6 — Tema claro e escuro.** `DeliveryTheme` com esquema de cor Material 3 nos dois
    modos, `Surface`/`Scaffold` na raiz, zero cor fixa à mão. (Hoje é `MaterialTheme {}` cru.)
    > **Por quê:** cor por papel semântico (`colorScheme.surface`, `onSurface`) em vez de
    > valor fixo. O componente não decide a cor — ele declara o papel, e o tema resolve. É
    > o que faz o mesmo código servir aos dois modos sem ramificação. Ver D3.
14. **T2 — Telas do MVP + componentes.** Uma tela por história P1 em `commonMain`.
    Extrair `RestauranteCard`, `ItemLinha`, `CampoTexto`. Estado elevado acima da navegação
    (dados em memória nesta sprint — `ViewModel` é Sprint 2). `@Preview` de cada tela,
    **incluindo o estado vazio**.
    > **Por quê estado elevado:** a tela recebe o que mostra e devolve eventos; ela não guarda
    > a verdade. Fluxo de dados num sentido só — estado desce, evento sobe. Isso elimina a
    > classe de bug em que duas telas têm cópias divergentes do mesmo dado, e é o que torna a
    > tela testável: você a desenha com qualquer estado que quiser, sem simular a jornada até
    > ele.
    > **Por quê o preview do estado vazio:** lista sem itens é o primeiro estado que todo
    > usuário novo vê, e o que ninguém lembra de desenhar. O preview é o ciclo de feedback
    > mais barato que existe — vê o resultado sem compilar o app inteiro nem navegar até lá.
15. **T3 — Validação do formulário.** Validade derivada por função **pura** (sem `remember`
    extra) — essa função vira teste barato. Botão desabilitado + mensagem de erro.
    Teclado certo por campo.
    > **Por quê derivar em vez de guardar:** se `formularioValido` for um estado próprio, ele
    > pode ficar dessincronizado dos campos — alguém esquece de atualizá-lo num caminho e o
    > botão libera entrada inválida. Estado derivado não tem como divergir da sua origem: ele
    > é recalculado. E como a função é pura, ela se testa com `kotlin.test` em microssegundos,
    > sem subir interface nenhuma.

---

### Dias 4–5 (25–26/09): navegação, contratos e o que aparece no vídeo

#### Web II [W]

16. **T7 — Validação e *problem details*.** Validar todo corpo e parâmetro; tratar num lugar
    só (`StatusPages`, já declarado no `build.gradle.kts`); responder
    `application/problem+json` com `type`, `title`, `status`, `detail`.
    **Testar que violação de constraint do banco não vaza como `500`.**
    > **Por quê falhar na borda:** quanto mais fundo a entrada inválida entra, mais caro é o
    > estrago e mais difícil é dizer de onde veio. Valide uma vez, na porta. E a distinção de
    > status não é detalhe: `4xx` significa "você mandou errado", `5xx` significa "eu tenho um
    > bug". Devolver `500` para entrada ruim é o servidor assumindo culpa que não é dele — e
    > mascara os `500` de verdade, que você precisa enxergar.
    > **Por quê problem details:** erro também é parte da API, não um acidente. Um formato
    > único e padronizado (RFC 9457) significa que o cliente trata todos os erros com o mesmo
    > código, em vez de adivinhar o que veio em cada rota. Tratar num lugar só (`StatusPages`)
    > é o que garante que nenhuma rota nova esqueça de fazer isso.
17. **T8 — OpenAPI.** `describe` do Ktor (gerador nativo) + Swagger UI. Documentar todas as
    rotas, tipos certos nos parâmetros e **as respostas de erro**. Endereço no README.
    > **Por quê gerar do código:** documentação escrita à mão diverge — sempre. Não por
    > desleixo, mas porque nada obriga a atualizá-la quando a rota muda. Gerada do código, ela
    > é uma projeção do que existe de fato; a rota e a documentação não têm como discordar.
18. **T6 (segunda metade) — Teste de arquitetura.** ArchUnit em `api/src/test`, com a regra
    de D1. **Quebre de propósito uma vez** (importe `io.ktor` num caso de uso), grave a tela
    do teste vermelho para o vídeo, e reverta.
    > **Por quê quebrar de propósito:** um teste que nunca reprovou nada não prova nada — pode
    > estar apontando para o pacote errado ou com a regra invertida, e passaria igual. Ver o
    > vermelho é como se verifica o verificador. **Faça isso em `api/aplicacao/`, não no
    > `shared/`** — a explicação completa está em D1, e a mensagem esperada do ArchUnit também.

#### Mobile [M]

19. **T4 — Navegação tipada.** Adicionar `navigation-compose` **e o plugin
    `kotlinSerialization` ao `app/build.gradle.kts`** (hoje só o `api` aplica esse plugin —
    sem ele as rotas `@Serializable` não compilam). Cada destino é um tipo `@Serializable`
    com os argumentos como propriedades; as telas recebem lambdas (`onAbrir`, `onVoltar`),
    nunca o `NavController`; passe **ids**, não objetos.
    **Armadilha:** `RestaurantId` embrulha um `kotlin.uuid.Uuid`, que **não tem `NavType`**
    e não pode ser argumento de rota tipada nem vir de um deep link. Declare o argumento
    como `String` (o texto do UUID) e converta para `RestaurantId` na borda da tela.
    > **Por quê rota tipada:** rota em string (`"detalhe/$id"`) só quebra em runtime, e quebra
    > longe — erro de digitação vira crash na mão do usuário. Como tipo `@Serializable`, o
    > destino errado ou o argumento faltando viram **erro de compilação**. É empurrar a
    > detecção do erro para o momento mais barato possível.
    > **Por quê a tela não recebe o `NavController`:** recebendo lambdas (`onAbrir`,
    > `onVoltar`), a tela não sabe para onde vai — só avisa que algo aconteceu. Quem decide o
    > destino é o `NavHost`, num lugar só. A tela fica reutilizável e testável sem grafo de
    > navegação nenhum. É a mesma inversão de dependência de D1, aplicada à interface.
20. **T5 — Deep link.** `navDeepLink<Rota>(basePath = "delivery://restaurantes")` no destino
    + `intent-filter` no `AndroidManifest.xml` (hoje só tem MAIN/LAUNCHER). Testar com
    `adb shell am start -a android.intent.action.VIEW -d "delivery://restaurantes/<id>"`,
    conferir o Voltar, e registrar o comando no README.
    > **Por quê:** o deep link torna o app um destino endereçável de fora — uma notificação,
    > um link numa página, uma mensagem levam direto ao item, sem o usuário navegar de novo
    > até lá. O detalhe que costuma quebrar é a **pilha de retorno**: quem entrou pelo meio
    > não tem histórico, e o Voltar precisa mesmo assim levar a um lugar que faça sentido, não
    > fechar o app. Por isso a T5 manda conferir o Voltar, e não só a abertura.
21. **T7 — Duas larguras e sem overflow.** `currentWindowAdaptiveInfo().windowSizeClass`; lista+detalhe
    lado a lado a partir de 600 dp. **Separe a decisão de layout do conteúdo**
    (`Conteudo(largo: Boolean)`) — sem isso os dois modos não são testáveis.
    A largura decide, não a orientação nem "é tablet".
    **Metade do critério é o overflow**, cobrado explicitamente na rubrica: texto longo quebra
    ou encurta, listas rolam, nada cortado na borda, e `safeDrawingPadding()` (ou o `Scaffold`)
    afastando o conteúdo das barras do sistema. Confira celular em pé, deitado e desktop
    redimensionado.
    > **Por quê a largura, e não o dispositivo:** "é tablet?" é uma heurística que erra nos
    > casos que existem de verdade — celular deitado, janela de desktop redimensionada pela
    > metade, app em tela dividida. A largura disponível é o fato; o tipo de aparelho é um
    > palpite sobre ele. Decida pelo fato.
    > **Por quê separar a decisão do conteúdo:** `Conteudo(largo: Boolean)` transforma "qual
    > layout" numa entrada comum da função. Sem isso, testar o modo largo exigiria simular uma
    > janela grande; com isso, é só passar `true`.

---

### Dia 6 (27/09): testes, acessibilidade e CI verde

22. **[W] T9 — Testes.** Unitários do domínio e dos casos de uso (sem banco, repositório em
    memória); testes de rota com `ktor-server-test-host`; integração com Testcontainers
    cobrindo migração, CRUD, relacionamento e paginação. **Sem URL de banco fixa nos testes.**
    > **Por quê essa divisão (pirâmide de testes):** muitos testes rápidos embaixo, poucos e
    > lentos em cima. Os unitários usam o repositório em memória e rodam em milissegundos, então
    > você os roda a cada salvamento — é onde a maioria dos bugs é pega. Os de integração são
    > caros (sobem um container), então cobrem só o que só eles conseguem cobrir: a migração
    > aplicada de verdade, o SQL gerado, a chave estrangeira, o `LIMIT/OFFSET`. Inverter a
    > pirâmide dá uma suíte lenta demais para rodar com frequência — e suíte que não roda não
    > pega bug.
    > **Por quê Testcontainers e não um banco de mentira:** um mock testa o que você *acha*
    > que o Postgres faz. O container testa o Postgres. A diferença aparece exatamente nas
    > coisas que a rubrica pede — constraint, cascade, tipo de coluna — que nenhum mock
    > reproduz.
    > **Por quê nenhuma URL fixa:** o teste precisa ser hermético, isto é, trazer sua própria
    > dependência em vez de exigir que alguém tenha subido um banco antes. É isso que faz a
    > mesma suíte rodar igual no seu Docker e no runner do Actions, sem trocar configuração —
    > exatamente o que a rubrica cobra.
23. **[M] T8 — Acessibilidade.** `contentDescription` em todo interativo sem texto;
    `Modifier.toggleable` com `role` nas linhas que alternam algo; `semantics { heading() }`
    nos títulos; alvos ≥ 48 dp (cuidado com `clickable` em ícone feito à mão); contraste pelo
    Accessibility Scanner; percorrer com TalkBack e **registrar no README o que foi ajustado**.
    > **Por quê:** além dos pixels, o Compose mantém uma **árvore de semântica** — o que cada
    > elemento é, o que faz, se é um título, se está marcado. Ela é a interface que o leitor de
    > tela consome. Um ícone sem `contentDescription` existe visualmente e é invisível ali.
    > Uma linha quebrada em "caixinha + texto solto" vira dois elementos que o usuário precisa
    > alcançar separados, em vez de um alvo só — daí o `toggleable` com `role`.
    > E os 48 dp não são só para deficiência: são para qualquer pessoa em pé num ônibus.
    > **O detalhe que rende:** é a mesma árvore que os testes de interface do item 24 usam.
    > Acessibilidade bem feita é testabilidade de graça — e vice-versa.
24. **[M] T9 — ≥ 5 testes de interface** em `commonTest`, rodando no desktop: um por tela
    principal, formulário inválido, formulário válido, e um de navegação (abre, age, volta,
    confere). Busque elementos pelo texto/descrição e aja como usuário (`performClick`,
    `performTextInput`) — assim o teste também valida a acessibilidade.
    > **Por quê buscar por texto visível:** um teste que acha o elemento por um identificador
    > interno passa mesmo que o botão esteja sem rótulo e ilegível para o usuário. Buscando
    > pelo texto ou pela descrição e agindo como uma pessoa (`performClick`), o teste verifica
    > comportamento e acessibilidade no mesmo gesto — e falha quando a tela deixa de ser
    > usável, não só quando deixa de compilar.
25. **[WM] Fechar o CI verde em `master`.** Último dia útil para descobrir problema de CI.
    > **Por quê esta data:** o que é avaliado é o estado de `master`, pelo hash do commit. Um
    > CI vermelho no dia 02/10 não é um detalhe a explicar na apresentação — é perda direta
    > nos 30% de atividade no repositório, **nas duas disciplinas ao mesmo tempo**.
26. **[WM] Fechar os cartões** ligando cada um ao PR que o resolveu.

---

### Dia 7 (28/09): apresentação da Coorte B

27. **[WM] Ensaiar e apresentar ao vivo.** Nada de gravação nesta noite — os vídeos
    vencem em 02/10 e cabem no buffer.

**Roteiro Web II (5 min):** 30 s o que entregou · 1 min 30 entidades, relacionamento e CRUD
pela Swagger UI com paginação e filtro · 1 min migrações e `docker compose up` · 1 min
camadas e o teste de arquitetura falhando e passando · 1 min testes e CI verde + o que fica
para a Sprint 2.

**Roteiro Mobile (5 min):** 30 s uma frase por tela · 1 min 30 telas e navegação, incluindo o
Voltar · 1 min deep link funcionando e as duas larguras · 1 min TalkBack percorrendo uma tela
e o que foi ajustado · 1 min testes rodando e CI verde + o que fica para a Sprint 2.

---

### Dias 8–11 (29/09–02/10): buffer

**Gravar os dois vídeos de 5 minutos** (a apresentação de 28/09 já serviu de ensaio) e
linkar ambos no README. Fora isso, só polimento e o que ficou vermelho. README final (dois vídeos, endereço da Swagger UI,
comando do deep link, log de acessibilidade, como rodar a suíte), `docs/uso-de-ia.md`,
merge dos últimos PRs. **A entrega é o estado de `master` em 02/10 23:59.**

---

## Checklist de armadilhas específicas deste repositório

- [ ] O módulo é `:app`, **não** `:composeApp` — corrija todo comando copiado do enunciado.
- [ ] `app` não tem `commonTest`/`desktopTest` configurados. Começa do zero.
- [ ] `app` não aplica `kotlinSerialization` — sem ele, rota tipada não compila.
- [ ] `api` não tem source set de teste nem `ktor-server-test-host`. Começa do zero.
- [ ] `mise run test` hoje não roda a suíte. As duas rubricas cobram isso.
- [ ] `libgl1 libegl1 libfontconfig1` no CI **antes** do `desktopTest`.
- [ ] ktlint e detekt pegam todo código novo (aplicados em `subprojects {}`). Rode
      `./gradlew ktlintFormat` antes de abrir PR, ou o CI fica vermelho por formatação.
- [ ] Nenhuma credencial no código — só variável de ambiente.
- [ ] Nada de `ddl-auto`/`SchemaUtils.create`: esquema só por migração.
- [ ] Zero push direto em `master`.

## Riscos de versão (confirmar antes de fixar)

| Item | Situação |
|---|---|
| `material3-window-size-class:1.7.3` e `material3.adaptive:adaptive:1.0.0-alpha03` | **Confirmado** como o par correto para Compose MP 1.7.3 |
| `org.jetbrains.androidx.navigation:navigation-compose` | Versão a confirmar — é a variante JetBrains, numerada independente da androidx |
| Exposed 0.57 | A API de `limit`/`offset` mudou entre versões próximas; confirme a assinatura antes de escrever a paginação |
| Flyway / Testcontainers / ArchUnit | Versões a confirmar; importe o **BOM** do Testcontainers |

Se alguma dessas travar mais de uma hora, considere subir o Compose MP e o Kotlin — mas só
no Dia 0, nunca depois do Dia 3.

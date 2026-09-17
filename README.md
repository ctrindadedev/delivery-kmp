# Delivery KMP

Plataforma de delivery desenvolvida em conjunto nas disciplinas DIM0547,
Desenvolvimento de Sistemas Web II, e DIM0524, Sistemas para Dispositivos Móveis,
da UFRN.

O projeto reúne uma API em Kotlin com Ktor, um serviço de catálogo em Go e um
aplicativo Compose Multiplatform para Android e desktop. O domínio de restaurantes
e cardápios fica em um módulo Kotlin Multiplatform compartilhado entre a API e o
aplicativo.

## Equipe

| Integrante | Matrícula | Papel |
|---|---|---|
| Iury Fredson Germano Miranda | 20240050336 | Desenvolvedor Full Stack |
| Caio de Medeiros Trindade | 20230045477 | Desenvolvedor Full Stack |

**Coorte de apresentação:** Coorte B, com apresentações online pelo Google Meet.

## Planejamento

O backlog está disponível no
[GitHub Projects](https://github.com/users/IuryFredson/projects/2). Cada item
contém história de usuário, critérios de aceitação, prioridade, estimativa e
sprint prevista.

A proposta completa, as decisões técnicas e o escopo do MVP estão em
[`docs/proposta.md`](docs/proposta.md).

## Vídeo da Sprint 0

[Link para o vídeo](https://youtu.be/2rv-AbaRumQ)

## Estado da Sprint 0

Nesta etapa, o repositório contém:

- domínio compartilhado de restaurantes, itens de cardápio, identificadores e
  valores monetários;
- esqueleto da API Ktor com endpoint de verificação em `/health`;
- esqueleto do serviço Go e contrato inicial em Protocol Buffers;
- aplicativo Android e desktop com lista de restaurantes, busca e estado vazio;
- dados locais de exemplo, sem consumo da API nesta sprint;
- verificações de estilo e análise estática com ktlint e detekt;
- pipeline de integração contínua para Kotlin, Android, desktop e Go.

## Tecnologias

- Kotlin 2.0.21
- Kotlin Multiplatform
- Ktor 3.0.3
- Compose Multiplatform 1.7.3
- Android Gradle Plugin 8.7.3
- Java 21
- Go 1.23
- gRPC e Protocol Buffers
- PostgreSQL e Exposed, previstos para as próximas sprints

## Estrutura do repositório

```text
delivery-kmp/
├── app/                 Aplicativo Compose para Android e desktop
├── api/                 Serviço HTTP em Kotlin com Ktor
├── shared/              Entidades e regras em Kotlin Multiplatform
├── services/catalogo/   Serviço de catálogo em Go
├── protos/              Contratos Protocol Buffers
├── docs/                Proposta e decisões de arquitetura
├── config/              Configuração das verificações de qualidade
├── .github/workflows/   Pipeline de integração contínua
├── docker-compose.yml   PostgreSQL para desenvolvimento local
├── mise.toml            Versões das ferramentas e tarefas locais
└── settings.gradle.kts  Módulos do projeto Gradle
```

## Pré-requisitos

- JDK 21
- Git
- Android Studio com Android SDK 34 para executar o aplicativo Android
- Go 1.23 para compilar o serviço de catálogo
- Docker para iniciar o PostgreSQL local

No Windows, o Java 21 incluído no Android Studio pode ser ativado no PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java --version
```

## Executar o aplicativo no desktop

Na raiz do repositório:

```powershell
.\gradlew.bat :app:run
```

No Linux ou macOS:

```sh
./gradlew :app:run
```

## Executar o aplicativo no Android

1. Abra a raiz do repositório no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Inicie um emulador com API 24 ou superior.
4. Selecione a configuração do módulo `app`.
5. Execute o aplicativo.

Com um emulador ou dispositivo conectado, também é possível instalar o build de
depuração pelo PowerShell:

```powershell
.\gradlew.bat :app:installDebug
```

## Executar a API

```powershell
.\gradlew.bat :api:run
```

Com a API ativa, acesse `http://localhost:8080/health`. A resposta esperada é:

```json
{"status":"ok"}
```

## Iniciar o PostgreSQL

```powershell
docker compose up -d postgres
```

O banco local usa, por padrão, o nome, o usuário e a senha `delivery`, na porta
`5432`. Esses valores podem ser alterados pelas variáveis `POSTGRES_DB`,
`POSTGRES_USER`, `POSTGRES_PASSWORD` e `POSTGRES_PORT`.

## Executar o serviço Go

```powershell
cd services\catalogo
go run .\cmd\server
```

Na Sprint 0, esse comando inicia apenas o esqueleto do serviço. A implementação
gRPC será feita nas próximas sprints.

## Build e verificações

No Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat :app:assembleDebug :app:packageUberJarForCurrentOS
.\gradlew.bat ktlintCheck
.\gradlew.bat detekt :shared:detektMetadataMain
cd services\catalogo
go build ./...
go test ./...
```

Com o mise instalado, as tarefas gerais são:

```sh
mise run build
mise run test
```

O pipeline publica o APK Android e o JAR executável de desktop na seção
**Artifacts** de cada execução do GitHub Actions.

## Referência

Projeto de referência da disciplina:
[github.com/fmarquesfilho/musi](https://github.com/fmarquesfilho/musi).

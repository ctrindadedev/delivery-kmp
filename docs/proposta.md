# Proposta — Delivery Platform (KMP)

Este documento cobre as duas disciplinas que compartilham este monorepo:

- **DIM0547 — Desenvolvimento de Sistemas Web II**
- **DIM0524 — Sistemas para Dispositivos Móveis** 

É o mesmo produto, com o domínio compartilhado via `shared/` (Kotlin Multiplatform,
ver [ADR-0001](decisoes/0001-estrutura-monorepo.md)). As seções de Visão e MVP valem
para as duas disciplinas; a Justificativa Kotlin × Go é específica do Web II.

> **Status:** seções Web II e Mobile fechadas para a Sprint 0.

---

## Visão do produto

| | |
|---|---|
| Para | usuários que querem pedir comida online |
| Que | não têm como descobrir restaurantes e cardápios próximos facilmente |
| O **DeliveryApp** | é uma plataforma de delivery de comida |
| Que | conecta clientes a restaurantes com catálogo em tempo real |
| Diferente de | sistemas manuais ou apps sem filtragem por proximidade |
| Nosso produto | serve o cardápio em cache com baixa latência via Go + gRPC |

---

## MVP

| No MVP | Fora do MVP |
|---|---|
| Cadastro e consulta de restaurantes | Rastreamento em tempo real |
| Cardápio com preços e disponibilidade | Pagamento integrado |
| Cache de catálogo em Go via gRPC | Avaliações e reviews |
| Busca por restaurante | Módulo de entrega |

---

## Backlog (por sprint)

| Sprint | Web II | Mobile |
|---|---|---|
| Sprint 0 | Monorepo, `docs/proposta.md`, CI verde (Kotlin + Go), `mise run build`/`test` | Proposta, ambiente KMP, primeira tela Compose |
| Sprint 1 | CRUD de `Restaurant` em Ktor, OpenAPI documentado | Navegação entre telas, tema Material 3 |
| Sprint 2 | Microsserviço `catalogo` em Go + gRPC, contrato `.proto` verificado (`buf`) | Estado gerenciado (ViewModel), consumindo a API real |
| Sprint 3 | PostgreSQL no Neon, cache HTTP (ETag/304), deploy no Render | Cache offline, câmera, GPS, publicação na Play Store |

---

## Justificativa Kotlin × Go

*Ver também [ADR-0001](decisoes/0001-estrutura-monorepo.md)*

O sistema é dividido em dois serviços, cada um na linguagem que melhor serve o
**perfil de carga** do trabalho que executa — não por preferência pessoal:

| Vai para o serviço Kotlin (Ktor) | Vai para o microsserviço Go |
|---|---|
| Entidades de domínio e regras de negócio | Cache do catálogo de cardápios (leitura massiva) |
| Persistência e migrações (Exposed + PostgreSQL) | Servir `GetMenu` com baixa latência via gRPC |
| Orquestração dos casos de uso | Invalidação de cache quando o cardápio muda |

**Por que essa divisão especificamente:**

O endpoint `GET /restaurants/{id}/menu` é **read-heavy**: todo usuário que abre o
app consulta o cardápio, mas o restaurante raramente o atualiza. Esse desequilíbrio
entre leitura e escrita justifica isolar essa rota num serviço dedicado a servir
dados em cache, com baixa latência — sem competir por recursos com o serviço que
processa transações (criação de restaurante, cadastro de item, etc.).

Go resolve isso com goroutines e overhead de memória baixo, adequado para um
serviço de cache que só precisa responder rápido — não para orquestrar regras de
negócio, que continuam em Kotlin/Ktor, coerente com o domínio compartilhado em
`shared/` (importado também pelo Mobile).

**Por que Kotlin em vez de Java no backend (ao invés de Quarkus):**

1. **Coerência com o Mobile:** `shared/` é KMP e é importado diretamente pela API.
   Um backend Java também poderia consumir o artefato JVM, mas Kotlin evita uma
   camada de interoperabilidade e mantém os modelos e as regras no mesmo idioma.
2. **Fundamentos são os mesmos:** coroutines vs. threads, Ktor vs. Quarkus, Exposed
   vs. Hibernate são ferramentas diferentes sobre os mesmos fundamentos (HTTP, TCP,
   ACID). A curva de aprendizado é sobre o "porquê", não sobre sintaxe nova.

O contrato entre os dois serviços é `protos/catalogo.proto` (rascunho na Sprint 0,
a ser implementado na Sprint 2), com `buf lint`/`buf breaking` garantindo que mudanças no
contrato não quebrem quem já o consome — o equivalente, na fronteira entre
serviços, do que testes de arquitetura fazem dentro de um módulo só.

---

## Plataforma-alvo e backend (Mobile)

- **Plataformas-alvo:** Android (primário) e desktop (secundário), com interface
  compartilhada em Compose Multiplatform. iOS fica fora da Sprint 0.
- **Backend:** a própria API de Web II

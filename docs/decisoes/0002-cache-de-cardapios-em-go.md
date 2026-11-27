# ADR-0002 — Separar o cache de cardápios num microsserviço Go

**Estado:** Aceita  
**Data:** 2026-11-23  
**Issue:** #51 (WEB-S2-T2), ligada à #13

---

## Contexto

A Sprint 2 de Web II pede um microsserviço em Go que tenha motivo para existir
fora da API Ktor e que converse com ela por gRPC. Os candidatos que o domínio de
delivery oferece hoje:

| Candidato | Tipo de trabalho | Por que não (ou sim) |
|---|---|---|
| Cálculo de frete / tempo de entrega | E/S com terceiros | Não existe endereço nem entregador no domínio ainda |
| Notificação de pedido | Agendamento / fila | Não existe pedido no domínio ainda |
| Geração de relatório | Processamento | Sem volume de dados que justifique |
| **Cache de cardápios** | Concorrência + memória | Rota mais lida do sistema, dado muda pouco |

Abrir um restaurante no app dispara `GET /restaurantes/{id}/itens`, que hoje vai
ao Postgres a cada chamada. O cardápio é lido muito mais do que é alterado: o
dono muda preço ou disponibilidade algumas vezes por dia, e os clientes abrem o
restaurante o dia inteiro. É uma leitura repetida e cacheável.

Há uma restrição da disciplina: **o serviço Go não pode dividir tabelas com a API.**
Isso decide quem busca o dado quando o cache não tem.

---

## Decisão

**O serviço `catalogo` (Go) mantém em memória os cardápios mais lidos, com
expiração e invalidação, para tirar a leitura repetida do Postgres.**

O padrão é **cache-aside, com a API como fonte da verdade**:

```
GET /restaurantes/{id}/itens
  API → catalogo.ObterCardapio(id)                       [deadline curto]
     ├─ acerto  → responde com o cache
     ├─ falta   → API lê do Postgres → catalogo.GuardarCardapio(id, itens) → responde
     └─ Go fora / prazo estourado → API lê do Postgres e responde normalmente

POST/PUT/DELETE de item, DELETE de restaurante
  API grava no Postgres → catalogo.InvalidarCardapio(id)
```

- **O Go não tem banco.** Ele recebe o cardápio pronto da API e não conhece
  `restaurante` nem `item_cardapio`.
- **A API não depende do Go.** Toda chamada tem prazo, e qualquer falha
  (`UNAVAILABLE`, `DEADLINE_EXCEEDED`) faz a API seguir pelo banco. O cache é
  uma otimização, nunca um caminho obrigatório.
- **Expiração e tamanho limitados.** Há TTL por entrada (é a rede de segurança se
  uma invalidação se perder) e uma capacidade máxima com despejo LRU, de modo que
  só os cardápios mais acessados ficam em memória.
- **A unidade de cache é o cardápio inteiro de um restaurante**, não uma página.
  A API pagina em memória quando acerta o cache.
- **Contrato** em `protos/`, pacote `delivery.catalogo.v1`, com três RPCs:
  `ObterCardapio`, `GuardarCardapio` e `InvalidarCardapio`.

### Por que fora da API

Um cache dentro do processo da API funcionaria enquanto houver uma única
instância. Com duas réplicas atrás de um balanceador, cada uma teria seu próprio
cache: o `PUT` que chega na réplica A invalida só o cache de A, e a réplica B
continua servindo o preço antigo até o TTL vencer. Um serviço único de cache
centraliza a invalidação. Além disso, o modelo de concorrência do Go (goroutine
por requisição, `sync.Mutex` num mapa em memória) atende muitas leituras
simultâneas com pouco consumo de memória e partida rápida.

---

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Cache dentro da API (Caffeine ou mapa local) | Mais simples, mas cada réplica tem o seu e a invalidação não se propaga; também não cumpre o objetivo da sprint (Go + gRPC) |
| Redis como cache compartilhado | Resolve a propagação, mas é mais uma infraestrutura para operar, e o "serviço" passaria a ser só um cliente de Redis |
| Read-through no Go (o Go busca o cardápio na falta) | O Go precisaria ler as tabelas da API (proibido: tabelas compartilhadas) ou chamar a API por HTTP (dependência circular: API → Go → API) |
| Sem cache | O mais simples, mas toda abertura de restaurante vai ao banco; não há trabalho que justifique um serviço Go |

---

## Consequências

- ✅ Responsabilidade única e pequena: o Go guarda, entrega e esquece cardápios
- ✅ Nenhuma tabela compartilhada: o Go não tem banco, e o esquema continua só da API
- ✅ A API continua de pé sem o Go, e o deploy pode publicar só a API
- ✅ Invalidação centralizada, que funciona com várias réplicas da API
- ⚠️ Mais um processo para buildar, empacotar, monitorar e subir no compose
- ⚠️ Consistência eventual: se a chamada `InvalidarCardapio` falhar depois de uma
  escrita, o cardápio antigo pode ser servido até o TTL vencer. O TTL precisa ser
  curto (minutos), e essa janela é aceita
- ⚠️ Na falta, a leitura custa uma ida e volta gRPC a mais do que ir direto ao banco
- ⚠️ O cache é volátil: reiniciar o Go esvazia tudo, e as primeiras leituras
  depois disso voltam ao banco
- ⚠️ A API passa a ler o cardápio inteiro na falta para guardar no cache, em vez
  de só a página pedida. Para cardápios de dezenas de itens isso é irrelevante

**Critério de revisão:** se a API passar a rodar em uma única instância por tempo
indeterminado e o custo operacional do Go pesar, reavaliar o cache em processo
(Caffeine) na própria API, mantendo a mesma porta `CacheDeCardapios`.

---

## Onde está no código

Pontos de integração que já existem na API:

- `api/src/main/kotlin/br/caio/delivery/aplicacao/ServicoDeItens.kt:26`: `listar`, a leitura que passa a consultar o cache
- `api/src/main/kotlin/br/caio/delivery/aplicacao/ServicoDeItens.kt:39`, `:49`, `:59`: `criar`, `atualizar` e `remover`, que passam a invalidar
- `api/src/main/kotlin/br/caio/delivery/aplicacao/ServicoDeRestaurantes.kt:32`: `remover` restaurante, que passa a invalidar
- `api/src/main/resources/db/migration/V1__esquema_inicial.sql:1`, `:6`: as tabelas `restaurante` e `item_cardapio`, que só a API acessa
- `api/src/test/kotlin/br/caio/delivery/ArquiteturaTest.kt:16`: regra que vai passar a proibir `io.grpc` no núcleo

O que ainda será criado:

- `protos/delivery/catalogo/v1/catalogo.proto`: contrato (#52)
- `services/catalogo/internal/cache/`: domínio do cache (#13)
- `services/catalogo/internal/grpcapi/` e `cmd/server/`: servidor gRPC (#53)
- `api/.../aplicacao/CacheDeCardapios` (porta) e `api/.../infra/grpc/` (adaptador): integração (#54)

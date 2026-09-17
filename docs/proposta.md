# Proposta do Delivery KMP

Este documento apresenta o produto desenvolvido em conjunto nas disciplinas
DIM0547, Desenvolvimento de Sistemas Web II, e DIM0524, Sistemas para
Dispositivos Móveis.

## 1. Visão do produto

| Campo | Definição |
|---|---|
| Para | Pessoas que pedem comida pela internet e querem consultar opções antes de escolher |
| Que | Precisam encontrar restaurantes e cardápios atualizados com rapidez |
| O Delivery KMP | É uma plataforma de consulta de restaurantes e cardápios |
| Que | Reúne a descoberta de restaurantes, a busca por nome e os itens disponíveis em cada cardápio |
| Diferente de | Catálogos manuais, desatualizados ou separados do sistema do restaurante |
| Nosso produto | Compartilha o domínio entre aplicativo e servidor e prepara o catálogo para consultas com baixa latência |

O produto será construído gradualmente nas duas disciplinas. Web II concentra o
backend, os contratos e a infraestrutura. Sistemas para Dispositivos Móveis
concentra a experiência do usuário em Android e desktop. As duas partes usam o
mesmo domínio em Kotlin Multiplatform.

## 2. MVP e hipótese de valor

| Dentro do MVP | Fora do MVP inicial |
|---|---|
| Cadastro e consulta de restaurantes | Pagamento integrado |
| Busca de restaurantes por nome | Rastreamento de entrega em tempo real |
| Cadastro e consulta de itens do cardápio | Avaliações e comentários |
| Preço e disponibilidade dos itens | Módulo de entregadores |
| Aplicativo Android e desktop | Aplicativo para iOS |
| Integração do aplicativo com a API do grupo | Programa de fidelidade e promoções |

**Hipótese de valor:** acreditamos que pessoas que pedem comida pela internet
usarão o aplicativo para descobrir restaurantes e consultar cardápios porque
terão acesso rápido e atualizado às opções e aos preços disponíveis.

O MVP valida a proposta quando o usuário consegue encontrar um restaurante,
consultar seu cardápio e identificar os itens disponíveis. A integração real
entre aplicativo e API será feita nas próximas sprints. Na Sprint 0, a tela usa
dados locais para permitir a validação da interface sem depender do backend.

## 3. Backlog inicial

O backlog está no
[GitHub Projects](https://github.com/users/IuryFredson/projects/2). Ele contém
histórias de usuário, critérios de aceitação, prioridade, estimativa e sprint
prevista.

As histórias P1 concentram a descoberta de restaurantes e o gerenciamento do
cardápio. O consumo da API pelo aplicativo está previsto para a Sprint 2, depois
da implementação dos endpoints necessários em Web II.

## 4. Entidades principais e relações

| Entidade ou objeto de valor | Responsabilidade |
|---|---|
| `Restaurant` | Representa o restaurante e mantém seu cardápio |
| `MenuItem` | Representa um item com nome, descrição, preço e disponibilidade |
| `Dinheiro` | Armazena valores monetários em centavos, sem ponto flutuante |
| `RestaurantId` | Identifica um restaurante de forma única |
| `MenuItemId` | Identifica um item do cardápio de forma única |

Um `Restaurant` possui nenhum ou vários `MenuItem`. Cada item pertence ao
cardápio de um restaurante e possui um preço representado por `Dinheiro`. O
restaurante funciona como raiz desse conjunto e concentra as regras de inclusão,
remoção e alteração da disponibilidade dos itens.

O domínio fica em `shared/src/commonMain`, sem dependência de Compose, Ktor,
banco de dados ou outra tecnologia de infraestrutura. Assim, a API e o
aplicativo usam as mesmas entidades e regras.

## 5. Decisões de plataforma e backend

### Aplicativo

O Android é a plataforma principal porque o produto será usado em mobilidade,
com interação por toque e acesso rápido durante a escolha de uma refeição. O
desktop é a plataforma secundária e permite validar o compartilhamento da
interface, além de oferecer uma forma rápida de executar e demonstrar o produto.

A interface usa Compose Multiplatform e permanece em `commonMain`. Android e
desktop mantêm apenas seus pontos de entrada. O layout limita a largura do
conteúdo em telas maiores para preservar a leitura no desktop, enquanto ocupa a
largura disponível no celular.

O iOS foi considerado, mas ficou fora do MVP porque exigiria ambiente e hardware
específicos para compilação e validação. Ele pode ser incluído futuramente sem
alterar o domínio compartilhado.

### Backend

O aplicativo consumirá a API criada pelo próprio grupo em Web II. Essa escolha
permite controlar as regras de restaurante e cardápio, a atualização dos dados e
a estratégia de cache. Na Sprint 0, dados locais substituem temporariamente a
API para que a interface possa ser desenvolvida e executada de forma
independente.

Uma API externa e um Backend as a Service foram considerados, mas descartados
porque reduziriam o controle sobre as regras do domínio e não permitiriam
praticar a arquitetura poliglota, o contrato gRPC e a estratégia de cache
previstos em Web II.

Para o serviço principal, o grupo escolheu Kotlin com Ktor. Kotlin permite que a
API consuma diretamente o domínio compartilhado no alvo JVM e mantém as regras
na mesma linguagem usada pelo aplicativo. Java com Quarkus também permitiria
consumir o artefato JVM, mas adicionaria uma fronteira de interoperabilidade sem
benefício para este produto.

## 6. Divisão entre o serviço principal e Go

| Kotlin com Ktor | Go com gRPC |
|---|---|
| Casos de uso e regras do domínio | Cache dos cardápios consultados com frequência |
| Cadastro e atualização de restaurantes | Atendimento de `GetMenu` com baixa latência |
| Cadastro e atualização de itens | Invalidação do cache quando o cardápio mudar |
| Persistência e transações | Métricas de acerto e erro do cache nas próximas sprints |

A consulta de cardápio tende a receber mais leituras do que alterações. Por isso,
o serviço principal continua responsável pelas regras e transações, enquanto o
serviço Go fica responsável pelo trabalho concorrente e intensivo em leitura.
Essa divisão evita transformar Go em um segundo backend completo e mantém cada
serviço com uma responsabilidade clara.

Os serviços se comunicarão por gRPC usando o contrato em
`protos/catalogo.proto`. Na Sprint 0, o contrato e os esqueletos estão presentes.
A geração dos stubs e a integração completa entram nas próximas sprints.

## 7. Equipe

| Integrante | Matrícula | Papel |
|---|---|---|
| Iury Fredson Germano Miranda | 20240050336 | Desenvolvedor Full Stack |
| Caio de Medeiros Trindade | A confirmar | Desenvolvedor Full Stack |

Os dois integrantes participam do aplicativo e do backend. Iury também atua na
organização do backlog e na integração do aplicativo. Caio também atua na
estrutura do backend, no serviço Go e na infraestrutura do projeto.

## 8. Coorte e integração entre disciplinas

A equipe escolheu a **Coorte B**, com apresentações online pelo Google Meet.

O mesmo produto e o mesmo monorepo serão usados nas duas disciplinas. O
aplicativo de Sistemas para Dispositivos Móveis consumirá a API construída em Web
II. O módulo `shared/` mantém as entidades e as regras comuns, enquanto `app/`,
`api/` e `services/catalogo/` preservam as responsabilidades específicas de cada
parte.

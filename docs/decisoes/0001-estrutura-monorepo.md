# ADR-0001 — Monorepo com shared/ em Kotlin Multiplatform

**Estado:** Aceita  
**Data:** 2026-08-26  
**Validada com:** Professor Fernando Marques (orientação presencial + referência ao MUSI)

---

## Contexto

O projeto cobre duas disciplinas com tecnologias diferentes:

- **DIM0547 — Web II:** servidor HTTP em Kotlin + Ktor
- **DIM0524 — Mobile:** app em Kotlin Multiplatform + Compose

As duas precisam do mesmo modelo de domínio: `Restaurant`, `MenuItem`,
Value Objects, regras de negócio. A questão é: como garantir que o domínio
seja o mesmo nos dois lados sem duplicação?

No projeto de referência (MUSI), o domínio existe duas vezes: uma em Kotlin
(`api-ktor/`) e uma em Java (`api-quarkus/`). A duplicação é proposital porque
a disciplina é comparativa entre linguagens. No nosso projeto não há essa
fronteira — tudo é Kotlin — então a duplicação não tem justificativa.

---

## Decisão

Um módulo `shared/` em Kotlin Multiplatform com o domínio em `commonMain`.

```
shared/src/commonMain/  → compila para JVM (api/) e Android (app/)
api/                    → Kotlin/JVM, importa shared/ como dependência
app/                    → KMP + Compose, importa shared/ (Sprint 2 do Mobile)
```

**Dinheiro:** `@JvmInline value class Dinheiro(val centavos: Long)`  
— sem `BigDecimal` (não existe em `commonMain`), sem ponto flutuante (impreciso).

**IDs:** `kotlin.uuid.Uuid` do stdlib do Kotlin 2.0+  
— multiplataforma, sem `expect/actual`, sem dependência externa.

**Android target:** desabilitado até o início da Sprint 2 do Mobile.  
— habilitar agora puxaria o Android SDK para o build sem necessidade.

---

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Domínio duplicado (como no MUSI) | No MUSI a duplicação serve à comparação Java × Kotlin. Sem essa fronteira, duplicar é só copia-cola que diverge |
| `BigDecimal` via `ionspin/bignum` | Dependência extra sem ganho real — dinheiro discreto em centavos é suficiente e mais simples |
| `java.util.UUID` com `expect/actual` | `kotlin.uuid.Uuid` resolve sem cerimônia desde Kotlin 2.0 |
| Android target ativo desde o início | Puxaria o Android SDK para o build da Sprint 0, que é exclusivamente servidor |

---

## Consequências

- ✅ Domínio existe uma única vez — regra de negócio muda em um lugar só
- ✅ `commonMain` puro de stdlib — compilador KMP garante sem teste de arquitetura
- ✅ Android target pode ser ligado na Sprint 2 sem refatoração do domínio
- ⚠️ Build de KMP é mais complexo que Kotlin/JVM puro — curva de aprendizado no início
- ⚠️ Erros de configuração KMP podem ter stack traces crípticos

**Critério de revisão (Plano B):** se 4h+ forem gastas em configuração de Gradle
sem progresso funcional, migrar para domínio duplicado com testes de contrato.

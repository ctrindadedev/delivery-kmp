package cache_test

import (
	"context"
	"errors"
	"fmt"
	"sync"
	"testing"
	"time"

	"github.com/ctrindadedev/delivery-kmp/services/catalogo/internal/cache"
)

type relogioFalso struct {
	mu    sync.Mutex
	agora time.Time
}

func (r *relogioFalso) Agora() time.Time {
	r.mu.Lock()
	defer r.mu.Unlock()
	return r.agora
}

func (r *relogioFalso) avancar(d time.Duration) {
	r.mu.Lock()
	defer r.mu.Unlock()
	r.agora = r.agora.Add(d)
}

func novoCache(t *testing.T, cfg cache.Config) (*cache.Cache, *relogioFalso) {
	t.Helper()
	relogio := &relogioFalso{agora: time.Date(2026, 11, 23, 12, 0, 0, 0, time.UTC)}
	cfg.Relogio = relogio
	c, err := cache.Novo(cfg)
	if err != nil {
		t.Fatalf("Novo: %v", err)
	}
	return c, relogio
}

func itens(ids ...string) []cache.Item {
	resultado := make([]cache.Item, 0, len(ids))
	for i, id := range ids {
		resultado = append(resultado, cache.Item{ID: id, Nome: "Item " + id, PrecoCentavos: int64(1000 + i), Disponivel: true})
	}
	return resultado
}

func guardar(t *testing.T, c *cache.Cache, id string, it []cache.Item) {
	t.Helper()
	if _, err := c.Guardar(context.Background(), id, it); err != nil {
		t.Fatalf("Guardar(%s): %v", id, err)
	}
}

func exigirFalta(t *testing.T, c *cache.Cache, id string) {
	t.Helper()
	_, err := c.Obter(context.Background(), id)
	if !errors.Is(err, cache.ErrNaoEncontrado) {
		t.Fatalf("Obter(%s): esperava ErrNaoEncontrado, veio %v", id, err)
	}
}

func exigirAcerto(t *testing.T, c *cache.Cache, id string) cache.Cardapio {
	t.Helper()
	cardapio, err := c.Obter(context.Background(), id)
	if err != nil {
		t.Fatalf("Obter(%s): %v", id, err)
	}
	return cardapio
}

func TestFaltaQuandoNadaFoiGuardado(t *testing.T) {
	c, _ := novoCache(t, cache.Config{})
	exigirFalta(t, c, "r1")
}

func TestAcertoDevolveOQueFoiGuardado(t *testing.T) {
	c, relogio := novoCache(t, cache.Config{})
	guardar(t, c, "r1", itens("a", "b"))

	cardapio := exigirAcerto(t, c, "r1")

	if cardapio.RestauranteID != "r1" || len(cardapio.Itens) != 2 || cardapio.Itens[1].ID != "b" {
		t.Fatalf("cardápio inesperado: %+v", cardapio)
	}
	if !cardapio.GuardadoEm.Equal(relogio.Agora()) {
		t.Fatalf("GuardadoEm = %v, esperava %v", cardapio.GuardadoEm, relogio.Agora())
	}
}

func TestCardapioSemItensEhValido(t *testing.T) {
	c, _ := novoCache(t, cache.Config{})
	guardar(t, c, "r1", nil)

	cardapio := exigirAcerto(t, c, "r1")

	if cardapio.Itens == nil || len(cardapio.Itens) != 0 {
		t.Fatalf("esperava fatia vazia, veio %#v", cardapio.Itens)
	}
}

func TestIdentificadorEhNormalizado(t *testing.T) {
	c, _ := novoCache(t, cache.Config{})
	guardar(t, c, "  r1 ", itens("a"))
	exigirAcerto(t, c, "r1")
}

func TestGuardarDeNovoSubstituiERenovaOPrazo(t *testing.T) {
	c, relogio := novoCache(t, cache.Config{TTL: time.Minute})
	guardar(t, c, "r1", itens("a"))
	relogio.avancar(50 * time.Second)
	guardar(t, c, "r1", itens("x", "y"))
	relogio.avancar(50 * time.Second)

	cardapio := exigirAcerto(t, c, "r1")

	if len(cardapio.Itens) != 2 || cardapio.Itens[0].ID != "x" {
		t.Fatalf("esperava o cardápio novo, veio %+v", cardapio.Itens)
	}
}

func TestExpiraAoAtingirOTTL(t *testing.T) {
	c, relogio := novoCache(t, cache.Config{TTL: time.Minute})
	guardar(t, c, "r1", itens("a"))

	relogio.avancar(time.Minute - time.Nanosecond)
	exigirAcerto(t, c, "r1")

	relogio.avancar(time.Nanosecond)
	exigirFalta(t, c, "r1")
}

func TestDespejaOMenosUsadoAoPassarDaCapacidade(t *testing.T) {
	c, _ := novoCache(t, cache.Config{Capacidade: 2})
	guardar(t, c, "r1", itens("a"))
	guardar(t, c, "r2", itens("b"))
	guardar(t, c, "r3", itens("c"))

	exigirFalta(t, c, "r1")
	exigirAcerto(t, c, "r2")
	exigirAcerto(t, c, "r3")
}

func TestLeituraRenovaAPosicaoNoLRU(t *testing.T) {
	c, _ := novoCache(t, cache.Config{Capacidade: 2})
	guardar(t, c, "r1", itens("a"))
	guardar(t, c, "r2", itens("b"))
	exigirAcerto(t, c, "r1")
	guardar(t, c, "r3", itens("c"))

	exigirAcerto(t, c, "r1")
	exigirFalta(t, c, "r2")
}

func TestInvalidarRemove(t *testing.T) {
	c, _ := novoCache(t, cache.Config{})
	guardar(t, c, "r1", itens("a"))

	removido, err := c.Invalidar(context.Background(), "r1")

	if err != nil || !removido {
		t.Fatalf("Invalidar: removido=%v err=%v", removido, err)
	}
	exigirFalta(t, c, "r1")
}

func TestInvalidarOQueNaoExisteNaoEhErro(t *testing.T) {
	c, _ := novoCache(t, cache.Config{})

	removido, err := c.Invalidar(context.Background(), "r1")

	if err != nil || removido {
		t.Fatalf("Invalidar: removido=%v err=%v", removido, err)
	}
}

func TestIdentificadorVazioEhInvalido(t *testing.T) {
	c, _ := novoCache(t, cache.Config{})
	ctx := context.Background()

	operacoes := map[string]func(string) error{
		"Obter":     func(id string) error { _, err := c.Obter(ctx, id); return err },
		"Guardar":   func(id string) error { _, err := c.Guardar(ctx, id, nil); return err },
		"Invalidar": func(id string) error { _, err := c.Invalidar(ctx, id); return err },
	}
	for nome, operacao := range operacoes {
		for _, id := range []string{"", "   "} {
			if err := operacao(id); !errors.Is(err, cache.ErrIDInvalido) {
				t.Errorf("%s(%q): esperava ErrIDInvalido, veio %v", nome, id, err)
			}
		}
	}
}

func TestItemInvalidoNaoEhGuardado(t *testing.T) {
	casos := map[string]cache.Item{
		"sem identificador": {ID: " ", Nome: "X", PrecoCentavos: 100},
		"preço negativo":    {ID: "a", Nome: "X", PrecoCentavos: -1},
	}
	for nome, item := range casos {
		t.Run(nome, func(t *testing.T) {
			c, _ := novoCache(t, cache.Config{})

			_, err := c.Guardar(context.Background(), "r1", []cache.Item{item})

			if !errors.Is(err, cache.ErrItemInvalido) {
				t.Fatalf("esperava ErrItemInvalido, veio %v", err)
			}
			exigirFalta(t, c, "r1")
		})
	}
}

func TestGuardarCopiaOsItens(t *testing.T) {
	c, _ := novoCache(t, cache.Config{})
	original := itens("a")
	guardar(t, c, "r1", original)

	original[0].Nome = "alterado fora"
	lido := exigirAcerto(t, c, "r1")
	lido.Itens[0].Nome = "alterado na leitura"

	if nome := exigirAcerto(t, c, "r1").Itens[0].Nome; nome != "Item a" {
		t.Fatalf("o cache foi alterado por fora: %q", nome)
	}
}

func TestContextoEncerradoInterrompeAsOperacoes(t *testing.T) {
	cancelado, cancelar := context.WithCancel(context.Background())
	cancelar()
	vencido, cancelarVencido := context.WithDeadline(context.Background(), time.Now().Add(-time.Second))
	defer cancelarVencido()

	casos := []struct {
		nome     string
		ctx      context.Context
		esperado error
	}{
		{"cancelado", cancelado, context.Canceled},
		{"prazo vencido", vencido, context.DeadlineExceeded},
	}
	for _, caso := range casos {
		t.Run(caso.nome, func(t *testing.T) {
			c, _ := novoCache(t, cache.Config{})
			guardar(t, c, "r1", itens("a"))

			if _, err := c.Obter(caso.ctx, "r1"); !errors.Is(err, caso.esperado) {
				t.Errorf("Obter: esperava %v, veio %v", caso.esperado, err)
			}
			if _, err := c.Guardar(caso.ctx, "r2", itens("b")); !errors.Is(err, caso.esperado) {
				t.Errorf("Guardar: esperava %v, veio %v", caso.esperado, err)
			}
			if _, err := c.Invalidar(caso.ctx, "r1"); !errors.Is(err, caso.esperado) {
				t.Errorf("Invalidar: esperava %v, veio %v", caso.esperado, err)
			}
			exigirAcerto(t, c, "r1")
			exigirFalta(t, c, "r2")
		})
	}
}

func TestConfiguracao(t *testing.T) {
	invalidas := map[string]cache.Config{
		"ttl negativo":        {TTL: -time.Second},
		"capacidade negativa": {Capacidade: -1},
	}
	for nome, cfg := range invalidas {
		t.Run(nome, func(t *testing.T) {
			if _, err := cache.Novo(cfg); !errors.Is(err, cache.ErrConfiguracaoInvalida) {
				t.Fatalf("esperava ErrConfiguracaoInvalida, veio %v", err)
			}
		})
	}

	t.Run("zero usa os padrões", func(t *testing.T) {
		c, relogio := novoCache(t, cache.Config{})
		for i := range cache.CapacidadePadrao + 1 {
			guardar(t, c, fmt.Sprintf("r%d", i), nil)
		}
		exigirFalta(t, c, "r0")
		exigirAcerto(t, c, "r1")

		relogio.avancar(cache.TTLPadrao)
		exigirFalta(t, c, "r1")
	})
}

func TestAcessoConcorrente(t *testing.T) {
	c, _ := novoCache(t, cache.Config{Capacidade: 8})
	ctx := context.Background()

	var grupo sync.WaitGroup
	for g := range 16 {
		grupo.Add(1)
		go func() {
			defer grupo.Done()
			for i := range 200 {
				id := fmt.Sprintf("r%d", (g+i)%12)
				switch i % 3 {
				case 0:
					_, _ = c.Guardar(ctx, id, itens("a", "b"))
				case 1:
					_, _ = c.Obter(ctx, id)
				default:
					_, _ = c.Invalidar(ctx, id)
				}
			}
		}()
	}
	grupo.Wait()
}

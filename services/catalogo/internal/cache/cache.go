package cache

import (
	"container/list"
	"context"
	"errors"
	"fmt"
	"strings"
	"sync"
	"time"
)

var (
	ErrNaoEncontrado        = errors.New("cardápio não encontrado no cache")
	ErrIDInvalido           = errors.New("identificador de restaurante inválido")
	ErrItemInvalido         = errors.New("item de cardápio inválido")
	ErrConfiguracaoInvalida = errors.New("configuração de cache inválida")
)

const (
	TTLPadrao        = 5 * time.Minute
	CapacidadePadrao = 1000
)

type Item struct {
	ID            string
	Nome          string
	Descricao     string
	PrecoCentavos int64
	Disponivel    bool
}

type Cardapio struct {
	RestauranteID string
	Itens         []Item
	GuardadoEm    time.Time
}

type Relogio interface {
	Agora() time.Time
}

type relogioDoSistema struct{}

func (relogioDoSistema) Agora() time.Time { return time.Now() }

type Config struct {
	TTL        time.Duration
	Capacidade int
	Relogio    Relogio
}

type Cache struct {
	ttl        time.Duration
	capacidade int
	relogio    Relogio

	mu       sync.Mutex
	ordem    *list.List
	entradas map[string]*list.Element
}

func Novo(cfg Config) (*Cache, error) {
	if cfg.TTL == 0 {
		cfg.TTL = TTLPadrao
	}
	if cfg.Capacidade == 0 {
		cfg.Capacidade = CapacidadePadrao
	}
	if cfg.Relogio == nil {
		cfg.Relogio = relogioDoSistema{}
	}
	if cfg.TTL < 0 {
		return nil, fmt.Errorf("ttl %s: %w", cfg.TTL, ErrConfiguracaoInvalida)
	}
	if cfg.Capacidade < 0 {
		return nil, fmt.Errorf("capacidade %d: %w", cfg.Capacidade, ErrConfiguracaoInvalida)
	}
	return &Cache{
		ttl:        cfg.TTL,
		capacidade: cfg.Capacidade,
		relogio:    cfg.Relogio,
		ordem:      list.New(),
		entradas:   make(map[string]*list.Element),
	}, nil
}

func (c *Cache) Obter(ctx context.Context, restauranteID string) (Cardapio, error) {
	id, err := preparar(ctx, "obter cardápio", restauranteID)
	if err != nil {
		return Cardapio{}, err
	}

	c.mu.Lock()
	defer c.mu.Unlock()

	elemento, ok := c.entradas[id]
	if !ok {
		return Cardapio{}, fmt.Errorf("restaurante %s: %w", id, ErrNaoEncontrado)
	}
	cardapio := elemento.Value.(Cardapio)
	if c.expirou(cardapio) {
		c.remover(elemento)
		return Cardapio{}, fmt.Errorf("restaurante %s expirou: %w", id, ErrNaoEncontrado)
	}
	c.ordem.MoveToFront(elemento)
	return copiar(cardapio), nil
}

func (c *Cache) Guardar(ctx context.Context, restauranteID string, itens []Item) (Cardapio, error) {
	id, err := preparar(ctx, "guardar cardápio", restauranteID)
	if err != nil {
		return Cardapio{}, err
	}
	for i, item := range itens {
		if err := validarItem(item); err != nil {
			return Cardapio{}, fmt.Errorf("guardar cardápio de %s, item %d: %w", id, i, err)
		}
	}

	cardapio := copiar(Cardapio{RestauranteID: id, Itens: itens, GuardadoEm: c.relogio.Agora()})

	c.mu.Lock()
	defer c.mu.Unlock()

	if elemento, ok := c.entradas[id]; ok {
		elemento.Value = cardapio
		c.ordem.MoveToFront(elemento)
	} else {
		c.entradas[id] = c.ordem.PushFront(cardapio)
	}
	for c.ordem.Len() > c.capacidade {
		c.remover(c.ordem.Back())
	}
	return copiar(cardapio), nil
}

func (c *Cache) Invalidar(ctx context.Context, restauranteID string) (bool, error) {
	id, err := preparar(ctx, "invalidar cardápio", restauranteID)
	if err != nil {
		return false, err
	}

	c.mu.Lock()
	defer c.mu.Unlock()

	elemento, ok := c.entradas[id]
	if !ok {
		return false, nil
	}
	c.remover(elemento)
	return true, nil
}

func (c *Cache) expirou(cardapio Cardapio) bool {
	return !c.relogio.Agora().Before(cardapio.GuardadoEm.Add(c.ttl))
}

func (c *Cache) remover(elemento *list.Element) {
	c.ordem.Remove(elemento)
	delete(c.entradas, elemento.Value.(Cardapio).RestauranteID)
}

func preparar(ctx context.Context, operacao, restauranteID string) (string, error) {
	if err := ctx.Err(); err != nil {
		return "", fmt.Errorf("%s: %w", operacao, err)
	}
	id := strings.TrimSpace(restauranteID)
	if id == "" {
		return "", fmt.Errorf("%s: %w", operacao, ErrIDInvalido)
	}
	return id, nil
}

func validarItem(item Item) error {
	if strings.TrimSpace(item.ID) == "" {
		return fmt.Errorf("sem identificador: %w", ErrItemInvalido)
	}
	if item.PrecoCentavos < 0 {
		return fmt.Errorf("preço negativo (%d): %w", item.PrecoCentavos, ErrItemInvalido)
	}
	return nil
}

func copiar(cardapio Cardapio) Cardapio {
	cardapio.Itens = append([]Item(nil), cardapio.Itens...)
	if cardapio.Itens == nil {
		cardapio.Itens = []Item{}
	}
	return cardapio
}

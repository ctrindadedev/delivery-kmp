CREATE TABLE restaurante (
    id   UUID PRIMARY KEY,
    nome TEXT NOT NULL
);

CREATE TABLE item_cardapio (
    id             UUID PRIMARY KEY,
    restaurante_id UUID    NOT NULL REFERENCES restaurante (id) ON DELETE CASCADE,
    nome           TEXT    NOT NULL,
    descricao      TEXT,
    preco_centavos BIGINT  NOT NULL CHECK (preco_centavos > 0),
    disponivel     BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_item_cardapio_restaurante_id ON item_cardapio (restaurante_id);

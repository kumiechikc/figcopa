-- Migrations para suportar imagens de jogadores e escudos
-- Adiciona campos para armazenar URLs de imagens reais

ALTER TABLE figurinha ADD COLUMN url_imagem_jogador VARCHAR(500) NULL DEFAULT NULL;
ALTER TABLE figurinha ADD COLUMN url_imagem_escudo VARCHAR(500) NULL DEFAULT NULL;

-- Índices para melhorar performance em consultas de imagens
CREATE INDEX idx_figurinha_url_jogador ON figurinha(url_imagem_jogador);
CREATE INDEX idx_figurinha_url_escudo ON figurinha(url_imagem_escudo);

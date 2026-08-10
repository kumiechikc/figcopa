-- -----------------------------------------------------------------------------
-- NAO E PRECISO RODAR ESTE ARQUIVO NUMA INSTALACAO NOVA.
--
-- As colunas de imagem ja fazem parte do 01_schema.sql. Este arquivo existe so
-- para quem criou o banco antes delas e nao quer recriar tudo: ele acrescenta
-- as duas colunas a uma tabela "figurinha" que ja tem dados.
--
-- Rodar num banco novo da erro de coluna duplicada — o que e inofensivo, mas
-- desnecessario.
-- -----------------------------------------------------------------------------

ALTER TABLE figurinha ADD COLUMN url_imagem_jogador VARCHAR(500) NULL DEFAULT NULL;
ALTER TABLE figurinha ADD COLUMN url_imagem_escudo  VARCHAR(500) NULL DEFAULT NULL;

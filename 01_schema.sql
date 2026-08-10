-- =====================================================================
-- THE CHAMPIONS — Modelo Físico (MySQL 8 / MariaDB)
-- Sistema P2P de troca de figurinhas da Copa do Mundo 2026
-- Desenvolvimento de Sistemas Web III — QI Faculdade
-- =====================================================================

DROP DATABASE IF EXISTS the_champions;
CREATE DATABASE the_champions
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE the_champions;

-- ---------------------------------------------------------------------
-- 1. RARIDADE
-- Classificação das figurinhas. Tabela pequena e estável (4 registros).
-- ---------------------------------------------------------------------
CREATE TABLE raridade (
  id_raridade      INT AUTO_INCREMENT PRIMARY KEY,
  nome             VARCHAR(30)     NOT NULL UNIQUE,
  cor_hex          CHAR(7)         NOT NULL,
  valor_referencia DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
  probabilidade    DECIMAL(5,4)    NOT NULL,   -- ex.: 0.0053 = 0,53%
  ordem            TINYINT         NOT NULL    -- 1=comum ... 4=lendária
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. USUARIO
-- senha_hash: NUNCA guardar senha em texto puro (requisito da Etapa 1).
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
  id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
  nome            VARCHAR(120)  NOT NULL,
  email           VARCHAR(180)  NOT NULL UNIQUE,
  senha_hash      VARCHAR(255)  NOT NULL,
  tipo_conta      ENUM('COMUM','ADMIN') NOT NULL DEFAULT 'COMUM',
  reputacao_media DECIMAL(3,2)  NOT NULL DEFAULT 0.00,
  data_cadastro   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ativo           BOOLEAN       NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. FIGURINHA
-- Catálogo do álbum. numero_album é o "Nº 231" que aparece nas telas.
-- ---------------------------------------------------------------------
CREATE TABLE figurinha (
  id_figurinha  INT AUTO_INCREMENT PRIMARY KEY,
  numero_album  VARCHAR(10)  NOT NULL UNIQUE,
  nome_jogador  VARCHAR(120) NOT NULL,
  selecao       VARCHAR(60)  NOT NULL,
  sigla_selecao CHAR(3)      NOT NULL,
  posicao       VARCHAR(30)      NULL,   -- NULL para escudos/itens especiais
  id_raridade   INT          NOT NULL,

  -- Foto do jogador e do escudo. Ficam NULL enquanto nao houver imagem: a carta
  -- cai no desenho SVG. As consultas do FigurinhaDAO citam estas duas colunas,
  -- entao elas precisam existir mesmo vazias — sem isso o album, os pacotes e
  -- as trocas falham com "Unknown column".
  url_imagem_jogador VARCHAR(500) NULL,
  url_imagem_escudo  VARCHAR(500) NULL,

  CONSTRAINT fk_figurinha_raridade
    FOREIGN KEY (id_raridade) REFERENCES raridade(id_raridade)
) ENGINE=InnoDB;

CREATE INDEX idx_figurinha_selecao  ON figurinha(sigla_selecao);
CREATE INDEX idx_figurinha_raridade ON figurinha(id_raridade);

-- ---------------------------------------------------------------------
-- 4. COLECAO  (associativa Usuario <-> Figurinha)
-- Resolve o N:N. quantidade > 1 significa que o usuário tem repetidas.
-- UNIQUE evita duas linhas para o mesmo par usuário+figurinha.
-- ---------------------------------------------------------------------
CREATE TABLE colecao (
  id_colecao      INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario      INT NOT NULL,
  id_figurinha    INT NOT NULL,
  quantidade      INT NOT NULL DEFAULT 1,
  data_obtencao   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_colecao_usuario
    FOREIGN KEY (id_usuario)   REFERENCES usuario(id_usuario)   ON DELETE CASCADE,
  CONSTRAINT fk_colecao_figurinha
    FOREIGN KEY (id_figurinha) REFERENCES figurinha(id_figurinha),
  CONSTRAINT uq_colecao UNIQUE (id_usuario, id_figurinha),
  CONSTRAINT ck_colecao_qtd CHECK (quantidade >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. PACOTE
-- ---------------------------------------------------------------------
CREATE TABLE pacote (
  id_pacote     INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario    INT NOT NULL,
  tipo          ENUM('DIARIO','PADRAO','ESPECIAL') NOT NULL,
  qtd_figurinhas TINYINT NOT NULL DEFAULT 7,
  aberto        BOOLEAN  NOT NULL DEFAULT FALSE,
  data_criacao  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  data_abertura DATETIME     NULL,
  CONSTRAINT fk_pacote_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 6. PACOTE_ITEM  (associativa Pacote <-> Figurinha)
-- Guarda o que saiu em cada pacote aberto.
-- ---------------------------------------------------------------------
CREATE TABLE pacote_item (
  id_pacote_item INT AUTO_INCREMENT PRIMARY KEY,
  id_pacote      INT NOT NULL,
  id_figurinha   INT NOT NULL,
  era_nova       BOOLEAN NOT NULL,   -- TRUE = entrou nova no álbum
  CONSTRAINT fk_pacote_item_pacote
    FOREIGN KEY (id_pacote)    REFERENCES pacote(id_pacote) ON DELETE CASCADE,
  CONSTRAINT fk_pacote_item_figurinha
    FOREIGN KEY (id_figurinha) REFERENCES figurinha(id_figurinha)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 7. OFERTA
-- O usuário publica o que OFERECE (repetidas) e o que PROCURA (faltantes).
-- É a base do matching automático.
-- ---------------------------------------------------------------------
CREATE TABLE oferta (
  id_oferta    INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario   INT NOT NULL,
  id_figurinha INT NOT NULL,
  tipo         ENUM('OFERECE','PROCURA') NOT NULL,
  ativa        BOOLEAN  NOT NULL DEFAULT TRUE,
  data_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_oferta_usuario
    FOREIGN KEY (id_usuario)   REFERENCES usuario(id_usuario) ON DELETE CASCADE,
  CONSTRAINT fk_oferta_figurinha
    FOREIGN KEY (id_figurinha) REFERENCES figurinha(id_figurinha),
  CONSTRAINT uq_oferta UNIQUE (id_usuario, id_figurinha, tipo)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 8. TROCA  *** ENTIDADE CENTRAL ***
-- DUAS FKs para usuario: proponente e receptor.
-- É isso que torna o sistema peer-to-peer de verdade.
-- Dupla confirmação: a troca só executa quando os dois confirmam.
-- ---------------------------------------------------------------------
CREATE TABLE troca (
  id_troca             INT AUTO_INCREMENT PRIMARY KEY,
  codigo               VARCHAR(20) NOT NULL UNIQUE,   -- ex.: TRC-4187
  id_usuario_proponente INT NOT NULL,
  id_usuario_receptor   INT NOT NULL,
  status               ENUM('PENDENTE','ACEITA','CONCLUIDA','RECUSADA','CANCELADA')
                       NOT NULL DEFAULT 'PENDENTE',
  confirmou_proponente BOOLEAN  NOT NULL DEFAULT FALSE,
  confirmou_receptor   BOOLEAN  NOT NULL DEFAULT FALSE,
  data_criacao         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  data_expiracao       DATETIME     NULL,
  data_conclusao       DATETIME     NULL,
  CONSTRAINT fk_troca_proponente
    FOREIGN KEY (id_usuario_proponente) REFERENCES usuario(id_usuario),
  CONSTRAINT fk_troca_receptor
    FOREIGN KEY (id_usuario_receptor)   REFERENCES usuario(id_usuario),
  CONSTRAINT ck_troca_usuarios_distintos
    CHECK (id_usuario_proponente <> id_usuario_receptor)
) ENGINE=InnoDB;

CREATE INDEX idx_troca_proponente ON troca(id_usuario_proponente);
CREATE INDEX idx_troca_receptor   ON troca(id_usuario_receptor);
CREATE INDEX idx_troca_status     ON troca(status);

-- ---------------------------------------------------------------------
-- 9. TROCA_ITEM  *** A PEÇA QUE FALTAVA ***
-- Uma troca envolve N figurinhas de CADA lado.
-- id_usuario_remetente diz de qual lado da troca a figurinha saiu.
-- ---------------------------------------------------------------------
CREATE TABLE troca_item (
  id_troca_item        INT AUTO_INCREMENT PRIMARY KEY,
  id_troca             INT NOT NULL,
  id_figurinha         INT NOT NULL,
  id_usuario_remetente INT NOT NULL,   -- quem está ENVIANDO esta figurinha
  quantidade           INT NOT NULL DEFAULT 1,
  CONSTRAINT fk_troca_item_troca
    FOREIGN KEY (id_troca)     REFERENCES troca(id_troca) ON DELETE CASCADE,
  CONSTRAINT fk_troca_item_figurinha
    FOREIGN KEY (id_figurinha) REFERENCES figurinha(id_figurinha),
  CONSTRAINT fk_troca_item_remetente
    FOREIGN KEY (id_usuario_remetente) REFERENCES usuario(id_usuario),
  CONSTRAINT ck_troca_item_qtd CHECK (quantidade > 0)
) ENGINE=InnoDB;

CREATE INDEX idx_troca_item_troca ON troca_item(id_troca);

-- ---------------------------------------------------------------------
-- 10. AVALIACAO
-- Após a troca concluída, cada lado avalia o outro (sistema de reputação).
-- UNIQUE garante uma avaliação por avaliador por troca.
-- ---------------------------------------------------------------------
CREATE TABLE avaliacao (
  id_avaliacao  INT AUTO_INCREMENT PRIMARY KEY,
  id_troca      INT NOT NULL,
  id_avaliador  INT NOT NULL,
  id_avaliado   INT NOT NULL,
  nota          TINYINT NOT NULL,
  comentario    VARCHAR(500) NULL,
  data_avaliacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_avaliacao_troca
    FOREIGN KEY (id_troca)    REFERENCES troca(id_troca) ON DELETE CASCADE,
  CONSTRAINT fk_avaliacao_avaliador
    FOREIGN KEY (id_avaliador) REFERENCES usuario(id_usuario),
  CONSTRAINT fk_avaliacao_avaliado
    FOREIGN KEY (id_avaliado)  REFERENCES usuario(id_usuario),
  CONSTRAINT uq_avaliacao UNIQUE (id_troca, id_avaliador),
  CONSTRAINT ck_avaliacao_nota CHECK (nota BETWEEN 1 AND 5),
  CONSTRAINT ck_avaliacao_distintos CHECK (id_avaliador <> id_avaliado)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 11. NOTIFICACAO  (suporte à tela de Dashboard)
-- ---------------------------------------------------------------------
CREATE TABLE notificacao (
  id_notificacao INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario     INT NOT NULL,
  tipo           ENUM('TROCA','PACOTE','AVALIACAO','MATCH','SISTEMA') NOT NULL,
  mensagem       VARCHAR(300) NOT NULL,
  lida           BOOLEAN  NOT NULL DEFAULT FALSE,
  data_criacao   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notificacao_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB;

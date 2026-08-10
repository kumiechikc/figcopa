-- =====================================================================
-- THE CHAMPIONS — Dados iniciais para demonstração
-- Rodar DEPOIS de 01_schema.sql
-- =====================================================================
USE the_champions;

-- ---------------------------------------------------------------------
-- RARIDADES
-- ---------------------------------------------------------------------
INSERT INTO raridade (nome, cor_hex, valor_referencia, probabilidade, ordem) VALUES
('Comum',     '#8A93A8',   2.00, 0.7800, 1),
('Brilhante', '#22D3EE',  25.00, 0.1500, 2),
('Rara',      '#F5883D', 120.00, 0.0400, 3),
('Lendaria',  '#A855F7', 830.00, 0.0053, 4);

-- ---------------------------------------------------------------------
-- USUARIOS
-- Senha de todos: 123456
--
-- ATENCAO (correcao aplicada na Etapa 3):
-- Os hashes originais deste arquivo eram todos iguais e NAO correspondiam a
-- nenhuma senha real -- testados com jBCrypt, "123456" e "password" falhavam
-- os dois. Com eles, nenhum usuario de teste conseguiria logar.
-- Os hashes abaixo foram gerados com BCrypt custo 10 (prefixo $2a$, o mesmo
-- que o jBCrypt usa) e verificados: todos aceitam a senha 123456.
-- Cada usuario tem seu proprio salt, como deve ser.
-- ---------------------------------------------------------------------
INSERT INTO usuario (nome, email, senha_hash, tipo_conta, reputacao_media) VALUES
('Vinicius K.',    'vinicius.k@email.com',   '$2a$10$uLtcHcHenytfQtOAubEHaO/Fz8gMmseHsP5wO1S8NT0IJTkIfnDUG','COMUM', 4.90),
('Pedro Marques',  'pedro.marques@email.com','$2a$10$evCSv3JLE7tUyQ9GFXhBG.Rim9W9NNsxo1ssgOBBMUQp2pKQHarG.','COMUM', 4.80),
('Arthur Tarrago', 'arthur.tarrago@email.com','$2a$10$SaRvPNegXgXmIDSLYqMAGO0O1PuL5Oz2TrhauMCYx/XZ/Ol8TWXYe','COMUM', 4.60),
('Marcos Vinicius','marcos.v@email.com',     '$2a$10$8qjLL1jnjWDWSuWKyUXG7OFVF7Pp50QEEeE4EUVxLjHFslOOlvrtO','COMUM', 4.70),
('Luana Figus',    'lu.figus@email.com',     '$2a$10$YdhlbtKJW3LYLCOzsF/h1ucZCLcHi5LyXmz0vIVRt7oJXv7dobQ6q','COMUM', 4.30),
('Administrador',  'admin@thechampions.com', '$2a$10$AIS7kldDwIwN6aBRHCrtbOINMDsf3Q7y2Hn7gNby2Wz5t5J1xdsK2','ADMIN', 0.00);

-- ---------------------------------------------------------------------
-- FIGURINHAS — Brasil
-- ---------------------------------------------------------------------
INSERT INTO figurinha (numero_album, nome_jogador, selecao, sigla_selecao, posicao, id_raridade) VALUES
('231','Escudo CBF',      'Brasil','BRA', NULL,       2),
('L-07','Vinicius Jr.',   'Brasil','BRA','Atacante',  4),
('233','Alisson',         'Brasil','BRA','Goleiro',   1),
('234','Marquinhos',      'Brasil','BRA','Zagueiro',  1),
('235','Militao',         'Brasil','BRA','Zagueiro',  1),
('236','Danilo',          'Brasil','BRA','Lateral',   1),
('237','Casemiro',        'Brasil','BRA','Volante',   1),
('238','Raphinha',        'Brasil','BRA','Atacante',  3),
('239','G. Magalhaes',    'Brasil','BRA','Zagueiro',  1),
('240','Endrick',         'Brasil','BRA','Atacante',  3),
('241','L. Paqueta',      'Brasil','BRA','Meia',      1),
('242','Rodrygo',         'Brasil','BRA','Atacante',  2),
('243','Bremer',          'Brasil','BRA','Zagueiro',  1),
('244','B. Guimaraes',    'Brasil','BRA','Volante',   1);

-- FIGURINHAS — Argentina
INSERT INTO figurinha (numero_album, nome_jogador, selecao, sigla_selecao, posicao, id_raridade) VALUES
('151','Escudo AFA',      'Argentina','ARG', NULL,      2),
('L-01','L. Messi',       'Argentina','ARG','Atacante', 4),
('153','E. Martinez',     'Argentina','ARG','Goleiro',  3),
('154','C. Romero',       'Argentina','ARG','Zagueiro', 1),
('155','R. De Paul',      'Argentina','ARG','Volante',  1),
('156','J. Alvarez',      'Argentina','ARG','Atacante', 2),
('157','E. Fernandez',    'Argentina','ARG','Meia',     1);

-- FIGURINHAS — Alemanha
INSERT INTO figurinha (numero_album, nome_jogador, selecao, sigla_selecao, posicao, id_raridade) VALUES
('101','Escudo DFB',      'Alemanha','ALE', NULL,       2),
('104','J. Musiala',      'Alemanha','ALE','Meia',      3),
('105','K. Havertz',      'Alemanha','ALE','Atacante',  1),
('106','A. Rudiger',      'Alemanha','ALE','Zagueiro',  1),
('107','M. Neuer',        'Alemanha','ALE','Goleiro',   2),
('108','J. Kimmich',      'Alemanha','ALE','Volante',   1);

-- FIGURINHAS — Franca
INSERT INTO figurinha (numero_album, nome_jogador, selecao, sigla_selecao, posicao, id_raridade) VALUES
('201','Escudo FFF',      'Franca','FRA', NULL,         2),
('L-03','K. Mbappe',      'Franca','FRA','Atacante',    4),
('212','A. Tchouameni',   'Franca','FRA','Volante',     1),
('214','T. Hernandez',    'Franca','FRA','Lateral',     1),
('215','W. Saliba',       'Franca','FRA','Zagueiro',    1),
('216','O. Dembele',      'Franca','FRA','Atacante',    3);

-- FIGURINHAS — Inglaterra
INSERT INTO figurinha (numero_album, nome_jogador, selecao, sigla_selecao, posicao, id_raridade) VALUES
('301','Escudo FA',       'Inglaterra','ING', NULL,     2),
('318','L. Shaw',         'Inglaterra','ING','Lateral', 1),
('319','J. Bellingham',   'Inglaterra','ING','Meia',    3),
('320','H. Kane',         'Inglaterra','ING','Atacante',3),
('321','P. Foden',        'Inglaterra','ING','Meia',    2),
('322','D. Rice',         'Inglaterra','ING','Volante', 1);

-- FIGURINHAS — Portugal / Espanha / Japao / Nigeria / Croacia
INSERT INTO figurinha (numero_album, nome_jogador, selecao, sigla_selecao, posicao, id_raridade) VALUES
('401','Escudo FPF',      'Portugal','POR', NULL,       2),
('402','B. Fernandes',    'Portugal','POR','Meia',      1),
('403','R. Leao',         'Portugal','POR','Atacante',  3),
('421','Escudo RFEF',     'Espanha','ESP', NULL,        2),
('422','Pedri',           'Espanha','ESP','Meia',       2),
('423','Lamine Yamal',    'Espanha','ESP','Atacante',   3),
('088','T. Kubo',         'Japao','JAP','Meia',         2),
('089','W. Endo',         'Japao','JAP','Volante',      1),
('L-11','V. Osimhen',     'Nigeria','NIG','Atacante',   4),
('462','A. Iwobi',        'Nigeria','NIG','Meia',       1),
('461','Escudo HNS',      'Croacia','CRO', NULL,        1),
('463','L. Modric',       'Croacia','CRO','Meia',       3);

-- ---------------------------------------------------------------------
-- COLECAO — Vinicius (id 1) tem varias, algumas repetidas
-- ---------------------------------------------------------------------
INSERT INTO colecao (id_usuario, id_figurinha, quantidade) VALUES
(1,1,1),(1,2,1),(1,3,2),(1,4,1),(1,5,1),(1,7,4),(1,8,3),(1,10,1),(1,12,1),(1,13,1),
(1,15,1),(1,16,1),(1,18,1),(1,22,1),(1,25,1),(1,28,1),(1,31,1),(1,34,1),
(1,38,1),(1,41,1),(1,44,2),(1,46,1);

-- COLECAO — Pedro (id 2) tem o que o Vinicius precisa
INSERT INTO colecao (id_usuario, id_figurinha, quantidade) VALUES
(2,1,1),(2,3,1),(2,6,3),(2,9,2),(2,11,1),(2,14,1),(2,23,2),(2,26,1),
(2,29,1),(2,32,1),(2,35,1),(2,39,1),(2,42,1),(2,45,1);

-- COLECAO — Arthur (id 3)
INSERT INTO colecao (id_usuario, id_figurinha, quantidade) VALUES
(3,2,1),(3,4,2),(3,6,1),(3,9,1),(3,17,3),(3,20,1),(3,24,1),(3,30,2),(3,36,1),(3,43,1);

-- COLECAO — Marcos (id 4)
INSERT INTO colecao (id_usuario, id_figurinha, quantidade) VALUES
(4,5,2),(4,7,1),(4,13,1),(4,19,1),(4,21,2),(4,27,1),(4,33,1),(4,40,1),(4,47,1);

-- COLECAO — Luana (id 5)
INSERT INTO colecao (id_usuario, id_figurinha, quantidade) VALUES
(5,8,1),(5,10,2),(5,12,1),(5,18,1),(5,28,2),(5,31,1),(5,37,1),(5,44,1);

-- ---------------------------------------------------------------------
-- OFERTAS — alimentam o matching automatico
-- Vinicius OFERECE suas repetidas
-- ---------------------------------------------------------------------
INSERT INTO oferta (id_usuario, id_figurinha, tipo) VALUES
(1,3,'OFERECE'),(1,7,'OFERECE'),(1,8,'OFERECE'),(1,44,'OFERECE'),
(1,6,'PROCURA'),(1,9,'PROCURA'),(1,14,'PROCURA'),(1,23,'PROCURA'),(1,26,'PROCURA');

-- Pedro OFERECE o que tem repetido e PROCURA o que falta
INSERT INTO oferta (id_usuario, id_figurinha, tipo) VALUES
(2,6,'OFERECE'),(2,9,'OFERECE'),(2,23,'OFERECE'),
(2,7,'PROCURA'),(2,8,'PROCURA'),(2,44,'PROCURA'),(2,2,'PROCURA');

-- Arthur
INSERT INTO oferta (id_usuario, id_figurinha, tipo) VALUES
(3,4,'OFERECE'),(3,17,'OFERECE'),(3,30,'OFERECE'),
(3,3,'PROCURA'),(3,7,'PROCURA'),(3,12,'PROCURA');

-- Marcos
INSERT INTO oferta (id_usuario, id_figurinha, tipo) VALUES
(4,5,'OFERECE'),(4,21,'OFERECE'),
(4,8,'PROCURA'),(4,44,'PROCURA');

-- Luana
INSERT INTO oferta (id_usuario, id_figurinha, tipo) VALUES
(5,10,'OFERECE'),(5,28,'OFERECE'),
(5,3,'PROCURA'),(5,7,'PROCURA');

-- ---------------------------------------------------------------------
-- TROCAS — uma pendente (para demonstrar a dupla confirmacao)
-- e algumas concluidas (para o historico do dashboard)
-- ---------------------------------------------------------------------
INSERT INTO troca (codigo, id_usuario_proponente, id_usuario_receptor, status,
                   confirmou_proponente, confirmou_receptor, data_expiracao) VALUES
('TRC-4187', 2, 1, 'PENDENTE', TRUE,  FALSE, DATE_ADD(NOW(), INTERVAL 22 HOUR));

-- Pedro envia: Danilo(6), G.Magalhaes(9), Musiala(23)
INSERT INTO troca_item (id_troca, id_figurinha, id_usuario_remetente, quantidade) VALUES
(1, 6, 2, 1),(1, 9, 2, 1),(1, 23, 2, 1);
-- Vinicius envia: T.Kubo(44), Casemiro(7), Escudo HNS(46)
INSERT INTO troca_item (id_troca, id_figurinha, id_usuario_remetente, quantidade) VALUES
(1, 44, 1, 1),(1, 7, 1, 1),(1, 46, 1, 1);

-- Trocas concluidas (historico)
INSERT INTO troca (codigo, id_usuario_proponente, id_usuario_receptor, status,
                   confirmou_proponente, confirmou_receptor, data_conclusao) VALUES
('TRC-4102', 1, 2, 'CONCLUIDA', TRUE, TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('TRC-4098', 1, 4, 'CONCLUIDA', TRUE, TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('TRC-4055', 1, 5, 'RECUSADA',  TRUE, FALSE,NULL);

INSERT INTO troca_item (id_troca, id_figurinha, id_usuario_remetente, quantidade) VALUES
(2, 8, 1, 1),(2, 24, 2, 1),
(3, 44, 1, 1),(3, 5, 4, 1),
(4, 1, 1, 1),(4, 28, 5, 1);

-- ---------------------------------------------------------------------
-- AVALIACOES
-- ---------------------------------------------------------------------
INSERT INTO avaliacao (id_troca, id_avaliador, id_avaliado, nota, comentario) VALUES
(2, 1, 2, 5, 'Troca rapida e certinha.'),
(2, 2, 1, 5, 'Otimo parceiro, recomendo.'),
(3, 1, 4, 5, 'Tudo certo, respondeu rapido.'),
(3, 4, 1, 4, 'Demorou um pouco mas fechou.');

-- ---------------------------------------------------------------------
-- PACOTES — 3 disponiveis para o Vinicius (nao abertos)
-- ---------------------------------------------------------------------
INSERT INTO pacote (id_usuario, tipo, qtd_figurinhas, aberto) VALUES
(1,'ESPECIAL',7,FALSE),
(1,'PADRAO',  7,FALSE),
(1,'PADRAO',  7,FALSE),
(2,'DIARIO',  7,FALSE),
(3,'PADRAO',  7,FALSE);

-- ---------------------------------------------------------------------
-- NOTIFICACOES
-- ---------------------------------------------------------------------
INSERT INTO notificacao (id_usuario, tipo, mensagem, lida) VALUES
(1,'TROCA','Pedro Marques propos uma troca. Ele tem 3 figurinhas que voce procura.', FALSE),
(1,'MATCH','12 novos matches compativeis com suas faltantes desde ontem.', FALSE),
(1,'AVALIACAO','Marcos Vinicius avaliou sua troca com 5 estrelas.', FALSE),
(1,'PACOTE','Seu pacote diario foi creditado. Bom jogo!', TRUE);

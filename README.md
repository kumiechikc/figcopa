# The Champions

Marketplace peer-to-peer de troca de figurinhas da Copa do Mundo 2026. Colecionadores
publicam o que têm repetido e o que ainda falta; o sistema cruza as duas pontas e a troca
só se concretiza quando **os dois lados confirmam**.

---

## Contexto acadêmico

| | |
|---|---|
| **Disciplina** | Desenvolvimento de Sistemas Web III |
| **Curso** | Técnico em Informática para a Internet |
| **Professor** | Paulo Donini |
| **Instituição** | QI Faculdade & Escola Técnica |
| **Etapa** | Projeto Final — implementação (continuação das Etapas 1 e 2) |

**Integrantes:** Arthur Tarrago Pedo · Pedro Guilherme Machado Marques ·
Vinicius Kumiechiki da Silva · Marcos Vinicius

---

## Stack

| Camada | Tecnologia |
|---|---|
| Apresentação | HTML, CSS e JavaScript sem framework nem etapa de build |
| Controle | Servlets Java 17 (`jakarta.servlet`, Tomcat 10+) |
| Visão | JSP + JSTL 3 |
| Persistência | JDBC puro — sem Spring, sem Hibernate |
| Banco | MySQL 8 / MariaDB 10.11 (XAMPP) |
| Segurança | jBCrypt (hash de senha com salt, custo 10) |
| Arquitetura | MVC + DAO |

> **Atenção à versão do Tomcat.** O projeto usa `jakarta.servlet`, adotado a partir do
> **Tomcat 10**. No Tomcat 9 o pacote ainda é `javax.servlet` e a aplicação não sobe.
> Confira a versão antes de qualquer coisa: essa é a armadilha número um deste stack.

---

## Estrutura de pastas

```
figcopa/
├── 01_schema.sql                    modelo físico: 11 tabelas
├── 02_dados.sql                     carga de demonstração (rodar depois do schema)
├── 03_imagens.sql                   colunas de foto do jogador/escudo (opcional)
├── pom.xml                          dependências e empacotamento do .war
│
└── src/main/
    ├── java/br/com/thechampions/
    │   ├── config/ConexaoDB.java    ponto único de conexão JDBC
    │   ├── model/                   entidades de domínio (Usuario, Figurinha, Troca, ...)
    │   ├── dao/                     acesso ao banco, uma classe por agregado
    │   ├── servlet/                 controladores (um por tela)
    │   ├── filter/                  filtro de autenticação
    │   └── util/                    hash de senha e formatação de datas
    │
    ├── resources/
    │   ├── database.properties.example   modelo de configuração (vai para o repo)
    │   └── database.properties           credenciais reais (ignorado pelo Git)
    │
    ├── webapp/                      APLICAÇÃO REAL (Tomcat + MySQL)
    │   ├── index.jsp                landing pública
    │   ├── assets/css/app.css       identidade visual completa
    │   ├── assets/js/               animações (barras de progresso e revelação de pacote)
    │   └── WEB-INF/
    │       ├── web.xml
    │       ├── tags/carta.tag       componente da figurinha, reusado em todas as telas
    │       └── jsp/                 telas internas + fragmentos de layout
    │
    └── webapp-static/               VITRINE do GitHub Pages (sem servidor)
        ├── index.html               landing
        ├── app.html                 SPA com todas as telas internas
        └── assets/
            ├── css/app.css          cópia do CSS da aplicação real ¹
            ├── icones.svg           sprite extraído de frag/icones.jsp ¹
            └── js/
                ├── api.js           usa o backend se houver; senão, os dados de exemplo
                ├── dados-demo.js    catálogo espelhando o 02_dados.sql
                └── components.js    carta.tag reescrito em JS
```

¹ Gerados por `./sincronizar-vitrine.sh` — a fonte de verdade é a pasta `webapp/`. Rode o
script depois de mexer no `app.css`, no `icones.jsp` ou no `revelacao.js`; o workflow do
Pages roda sozinho antes de publicar.

### O contrato entre a vitrine e o `app.css`

A vitrine reaproveita a folha de estilo da aplicação, e o CSS **assume uma estrutura
interna** em vários componentes: `.pacote-card` espera um `.arte` ao lado do texto,
`.troca-lados` é um grid de três colunas (envio, seta, recebimento), e `.carta-flip` só gira
se tiver `.giro`/`.verso`/`.frente` dentro.

Quando a vitrine desenha o markup por conta própria, nada quebra ruidosamente — o componente
só aparece sem estilo, ou sem a animação. Foi assim que a revelação do pacote ficou sem virar
carta nenhuma: o teste olhava a classe `.virada`, que era mesmo aplicada, enquanto o CSS
esperava um `.giro` que nunca existiu.

`e2e-contrato.js` lê os seletores descendentes do próprio `app.css` e cobra que a vitrine os
produza — um componente novo entra na checagem sozinho. Ele também mede a animação de fato
(o `transform` computado antes e depois do giro), em vez de confiar na classe.

---

## Como rodar do zero

### 1. Pré-requisitos

| Ferramenta | Versão | Onde baixar |
|---|---|---|
| XAMPP (Apache + MySQL + phpMyAdmin) | qualquer recente | https://www.apachefriends.org |
| JDK | 17 ou superior | https://adoptium.net |
| Apache Tomcat | **10 ou superior** | https://tomcat.apache.org |
| Maven | 3.8+ | https://maven.apache.org (o NetBeans e o IntelliJ já trazem) |

### 2. Criar o banco

Abra o XAMPP, dê **Start** em MySQL, e vá em http://localhost/phpmyadmin → aba **SQL**:

1. Cole o conteúdo inteiro de `01_schema.sql` → **Executar**
2. Cole o conteúdo inteiro de `02_dados.sql` → **Executar**

Confira a carga:

```sql
USE the_champions;
SELECT (SELECT COUNT(*) FROM usuario)   AS usuarios,
       (SELECT COUNT(*) FROM figurinha) AS figurinhas,
       (SELECT COUNT(*) FROM colecao)   AS itens_colecao,
       (SELECT COUNT(*) FROM troca)     AS trocas;
```

O resultado tem de ser exatamente **6 · 51 · 63 · 4**.

### 3. Configurar o acesso ao banco

```bash
cp src/main/resources/database.properties.example src/main/resources/database.properties
```

Edite o arquivo com o usuário e a senha do MySQL da sua máquina. No XAMPP padrão o
usuário é `root` e a senha é vazia — que também é o comportamento assumido caso o arquivo
não exista, então em uma instalação XAMPP limpa esse passo é opcional.

O arquivo real está no `.gitignore` e **nunca** deve ser commitado.

### 4. Gerar o `.war` e publicar

```bash
mvn clean package
```

O arquivo sai em `target/the-champions.war`. Copie-o para a pasta `webapps/` do Tomcat e
inicie o servidor (`bin/startup.sh` no Linux/macOS, `bin\startup.bat` no Windows).

No NetBeans ou IntelliJ, basta abrir a pasta como projeto Maven e usar **Run** com o
Tomcat 10 configurado como servidor.

### 5. Acessar

```
http://localhost:8080/the-champions/
```

**Abra primeiro `/diagnostico`.** Essa página mostra se o Tomcat está falando com o MySQL
e confere as contagens das quatro tabelas principais. Se ela estiver verde, todo o resto
funciona; se não, ela aponta a causa exata.

---

## Vitrine no GitHub Pages

O GitHub Pages serve **apenas arquivos estáticos** — ele não roda Java nem MySQL. Por
isso a aplicação real não pode ser hospedada lá. O que vai para o Pages é a pasta
`src/main/webapp-static`: uma vitrine que reproduz todas as telas com o mesmo CSS e o
mesmo catálogo do `02_dados.sql`, rodando inteira no navegador.

O que a vitrine faz:

- navega por todas as telas (dashboard, álbum com filtros, pacotes, trocas, negociação,
  perfil, ranking, histórico);
- abre pacotes com o mesmo sorteio ponderado por raridade do backend;
- fecha uma negociação com a animação de dupla confirmação.

O que ela **não** faz: nada é persistido — recarregar a página zera tudo. Não há cadastro,
login nem banco. É uma demonstração da interface, não o sistema.

### Ligar

1. No repositório: **Settings → Pages → Source: GitHub Actions**.
2. Um push na `main` que toque `src/main/webapp-static/**` dispara o workflow
   `.github/workflows/pages.yml`. Para publicar sem esperar um push, use **Actions →
   Publicar vitrine no GitHub Pages → Run workflow**.
3. O endereço sai em `https://<usuario>.github.io/figcopa/`.

### Apontar a vitrine para o backend

Quando o `.war` estiver no ar, a mesma vitrine passa a consumir dados reais. No console do
navegador, na página da vitrine:

```js
localStorage.setItem('tc_backend_url', 'https://seu-backend.exemplo.com/the-champions');
location.reload();
```

O `api.js` sonda `/api/status` no boot; se responder, ele troca os dados de exemplo pelas
chamadas reais e a faixa roxa de "modo demonstração" some. Para voltar ao demo,
`localStorage.removeItem('tc_backend_url')`.

Falta só publicar o backend em algum lugar com HTTPS e acrescentar a origem do Pages à
lista `ORIGENS` do `ApiFilter` — a API e o CORS já estão prontos (veja abaixo).

---

## API JSON

Existe para a vitrine poder falar com o backend de outro domínio. **As telas JSP não
mudaram**: a API não substitui nenhuma delas, só expõe os mesmos DAOs em JSON.

| Método | Rota | O que faz |
|---|---|---|
| GET | `/api/status` | responde se o backend está no ar (aberta) |
| POST | `/api/login` | e-mail + senha → token (aberta) |
| GET | `/api/usuario/perfil` | dados de quem está logado |
| GET | `/api/album` | catálogo com a quantidade de cada um; aceita `?selecao=BRA&status=faltantes` |
| GET | `/api/album/resumo` | progresso, quebra por raridade e valor do acervo |
| GET | `/api/pacotes` | pacotes fechados |
| POST | `/api/pacotes/{id}/abrir` | abre e devolve as 7 cartas |
| GET | `/api/trocas` | parceiros compatíveis (matching) |
| POST | `/api/trocas/propor` | grava a proposta a partir de um match → código |
| GET | `/api/troca/{codigo}` | uma troca, com os dois lados na sua perspectiva |
| POST | `/api/troca/{codigo}/confirmar` | confirma o seu lado |
| GET | `/api/ranking`, `/api/historico`, `/api/notificacoes` | listagens |

### Autenticação

As telas JSP usam `HttpSession` com cookie. A API não pode: a vitrine roda em outro domínio
e o cookie de sessão não acompanha uma requisição cross-site sem `SameSite=None; Secure` —
o que exigiria HTTPS configurado antes de qualquer teste. Por isso o token vai no cabeçalho:

```bash
TOKEN=$(curl -s -X POST localhost:8080/the-champions/api/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"vinicius.k@email.com","senha":"123456"}' | grep -o '"token":"[^"]*"')

curl -H "Authorization: Bearer $TOKEN" localhost:8080/the-champions/api/album
```

Os tokens ficam **em memória** (`util/Tokens.java`), valem 8 horas e não sobrevivem a um
restart do Tomcat. Não há tabela de sessão no modelo e o projeto é acadêmico; em produção
isso viraria uma tabela ou um Redis.

### CORS

`ApiFilter` libera apenas as origens listadas em `ORIGENS` — não usa `*`. Para publicar em
outro endereço, acrescente-o lá. Requisições de origem não listada simplesmente não recebem
o cabeçalho, e o navegador bloqueia.

---

## Usuários de teste

Senha de todos: `123456`

| E-mail | Papel |
|---|---|
| `vinicius.k@email.com` | usuário principal — álbum com 22 figurinhas, 3 pacotes fechados |
| `pedro.marques@email.com` | parceiro da troca TRC-4187 (já confirmou o lado dele) |
| `arthur.tarrago@email.com` | usuário secundário |
| `marcos.v@email.com` | usuário secundário |
| `lu.figus@email.com` | usuária secundária |
| `admin@thechampions.com` | administrador |

> Os hashes originais do `02_dados.sql` eram iguais para os seis usuários e não
> correspondiam a nenhuma senha — testados com BCrypt contra `123456` e contra `password`,
> os dois falhavam, e nenhum login funcionaria. Foram regerados com salt próprio por
> usuário e verificados um a um.

### Roteiro de demonstração

1. Entre como `vinicius.k@email.com`
2. **Dashboard** — 43% do álbum, quebra por raridade, trocas recentes com status variados
3. **Meu Álbum** — obtidas, faltantes escurecidas com "?" e repetidas com selo ×N
4. **Abrir Pacotes** — abra o Pacote Especial e veja as sete cartas virando em sequência
5. **Buscar Trocas** — o Pedro Marques aparece como match perfeito
6. **Negociação `TRC-4187`** — o Pedro já confirmou; clique em **Confirmar minha parte**
   e as figurinhas mudam de dono na hora. Dá para provar no phpMyAdmin:

```sql
SELECT status, confirmou_proponente, confirmou_receptor, data_conclusao
  FROM troca WHERE codigo = 'TRC-4187';
```

---

## Funcionalidades

### Implementadas e funcionais

- **Cadastro e login** com hash BCrypt, sessão HTTP e filtro que protege todas as telas internas
- **Dashboard** com progresso do álbum, quebra por raridade, trocas recentes e notificações
- **Meu Álbum** por seleção, com filtros por status e raridade e os três estados visuais
- **Catálogo** completo com os mesmos filtros
- **Abertura de pacotes** com sorteio ponderado pela coluna `probabilidade` da tabela
  `raridade`, gravação em `pacote_item`, atualização da coleção e revelação animada
- **Matching automático** cruzando repetidas de um lado com faltantes do outro, nos dois sentidos
- **Proposta de troca** a partir de um match
- **Negociação com dupla confirmação** e transferência executada dentro de uma transação SQL
- **Perfil, ranking e histórico de trocas**
- **Página de diagnóstico** do ambiente

### Planejadas, não implementadas

- Recuperação de senha por e-mail
- Avaliação do parceiro após a troca (a tabela `avaliacao` existe e tem carga, mas não há tela)
- Painel administrativo (moderação, CRUD do catálogo, relatórios gerenciais)
- Edição manual da proposta antes de enviar (hoje a proposta segue o que o matching sugere)
- Pacote diário automático por login
- Cadastro pela API (`POST /api/cadastro` está reservado no filtro, mas sem implementação)

---

## Fotos dos jogadores

As cartas desenham um busto em SVG colorido pela raridade. A estrutura para trocar isso
por fotos reais **já está pronta** — falta só a fonte das imagens.

O que existe:

- `03_imagens.sql` adiciona `url_imagem_jogador` e `url_imagem_escudo` na tabela `figurinha`;
- `FigurinhaDAO` já traz as duas colunas em todas as consultas;
- `carta.tag` (Tomcat) e `components.js` (vitrine) usam `<img>` quando a URL existe e caem
  no SVG quando ela é `NULL`.

Ou seja: preencher as colunas é o suficiente para as fotos aparecerem nas duas frentes.

```sql
UPDATE figurinha SET url_imagem_jogador = 'https://.../vinicius-jr.jpg'
 WHERE numero_album = 'L-07';
```

### Sobre usar as imagens da Panini

Não dá para simplesmente baixar do site da Panini e subir para o repositório. As figurinhas
e as fotos são material licenciado — republicar isso num site público (que é exatamente o
que o GitHub Pages faz) é redistribuição de obra protegida. Some-se a isso que a arte da
Panini é o produto que este projeto imita, o que torna o uso ainda mais difícil de defender
como citação.

O caminho limpo é usar fotos com licença livre. O **Wikimedia Commons** tem retrato de boa
parte dos jogadores das seleções em CC BY-SA, que pode ser usado desde que se credite o
autor e mantenha a licença. A API é pública:

```
https://commons.wikimedia.org/w/api.php?action=query&titles=File:NOME.jpg
  &prop=imageinfo&iiprop=url|extmetadata&format=json
```

Se for por esse caminho, guarde junto o autor e a licença de cada foto e mostre o crédito
em algum lugar da interface — é o que a licença exige.

> Nenhuma imagem foi baixada até aqui: o ambiente onde este código foi escrito bloqueia
> saída de rede para `panini.com.br` e `commons.wikimedia.org`, então a coleta ficou para
> quem tiver rede aberta.

---

## Modelo de dados

Onze tabelas. `troca` é a entidade central: ela é a única com **duas** chaves estrangeiras
para `usuario`, e é isso que torna o sistema peer-to-peer de verdade.

| Tabela | Papel |
|---|---|
| `raridade` | Os quatro níveis, com cor, valor de referência e probabilidade de sorteio |
| `usuario` | Colecionador: dados de acesso, reputação e tipo de conta |
| `figurinha` | Catálogo do álbum: jogador, seleção, posição e raridade |
| `colecao` | Associativa usuário ↔ figurinha; `quantidade > 1` significa repetida |
| `pacote` | Pacotes distribuídos, abertos ou não |
| `pacote_item` | O que saiu em cada pacote aberto, com marca de "entrou nova no álbum" |
| `oferta` | O que o usuário publica que oferece ou procura |
| `troca` | **Entidade central**: dois usuários, status e as duas flags de confirmação |
| `troca_item` | Figurinhas de cada lado; `id_usuario_remetente` diz de quem elas saem |
| `avaliacao` | Nota e comentário após a troca concluída |
| `notificacao` | Avisos do dashboard |

### As duas regras de negócio que valem a pena ler no código

**Sorteio do pacote** — `PacoteDAO.sortear()`. Sorteia primeiro a *raridade*, por roleta
acumulada ponderada pela coluna `probabilidade`, e só depois uma figurinha qualquer daquele
nível. Fazer o contrário deixaria as lendárias mais prováveis apenas porque existem poucas
cadastradas. O Pacote Especial ainda garante ao menos uma Rara.

**Execução da troca** — `TrocaDAO.confirmar()`. A transferência move várias linhas de
`colecao` de uma vez. Sem transação, uma falha no meio deixaria um usuário sem a figurinha
e o outro sem recebê-la — figurinha sumindo do sistema. Por isso tudo roda com
`setAutoCommit(false)` e um rollback cobre qualquer erro, inclusive o caso de alguém não
ter mais a figurinha prometida.

---

## Identidade visual

Tema escuro sobre azul-profundo, roxo como cor de marca, tipografia Saira (títulos),
Manrope (texto) e Space Mono (números). Ícones são SVG de linha da biblioteca Lucide,
desenhados inline — nenhum emoji é usado como ícone.

O núcleo visual é o **sistema de raridade**: a cor da moldura e a intensidade do brilho
comunicam o valor da figurinha em qualquer tela, sem depender de texto.

| Raridade | Cor | Brilho | Frequência |
|---|---|---|---|
| Comum | `#8A93A8` | nenhum | 78% |
| Brilhante | `#22D3EE` | 8px | 15% |
| Rara | `#F5883D` | 10px | 4% |
| Lendária | `#A855F7` | 14px | 0,53% |

As animações usam apenas CSS transitions e a Web Animations API, sem biblioteca externa, e
respeitam `prefers-reduced-motion`.

---

## Verificação

O projeto foi validado contra um MySQL real antes da entrega:

- as 13 JSPs traduzem e compilam sem erro no Jasper do Tomcat 10;
- 49 verificações dos DAOs contra o banco com a carga de demonstração, cobrindo login dos
  seis usuários, contagens do álbum, matching, sorteio de pacote, dupla confirmação e as
  tentativas que devem falhar (reabrir pacote, confirmar troca alheia, confirmar duas vezes);
- fluxo completo no navegador, do login à troca concluída, em 360px, 390px, 768px e 1440px.

A vitrine estática foi verificada à parte, com 35 asserções no navegador: renderização das
51 cartas com as molduras de raridade, filtros de álbum combinados (status + seleção),
abertura de pacote com as 7 cartas virando, dupla confirmação, sprite de ícones resolvido,
rota inválida caindo no dashboard e ausência de rolagem horizontal em 390px.

```bash
cd src/main/webapp-static && python3 -m http.server 8123   # e, noutro terminal:
node e2e-static.js       # vitrine sozinha, com os dados de exemplo
node e2e-integrado.js    # vitrine + Tomcat + MySQL
```

E a integração entre as duas frentes, com mais 21 asserções (`e2e-integrado.js`): a vitrine
servida em `:8123` consumindo o Tomcat em `:8080` — sai do modo demonstração, o progresso e
as 51 cartas vêm do MySQL, abre um pacote real, propõe uma troca que é gravada com código
`TRC-…`, mostra que quem propôs já confirmou o próprio lado, e devolve ao login (em vez de
tela morta) quando o token vence.

A API foi verificada também por fora, com `curl`: as 14 rotas, o preflight `OPTIONS`, o
cabeçalho CORS presente para origem autorizada e ausente para as demais, e as recusas —
reabrir um pacote já aberto (409), abrir pacote de outro usuário (409, sem abri-lo),
confirmar troca alheia (403) e confirmar duas vezes (409).

---

## Problemas comuns

| Sintoma | Causa provável |
|---|---|
| `ClassNotFoundException: jakarta.servlet.http.HttpServlet` | Tomcat 9 em vez de 10+ |
| `Access denied for user` | usuário/senha errados no `database.properties` |
| `Unknown database 'the_champions'` | os `.sql` não foram importados |
| Página em branco ou erro 500 | abra `/diagnostico`, que aponta a causa |
| Fontes sem o visual esperado | máquina sem internet — o Google Fonts não carrega, o layout continua funcionando |

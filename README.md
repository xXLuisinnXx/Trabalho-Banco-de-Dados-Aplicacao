# Sistema de Restaurante — Trabalho de Laboratório de Banco de Dados

Aplicação Java (Swing) com MySQL para controle de mesas, comandas, pedidos e cardápio
de um restaurante, com autenticação e três níveis de acesso.

## Requisitos

- MySQL Server 8.0 (padrão: `localhost:3306`)
- JDK 21
- NetBeans (o projeto já vem com `nbproject/`) ou `javac`/`ant` na linha de comando
- Driver JDBC `mysql-connector-j-8.0.33.jar`, já incluído em `Restaurante/lib/`

## 1. Criar o banco

Execute os scripts da pasta `banco/` **nesta ordem**. O primeiro precisa de uma conta
administrativa do MySQL (ele cria o schema, os objetos e as contas da aplicação); os
demais já rodam com a conta `admin_user` criada pelo próprio script.

```bash
mysql -h localhost -P 3306 -u root -p < banco/01_estrutura.sql
mysql -h localhost -P 3306 -u admin_user -p restaurante < banco/02_dados.sql
mysql -h localhost -P 3306 -u admin_user -p restaurante < banco/03_usuarios.sql
```

| Script | O que faz |
|---|---|
| `01_estrutura.sql` | Tabelas, índices, views, functions, procedures, triggers e as contas MySQL da aplicação |
| `02_dados.sql` | Dados de exemplo (categorias, produtos, mesas, garçons, contas, pedidos) |
| `03_usuarios.sql` | Usuários do sistema, com senha em hash PBKDF2 |

## 2. Executar a aplicação

O sistema tem duas interfaces sobre a mesma camada de regras de negócio.

### 2.1 Aplicação desktop (operação do restaurante)

Abra o projeto `Restaurante/` no NetBeans e rode (F6). A classe principal é
`br.com.visao.TelaLogin`.

Pela linha de comando:

```bash
cd Restaurante && javac -encoding UTF-8 -d build/classes -cp lib/mysql-connector-j-8.0.33.jar $(find src -name "*.java") && java -cp "build/classes;lib/mysql-connector-j-8.0.33.jar" br.com.visao.TelaLogin
```

### 2.2 Interface web (consulta)

Compile como acima e suba o servidor a partir da pasta `Restaurante/`:

```bash
cd Restaurante && java -cp "build/classes;lib/mysql-connector-j-8.0.33.jar" br.com.web.ServidorWeb 8080
```

Depois abra <http://localhost:8080> no navegador. O servidor entrega os arquivos de
`Restaurante/web/` e expõe a API em `/api/…`; o navegador nunca fala com o MySQL.

| Rota | Objeto do banco por trás |
|---|---|
| `POST /api/login`, `/api/logout`, `GET /api/sessao` | `usuarios` + `grupos_usuarios` |
| `GET /api/cardapio` | view `vw_cardapio_completo` |
| `GET /api/mesas` | entidade `mesa` |
| `GET /api/contas-abertas` | view `vw_contas_abertas` (+ function `calcular_total_conta`) |
| `GET /api/contas?inicio=&fim=` | índice `idx_conta_data_abertura` + function `calcular_total_conta` |
| `GET /api/relatorio?inicio=&fim=` | índice `idx_pedido_data_hora` |
| `GET /api/usuarios` | entidades `usuarios` e `grupos_usuarios` (só ADMIN) |
| `POST /api/fechar-conta` | procedure `fechar_conta` (transação + trigger `trg_conta_au`) |

Se o seu MySQL não estiver na porta 3306, ajuste as constantes `HOST`, `PORTA` e `BANCO`
no início de `Restaurante/src/br/com/entidade/DAO.java`.

## 3. Credenciais de demonstração

| E-mail | Senha | Perfil | Conta MySQL usada |
|---|---|---|---|
| `admin@restaurante.com` | `admin123` | ADMIN | `admin_user` |
| `gerente@restaurante.com` | `gerente123` | GERENTE | `gerente_user` |
| `garcom@restaurante.com` | `garcom123` | GARCOM | `garcom_user` |

Para criar o primeiro administrador em um banco sem nenhum usuário (alternativa ao
`03_usuarios.sql`), use a classe utilitária:

```bash
java -cp "build/classes;lib/mysql-connector-j-8.0.33.jar" br.com.controle.CriarAdminInicial "Nome" email@dominio.com SenhaForte123
```

## Controle de acesso

A aplicação **nunca usa o root**. Antes do login a conexão usa a conta `app_login`, que só
enxerga as tabelas `usuarios` e `grupos_usuarios`. Depois do login, a `Sessao` troca a
conexão para a conta MySQL do grupo do usuário, de modo que as permissões são aplicadas
em duas camadas: na aplicação (enum `Permissao`) e no próprio SGBD (GRANTs).

| Perfil | Pode |
|---|---|
| GARCOM | Consultar cadastros, abrir/fechar contas e registrar pedidos |
| GERENTE | Tudo do garçom + cadastrar produtos, categorias, garçons e mesas + relatórios |
| ADMIN | Acesso total, incluindo o cadastro de usuários |

## Documentação

- [`docs/artigo.md`](docs/artigo.md) — artigo técnico com a justificativa de cada
  índice, view, trigger, procedure, function e do controle de acesso, além do roteiro
  da apresentação.
- [`docs/der.svg`](docs/der.svg) — Diagrama Entidade-Relacionamento.

## Estrutura do repositório

```
banco/        scripts SQL (estrutura, dados e usuários)
docs/         artigo técnico e DER
Restaurante/  projeto Java
  src/br/com/visao/       telas Swing
  src/br/com/controle/    regras de negócio, autenticação e permissões
  src/br/com/entidade/    entidades e acesso a dados (DAO + repositórios)
  src/br/com/web/         servidor HTTP e API JSON
  web/                    interface web (HTML, CSS e JS)
```


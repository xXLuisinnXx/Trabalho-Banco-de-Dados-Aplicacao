# 🍽️ Sistema de Restaurante

Sistema desktop para gerenciar o dia a dia de um restaurante: cadastro de produtos, mesas e garçons, abertura de contas, registro de pedidos, fechamento de conta e relatórios. Desenvolvido em **Java (Swing)** com banco **MySQL** para a disciplina de **Programação Orientada a Objetos**.

---

## 📌 Sobre o projeto

O objetivo é simular o fluxo real de um restaurante:

1. O cliente senta em uma **mesa**.
2. Um **garçom** registra um **pedido** (e a conta da mesa é aberta automaticamente).
3. A cozinha prepara; o pedido é **finalizado** ou **cancelado**.
4. Ao fim, a **conta é fechada**, o total é calculado e a mesa volta a ficar livre.
5. A gerência consulta **relatórios** de pedidos e faturamento.

Além do CRUD básico, o projeto traz:

- **Controle de acesso por perfil** (login, sessão e matriz de permissões).
- **Regras de negócio no banco** (views, functions, procedures, triggers e índices), para garantir consistência mesmo com várias telas usando o sistema ao mesmo tempo.
- **Segurança em duas camadas**: o Java barra o que o perfil não pode fazer e o próprio MySQL também (cada perfil conecta com uma conta MySQL diferente).

---

## 🛠️ Ferramentas e tecnologias

| Item | Uso |
|---|---|
| **Java 8+** (recomendado 11 ou superior) | Linguagem principal |
| **Java Swing** | Interface gráfica (telas) |
| **NetBeans** | IDE usada no desenvolvimento (telas criadas com o editor visual) |
| **MySQL 8** | Banco de dados |
| **MySQL Connector/J** (`com.mysql.cj.jdbc.Driver`) | Driver JDBC |
| **JDBC / PreparedStatement** | Acesso ao banco, protegido contra SQL Injection |
| **PBKDF2WithHmacSHA256** (JDK) | Hash de senhas, sem biblioteca externa |

---

## 🧱 Arquitetura

O código é separado em camadas, seguindo a ideia de MVC com DAO:

```
br.com
├── visao                  → Telas Swing (TelaLogin, TelaPrincipal, TelaProduto, ...)
├── controle               → Regras de negócio e validações (ProdutoControle, PedidoControle, ...)
├── entidade               → Classes de domínio (Mesa, Conta, Pedido, Usuario, enums de situação, DAO)
└── entidade.repository    → Acesso ao banco (ManterMesa, ManterPedido, ManterConta, ...)
```

**Fluxo de uma operação:** `Tela` → `Controle` (valida e checa permissão) → `Manter...` (SQL) → `MySQL`.

### Principais classes

| Classe | Responsabilidade |
|---|---|
| `DAO` | Abre/fecha a conexão. Usa a conta MySQL do perfil atual |
| `PerfilBanco` | Contas MySQL: `app_login`, `garcom_user`, `gerente_user`, `admin_user` |
| `Sessao` | Guarda o usuário logado e troca o perfil de conexão ao entrar/sair |
| `Permissao` | Matriz de permissões (o que cada grupo pode fazer) |
| `AutenticacaoControle` | Login (e-mail + senha com hash) |
| `SenhaUtil` | Gera e verifica hash PBKDF2 (120 mil iterações, salt aleatório) |
| `TotalizadorPedidos` | Soma de itens e de pedidos cobráveis |
| `RegraNegocioException` / `AcessoNegadoException` | Erros de regra e de acesso, exibidos direto nas telas |

---

## 🔐 Perfis e permissões

Existem três grupos de usuário. Cada um conecta ao banco com uma conta MySQL própria.

| Permissão | Garçom | Gerente | Admin |
|---|:---:|:---:|:---:|
| Consultar cadastros e cardápio | ✅ | ✅ | ✅ |
| Operar contas e pedidos | ✅ | ✅ | ✅ |
| Consultar relatórios | ❌ | ✅ | ✅ |
| Cadastrar/alterar produtos, categorias, garçons e mesas | ❌ | ✅ | ✅ |
| Excluir cadastros | ❌ | ❌ | ✅ |
| Gerenciar usuários | ❌ | ❌ | ✅ |

A matriz fica toda no enum `Permissao`: alterar ali muda o sistema inteiro.

---

## 🗄️ Banco de dados

Banco: `restaurante` (MySQL, porta `3306`).

**Tabelas principais:** `categoria`, `produto`, `garcom`, `mesa`, `conta`, `pedido`, `item_pedido`, `usuarios`, `grupos_usuarios`, `sequencia_id`.

**Recursos do banco usados pelo sistema:**

| Tipo | Nome | Para que serve |
|---|---|---|
| View | `vw_cardapio_completo` | Cardápio (produto + categoria) pronto para exibir |
| View | `vw_contas_abertas` | Contas abertas com total parcial já calculado |
| Function | `calcular_total_conta` | Calcula o valor de uma conta |
| Function | `gerar_id_usuario` | Gera o ID `USR-000001` dos usuários |
| Procedure | `registrar_pedido` | Trava a conta, confere se está ABERTA e grava pedido + itens |
| Procedure | `fechar_conta` | Trava a conta, valida pedidos em preparo, calcula total e define FECHADA/CANCELADA |
| Trigger | `trg_conta_ai` / `trg_conta_au` | Marca a mesa como OCUPADA ao abrir conta e LIVRE ao fechar |
| Trigger | `trg_item_pedido_ai` / `_au` / `_ad` | Recalculam `pedido.total` quando um item entra, muda ou sai |
| Índice | `idx_conta_data_abertura` | Acelera o histórico de contas por período |
| Constraint | `uq_conta_aberta_por_mesa` | Impede duas contas abertas na mesma mesa |

---

## 🚀 Como executar

### 1. Pré-requisitos

- JDK 8 ou superior
- MySQL 8 em execução
- NetBeans (ou outra IDE Java) com o **MySQL Connector/J** adicionado às bibliotecas do projeto

### 2. Criar o banco

Execute os scripts SQL da pasta `banco`, **em ordem numérica**, começando pelo `01_estrutura.sql`. Eles criam o banco `restaurante`, as tabelas, views, functions, procedures, triggers, índices e as quatro contas MySQL usadas pelo sistema.

### 3. Conferir a conexão

Em `DAO.java`, ajuste se necessário:

```java
private static final String HOST  = "localhost";
private static final int    PORTA = 3306;
private static final String BANCO = "restaurante";
```

Teste a conexão rodando a classe `TestCon`.

### 4. Criar o primeiro administrador

Com a tabela `usuarios` vazia, execute **uma única vez**:

```
CriarAdminInicial "Seu Nome" seu@email.com SuaSenha123
```

> A senha precisa ter no mínimo 8 caracteres, com letras e números. Se já existir qualquer usuário, o comando se recusa a criar outro admin.

### 5. Iniciar o sistema

Execute `TelaLogin` (ou `TelaPrincipal`, que redireciona para o login se ninguém estiver logado) e entre com o e-mail e a senha cadastrados.

---

## 🔄 Rotina do sistema

### 1. Acesso

1. O sistema abre na **TelaLogin**, usando a conta MySQL `app_login`, que só consegue ler `usuarios` e `grupos_usuarios`.
2. O e-mail e a senha são conferidos pelo hash PBKDF2. A mensagem de erro é a mesma para e-mail inexistente ou senha errada, e o tempo de resposta também, para não revelar quais e-mails existem.
3. No login, a `Sessao` guarda o usuário (sem o hash) e troca a conexão para a conta MySQL do grupo dele.
4. A **TelaPrincipal** mostra apenas os botões que o perfil pode usar.
5. Em **Sair**, a sessão é encerrada e a conexão volta ao perfil de login.

### 2. Preparação (Gerente/Admin)

1. Cadastrar **categorias** (ex.: Bebidas, Pratos, Sobremesas).
2. Cadastrar **produtos** com nome, preço e categoria.
3. Cadastrar **mesas** (número e capacidade) e **garçons**.
4. O **Admin** cadastra os **usuários** e define o perfil de cada um.

### 3. Atendimento (Garçom/Gerente/Admin), na tela *Contas / Pedidos*

1. Escolher a **mesa**, o **garçom** e montar o pedido adicionando produtos (produto repetido soma a quantidade).
2. Clicar em **Registrar pedido**:
   - **Mesa livre** → abre uma nova conta e grava o pedido na mesma transação. A trigger marca a mesa como OCUPADA.
   - **Mesa ocupada** → o pedido entra na conta aberta via procedure `registrar_pedido`, que trava a conta antes de gravar.
3. O pedido nasce **EM_PREPARO**. Enquanto estiver assim, é possível adicionar item, alterar quantidade, remover item (o pedido nunca fica vazio), cancelar ou excluir.
4. Ao ficar pronto, o pedido é **FINALIZADO** (entra no total da conta) ou **CANCELADO** (não entra).
5. O preço do produto é **copiado para o item** no momento do pedido, então mudar o preço depois não altera pedidos antigos.
6. O **total parcial** da conta é exibido na tela e considera só pedidos finalizados.

### 4. Fechamento da conta

1. Clicar em **Fechar conta** na mesa desejada.
2. Se houver pedido EM_PREPARO, o fechamento é bloqueado (a checagem no Java é só para avisar rápido; a regra definitiva está na procedure `fechar_conta`).
3. A procedure calcula o total, define a situação (**FECHADA** ou **CANCELADA**) e a trigger **libera a mesa**.

### 5. Gestão (Gerente/Admin)

- **Relatório de pedidos:** filtro por período, garçom, mesa e situação, com quantidade de pedidos e faturamento (somente finalizados).
- **Histórico de contas por período:** usa o índice por data de abertura e a function `calcular_total_conta`.

---

## 📏 Principais regras de negócio

- Uma mesa só pode ter **uma conta aberta** por vez.
- Pedido **EM_PREPARO** é o único que pode ser alterado, cancelado ou excluído.
- Só pedidos **FINALIZADOS** entram no total e no faturamento.
- Não é possível excluir:
  - categoria com produtos;
  - produto já usado em pedidos;
  - mesa ocupada ou com histórico de contas;
  - garçom com pedidos registrados.
- Nome de categoria e número de mesa são **únicos**; e-mail de usuário também.
- Senha: mínimo de 8 caracteres, com letras e números (máximo 100).
- O administrador **não pode alterar o próprio perfil nem excluir o próprio usuário**, o que garante que sempre sobre ao menos um admin.

---

## ⚠️ Observações

- As senhas das contas MySQL estão fixas em `PerfilBanco.java` apenas por ser um trabalho acadêmico. Em produção, devem vir de um arquivo de configuração ou variável de ambiente.
- As telas apenas escondem ou desabilitam botões sem permissão. A barreira real está no controle (`Sessao.exigir`) e nas permissões das contas MySQL.

---

## 👨‍💻 Autor

Trabalho desenvolvido para a disciplina de Programação Orientada a Objetos.

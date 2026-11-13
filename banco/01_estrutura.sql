CREATE DATABASE restaurante;

USE restaurante;

-- =====================================================================
-- TABELAS
-- =====================================================================

CREATE TABLE categoria (
    id_categoria INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT chk_categoria_nome CHECK (TRIM(nome) <> '')
);

CREATE TABLE produto (
    id_produto INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(55) NOT NULL,
    preco DECIMAL(10,2) NOT NULL,
    id_categoria INT NOT NULL,
    CONSTRAINT FK_produto_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria(id_categoria) ON DELETE RESTRICT,
    CONSTRAINT chk_produto_nome CHECK (TRIM(nome) <> ''),
    CONSTRAINT chk_produto_preco CHECK (preco > 0)
);

CREATE TABLE mesa (
    id_mesa INT PRIMARY KEY AUTO_INCREMENT,
    numero INT NOT NULL UNIQUE,
    capacidade INT NOT NULL,
    situacao ENUM('LIVRE', 'OCUPADA') NOT NULL DEFAULT 'LIVRE',
    CONSTRAINT chk_mesa_numero CHECK (numero > 0),
    CONSTRAINT chk_mesa_capacidade CHECK (capacidade > 0)
);

CREATE TABLE garcom (
    id_garcom INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    telefone VARCHAR(20),
    CONSTRAINT chk_garcom_nome CHECK (TRIM(nome) <> '')
);

CREATE TABLE conta (
    id_conta INT PRIMARY KEY AUTO_INCREMENT,
    id_mesa INT NOT NULL,
    data_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento DATETIME,
    situacao ENUM('ABERTA', 'FECHADA', 'CANCELADA') NOT NULL DEFAULT 'ABERTA',
    total DECIMAL(10,2) NOT NULL DEFAULT 0,
    -- vale o id da mesa so enquanto a conta esta aberta: com o UNIQUE,
    -- impede duas contas abertas na mesma mesa
    mesa_aberta INT GENERATED ALWAYS AS (IF(situacao = 'ABERTA', id_mesa, NULL)) STORED,
    CONSTRAINT uq_conta_aberta_por_mesa UNIQUE (mesa_aberta),
    CONSTRAINT FK_conta_mesa FOREIGN KEY (id_mesa)
        REFERENCES mesa(id_mesa) ON DELETE RESTRICT,
    CONSTRAINT chk_conta_total CHECK (total >= 0),
    CONSTRAINT chk_conta_fechamento CHECK (
        (situacao = 'ABERTA' AND data_fechamento IS NULL) OR
        (situacao <> 'ABERTA' AND data_fechamento IS NOT NULL)),
    CONSTRAINT chk_conta_datas CHECK (data_fechamento IS NULL OR data_fechamento >= data_abertura),
    CONSTRAINT chk_conta_cancelada CHECK (situacao <> 'CANCELADA' OR total = 0)
);

CREATE TABLE pedido (
    id_pedido INT PRIMARY KEY AUTO_INCREMENT,
    id_conta INT NOT NULL,
    id_garcom INT NOT NULL,
    data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    situacao ENUM('EM_PREPARO', 'FINALIZADO', 'CANCELADO') NOT NULL DEFAULT 'EM_PREPARO',
    total DECIMAL(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT FK_pedido_conta FOREIGN KEY (id_conta)
        REFERENCES conta(id_conta) ON DELETE RESTRICT,
    CONSTRAINT FK_pedido_garcom FOREIGN KEY (id_garcom)
        REFERENCES garcom(id_garcom) ON DELETE RESTRICT,
    CONSTRAINT chk_pedido_total CHECK (total >= 0)
);

CREATE TABLE item_pedido (
    id_item INT PRIMARY KEY AUTO_INCREMENT,
    id_pedido INT NOT NULL,
    id_produto INT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL, -- preco do produto no momento da venda
    CONSTRAINT FK_item_pedido FOREIGN KEY (id_pedido)
        REFERENCES pedido(id_pedido) ON DELETE RESTRICT,
    CONSTRAINT FK_item_produto FOREIGN KEY (id_produto)
        REFERENCES produto(id_produto) ON DELETE RESTRICT,
    CONSTRAINT chk_item_quantidade CHECK (quantidade > 0),
    CONSTRAINT chk_item_preco CHECK (preco_unitario > 0)
);

CREATE TABLE grupos_usuarios (
    id_grupo INT PRIMARY KEY AUTO_INCREMENT,
    nome_grupo VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255)
);

-- Grupos usados pelo enum GrupoUsuario (os nomes têm que ser exatamente estes)
INSERT IGNORE INTO grupos_usuarios (nome_grupo, descricao) VALUES
  ('GARCOM',  'Abre/fecha contas, registra pedidos, consulta cardápio e mesas'),
  ('GERENTE', 'Perfil do garçom + cadastros e relatórios'),
  ('ADMIN',   'Acesso total, inclusive gestão de usuários');

CREATE TABLE usuarios (
    id_usuario VARCHAR(10) PRIMARY KEY, -- Aqui você pode usar a sua função gerar_id_usuario()
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    id_grupo INT NOT NULL,
    CONSTRAINT FK_usuario_grupo FOREIGN KEY (id_grupo)
        REFERENCES grupos_usuarios(id_grupo) ON DELETE RESTRICT
);

-- =====================================================================
-- ÍNDICES
-- =====================================================================

CREATE INDEX idx_item_pedido_produto ON item_pedido (id_produto);
CREATE INDEX idx_conta_data_abertura ON conta (data_abertura);
-- Índice que o relatório realmente usa (filtra e ordena por pedido.data_hora)
CREATE INDEX idx_pedido_data_hora ON pedido (data_hora);

-- =====================================================================
-- FUNCTIONS
-- =====================================================================

SET GLOBAL log_bin_trust_function_creators = 1;

CREATE TABLE sequencia_id (
    nome VARCHAR(30) PRIMARY KEY,
    ultimo_valor INT UNSIGNED NOT NULL DEFAULT 0
) ENGINE=InnoDB;

INSERT INTO sequencia_id (nome, ultimo_valor) VALUES ('USR', 0);

DELIMITER $$

-- Gera IDs no padrao USR-000001, USR-000002, ...
CREATE FUNCTION gerar_id_usuario()
RETURNS VARCHAR(10)
NOT DETERMINISTIC
MODIFIES SQL DATA
BEGIN
    UPDATE sequencia_id
       SET ultimo_valor = LAST_INSERT_ID(ultimo_valor + 1)
     WHERE nome = 'USR';

    RETURN CONCAT('USR-', LPAD(LAST_INSERT_ID(), 6, '0'));
END$$

-- Total de uma conta: só pedido FINALIZADO entra (mesma regra de SituacaoPedido.entraNoTotal)
CREATE FUNCTION calcular_total_conta(p_id_conta INT)
RETURNS DECIMAL(10,2)
NOT DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_total DECIMAL(10,2);

    SELECT COALESCE(SUM(total), 0)
      INTO v_total
      FROM pedido
     WHERE id_conta = p_id_conta
       AND situacao = 'FINALIZADO';

    RETURN v_total;
END$$

DELIMITER ;

-- =====================================================================
-- VIEWS (criadas após as functions que utilizam)
-- =====================================================================

-- View 1: Resumo das contas abertas com o total parcial
CREATE VIEW vw_contas_abertas AS
SELECT 
    c.id_conta,
    c.id_mesa,
    m.numero AS numero_mesa,
    c.data_abertura,
    calcular_total_conta(c.id_conta) AS total_parcial
FROM conta c
JOIN mesa m ON c.id_mesa = m.id_mesa
WHERE c.situacao = 'ABERTA';

-- View 2: Cardápio completo com categorias
CREATE VIEW vw_cardapio_completo AS
SELECT 
    p.id_produto,
    p.nome AS produto,
    p.preco,
    c.nome AS categoria
FROM produto p
JOIN categoria c ON p.id_categoria = c.id_categoria;

-- =====================================================================
-- TRIGGERS
-- =====================================================================

DELIMITER $$

-- Atualizar automaticamente pedido.total (a partir de item_pedido)
CREATE TRIGGER trg_item_pedido_ai
AFTER INSERT ON item_pedido
FOR EACH ROW
BEGIN
    UPDATE pedido
       SET total = (SELECT COALESCE(SUM(quantidade * preco_unitario), 0)
                      FROM item_pedido
                     WHERE id_pedido = NEW.id_pedido)
     WHERE id_pedido = NEW.id_pedido;
END$$

CREATE TRIGGER trg_item_pedido_au
AFTER UPDATE ON item_pedido
FOR EACH ROW
BEGIN
    UPDATE pedido
       SET total = (SELECT COALESCE(SUM(quantidade * preco_unitario), 0)
                      FROM item_pedido
                     WHERE id_pedido = NEW.id_pedido)
     WHERE id_pedido = NEW.id_pedido;

    IF OLD.id_pedido <> NEW.id_pedido THEN
        UPDATE pedido
           SET total = (SELECT COALESCE(SUM(quantidade * preco_unitario), 0)
                          FROM item_pedido
                         WHERE id_pedido = OLD.id_pedido)
         WHERE id_pedido = OLD.id_pedido;
    END IF;
END$$

CREATE TRIGGER trg_item_pedido_ad
AFTER DELETE ON item_pedido
FOR EACH ROW
BEGIN
    UPDATE pedido
       SET total = (SELECT COALESCE(SUM(quantidade * preco_unitario), 0)
                      FROM item_pedido
                     WHERE id_pedido = OLD.id_pedido)
     WHERE id_pedido = OLD.id_pedido;
END$$

-- Sincronizar mesa.situacao com conta.situacao
CREATE TRIGGER trg_conta_ai
AFTER INSERT ON conta
FOR EACH ROW
BEGIN
    IF NEW.situacao = 'ABERTA' THEN
        UPDATE mesa SET situacao = 'OCUPADA' WHERE id_mesa = NEW.id_mesa;
    END IF;
END$$

CREATE TRIGGER trg_conta_au
AFTER UPDATE ON conta
FOR EACH ROW
BEGIN
    IF OLD.situacao = 'ABERTA' AND NEW.situacao <> 'ABERTA' THEN
        UPDATE mesa
           SET situacao = 'LIVRE'
         WHERE id_mesa = NEW.id_mesa
           AND NOT EXISTS (SELECT 1
                             FROM conta
                            WHERE id_mesa = NEW.id_mesa
                              AND situacao = 'ABERTA'
                              AND id_conta <> NEW.id_conta);
    END IF;

    IF OLD.situacao <> 'ABERTA' AND NEW.situacao = 'ABERTA' THEN
        UPDATE mesa SET situacao = 'OCUPADA' WHERE id_mesa = NEW.id_mesa;
    END IF;

    IF OLD.situacao = 'ABERTA' AND NEW.situacao = 'ABERTA'
       AND OLD.id_mesa <> NEW.id_mesa THEN
        UPDATE mesa
           SET situacao = 'LIVRE'
         WHERE id_mesa = OLD.id_mesa
           AND NOT EXISTS (SELECT 1
                             FROM conta
                            WHERE id_mesa = OLD.id_mesa
                              AND situacao = 'ABERTA'
                              AND id_conta <> NEW.id_conta);
        UPDATE mesa SET situacao = 'OCUPADA' WHERE id_mesa = NEW.id_mesa;
    END IF;
END$$

-- =====================================================================
-- PROCEDURES
-- =====================================================================

CREATE PROCEDURE registrar_pedido(
    IN  p_id_conta  INT,
    IN  p_id_garcom INT,
    IN  p_itens     JSON,
    OUT p_id_pedido INT
)
BEGIN
    DECLARE v_situacao_conta VARCHAR(10);
    DECLARE v_qtd_itens      INT;
    DECLARE v_i              INT DEFAULT 0;
    DECLARE v_id_produto     INT;
    DECLARE v_quantidade     INT;
    DECLARE v_preco          DECIMAL(10,2);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SET p_id_pedido = NULL;

    IF p_itens IS NULL OR JSON_TYPE(p_itens) <> 'ARRAY' OR JSON_LENGTH(p_itens) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Informe ao menos um item no pedido.';
    END IF;

    SET v_qtd_itens = JSON_LENGTH(p_itens);

    START TRANSACTION;

    -- Trava a conta para ninguem fecha-la enquanto o pedido e gravado.
    SELECT situacao INTO v_situacao_conta
      FROM conta
     WHERE id_conta = p_id_conta
       FOR UPDATE;

    IF v_situacao_conta IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Conta inexistente.';
    END IF;
    IF v_situacao_conta <> 'ABERTA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A conta nao esta aberta.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM garcom WHERE id_garcom = p_id_garcom) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Garcom inexistente.';
    END IF;

    INSERT INTO pedido (id_conta, id_garcom) VALUES (p_id_conta, p_id_garcom);
    SET p_id_pedido = LAST_INSERT_ID();

    -- Percorre o array JSON item por item
    WHILE v_i < v_qtd_itens DO
        SET v_id_produto = CAST(JSON_UNQUOTE(JSON_EXTRACT(p_itens,
                               CONCAT('$[', v_i, '].id_produto'))) AS SIGNED);
        SET v_quantidade = CAST(JSON_UNQUOTE(JSON_EXTRACT(p_itens,
                               CONCAT('$[', v_i, '].quantidade'))) AS SIGNED);

        IF v_id_produto IS NULL OR v_quantidade IS NULL OR v_quantidade < 1 THEN
            SIGNAL SQLSTATE '45000'
               SET MESSAGE_TEXT = 'Item invalido: informe id_produto e quantidade maior que 0.';
        END IF;

        SET v_preco = NULL;
        SELECT preco INTO v_preco FROM produto WHERE id_produto = v_id_produto;

        IF v_preco IS NULL THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Item invalido: produto inexistente.';
        END IF;

        INSERT INTO item_pedido (id_pedido, id_produto, quantidade, preco_unitario)
        VALUES (p_id_pedido, v_id_produto, v_quantidade, v_preco);

        SET v_i = v_i + 1;
    END WHILE;

    COMMIT;
END$$

-- fechar_conta: devolve a situação e usa CANCELADA quando nada foi cobrado
CREATE PROCEDURE fechar_conta(
    IN  p_id_conta INT,
    OUT p_total    DECIMAL(10,2),
    OUT p_situacao VARCHAR(10)
)
BEGIN
    DECLARE v_situacao VARCHAR(10);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SET p_total = NULL;
    SET p_situacao = NULL;

    START TRANSACTION;

    SELECT situacao INTO v_situacao
      FROM conta
     WHERE id_conta = p_id_conta
       FOR UPDATE;

    IF v_situacao IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Conta inexistente.';
    END IF;
    IF v_situacao <> 'ABERTA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A conta nao esta aberta.';
    END IF;
    IF EXISTS (SELECT 1 FROM pedido
                WHERE id_conta = p_id_conta AND situacao = 'EM_PREPARO') THEN
        SIGNAL SQLSTATE '45000'
           SET MESSAGE_TEXT = 'Existem pedidos EM_PREPARO: finalize ou cancele antes de fechar a conta.';
    END IF;

    SET p_total = calcular_total_conta(p_id_conta);
    SET p_situacao = IF(p_total = 0, 'CANCELADA', 'FECHADA');

    UPDATE conta
       SET total = p_total,
           situacao = p_situacao,
           data_fechamento = NOW()
     WHERE id_conta = p_id_conta;

    COMMIT;
END$$

DELIMITER ;

-- =====================================================================
-- NÍVEIS DE PERMISSÃO E USUÁRIOS
-- =====================================================================

-- 0. PERFIL: APP_LOGIN (Autenticação prévia antes do login)
CREATE USER IF NOT EXISTS 'app_login'@'localhost' IDENTIFIED BY 'LoginSenha123!';
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'app_login'@'localhost';

GRANT SELECT ON restaurante.usuarios        TO 'app_login'@'localhost';
GRANT SELECT ON restaurante.grupos_usuarios TO 'app_login'@'localhost';


-- 1. PERFIL: GARÇOM 
-- Regra: Abrir/fechar contas, registrar/atualizar/excluir pedidos e consultar cardápio/mesas.
CREATE USER IF NOT EXISTS 'garcom_user'@'localhost' IDENTIFIED BY 'GarcomSenha123!';
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'garcom_user'@'localhost';

-- Consultas necessárias para operação básica
GRANT SELECT ON restaurante.mesa TO 'garcom_user'@'localhost';
GRANT SELECT ON restaurante.garcom TO 'garcom_user'@'localhost';
GRANT SELECT ON restaurante.produto TO 'garcom_user'@'localhost';
GRANT SELECT ON restaurante.categoria TO 'garcom_user'@'localhost';
GRANT SELECT ON restaurante.vw_cardapio_completo TO 'garcom_user'@'localhost';
GRANT SELECT ON restaurante.vw_contas_abertas TO 'garcom_user'@'localhost';

-- Manipulação de Contas e Pedidos (incluindo ajustes de UPDATE situacao e DELETE)
GRANT SELECT, INSERT, UPDATE ON restaurante.conta TO 'garcom_user'@'localhost';
GRANT SELECT, INSERT, DELETE ON restaurante.pedido TO 'garcom_user'@'localhost';
GRANT UPDATE (situacao) ON restaurante.pedido TO 'garcom_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON restaurante.item_pedido TO 'garcom_user'@'localhost';

-- Execução das Procedures e Funções de negócio
GRANT EXECUTE ON PROCEDURE restaurante.registrar_pedido TO 'garcom_user'@'localhost';
GRANT EXECUTE ON PROCEDURE restaurante.fechar_conta TO 'garcom_user'@'localhost';
GRANT EXECUTE ON FUNCTION restaurante.calcular_total_conta TO 'garcom_user'@'localhost';


-- 2. PERFIL: GERENTE
-- Regra: Mesmas permissões do garçom + cadastro de novos produtos, categorias, garçons e mesas.
CREATE USER IF NOT EXISTS 'gerente_user'@'localhost' IDENTIFIED BY 'GerenteSenha123!';
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'gerente_user'@'localhost';

-- Permissões de Gestão
GRANT SELECT, INSERT, UPDATE ON restaurante.produto TO 'gerente_user'@'localhost';
GRANT SELECT, INSERT, UPDATE ON restaurante.categoria TO 'gerente_user'@'localhost';
GRANT SELECT, INSERT, UPDATE ON restaurante.garcom TO 'gerente_user'@'localhost';
GRANT SELECT, INSERT, UPDATE ON restaurante.mesa TO 'gerente_user'@'localhost';

-- Herda permissões operacionais de contas e pedidos do garçom
GRANT SELECT, INSERT, UPDATE ON restaurante.conta TO 'gerente_user'@'localhost';
GRANT SELECT, INSERT, DELETE ON restaurante.pedido TO 'gerente_user'@'localhost';
GRANT UPDATE (situacao) ON restaurante.pedido TO 'gerente_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON restaurante.item_pedido TO 'gerente_user'@'localhost';
GRANT SELECT ON restaurante.vw_cardapio_completo TO 'gerente_user'@'localhost';
GRANT SELECT ON restaurante.vw_contas_abertas TO 'gerente_user'@'localhost';

-- Execução das Procedures e Funções
GRANT EXECUTE ON PROCEDURE restaurante.registrar_pedido TO 'gerente_user'@'localhost';
GRANT EXECUTE ON PROCEDURE restaurante.fechar_conta TO 'gerente_user'@'localhost';
GRANT EXECUTE ON FUNCTION restaurante.calcular_total_conta TO 'gerente_user'@'localhost';


-- 3. PERFIL: ADMIN (Administrador do Sistema)
-- Regra: Pode fazer tudo no banco do restaurante (substitui o uso do root).
CREATE USER IF NOT EXISTS 'admin_user'@'localhost' IDENTIFIED BY 'AdminSenha123!';
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'admin_user'@'localhost';

-- Privilégios totais restritos exclusivamente ao banco de dados do restaurante
GRANT ALL PRIVILEGES ON restaurante.* TO 'admin_user'@'localhost';

FLUSH PRIVILEGES;
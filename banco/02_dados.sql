-- =====================================================================
-- INSERÇÃO DE DADOS (3 registros por tabela)
-- =====================================================================

-- 1. Categoria (Sem dependências)
INSERT INTO categoria (nome) VALUES 
('Bebidas'),
('Pratos Principais'),
('Sobremesas');

-- 2. Produto (Depende de categoria)
INSERT INTO produto (nome, preco, id_categoria) VALUES 
('Refrigerante Lata', 6.50, 1),
('Filé à Parmegiana', 45.00, 2),
('Pudim de Leite', 12.00, 3);

-- 3. Mesa (Sem dependências)
INSERT INTO mesa (numero, capacidade, situacao) VALUES 
(1, 4, 'LIVRE'),
(2, 2, 'LIVRE'),
(3, 6, 'LIVRE');

-- 4. Garçom (Sem dependências)
INSERT INTO garcom (nome, telefone) VALUES 
('Carlos Silva', '11988887777'),
('Ana Souza', '11977776666'),
('Marcos Oliveira', '11966665555');

-- 5. Conta (Depende de mesa)
-- Contas 1 e 2 abertas (vão disparar a trigger para atualizar as mesas para 'OCUPADA')
INSERT INTO conta (id_mesa, situacao) VALUES 
(1, 'ABERTA'),
(2, 'ABERTA');

-- Conta 3 fechada (requer data_fechamento conforme a constraint)
INSERT INTO conta (id_mesa, situacao, data_fechamento) VALUES 
(3, 'FECHADA', NOW());

-- 6. Pedido (Depende de conta e garçom)
INSERT INTO pedido (id_conta, id_garcom, situacao) VALUES 
(1, 1, 'EM_PREPARO'),
(1, 2, 'FINALIZADO'),
(3, 3, 'FINALIZADO');

-- 7. Item do Pedido (Depende de pedido e produto)
-- Nota: As triggers de item_pedido atualizarão automaticamente os totais dos pedidos.
INSERT INTO item_pedido (id_pedido, id_produto, quantidade, preco_unitario) VALUES 
(1, 1, 2, 6.50),   -- Pedido 1 (2x Refrigerante)
(2, 2, 1, 45.00),  -- Pedido 2 (1x Filé à Parmegiana)
(3, 3, 2, 12.00);
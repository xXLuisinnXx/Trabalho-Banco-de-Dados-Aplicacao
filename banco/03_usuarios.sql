-- =====================================================================
-- USUÁRIOS DO SISTEMA (tabela usuarios)
-- Execute depois do 01_estrutura.sql e do 02_dados.sql.
--
-- Rode este script com a conta admin_user (nunca com root):
--   mysql -h localhost -P 3306 -u admin_user -p restaurante < 03_usuarios.sql
--
-- O id NÃO é AUTO_INCREMENT: vem da função gerar_id_usuario(), que entrega
-- USR-000001, USR-000002, ... a partir da tabela sequencia_id.
--
-- A senha nunca é gravada em texto. A coluna senha_hash guarda o formato
-- pbkdf2$iteracoes$salt$hash produzido por br.com.controle.SenhaUtil
-- (PBKDF2WithHmacSHA256, 120.000 iterações, salt aleatório de 16 bytes).
-- Por isso estes hashes foram gerados pela própria aplicação: cada um já
-- corresponde à senha indicada no comentário ao lado.
--
-- Credenciais de demonstração:
--   admin@restaurante.com   / admin123     -> perfil ADMIN   (conta MySQL admin_user)
--   gerente@restaurante.com / gerente123   -> perfil GERENTE (conta MySQL gerente_user)
--   garcom@restaurante.com  / garcom123    -> perfil GARCOM  (conta MySQL garcom_user)
-- Troque as senhas pela própria tela de Usuários depois do primeiro acesso.
-- =====================================================================

USE restaurante;

INSERT INTO usuarios (id_usuario, nome, email, senha_hash, id_grupo) VALUES (
    gerar_id_usuario(),
    'Ana Administradora',
    'admin@restaurante.com',
    'pbkdf2$120000$XKaKO6pr3Y0d8RJByKHC1A==$Y30Wpms8hIlCDVzhVjqqsYbMpx9R1oGE3UU7y/vuhKg=', -- senha: admin123
    (SELECT id_grupo FROM grupos_usuarios WHERE nome_grupo = 'ADMIN'));

INSERT INTO usuarios (id_usuario, nome, email, senha_hash, id_grupo) VALUES (
    gerar_id_usuario(),
    'Bruno Gerente',
    'gerente@restaurante.com',
    'pbkdf2$120000$NnY1bUdEC+wdZAzHNuiHNg==$bIze725GdF8uqd1hB4M7jn2KKCpo3G055u/S93T386s=', -- senha: gerente123
    (SELECT id_grupo FROM grupos_usuarios WHERE nome_grupo = 'GERENTE'));

INSERT INTO usuarios (id_usuario, nome, email, senha_hash, id_grupo) VALUES (
    gerar_id_usuario(),
    'Carlos Garçom',
    'garcom@restaurante.com',
    'pbkdf2$120000$p/boXm+Wyfh1FelNEpdlOQ==$sit6z5WZekoy93MHLPeAAndj1QB+1yqYIXyWCiftKRk=', -- senha: garcom123
    (SELECT id_grupo FROM grupos_usuarios WHERE nome_grupo = 'GARCOM'));

-- Conferência: deve listar os 3 usuários com os IDs USR-000001 a USR-000003
SELECT u.id_usuario, u.nome, u.email, g.nome_grupo
  FROM usuarios u
  JOIN grupos_usuarios g ON g.id_grupo = u.id_grupo
 ORDER BY u.id_usuario;

package br.com.controle;

import java.util.Locale;

import br.com.entidade.Usuario;
import br.com.entidade.repository.ManterUsuario;

public class AutenticacaoControle {

    // usado quando o e-mail não existe, para o tempo de resposta não revelar se ele está cadastrado
    private static final String HASH_FICTICIO = SenhaUtil.gerarHash("senha-ficticia-1");

    private final ManterUsuario manterUsuario = new ManterUsuario();

    public Usuario entrar(String email, String senha) throws Exception {
        if (email == null || email.trim().isEmpty() || senha == null || senha.isEmpty()) {
            throw new RegraNegocioException("Informe o e-mail e a senha.");
        }
        Sessao.encerrar(); // garante a conexão de login (app_login) para consultar usuarios

        Usuario usuario = manterUsuario.buscarPorEmail(email.trim().toLowerCase(Locale.ROOT));
        boolean senhaConfere = SenhaUtil.verificar(senha,
                usuario == null ? HASH_FICTICIO : usuario.getSenhaHash());
        if (usuario == null || !senhaConfere) {
            // mesma mensagem nos dois casos: não revela se o e-mail existe
            throw new RegraNegocioException("E-mail ou senha inválidos.");
        }
        usuario.setSenhaHash(null); // o hash não precisa ficar na memória da sessão
        Sessao.iniciar(usuario);
        return usuario;
    }

    public void sair() {
        Sessao.encerrar();
    }
}

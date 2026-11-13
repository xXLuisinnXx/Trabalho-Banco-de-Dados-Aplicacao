package br.com.controle;

import br.com.entidade.DAO;
import br.com.entidade.PerfilBanco;

/**
 * Roda uma vez, depois dos scripts da pasta banco, para criar o primeiro administrador.
 * Uso: CriarAdminInicial "Nome" email@dominio.com SenhaForte123
 */
public class CriarAdminInicial {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Uso: CriarAdminInicial <nome> <email> <senha>");
            return;
        }
        DAO.definirPerfil(PerfilBanco.ADMIN);
        try {
            new UsuarioControle().cadastrarPrimeiroAdmin(args[0], args[1], args[2]);
            System.out.println("Administrador criado: " + args[1]);
        } catch (RegraNegocioException e) {
            System.out.println("Não foi possível criar: " + e.getMessage());
        }
    }
}

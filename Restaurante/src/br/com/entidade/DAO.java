/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.entidade;

/**
 *
 * @author morai
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DAO {
    // perfil usado nas conexões: LOGIN antes do login; a Sessao troca ao entrar/sair
    private static volatile PerfilBanco perfilAtual = PerfilBanco.LOGIN;

    public static void definirPerfil(PerfilBanco perfil) {
        perfilAtual = perfil == null ? PerfilBanco.LOGIN : perfil;
    }

    public static PerfilBanco getPerfil() {
        return perfilAtual;
    }

  protected Connection con;// criação do objeto conexao
    protected PreparedStatement pst;//criação do objeto preparação de ambiente
    protected ResultSet rs;// criação do objeto que recebe resultados
   
    public void abrirBanco() throws SQLException {// criando metodo para acessar o banco
      try {
        Class.forName("com.mysql.jdbc.Driver");//classe para utilização do arquivo com configurções do serivdor mysql
        String url = "jdbc:mysql://localhost:3307/restaurante";// drive servidor e banco de dados a serem utilizados e indicação do banco a ser utilizado
        String user = perfilAtual.getUsuario();// conta MySQL do usuário logado (ver Sessao)
        String senha = perfilAtual.getSenha();// senha da conta MySQL do perfil
        con=(Connection) DriverManager.getConnection(url,user,senha);//metodo que usa os parametros para conectar com o banco
         System.out.println("Conectado ao banco de dados ");
      } catch (ClassNotFoundException ex) {//tratamento de erro de drive
          System.out.println("Classe não encontrada, adicione o driver nas bibliotecas.");
        Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch(SQLException e) {//tratamento de erro de SQL
          System.out.println(e);
          throw new RuntimeException(e);
       } 
     }    
    
    public void fecharBanco() throws Exception {//criando metodo que fecha a conexao com o banco
        try {
            if (pst != null) { //limpando os dados de conexao
                pst.close();//fechando o ambiente de conexao
                System.out.println("Execuçao da Query fechada\n");
            }
        } finally {
            if (con != null) {
                con.close();//fecha a conexao com o banco, nao so a query
            }
            pst = null;
            con = null;
        }
    }
}


package br.gov.caixa.negocio;

import br.gov.caixa.clientes.Cliente;
import br.gov.caixa.clientes.Usuario;

public interface IGerenciadorClientes {
    public Usuario cadastrarUsuario(String cpf_cnpj, String nome, Cliente.Tipo classificacao);
    public Usuario getUsuario(long id);
}

package br.gov.caixa.negocio;

import br.gov.caixa.clientes.Cliente;
import br.gov.caixa.clientes.Usuario;

import java.util.ArrayList;

public class GerenciadorClientes implements IGerenciadorClientes {
    private final ArrayList<Usuario> clientes = new ArrayList<>();

    @Override
    public Usuario cadastrarUsuario(String cpf_cnpj, String nome, Usuario.Tipo classificacao) {
        Usuario novo = new Cliente(cpf_cnpj, nome, classificacao);
        this.clientes.add(novo);
        return novo;
    }

    //Procura um cliente pelo CPF/CNPJ
    public Usuario getUsuario(long id) {
        for (Usuario cliente : this.clientes) {
            if (cliente.getId() == id) {
                return cliente;
            }
        }
        return null;
    }
}

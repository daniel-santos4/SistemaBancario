package br.gov.caixa.negocio;

import br.gov.caixa.clientes.Usuario;
import br.gov.caixa.contas.*;

public interface IGerenciadorContas {
    public ContaPoupanca abrirContaCorrente(long idCliente);
    public ContaPoupanca abrirContaPoupanca(long idCliente);
    public ContaInvestimento abrirContaInvestimento(long idTitular);
    public Conta getConta(Usuario titular, Banco.TipoConta tipo);
    public Conta getConta(long id, Banco.TipoConta tipo);
}

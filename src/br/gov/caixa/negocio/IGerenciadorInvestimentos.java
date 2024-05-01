package br.gov.caixa.negocio;

import br.gov.caixa.contas.Conta;

import java.util.List;

public interface IGerenciadorInvestimentos {
    public void creditarRendimentos(List<Conta> contasInvestimento);
}

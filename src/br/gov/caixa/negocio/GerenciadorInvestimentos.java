package br.gov.caixa.negocio;

import br.gov.caixa.clientes.Usuario;
import br.gov.caixa.contas.Conta;
import java.util.List;

public class GerenciadorInvestimentos implements IGerenciadorInvestimentos {
    public void creditarRendimentos(List<Conta> contasInvestimento) {
        for (Conta conta : contasInvestimento) {
            if (conta.getStatus() == Conta.Situacao.ATIVA) {
                Usuario titular = Banco.CAIXA.getUsuario(conta.getIdUsuario());
                if (titular != null) {
                    if (titular.getStatus() == Usuario.Situacao.ATIVO) {
                        if (titular.getClassificacao() == Usuario.Tipo.PF) {
                            conta.setSaldo(conta.getSaldo() * 1.01);
                        } else { // PJ
                            conta.setSaldo(conta.getSaldo() * 1.02);
                        }
                    }
                } else {
                    System.out.printf("Titular da conta %d não cadastrado!\n", conta.getId()); //TODO lançar exceção
                }
            }
        }
    }
}

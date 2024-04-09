package br.gov.caixa.operacoes;

import br.gov.caixa.Usuario;
import br.gov.caixa.contas.Conta;

import java.util.Date;

public class Consulta implements Operacao {
    private Conta conta;
    private Usuario titular;

    public Consulta(Conta conta, Usuario titular) {
        this.conta = conta;
        this.titular = titular;
    }

    @Override
    public boolean executar() {
        new Transacao(new Date(), Operacao.Tipo.CONSULTA_SALDO, 0.0, 0.0,
                this.titular, null,"");
        return false;
    }
}

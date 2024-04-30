package br.gov.caixa.operacoes;

import br.gov.caixa.Usuario;
import br.gov.caixa.contas.Conta;
import java.time.LocalDate;

public class Deposito implements Operacao {
    private Conta conta;
    private double valor;
    private Usuario titular;

    public Deposito(Conta conta, double valor, Usuario titular) {
        this.conta = conta;
        this.valor = valor;
        this.titular = titular;
    }

    @Override
    public boolean executar() {
        this.conta.setSaldo(this.conta.getSaldo() + this.valor);
        Transacao saque = new Transacao(LocalDate.now(), Tipo.DEPOSITO, this.valor, this.valor, this.titular,
                null, "");
        return true;
    }
}

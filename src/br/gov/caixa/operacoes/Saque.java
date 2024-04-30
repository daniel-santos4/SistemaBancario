package br.gov.caixa.operacoes;

import br.gov.caixa.Usuario;
import br.gov.caixa.contas.Conta;
import java.time.LocalDate;

public class Saque implements Operacao {
    private Conta conta;
    private double valor;
    private Usuario titular;

    public Saque(Conta conta, double valor, Usuario titular) {
        this.conta = conta;
        this.valor = valor;
        this.titular = titular;
    }

    @Override
    public boolean executar() {
        double valorReal = this.valor;
        if (this.titular.getClassificacao() == Usuario.Tipo.PJ) {
            valorReal = this.valor * 1.005;
        }
        this.conta.setSaldo(this.conta.getSaldo() - valorReal);
        Transacao saque = new Transacao(LocalDate.now(), Operacao.Tipo.SAQUE, this.valor, valorReal, this.titular,
                null, "");
        return false;
    }
}

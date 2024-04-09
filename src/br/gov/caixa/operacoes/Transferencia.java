package br.gov.caixa.operacoes;

import br.gov.caixa.Banco;
import br.gov.caixa.Usuario;
import br.gov.caixa.contas.Conta;
import br.gov.caixa.contas.ContaCorrente;

import java.util.Date;

public class Transferencia implements Operacao {
    private Conta contaOrigem;
    private Conta contaDestino;
    private double valor;
    private Usuario usuarioOrigem;
    private Usuario usuarioDestino;

    public Transferencia(Conta contaOrigem, Conta contaDestino, double valor, Usuario usuarioOrigem, Usuario usuarioDestino) {
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.valor = valor;
        this.usuarioOrigem = usuarioOrigem;
        this.usuarioDestino = usuarioDestino;
    }

    @Override
    public boolean executar() {
        double valorReal = this.valor;
        String observacao = contaDestino instanceof ContaCorrente ? "Crédito em Conta Corrente" : "";
        if (usuarioOrigem.getClassificacao() == Usuario.Tipo.PJ) {
            valorReal = this.valor * 1.005;
        }
        Transacao transferencia = new Transacao(new Date(), Operacao.Tipo.TRANSFERENCIA, this.valor, valorReal,
                this.usuarioOrigem, this.usuarioDestino, observacao);
        if (this.contaOrigem.debitar(transferencia)) {
            contaDestino.creditar(transferencia);
            return true;
        }
        return false;
    }
}

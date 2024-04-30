package br.gov.caixa.operacoes;

import br.gov.caixa.Usuario;
import br.gov.caixa.contas.Conta;
import java.time.LocalDate;


public class Investimento implements Operacao {
    private Conta contaOrigem;
    private Conta contaDestino;
    private double valor;
    private Usuario usuario;

    public Investimento(Conta contaOrigem, Conta contaDestino, double valor, Usuario usuario) {
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.valor = valor;
        this.usuario = usuario;
    }

    @Override
    public boolean executar() {
        Transacao transferencia = new Transacao(LocalDate.now(), Operacao.Tipo.TRANSFERENCIA, this.valor, this.valor,
                this.usuario, this.usuario, "");
        if (this.contaOrigem.debitar(transferencia)) {
            contaDestino.creditar(transferencia);
            return true;
        }
        return false;
    }
}

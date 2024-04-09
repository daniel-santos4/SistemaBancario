package br.gov.caixa.operacoes;

import br.gov.caixa.Usuario;

import java.util.Date;

public interface Operacao {
    public enum Tipo {SAQUE, DEPOSITO, TRANSFERENCIA, INVESTIMENTO, CONSULTA_SALDO}
    public record Transacao(
            Date data,
            Operacao.Tipo tipo,
            double valorPretendido,
            double valorReal,
            Usuario usuarioOrigem,
            Usuario usuarioDestino,
            String observacao
    ) { }
    boolean executar();
}

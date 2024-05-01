package br.gov.caixa.operacoes;

import br.gov.caixa.clientes.Usuario;
import java.time.LocalDate;

public interface Operacao {
    public enum Tipo {SAQUE, DEPOSITO, TRANSFERENCIA, INVESTIMENTO, CONSULTA_SALDO}
    public record Transacao(
            LocalDate data,
            Operacao.Tipo tipo,
            double valorPretendido,
            double valorReal,
            Usuario usuarioOrigem,
            Usuario usuarioDestino,
            String observacao
    ) { }
    boolean executar();
}

package br.gov.caixa;

import br.gov.caixa.contas.*;
import br.gov.caixa.operacoes.*;

import java.util.ArrayList;
import java.util.Calendar;

public class Banco {
    public static final Banco CAIXA = new Banco();
    public enum TipoConta {CORRENTE, POUPANCA, INVESTIMENTO}
    private final ArrayList<Usuario> clientes = new ArrayList<>();
    private final ArrayList<Conta> contasCorrentes = new ArrayList<>();
    private final ArrayList<Conta> contasPoupancas = new ArrayList<>();
    private final ArrayList<Conta> contasInvestimento = new ArrayList<>();

    public Banco() {
        new Thread(() -> {
            while (true) {
                Calendar hoje = Calendar.getInstance();
                if (hoje.get(Calendar.DAY_OF_MONTH) == 1) {
                    creditarRendimentos();
                    try {
                        synchronized (this) {
                            Banco.this.wait(28 * 24 * 3600 * 1000L); // espera 28 dias
                        }
                    } catch (InterruptedException iex) {
                        System.out.println(iex.getMessage());
                    }
                } else { // se não for o primeiro dia do mês
                    try {
                        synchronized (this) {
                            Banco.this.wait(24 * 3600 * 1000L); // espera um dia
                        }
                    } catch (InterruptedException iex) {
                        System.out.println(iex.getMessage());
                    }
                }
            }
        }).start();
    }

    public ContaCorrente cadastrarUsuario(String cpf_cnpj, String nome) {
        Usuario novo = new Cliente(cpf_cnpj, nome);
        this.clientes.add(novo);
        ContaCorrente conta = new ContaCorrente(contasCorrentes.size() + 1, novo.getId());
        contasCorrentes.add(conta);
        System.out.printf("Cliente cadastrado com sucesso! Conta corrente nº: %d\n",  conta.getId());
        return conta;
    }

    public ContaPoupanca abrirContaPoupanca(long idCliente) {
        Usuario titular = getUsuario(idCliente);
        if (titular != null) {
            if (titular.getStatus() == Usuario.Situacao.ATIVO) {
                if (titular.getClassificacao() == Usuario.Tipo.PF) {
                    ContaPoupanca poupanca = new ContaPoupanca(contasPoupancas.size() + 1, idCliente);
                    contasPoupancas.add(poupanca);
                    System.out.printf("Poupança nº %d cadastrada com sucesso!\n", poupanca.getId());
                    return poupanca;
                } else {
                    System.out.println("Poupança não disponível para pessoa jurídica");
                }
            } else {
                System.out.println("Cliente inativo.");
            }
        } else {
            System.out.println("CPF/CNPJ não cadastrado!");
        }
        return null;
    }

    public ContaInvestimento abrirContaInvestimento(long idTitular) {
        Usuario titular = getUsuario(idTitular);
        if (titular != null) {
            if (titular.getStatus() == Usuario.Situacao.ATIVO) {
                ContaInvestimento conta = new ContaInvestimento(contasInvestimento.size() + 1, idTitular);
                contasInvestimento.add(conta);
                System.out.printf("Conta Investimento nº %d cadastrada com sucesso!\n",  conta.getId());
                return conta;
            } else {
                System.out.println("Cliente inativo.");
            }
        } else {
            System.out.println("CPF/CNPJ não cadastrado!");
        }
        return null;
    }

    public void sacar(long idConta, TipoConta tipo, double valor) {
        Conta conta = getConta(idConta, tipo);
        if (conta != null) {
            if (conta.getStatus() == Conta.Situacao.ATIVA) {
                Usuario titular = getUsuario(conta.getIdUsuario());
                Saque saque = new Saque(conta, valor, titular);
                if (saque.executar()) {
                    System.out.println("Saque realizado com sucesso!");
                } else {
                    System.out.println("Saldo insuficiente!");
                }
            } else {
                System.out.println("Conta inativa!");
            }
        } else {
            System.out.println("Conta inválida!");
        }
    }

    public void depositar(long idConta, TipoConta tipo, double valor) {
        Conta conta = getConta(idConta, tipo);
        if (conta != null) {
            if (conta.getStatus() == Conta.Situacao.ATIVA) {
                Deposito deposito = new Deposito(conta, valor, getUsuario(conta.getIdUsuario()));
                deposito.executar();
                System.out.println("Depósito realizado com sucesso!");
            } else {
                System.out.println("Conta inativa!");
            }
        } else {
            System.out.println("Conta inválida!");
        }
    }

    public void transferir(long idContaOrigem, TipoConta tipoOrigem, long idContaDestino, TipoConta tipoDestino, double valor) {
        Conta contaOrigem = getConta(idContaOrigem, tipoOrigem);
        Conta contaDestino = getConta(idContaDestino, tipoDestino);

        if (contaOrigem != null && contaDestino != null) {
            if (contaOrigem.getStatus() == Conta.Situacao.ATIVA) {
                if (contaDestino.getStatus() == Conta.Situacao.ATIVA) {
                    Usuario usuarioOrigem = getUsuario(contaOrigem.getIdUsuario());
                    Usuario usuarioDestino = getUsuario(contaDestino.getIdUsuario());

                    if (usuarioDestino != null && usuarioDestino.getStatus() == Usuario.Situacao.ATIVO) {
                        Transferencia transferencia = new Transferencia(contaOrigem, contaDestino, valor, usuarioOrigem, usuarioDestino);
                        if (transferencia.executar()) {
                            System.out.println("Transferência realizada com sucesso!");
                        } else {
                            System.out.println("Saldo insuficiente!");
                        }
                    } else {
                        System.out.println("Cliente de destino inválido!");
                    }
                } else {
                    System.out.println("Conta destino inativa!");
                }
            } else {
                System.out.println("Conta origem inativa!");
            }
        } else {
            System.out.println("Conta inválida!");
        }
    }

    public void consultarSaldo(long id, TipoConta tipo) {
        Conta conta = getConta(id, tipo);
        if (conta != null) {
            if (conta.getStatus() == Conta.Situacao.ATIVA) {
                Consulta consulta = new Consulta(conta, getUsuario(conta.getIdUsuario()));
                consulta.executar();
                System.out.printf("Saldo: R$ %.2f\n", conta.getSaldo());
            } else {
                System.out.println("Conta inativa!");
            }
        } else {
            System.out.println("Conta inválida!");
        }
    }

    public ContaInvestimento investir(long id, double valor) {
        ContaCorrente contaOrigem = (ContaCorrente) getConta(id, TipoConta.CORRENTE);
        if (contaOrigem != null) {
            if (contaOrigem.getSaldo() >= valor) {
                if (contaOrigem.getStatus() == Conta.Situacao.ATIVA) {
                    Usuario usuario = getUsuario(contaOrigem.getIdUsuario());
                    ContaInvestimento contaDestino = (ContaInvestimento) getConta(usuario, TipoConta.INVESTIMENTO);
                    if (usuario.getStatus() == Usuario.Situacao.ATIVO) {
                        if (contaDestino == null) {
                            contaDestino = this.abrirContaInvestimento(usuario.getId());
                        }
                        if (contaDestino.getStatus() == Conta.Situacao.ATIVA) {
                            Investimento investimento = new Investimento(contaOrigem, contaDestino, valor, usuario);
                            if (investimento.executar()) {
                                System.out.println("Investimento realizada com sucesso!");
                            } else {
                                System.out.println("Não foi possível realizar a operação!");
                            }
                        } else {
                            System.out.println("Conta investimento inativa!");
                        }
                        return contaDestino;
                    } else {
                        System.out.println("Cliente inativo.");
                    }
                } else {
                    System.out.println("Conta corrente inativa!");
                }
            } else {
                System.out.println("Saldo insuficiente!");
            }
        } else {
            System.out.println("Conta inválida!");
        }
        return null;
    }

    // Procura a conta de um usuário pelo tipo
    public Conta getConta(Usuario titular, TipoConta tipo) {
        ArrayList<Conta> contas;
        if (tipo == TipoConta.CORRENTE) {
            contas = this.contasCorrentes;
        } else if (tipo == TipoConta.POUPANCA) {
            contas = this.contasPoupancas;
        } else {
            contas = this.contasInvestimento;
        }
        for (Conta conta : contas) {
            if (conta.getIdUsuario() == titular.getId()) {
                return conta;
            }
        }
        return null;
    }

    // Procura uma conta pelo número e pelo tipo
    public Conta getConta(long id, TipoConta tipo) {
        ArrayList<Conta> contas;
        if (tipo == TipoConta.CORRENTE) {
            contas = this.contasCorrentes;
        } else if (tipo == TipoConta.POUPANCA) {
            contas = this.contasPoupancas;
        } else {
            contas = this.contasInvestimento;
        }
        for (Conta conta : contas) {
            if (conta.getId() == id) {
                return conta;
            }
        }
        return null;
    }

    //Procura um cliente pelo CPF/CNPJ
    public Usuario getUsuario(long id) {
        for (Usuario cliente : this.clientes) {
            if (cliente.getId() == id) {
                return cliente;
            }
        }
        return null;
    }

    private void creditarRendimentos() {
        for (Conta conta : this.contasInvestimento) {
            if (conta.getStatus() == Conta.Situacao.ATIVA) {
                Usuario titular = getUsuario(conta.getIdUsuario());
                if (titular != null) {
                    if (titular.getStatus() == Usuario.Situacao.ATIVO) {
                        if (titular.getClassificacao() == Usuario.Tipo.PF) {
                            conta.setSaldo(conta.getSaldo() * 1.01);
                        } else { // PJ
                            conta.setSaldo(conta.getSaldo() * 1.02);
                        }
                    }
                } else {
                    System.out.printf("Titular da conta %d não cadastrado!\n", conta.getId());
                }
            }
        }
    }

    public static void main(String[] args) {
        ContaCorrente contaPF = Banco.CAIXA.cadastrarUsuario("123.456.789-00", "Fulano de Tal");
        ContaCorrente contaPJ = Banco.CAIXA.cadastrarUsuario("12.345.678/9000-00", "Sociedade Anônima S.A.");
        ContaPoupanca poupanca = Banco.CAIXA.abrirContaPoupanca(contaPF.getIdUsuario());
        Banco.CAIXA.depositar(contaPF.getId(), TipoConta.CORRENTE, 200.00);
        Banco.CAIXA.sacar(contaPF.getId(), TipoConta.CORRENTE, 100.00);
        Banco.CAIXA.depositar(poupanca.getId(), TipoConta.POUPANCA, 300.00);
        Banco.CAIXA.sacar(poupanca.getId(), TipoConta.POUPANCA, 250.00);
        Banco.CAIXA.transferir(contaPF.getId(), TipoConta.CORRENTE, poupanca.getId(), TipoConta.POUPANCA, 50.00);
        ContaInvestimento investimentoPF = Banco.CAIXA.investir(contaPF.getId(), 50.00);
        Banco.CAIXA.consultarSaldo(contaPF.getId(), TipoConta.CORRENTE);
        Banco.CAIXA.consultarSaldo(poupanca.getId(), TipoConta.POUPANCA);
        Banco.CAIXA.consultarSaldo(investimentoPF.getId(), TipoConta.INVESTIMENTO);
        Banco.CAIXA.depositar(contaPJ.getId(), TipoConta.CORRENTE, 10000.00);
        Banco.CAIXA.sacar(contaPJ.getId(), TipoConta.CORRENTE, 1000.00);
        Banco.CAIXA.consultarSaldo(contaPJ.getId(), TipoConta.CORRENTE);
        Banco.CAIXA.transferir(contaPJ.getId(), TipoConta.CORRENTE, contaPF.getId(), TipoConta.CORRENTE, 2000.00);
        Banco.CAIXA.consultarSaldo(contaPF.getId(), TipoConta.CORRENTE);
        Banco.CAIXA.consultarSaldo(contaPJ.getId(), TipoConta.CORRENTE);
        ContaInvestimento investimentoPJ = Banco.CAIXA.investir(contaPJ.getId(), 5000.00);
        Banco.CAIXA.investir(contaPJ.getId(), 1000.00);
        Banco.CAIXA.consultarSaldo(contaPJ.getId(), TipoConta.CORRENTE);
        Banco.CAIXA.consultarSaldo(investimentoPJ.getId(), TipoConta.INVESTIMENTO);
        investimentoPF.setStatus(Conta.Situacao.INATIVA);
        // Método sendo chamado aqui apenas pra teste. Deve ser chamado apenas na thread do construtor
        Banco.CAIXA.creditarRendimentos();
        Banco.CAIXA.consultarSaldo(investimentoPF.getId(), TipoConta.INVESTIMENTO);
        System.out.println(investimentoPF.getSaldo());
        Banco.CAIXA.consultarSaldo(investimentoPJ.getId(), TipoConta.INVESTIMENTO);
        for (Operacao.Transacao registro: contaPF.getHistorico()) {
            System.out.println(registro);
        }
    }
}

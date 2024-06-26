package br.gov.caixa.negocio;

import br.gov.caixa.clientes.Cliente;
import br.gov.caixa.clientes.Usuario;
import br.gov.caixa.contas.*;
import br.gov.caixa.operacoes.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

public class Banco {
    public static final Banco CAIXA = new Banco();
    public enum TipoConta {CORRENTE, POUPANCA, INVESTIMENTO}
    private IGerenciadorClientes gerenciadorClientes = new GerenciadorClientes();
    //TODO Criar a classe GerenciadorContas e classes de validação
    private final ArrayList<Conta> contasCorrentes = new ArrayList<>();
    private final ArrayList<Conta> contasPoupancas = new ArrayList<>();
    private final ArrayList<Conta> contasInvestimento = new ArrayList<>();

    public Banco() {
        new Thread(() -> {
            while (true) {
                Calendar hoje = Calendar.getInstance();
                IGerenciadorInvestimentos gerenciadorInvestimentos = new GerenciadorInvestimentos();
                if (hoje.get(Calendar.DAY_OF_MONTH) == 1) {
                    gerenciadorInvestimentos.creditarRendimentos(this.contasInvestimento);
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

    public ContaCorrente cadastrarUsuario(String cpf_cnpj, String nome, Cliente.Tipo classificacao) {
        Usuario cliente = this.gerenciadorClientes.cadastrarUsuario(cpf_cnpj, nome, classificacao);
        ContaCorrente conta = new ContaCorrente(contasCorrentes.size() + 1, cliente.getId());
        contasCorrentes.add(conta);
        System.out.printf("Cliente cadastrado com sucesso! Conta corrente nº: %d\n",  conta.getId());
        return conta;
    }

    public ContaPoupanca abrirContaPoupanca(long idCliente) {
        Usuario titular = this.gerenciadorClientes.getUsuario(idCliente);
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
        Usuario titular = this.gerenciadorClientes.getUsuario(idTitular);
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
                Usuario titular = this.gerenciadorClientes.getUsuario(conta.getIdUsuario());
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
                Deposito deposito = new Deposito(conta, valor, this.gerenciadorClientes.getUsuario(conta.getIdUsuario()));
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
                    Usuario usuarioOrigem = this.gerenciadorClientes.getUsuario(contaOrigem.getIdUsuario());
                    Usuario usuarioDestino = this.gerenciadorClientes.getUsuario(contaDestino.getIdUsuario());

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
                Consulta consulta = new Consulta(conta, this.gerenciadorClientes.getUsuario(conta.getIdUsuario()));
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
                    Usuario usuario = this.gerenciadorClientes.getUsuario(contaOrigem.getIdUsuario());
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

    public static void executarCampanhaMarketing(String caminhoCSV) {
        Path caminho = Path.of(caminhoCSV);
        try {
            Stream<String> linhas = Files.lines(caminho);
            List<String> lista = linhas.skip(1)
                    .map(linha -> linha.split(","))
                    .filter(colunas -> {
                        if ("2".equals(colunas[3])) {
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate nascimento = LocalDate.parse(colunas[1], dtf);
                            return ChronoUnit.YEARS.between(nascimento, LocalDate.now()) >= 18;
                        }
                        return true;
                    })
                    .map(colunas -> Banco.CAIXA.cadastrarUsuario(colunas[2], colunas[0],
                            "2".equals(colunas[3]) ? Usuario.Tipo.PF : Usuario.Tipo.PJ))
                    .peek(conta -> Banco.CAIXA.depositar(conta.getId(), TipoConta.CORRENTE, 50.0))
                    .map(conta-> Banco.CAIXA.getUsuario(conta.getIdUsuario()) + ";" +
                            conta.getId() + ";" + conta.getSaldo())
                    .toList();
            Path saida = Path.of("saida.csv");
            Files.write(saida, lista);
        } catch (IOException e) {
            System.out.println("Não foi possível abrir o arquivo " + caminhoCSV);
            e.printStackTrace();
        }
    }

    public Usuario getUsuario(long idUsuario) {
        return this.gerenciadorClientes.getUsuario(idUsuario);
    }

    public static void main(String[] args) {
        executarCampanhaMarketing("pessoas.csv");
    }
}

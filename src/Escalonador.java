import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Escalonador {


    public static void main(String[] args) {
        List<Processo> processos = LerArquivo();
        if (processos == null) {
            processos = new ArrayList<>();
            processos.add(new Processo(0,0,20));
            processos.add(new Processo(1,0,10));
            processos.add(new Processo(2,4,6));
            processos.add(new Processo(3,4,8));
            System.out.println("FCFS " + FCFS(new ArrayList<>(processos)));
            System.out.println("SJF " + SJF(new ArrayList<>(processos)));
            System.out.println("RR " + RR(new ArrayList<>(processos), 2));
            //System.out.println("Erro ao ler o arquivo.");
            return;
        }

        System.out.println("FCFS " + FCFS(new ArrayList<>(processos)));
        System.out.println("SJF " + SJF(new ArrayList<>(processos)));
        System.out.println("RR " + RR(new ArrayList<>(processos), 2));
    }

    private static List<Processo> LerArquivo() {
        // Inicializa uma lista para armazenar os processos
        List<Processo> processos = new ArrayList<>();

        // Abre o arquivo e ler o conteúdo usando um Scanner
        try (Scanner scanner = new Scanner(new File("input.txt"))) {
            int pid = 1; // Inicializa o identificador do processo

            // Lê os pares de números inteiros enquanto houver dados no arquivo
            while (scanner.hasNextInt()) {
                int tempoChegada = scanner.nextInt(); // Lê o tempo de chegada do processo
                int tempoExcucao = scanner.nextInt(); // Lê o tempo de burst do processo

                // Adiciona o processo à lista de processos
                processos.add(new Processo(pid++, tempoChegada, tempoExcucao));
            }
        } catch (FileNotFoundException e) {
            // Retorna nulo em caso de falha ao abrir o arquivo
            return null;
        }

        // Retorna a lista de processos preenchida
        return processos;
    }

    private static String FCFS(List<Processo> processos) {
        // Inicializa os vetores para armazenar os tempos de espera, retorno e resposta
        double[] tempoDeEspera = new double[processos.size()];
        double[] tempoDeRetorno = new double[processos.size()];
        double[] tempoDeResposta = new double[processos.size()];

        // Define os valores iniciais para o primeiro processo
        tempoDeEspera[0] = 0;
        tempoDeRetorno[0] = processos.get(0).tempoDeExcucao;
        tempoDeResposta[0] = 0;
        double tempoAtual = processos.get(0).tempoDeExcucao; // Define o tempo atual após a execução do primeiro processo

        // Itera pelos processos restantes
        for (int i = 1; i < processos.size(); i++) {
            // Calcula o tempo de espera, que é o tempo atual menos o tempo de chegada do processo
            tempoDeEspera[i] = Math.max(0, tempoAtual - processos.get(i).tempoDeChegada);

            // O tempo de resposta é igual ao tempo de espera no algoritmo FCFS
            tempoDeResposta[i] = tempoDeEspera[i];

            // Atualiza o tempo atual adicionando o tempo de burst do processo atual
            tempoAtual += processos.get(i).tempoDeExcucao;

            // Calcula o tempo de retorno, que é o tempo atual menos o tempo de chegada do processo
            tempoDeRetorno[i] = tempoAtual - processos.get(i).tempoDeChegada;
        }

        // Retorna a formatação das métricas calculadas
        return formatMetrics(tempoDeEspera, tempoDeRetorno, tempoDeResposta);
    }
    private static String SJF(List<Processo> processos) {
        // Ordena os processos pelo tempo de chegada
        processos.sort(Comparator.comparingInt(p -> p.tempoDeChegada));
        boolean[] completo = new boolean[processos.size()];
        double[] tempoDeEspera = new double[processos.size()];
        double[] tempoDeRetorno = new double[processos.size()];
        double[] tempoDeResposta = new double[processos.size()];

        int processosCompletos = 0;
        double tempoAtual = 0;

        // Executa enquanto todos os processos não forem concluídos
        while (processosCompletos < processos.size()) {
            int indiceProximoProcesso = -1;
            int tempoMinimoExcucao = Integer.MAX_VALUE;

            // Encontra o próximo processo com o menor tempo de burst e que ainda não foi concluído
            for (int i = 0; i < processos.size(); i++) {
                if (!completo[i] && processos.get(i).tempoDeChegada <= tempoAtual && processos.get(i).tempoDeExcucao < tempoMinimoExcucao) {
                    indiceProximoProcesso= i;
                    tempoMinimoExcucao = processos.get(i).tempoDeExcucao;
                }
            }

            // Se um processo válido foi encontrado
            if (indiceProximoProcesso != -1) {
                Processo processo = processos.get(indiceProximoProcesso);

                // Atualiza o tempo de espera, resposta e retorno do processo encontrado
                tempoDeEspera[indiceProximoProcesso] = tempoAtual - processo.tempoDeChegada;
                tempoDeResposta[indiceProximoProcesso] = tempoDeEspera[indiceProximoProcesso];
                tempoAtual += processo.tempoDeExcucao;
                tempoDeRetorno[indiceProximoProcesso] = tempoAtual - processo.tempoDeChegada;

                // Marca o processo como concluído
                completo[indiceProximoProcesso] = true;
                processosCompletos++;
            } else {
                // Se nenhum processo válido foi encontrado, avança o tempo atual
                tempoAtual++;
            }
        }

        return formatMetrics(tempoDeEspera, tempoDeRetorno, tempoDeResposta);
    }

    public static String RR(List<Processo> processos, int quantum) {
        // Ordena os processos pelo tempo de chegada
        processos.sort(Comparator.comparingInt(p -> p.tempoDeChegada));

        // Inicializa variáveis importantes
        int n = processos.size();  // Número de processos
        int[] tempoDeExecucaoQueFalta = new int[n];  // Vetor para armazenar os tempos de burst restantes de cada processo
        double[] tempoDeEspera = new double[n];  // Vetor para armazenar os tempos de espera de cada processo
        double[] tempoDeRetorno = new double[n];  // Vetor para armazenar os tempos de retorno de cada processo
        double[] tempoDeResposta = new double[n];  // Vetor para armazenar os tempos de resposta de cada processo
        boolean[] respostas = new boolean[n];  // Vetor para armazenar se cada processo já recebeu uma resposta

        // Inicializa o vetor de tempos de burst restantes com os tempos de burst dos processos
        for (int i = 0; i < n; i++) {
            tempoDeExecucaoQueFalta[i] = processos.get(i).tempoDeExcucao;
        }

        int processosConcluidos = 0;  // Contador de processos concluídos
        double tempoAtual = 0;  // Tempo atual

        // Executa enquanto todos os processos não forem concluídos
        while (processosConcluidos < n) {
            boolean progressMade = false;  // Indica se algum progresso foi feito durante a iteração atual

            // Percorre todos os processos
            for (int i = 0; i < n; i++) {
                // Verifica se o processo ainda não foi concluído e já chegou
                if (tempoDeExecucaoQueFalta[i] > 0 && processos.get(i).tempoDeChegada <= tempoAtual) {
                    progressMade = true;

                    // Atualiza o tempo de resposta, se ainda não foi respondido
                    if (!respostas[i]) {
                        tempoDeResposta[i] = tempoAtual - processos.get(i).tempoDeChegada;
                        respostas[i] = true;
                    }

                    // Calcula quanto tempo o processo será executado neste ciclo (até o quantum ou até terminar)
                    int executionTime = Math.min(quantum, tempoDeExecucaoQueFalta[i]);

                    // Atualiza o tempo de burst restante e o tempo atual
                    tempoDeExecucaoQueFalta[i] -= executionTime;
                    tempoAtual += executionTime;

                    // Se o processo foi concluído, atualiza o tempo de retorno e incrementa o contador de processos concluídos
                    if (tempoDeExecucaoQueFalta[i] == 0) {
                        tempoDeRetorno[i] = tempoAtual - processos.get(i).tempoDeChegada;
                        tempoDeEspera[i] = tempoDeRetorno[i] - processos.get(i).tempoDeExcucao;
                        processosConcluidos++;
                    }

                    // Atualize o tempo de espera para os outros processos que já chegaram e ainda não foram concluídos
                    for (int j = 0; j < n; j++) {
                        if (i != j && tempoDeExecucaoQueFalta[j] > 0 && processos.get(j).tempoDeChegada <= processosConcluidos) {
                            tempoDeEspera[j] += executionTime;
                        }
                    }
                }
            }

            // Se nenhum progresso foi feito, avança o tempo atual para a próxima chegada do processo
            if (!progressMade) {
                // Avança o tempo atual para a próxima chegada do processo
                double nextArrivalTime = Double.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (processos.get(i).tempoDeChegada > processosConcluidos && processos.get(i).tempoDeChegada < nextArrivalTime) {
                        nextArrivalTime = processos.get(i).tempoDeChegada;
                    }
                }
                tempoAtual = nextArrivalTime;
            }
        }

        return formatMetrics(tempoDeEspera, tempoDeRetorno, tempoDeResposta);
    }

    private static String formatMetrics(double[] tempoDeEspera, double[] tempoDeRetorno, double[] tempoDeResposta) {
        int n = tempoDeEspera.length;
        double tempoMedioEspera = Arrays.stream(tempoDeEspera).sum() / n;
        double tempoMedioRetorno = Arrays.stream(tempoDeRetorno).sum()/ n;
        double tempoMedioResposta = Arrays.stream(tempoDeResposta).sum()/ n;

        return String.format("%.1f %.1f %.1f", tempoMedioRetorno, tempoMedioResposta, tempoMedioEspera);
    }
}

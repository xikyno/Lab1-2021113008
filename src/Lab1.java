import java.io.*;
import java.util.*;

public class Lab1 {
    private static Map<String, Map<String, Integer>> graph = new HashMap<>();

    public static void main(String[] args) {
        if (args.length > 0) {
            String filePath = args[0];
            readFileAndCreateGraph(filePath);
        } else {
            System.out.println("Please provide a file path.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Choose an option:");
            System.out.println("1. Show directed graph");
            System.out.println("2. Query bridge words");
            System.out.println("3. Generate new text");
            System.out.println("4. Calculate shortest path");
            System.out.println("5. Random walk");
            System.out.println("6. Generate and visualize graph");
            System.out.println("0. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            switch (choice) {
                case 1:
                    showDirectedGraph();
                    break;
                case 2:
                    System.out.print("Enter two words: ");
                    String word1 = scanner.next();
                    String word2 = scanner.next();
                    System.out.println(queryBridgeWords(word1, word2));
                    break;
                case 3:
                    System.out.print("Enter a new text: ");
                    String inputText = scanner.nextLine();
                    System.out.println(generateNewText(inputText));
                    break;
                case 4:
                    System.out.print("Enter two words: ");
                    word1 = scanner.next();
                    word2 = scanner.next();
                    System.out.println(calcShortestPath(word1, word2));
                    break;
                case 5:
                    randomWalk();
                    break;
                case 6:
                    System.out.print("Enter output image file name (e.g., graph.png): ");
                    String fileName = scanner.next();
                    generateAndVisualizeGraph(fileName, null);
                    System.out.println("Graph image generated: " + fileName);
                    break;
                case 0:
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void readFileAndCreateGraph(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String prevWord = null;
            while ((line = reader.readLine()) != null) {
                line = line.toLowerCase().replaceAll("[^a-z\\s]", " ");
                String[] words = line.split("\\s+");
                for (String word : words) {
                    if (word.isEmpty()) continue;
                    if (!graph.containsKey(word)) {
                        graph.put(word, new HashMap<>());
                    }
                    if (prevWord != null) {
                        Map<String, Integer> edges = graph.get(prevWord);
                        edges.put(word, edges.getOrDefault(word, 0) + 1);
                    }
                    prevWord = word;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showDirectedGraph() {
        for (String node : graph.keySet()) {
            System.out.print(node + " -> ");
            Map<String, Integer> edges = graph.get(node);
            for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                System.out.print(edge.getKey() + "(" + edge.getValue() + ") ");
            }
            System.out.println();
        }
    }

    private static String queryBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }
        Set<String> bridgeWords = new HashSet<>();
        for (String intermediate : graph.get(word1).keySet()) {
            if (graph.get(intermediate).containsKey(word2)) {
                bridgeWords.add(intermediate);
            }
        }
        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        }
        return "The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", bridgeWords);
    }

    private static String generateNewText(String inputText) {
        String[] words = inputText.toLowerCase().split("\\s+");
        StringBuilder newText = new StringBuilder();
        for (int i = 0; i < words.length - 1; i++) {
            newText.append(words[i]).append(" ");
            Set<String> bridgeWords = new HashSet<>();
            if (graph.containsKey(words[i])) {
                for (String intermediate : graph.get(words[i]).keySet()) {
                    if (graph.get(intermediate).containsKey(words[i + 1])) {
                        bridgeWords.add(intermediate);
                    }
                }
            }
            if (!bridgeWords.isEmpty()) {
                List<String> bridgeWordsList = new ArrayList<>(bridgeWords);
                String bridgeWord = bridgeWordsList.get(new Random().nextInt(bridgeWordsList.size()));
                newText.append(bridgeWord).append(" ");
            }
        }
        newText.append(words[words.length - 1]);
        return newText.toString();
    }

    private static String calcShortestPath(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(distances::get));
        for (String node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(word1, 0);
        pq.add(word1);

        while (!pq.isEmpty()) {
            String current = pq.poll();
            if (current.equals(word2)) break;  // Early exit if the target is reached
            for (Map.Entry<String, Integer> neighbor : graph.get(current).entrySet()) {
                int newDist = distances.get(current) + neighbor.getValue();
                if (newDist < distances.get(neighbor.getKey())) {
                    distances.put(neighbor.getKey(), newDist);
                    previous.put(neighbor.getKey(), current);
                    pq.add(neighbor.getKey());
                }
            }
        }

        if (distances.get(word2) == Integer.MAX_VALUE) {
            return word2 + " is not reachable from " + word1;
        }

        List<String> path = new ArrayList<>();
        for (String at = word2; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);

        String outputFileName = "graph_with_path.png";
        generateAndVisualizeGraph(outputFileName, path);
        return "Shortest path: " + String.join(" -> ", path) + " (length: " + distances.get(word2) + ")\nGraph image generated: " + outputFileName;
    }

    private static void randomWalk() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("output.txt"))) {
            List<String> nodes = new ArrayList<>(graph.keySet());
            String current = nodes.get(new Random().nextInt(nodes.size()));
            writer.print(current);

            Set<String> visitedEdges = new HashSet<>();

            while (true) {
                Map<String, Integer> neighbors = graph.get(current);
                if (neighbors.isEmpty()) break;

                List<String> edges = new ArrayList<>(neighbors.keySet());
                String next = edges.get(new Random().nextInt(edges.size()));
                String edge = current + "->" + next;

                if (visitedEdges.contains(edge)) break;
                visitedEdges.add(edge);

                writer.print(" " + next);
                current = next;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateDotFile(String fileName, List<String> path) {
        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            writer.println("digraph G {");
            for (String node : graph.keySet()) {
                Map<String, Integer> edges = graph.get(node);
                for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                    if (path != null && path.contains(node) && path.contains(edge.getKey()) && path.indexOf(edge.getKey()) == path.indexOf(node) + 1) {
                        writer.printf("    \"%s\" -> \"%s\" [label=\"%d\", color=\"red\", penwidth=2.0];\n", node, edge.getKey(), edge.getValue());
                    } else {
                        writer.printf("    \"%s\" -> \"%s\" [label=\"%d\"];\n", node, edge.getKey(), edge.getValue());
                    }
                }
            }
            writer.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateAndVisualizeGraph(String outputFileName, List<String> path) {
        String dotFileName = "graph.dot";
        generateDotFile(dotFileName, path);

        try {
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", dotFileName, "-o", outputFileName);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

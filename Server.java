import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
  private static final int PORT = 12345;
  private static final int ROUND_TIME = 60;
  private static final int SIZE = 10;

  private static String[] words = new String[SIZE];
  private static String[][] board = new String[SIZE][SIZE];
  private static Map<String, Integer> scores = new ConcurrentHashMap<>();
  private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();

  public static void main(String[] args) throws Exception {
    loadWords("words.txt");
    initBoard();

    ServerSocket serverSocket = new ServerSocket(PORT, SIZE); // Nombre de joueurs maximal = 10
    System.out.println("Server running on port " + PORT + " (max 10 players)");

    // Thread pour accepter les clients
    new Thread(() -> {
      while (true) {
        try {
          if (clients.size() >= SIZE) {
            System.out.println("Max players reached (10). Refusing new connections.");
            Socket reject = serverSocket.accept();
            PrintWriter tmp = new PrintWriter(reject.getOutputStream(), true);
            tmp.println("Server full (10 players max). Connection closed.");
            reject.close();
            continue;
          }
          Socket socket = serverSocket.accept();
          ClientHandler handler = new ClientHandler(socket);
          clients.add(handler);
          handler.start();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
    // ATtendre 2 joueurs en minmum puis demarer le jeux en 10sec
    while (clients.size() < 2) {
      System.out.println("Waiting for at least 2 players to join...");
      Thread.sleep(1000);
    }
    broadcast("At least 2 players joined. Game will start in 10 seconds!");
    Thread.sleep(10_000);

    // loop de jeux
    for (int round = 0; round < SIZE; round++) {
      String answer = words[round];
      broadcast("\n=== Round " + (round + 1) + " ===");
      broadcast("Row " + (round + 1) + ": " + "_".repeat(answer.length()));
      broadcast("Each player has ONE attempt to guess!");

      boolean guessedBySomeone = false;
      long start = System.currentTimeMillis();

      // reset des attentatives des clients dans chaque manche
      for (ClientHandler ch : clients) {
        ch.resetAttempt();
      }
      // commencer un compteur et prendere les attentatives des clients
      while (System.currentTimeMillis() - start < ROUND_TIME * 1000) {
        boolean allTried = true;

        for (ClientHandler ch : clients) {
          String guess = ch.getGuess();
          if (guess != null && !ch.hasGuessed) {
            ch.hasGuessed = true;
            if (guess.equalsIgnoreCase(answer)) {
              broadcast(ch.name + " guessed correctly!");
              scores.put(ch.name, scores.getOrDefault(ch.name, 0) + 1);
              guessedBySomeone = true;
            } else {
              ch.send("Wrong guess! You cannot guess again this round.");
            }
          }
          if (!ch.hasGuessed) {
            allTried = false;
          }
        }

        if (allTried)
          break;
        Thread.sleep(200);
      }

      // Resultat de manche
      if (!guessedBySomeone) {
        broadcast("No winner this round. The correct word was: " + answer);
      } else {
        broadcast("The correct word was: " + answer);
      }

      // AFficher la matrice
      for (int j = 0; j < SIZE; j++) {
        board[round][j] = String.valueOf(answer.charAt(j));
      }
      printBoard();
      printScores();
    }

    broadcast("Game finished! Final scores:");
    printScores();
    System.exit(0);
  }

  // lire words.txt
  private static void loadWords(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename));
    for (int i = 0; i < SIZE; i++) {
      words[i] = br.readLine().trim();
    }
    br.close();
  }

  private static void initBoard() {
    for (int i = 0; i < SIZE; i++) {
      Arrays.fill(board[i], "_");
    }
  }

  private static void broadcast(String msg) {
    System.out.println(msg);
    for (ClientHandler ch : clients) {
      ch.send(msg);
    }
  }

  private static void printScores() {
    broadcast("Scores:");
    for (Map.Entry<String, Integer> e : scores.entrySet()) {
      broadcast(e.getKey() + " : " + e.getValue());
    }
  }

  private static void printBoard() {
    StringBuilder sb = new StringBuilder();
    sb.append("\nCurrent Board:\n");
    for (int i = 0; i < SIZE; i++) {
      for (int j = 0; j < SIZE; j++) {
        sb.append(board[i][j]).append(" ");
      }
      sb.append("\n");
    }
    broadcast(sb.toString());
  }

  // class pour controller logic de client dans le serveur
  static class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    public String name;
    private BlockingQueue<String> guesses = new LinkedBlockingQueue<>();
    public boolean hasGuessed = false;

    ClientHandler(Socket socket) throws IOException {
      this.socket = socket;
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void run() {
      try {
        out.println("Enter your name: ");
        name = in.readLine();
        if (name == null || name.isEmpty())
          name = "Player" + (clients.size());
        scores.putIfAbsent(name, 0);
        broadcast(name + " joined the game!");

        String line;
        while ((line = in.readLine()) != null) {
          guesses.offer(line.trim());
        }
      } catch (IOException e) {
        System.out.println("Client disconnected: " + name);
      } finally {
        try {
          socket.close();
        } catch (IOException ignored) {
        }
      }
    }

    public String getGuess() {
      return guesses.poll();
    }

    public void send(String msg) {
      out.println(msg);
    }

    public void resetAttempt() {
      hasGuessed = false;
    }
  }
}

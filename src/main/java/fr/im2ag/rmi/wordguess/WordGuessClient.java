package fr.im2ag.rmi.wordguess;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class WordGuessClient {
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Usage: java ... WordGuessClient <pseudo>");
      return;
    }
    String pseudo = args[0];
    int port = 1234;

    Registry registry = LocateRegistry.getRegistry("localhost", port);
    WordGuess game = (WordGuess) registry.lookup("WordGuess");
    game.registerPlayer(pseudo);
    System.out.println("[Client] Connecté en tant que " + pseudo);

    Scanner sc = new Scanner(System.in);

    int lastRowSeen = -2;
    while (!game.isGameOver()) {
      int row = game.getCurrentRow();
      if (row == -1)
        break;

      if (row != lastRowSeen) {
        System.out.println("\n=== MANCHE " + (row + 1) + "/10 ===");
        System.out.println("Indice: " + game.getHintForRow(row));
        printMatrix(game.getMatrix());
        printScores(game.getScores());
        lastRowSeen = row;

        System.out.print("Votre proposition pour la ligne active: ");
        String guess = sc.nextLine().trim();
        boolean ok = game.submitWord(pseudo, guess);
        System.out.println(ok ? "✔️ Correct!" : "❌ Incorrect (ou déjà tenté)");
      }

      Thread.sleep(1000);

      if (game.getCurrentRow() != row) {
        printMatrix(game.getMatrix());
        printScores(game.getScores());
      }
    }

    System.out.println("\n=== FIN DE PARTIE ===");
    printMatrix(game.getMatrix());
    printScores(game.getScores());
    System.out.println("Message: END");
    sc.close();
  }

  private static void printMatrix(List<String> matrix) {
    System.out.println("Matrice:");
    for (String line : matrix)
      System.out.println("  " + line);
  }

  private static void printScores(Map<String, Integer> scores) {
    System.out.println("Scores:");
    scores.forEach((p, s) -> System.out.println("  " + p + " : " + s));
  }
}

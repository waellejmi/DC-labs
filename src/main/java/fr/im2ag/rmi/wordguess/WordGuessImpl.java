package fr.im2ag.rmi.wordguess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implémentation simple: matrice PARTAGÉE 10x10.
 * - currentRow s'incrémente de 0 à 9.
 * - Chaque manche dure ROUND_MS, tout le monde a 1 tentative.
 * - Si au moins une réponse correcte arrive => on marque la ligne "mot + (C)".
 * - Sinon => à la fin du temps on révèle la ligne "mot + (S)".
 * - Scores: +3 pour une bonne réponse sur la manche (unique).
 */
public class WordGuessImpl extends UnicastRemoteObject implements WordGuess, Runnable {
  private static final int N = 10;
  private static final long ROUND_MS = 15_000;

  private final String[] words = new String[N];
  private final String[] hints = new String[N];
  private final List<String> matrix = new ArrayList<>(N);

  private final Map<String, Integer> scores = new ConcurrentHashMap<>();
  private final Set<String> attemptedThisRound = ConcurrentHashMap.newKeySet();
  private int currentRow = 0;
  private boolean rowSolved = false;

  private final Object roundLock = new Object();

  public WordGuessImpl(String wordsFile, String hintsFile) throws RemoteException {
    super();
    load(wordsFile, hintsFile);
    for (int i = 0; i < N; i++) {
      matrix.add("_".repeat(Math.max(1, words[i].length())));
    }

    new Thread(this, "WordGuess-RoundThread").start();
  }

  private void load(String wf, String hf) {
    try (BufferedReader bw = new BufferedReader(new FileReader(wf));
        BufferedReader bh = new BufferedReader(new FileReader(hf))) {
      for (int i = 0; i < N; i++) {
        words[i] = Optional.ofNullable(bw.readLine()).orElse("").trim().toLowerCase();
        hints[i] = Optional.ofNullable(bh.readLine()).orElse("").trim();
      }
    } catch (Exception e) {
      throw new RuntimeException("Impossible de charger words/indices: " + e.getMessage(), e);
    }
  }

  @Override
  public void registerPlayer(String pseudo) {
    scores.putIfAbsent(pseudo, 0);
  }

  @Override
  public synchronized int getCurrentRow() {
    return (currentRow >= N) ? -1 : currentRow;
  }

  @Override
  public synchronized String getHintForRow(int row) {
    if (row < 0 || row >= N)
      return "";
    return hints[row];
  }

  @Override
  public synchronized List<String> getMatrix() {
    return new ArrayList<>(matrix);
  }

  @Override
  public Map<String, Integer> getScores() {
    return new TreeMap<>(scores);
  }

  @Override
  public synchronized boolean submitWord(String pseudo, String guess) throws RemoteException {
    if (currentRow >= N)
      return false;
    if (attemptedThisRound.contains(pseudo))
      return false;
    attemptedThisRound.add(pseudo);

    String g = guess == null ? "" : guess.trim().toLowerCase();
    boolean ok = g.equals(words[currentRow]);
    if (ok && !rowSolved) {
      rowSolved = true;
      scores.merge(pseudo, 3, Integer::sum);
      matrix.set(currentRow, words[currentRow] + " (C)");
      System.out.println("[Row " + currentRow + "] Trouvé par " + pseudo + " -> " + words[currentRow]);

      synchronized (roundLock) {
        roundLock.notifyAll();
      }
    }
    return ok;
  }

  @Override
  public synchronized boolean isGameOver() {
    return currentRow >= N;
  }

  @Override
  public void run() {
    while (true) {
      int row;
      synchronized (this) {
        row = currentRow;
        if (row >= N)
          break;

        attemptedThisRound.clear();
        rowSolved = false;
        matrix.set(row, revealPartial(words[row]));
        System.out.println("[Row " + row + "] Indice: " + hints[row] + " | Mot partiel: " + matrix.get(row));
      }

      long end = System.currentTimeMillis() + ROUND_MS;
      synchronized (roundLock) {
        while (System.currentTimeMillis() < end && !rowSolved) {
          long remain = end - System.currentTimeMillis();
          try {
            roundLock.wait(Math.max(1, remain));
          } catch (InterruptedException ignored) {
          }
        }
      }

      synchronized (this) {
        if (!rowSolved) {

          matrix.set(row, words[row] + " (S)");
          System.out.println("[Row " + row + "] Temps écoulé. Solution: " + words[row]);
        }
        currentRow++;
      }
    }
    System.out.println("[GAME] Terminé. Envoyez END aux clients.");
  }

  private static String revealPartial(String word) {
    if (word.length() <= 2)
      return word;
    char first = word.charAt(0);
    char last = word.charAt(word.length() - 1);
    return first + "_".repeat(word.length() - 2) + last;
  }
}

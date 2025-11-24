package fr.im2ag.rmi.wordguess;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface WordGuess extends Remote {

  void registerPlayer(String pseudo) throws RemoteException;

  int getCurrentRow() throws RemoteException;

  String getHintForRow(int row) throws RemoteException;

  List<String> getMatrix() throws RemoteException;

  Map<String, Integer> getScores() throws RemoteException;

  boolean submitWord(String pseudo, String guess) throws RemoteException;

  boolean isGameOver() throws RemoteException;
}

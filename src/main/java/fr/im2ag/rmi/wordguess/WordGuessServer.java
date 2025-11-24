package fr.im2ag.rmi.wordguess;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class WordGuessServer {
    public static void main(String[] args) throws Exception {
        int port = 1234;
        String words = (args.length > 0) ? args[0] : "words.txt";
        String hints = (args.length > 1) ? args[1] : "indices.txt";

        LocateRegistry.createRegistry(port);
        WordGuess service = new WordGuessImpl(words, hints);
        Registry registry = LocateRegistry.getRegistry("localhost", port);
        registry.rebind("WordGuess", service);
        System.out.println("[WordGuessServer] Service 'WordGuess' prêt sur " + port);
        Object lock = new Object();
        synchronized (lock) { lock.wait(); }
    }
}

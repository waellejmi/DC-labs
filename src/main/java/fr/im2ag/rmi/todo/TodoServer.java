package fr.im2ag.rmi.todo;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TodoServer {
  public static void main(String[] args) throws Exception {
    int port = 1234;
    LocateRegistry.createRegistry(port);
    TodoList service = new TodoListImpl();
    Registry registry = LocateRegistry.getRegistry("localhost", port);
    registry.rebind("MyTodoList", service);
    System.out.println("[TodoServer] Service 'MaTodoList' enregistré sur le port " + port);
    System.out.println("[TodoServer] Ctrl+C pour arrêter.");
    Object lock = new Object();
    synchronized (lock) {
      lock.wait();
    }
  }
}

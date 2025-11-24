package fr.im2ag.rmi.todo;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class TodoClient {
  public static void main(String[] args) throws Exception {
    int port = 1234;
    Registry registry = LocateRegistry.getRegistry("localhost", port);
    TodoList todo = (TodoList) registry.lookup("MyTodoList");
    System.out.println("[Client] Stub reçu: " + todo);

    todo.addTask("Installer Linux");
    todo.addTask("Supprimer windows");
    todo.addTask("Supprimer windows");
    todo.removeTask("Supprimer windows");

    List<String> tasks = todo.getTasks();
    System.out.println("Liste des tâches = " + tasks);
  }
}

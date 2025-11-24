package fr.im2ag.rmi.todo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class TodoListImpl extends UnicastRemoteObject implements TodoList {
    private final List<String> tasks;

    public TodoListImpl() throws RemoteException {
        this.tasks = new ArrayList<>();
    }

    @Override
    public synchronized void addTask(String task) throws RemoteException {
        tasks.add(task);
        System.out.println("[Todo] Ajout: " + task);
    }

    @Override
    public synchronized void removeTask(String task) throws RemoteException {
        if (tasks.remove(task)) {
            System.out.println("[Todo] Suppression: " + task);
        } else {
            System.out.println("[Todo] Introuvable: " + task);
        }
    }

    @Override
    public synchronized List<String> getTasks() throws RemoteException {
        System.out.println("[Todo] Consultation (" + tasks.size() + " tâches)");
        return new ArrayList<>(tasks);
    }
}

package fr.im2ag.rmi.todo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TodoList extends Remote {
    void addTask(String task) throws RemoteException;
    void removeTask(String task) throws RemoteException;
    List<String> getTasks() throws RemoteException;
}

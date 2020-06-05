package ru.ifmo.rain.vlasova.bank;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Account extends Remote, Serializable {
    String getId() throws RemoteException;

    int getAmount() throws RemoteException;

    void changeAmount(int amount) throws RemoteException;
}
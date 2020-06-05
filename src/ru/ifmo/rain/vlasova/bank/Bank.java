package ru.ifmo.rain.vlasova.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Bank extends Remote {
    boolean createAccountIfAbsent(String passport, String id) throws RemoteException;

    Account getAccount(String id) throws RemoteException;

    Person getPerson(String passport, boolean isRemote) throws RemoteException;

    void createPersonIfAbsent(String passport, String name, String surname) throws RemoteException;

    List<String> getAccountsByPassport(String passport) throws RemoteException;

    void createAccountByPersonAndId(Person person, String id, int amount) throws RemoteException;


}
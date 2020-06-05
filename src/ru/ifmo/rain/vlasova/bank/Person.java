package ru.ifmo.rain.vlasova.bank;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote, Serializable {
    String getName() throws RemoteException;

    String getSurname() throws RemoteException;

    String getPassport() throws RemoteException;

//    LocalPerson getLocalPerson() throws RemoteException;

}

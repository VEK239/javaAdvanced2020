package ru.ifmo.rain.vlasova.bank;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    public static void main(String[] args) {
        try {
            final RemoteBank bank = new RemoteBank(8080);
            try {
                Registry r = LocateRegistry.createRegistry(8080);
                UnicastRemoteObject.exportObject(bank, 8080);
                r.rebind("//localhost/bank", bank);
            } catch (AccessException e) {
                System.err.println("Could not rebind localhost 8080");
                e.printStackTrace();
            }
        } catch (final RemoteException e) {
            System.err.println("Could not export object: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Server started");
    }
}

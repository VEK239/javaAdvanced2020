package ru.ifmo.rain.vlasova.bank;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        try {
            final Bank bank;
            Registry registry = LocateRegistry.getRegistry(8080);
            bank = (Bank) registry.lookup("//localhost/bank");

            if (args != null && args.length == 5) {
                String name = args[0], surname = args[1], passport = args[2], accountId = args[3];
                int amount = Integer.parseInt(args[4]);

                bank.createPersonIfAbsent(passport, name, surname);
                Person person = bank.getPerson(passport, true);
                System.out.println("Person's surname: " + person.getSurname());
                System.out.println("Person's name: " + person.getName());
                System.out.println("Person's passport: " + person.getPassport());

                if (bank.createAccountIfAbsent(passport, accountId)) {
                    Account account = bank.getAccount(passport + ":" + accountId);
                    if (account != null) {
                        System.out.println("Account id: " + account.getId());
                        System.out.println("Amount: " + account.getAmount());
                        System.out.println("Adding " + amount);
                        account.changeAmount(amount);
                        System.out.println("After adding: " + account.getAmount());
                    } else {
                        System.err.println("Could not get account!");
                    }
                } else {
                    System.err.println("Could not create account!");
                }
            } else {
                System.err.println("Expected name surname passport accountId amount");
            }
        } catch (final NotBoundException | RemoteException e) {
            System.err.println("Could not find server URL");
            System.err.println(e.getMessage());
        }
    }
}

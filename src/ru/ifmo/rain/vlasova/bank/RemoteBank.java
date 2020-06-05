package ru.ifmo.rain.vlasova.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts;
    private final ConcurrentMap<String, Person> people;
    private final ConcurrentMap<String, List<String>> accountsByPassport;

    public RemoteBank(final int port) {
        accounts = new ConcurrentHashMap<>();
        people = new ConcurrentHashMap<>();
        accountsByPassport = new ConcurrentHashMap<>();
        this.port = port;
    }

    public boolean createAccountIfAbsent(final String passport, final String id) throws RemoteException {
        if (accounts.get(passport + ":" + id) == null) {
            Person person = getPerson(passport, true);
            if (person == null) {
                System.err.println("No person with passport " + passport);
                return false;
            } else {
                createAccountByPersonAndId(person, id, 0);
            }
        }
        return true;
    }

    @Override
    public Account getAccount(String id) {
        return accounts.get(id);
    }

    @Override
    public Person getPerson(String passport, boolean isRemote) throws RemoteException {
        final RemotePerson person = (RemotePerson) people.get(passport);
        if (person == null) {
            System.err.println("No person with passport " + passport);
            return null;
        } else if (isRemote) {
            return person;
        } else {
            Map<String, LocalAccount> localAccounts = new TreeMap<>();
            List<String> accountNames = getAccountsByPassport(passport);
            if (accountNames != null) {
                for (String accountName: accountNames) {
                    Account account = getAccount(passport + ":" + accountName);
                    localAccounts.put(accountName, new LocalAccount(account.getId(), account.getAmount()));
                }
            }
            return new LocalPerson(person.getName(), person.getSurname(), person.getPassport(), localAccounts);
        }
    }

    @Override
    public void createPersonIfAbsent(String passport, String name, String surname) throws RemoteException {
        if (people.get(passport) == null) {
            final Person client = new RemotePerson(name, surname, passport);
            people.put(client.getPassport(), client);
            UnicastRemoteObject.exportObject(client, port);
        }
    }

    @Override
    public List<String> getAccountsByPassport(String passport) throws RemoteException {
        return accountsByPassport.get(passport);
    }

    @Override
    public void createAccountByPersonAndId(Person person, String id, int amount) throws RemoteException {
        String passport = person.getPassport();
        String accountId = passport + ":" + id;
        if (person instanceof LocalPerson) {
            ((LocalPerson) person).createAccount(new LocalAccount(accountId, amount));
        } else {
            Account account = new RemoteAccount(accountId, amount);
            accounts.put(accountId, account);
            accountsByPassport.putIfAbsent(passport, new ArrayList<>());
            List<String> accountIds = accountsByPassport.get(passport);
            accountIds.add(id);
            UnicastRemoteObject.exportObject(account, port);
        }
    }
}

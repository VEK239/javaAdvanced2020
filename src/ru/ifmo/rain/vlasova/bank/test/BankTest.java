package ru.ifmo.rain.vlasova.bank.test;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.ifmo.rain.vlasova.bank.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class BankTest {
    public static Bank bank;

    @BeforeClass
    public static void beforeClass() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(8080);
        bank = (Bank) registry.lookup("//localhost/bank");
    }

    @Test
    public void testRemotePersonData() throws RemoteException {
        String passport = getNextPassport();
        String name = "Lizka";
        String surname = "Parampam";
        bank.createPersonIfAbsent(passport, name, surname);
        Person person = bank.getPerson(passport, true);
        assertEquals(passport, person.getPassport());
        assertEquals(name, person.getName());
        assertEquals(surname, person.getSurname());
    }

    @Test
    public void testLocalPersonData() throws RemoteException {
        String passport = getNextPassport();
        String name = "Lizka";
        String surname = "Parampam";
        bank.createPersonIfAbsent(passport, name, surname);
        LocalPerson person = (LocalPerson) bank.getPerson(passport, false);
        assertNotEquals(null, person);
        assertEquals(passport, person.getPassport());
        assertEquals(name, person.getName());
        assertEquals(surname, person.getSurname());
        assertEquals(new TreeMap<String, LocalAccount>(), person.getAccounts());
    }

    @Test
    public void testAccountsForRemotePerson() throws RemoteException {
        String passport = getNextPassport();
        String name = "Lizka";
        String surname = "Parampam";
        bank.createPersonIfAbsent(passport, name, surname);
        Person person = bank.getPerson(passport, true);
        assertNotEquals(null, person);
        bank.createAccountIfAbsent(passport, "account1");
        bank.createAccountIfAbsent(passport, "account2");
        bank.createAccountIfAbsent(passport, "account3");
        bank.createAccountIfAbsent(passport, "account4");
        List<String> accountsByPassport = bank.getAccountsByPassport(passport);
        assertEquals(4, accountsByPassport.size());
        assertEquals("account2", accountsByPassport.get(1));
    }
    @Test
    public void testAccountsForLocalPerson() throws RemoteException {
        String passport = getNextPassport();
        String name = "Liza";
        String surname = "Param";
        bank.createPersonIfAbsent(passport, name, surname);
        LocalPerson localPerson = (LocalPerson) bank.getPerson(passport, false);
        localPerson.createAccount(new LocalAccount("account1", 5));
        localPerson.createAccount(new LocalAccount("account2", 5));
        assertEquals(2, localPerson.getAccounts().entrySet().size());
    }

    @Test
    public void testAccountsAfterChange() throws RemoteException {
        String passport = getNextPassport();
        String name = "Liza";
        String surname = "Param";
        bank.createPersonIfAbsent(passport, name, surname);
        bank.createAccountIfAbsent(passport, "acc1");
        LocalPerson localPerson = (LocalPerson)bank.getPerson(passport, false);
        bank.createAccountIfAbsent(passport, "acc2");
        assertEquals(1, localPerson.getAccounts().entrySet().size());
        assertEquals(2, bank.getAccountsByPassport(passport).size());
        LocalAccount localAccount = new LocalAccount("localacc", 10);
        localPerson.createAccount(localAccount);
        assertEquals(2, localPerson.getAccounts().entrySet().size());
        assertEquals(2, bank.getAccountsByPassport(passport).size());
    }

    @Test
    public void testAccountUpdates() throws RemoteException {
        String passport = getNextPassport();
        String name = "Liza";
        String surname = "Hehehe";
        bank.createPersonIfAbsent(passport, name, surname);
        Person person = bank.getPerson(passport, true);
        bank.createAccountByPersonAndId(person, "acc", 25);
        Account account = bank.getAccount(passport + ":" + "acc");
        assertEquals(25, account.getAmount());
        account.changeAmount(20);
        assertEquals(45, bank.getAccount(passport + ":" + "acc").getAmount());
        Map<String, LocalAccount> localAccounts = ((LocalPerson)bank.getPerson(passport, false)).getAccounts();
        LocalAccount localAccount = localAccounts.get("acc");
        assertNotNull(localAccount);
        assertEquals(45, localAccount.getAmount());
        account.changeAmount(20);
        assertEquals(45, localAccount.getAmount());
    }

    private String getNextPassport() throws RemoteException {
        int index = 1;
        while (bank.getPerson("passport" + index, true) != null) {
            index++;
        }
        return "passport" + index;
    }
}

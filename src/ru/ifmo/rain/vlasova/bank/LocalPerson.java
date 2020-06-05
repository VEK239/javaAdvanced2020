package ru.ifmo.rain.vlasova.bank;

import java.util.Map;

public class LocalPerson extends AbstractPerson {
    private Map<String, LocalAccount> accounts;

    LocalPerson(final String name, final String surname, final String passport, Map<String, LocalAccount> accounts) {
        super(name, surname, passport);
        this.accounts = accounts;
    }

    public Map<String, LocalAccount> getAccounts() {
        return accounts;
    }

    Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    public void createAccount(LocalAccount account) {
        accounts.put(account.getId(), account);
    }

}

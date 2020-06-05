package ru.ifmo.rain.vlasova.bank;

class RemoteAccount extends AbstractAccount {
    public RemoteAccount(String id) {
        super(id, 0);
    }

    public RemoteAccount(String id, int amount) {
        super(id, amount);
    }

    public synchronized LocalAccount getLocalAccount() {
        return new LocalAccount(id, amount);
    }
}
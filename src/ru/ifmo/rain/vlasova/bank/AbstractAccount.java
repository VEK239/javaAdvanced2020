package ru.ifmo.rain.vlasova.bank;

public class AbstractAccount implements Account {
    protected String id;
    protected int amount;

    public AbstractAccount(String id, int amount) {
        this.id = id;
        this.amount = amount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() {
        return amount;
    }

    @Override
    public synchronized void changeAmount(final int amount) {
        this.amount += amount;
    }

}

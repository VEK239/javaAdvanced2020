package ru.ifmo.rain.vlasova.bank;

public class AbstractPerson implements Person {
    protected String name, surname, passport;

    AbstractPerson(final String name, final String surname, final String passport) {
        this.name = name;
        this.passport = passport;
        this.surname = surname;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname(){
        return surname;
    }

    @Override
    public String getPassport(){
        return passport;
    }

}

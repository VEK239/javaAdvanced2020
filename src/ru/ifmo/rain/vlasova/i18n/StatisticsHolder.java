package ru.ifmo.rain.vlasova.i18n;

public abstract class StatisticsHolder {
    String name;
    int entryCount, uniqueEntryCount,minLength, maxLength;
    double mediumLength;
    public abstract Object[] getArguments();
}

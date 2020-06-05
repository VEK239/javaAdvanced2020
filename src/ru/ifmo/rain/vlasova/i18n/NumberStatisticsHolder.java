package ru.ifmo.rain.vlasova.i18n;

public class NumberStatisticsHolder<T> extends StatisticsHolder {
    Number minValue = null;
    Number maxValue = null;

    public Object[] getArguments() {
        return new Object[]{0, "", entryCount, uniqueEntryCount, minValue, maxValue, minLength, maxLength, mediumLength};
    }
}

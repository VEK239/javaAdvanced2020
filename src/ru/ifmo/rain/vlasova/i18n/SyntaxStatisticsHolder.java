package ru.ifmo.rain.vlasova.i18n;

public class SyntaxStatisticsHolder extends StatisticsHolder {
    String minValue;
    String maxValue;
    public Object[] getArguments() {
        return new Object[]{0, "", entryCount, uniqueEntryCount, minValue, maxValue, minLength, maxLength, mediumLength};
    }
}

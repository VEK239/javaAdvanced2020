package ru.ifmo.rain.vlasova.i18n;

import java.util.Date;

public class DateStatisticsHolder extends StatisticsHolder {
    Date minValue;
    Date maxValue;
    public Object[] getArguments() {
        return new Object[]{0, "", entryCount, uniqueEntryCount, minValue, maxValue, minLength, maxLength, mediumLength};
    }
}

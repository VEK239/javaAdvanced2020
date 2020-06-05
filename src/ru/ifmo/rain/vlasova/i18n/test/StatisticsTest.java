package ru.ifmo.rain.vlasova.i18n.test;

import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.rain.vlasova.i18n.StatisticsManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class StatisticsTest {

    @Test
    public void testSyntaxEnglish() {
        String text = "This is my first test. My Welcomes";
        StatisticsManager manager = new StatisticsManager(Locale.US, text);
        Object[] expected = {0, "", 8, 8, ".", "Welcomes", 1, 8, 3.5};
        manager.createStatistics();
        Assert.assertArrayEquals(expected, manager.getWordsStats().getArguments());
        expected = new Object[]{0, "", 2, 2, "My Welcomes", "This is my first test.", 11, 22, 16.5};
        Assert.assertArrayEquals(expected, manager.getSentencesStats().getArguments());
        expected = new Object[]{0, "", 1, 1, "This is my first test. My Welcomes", "This is my first test. My Welcomes", 34, 34, 34.0};
        Assert.assertArrayEquals(expected, manager.getLineStats().getArguments());
    }

    @Test
    public void testSyntaxFrench() {
        String text = "c'est mon premier test";
        StatisticsManager manager = new StatisticsManager(Locale.FRANCE, text);
        Object[] expected = {0, "", 4, 4, "c'est", "test", 3, 7, 4.75};
        manager.createStatistics();
        Assert.assertArrayEquals(expected, manager.getWordsStats().getArguments());
    }

    @Test
    public void testDates() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        String text = "10/05/2020 11/05/2020";

        StatisticsManager manager = new StatisticsManager(Locale.US, text);
        Object[] expected = {0, "", 2, 2, format.parse("05.10.2020"), format.parse("05.11.2020"), 10, 10, 10.0};
        manager.createStatistics();
        Assert.assertArrayEquals(expected, manager.getDateStats().getArguments());

        manager = new StatisticsManager(Locale.FRANCE, text);
        expected = new Object[]{
            0, "", 2, 2, format.parse("10.05.2020"), format.parse("11.05.2020"), 10, 10, 10.0
        };
        manager.createStatistics();
        Assert.assertArrayEquals(expected, manager.getDateStats().getArguments());
    }

    @Test
    public void testCurrency() {
        String text = "$15.00 15.00 $ 15$";

        StatisticsManager manager = new StatisticsManager(Locale.US, text);
        Object[] expected = {0, "", 1, 1, 15L, 15L, 6, 6, 6.00};
        manager.createStatistics();
        Assert.assertArrayEquals(expected, manager.getCurrencyStats().getArguments());
   }
}

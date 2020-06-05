package ru.ifmo.rain.vlasova.i18n;

import java.text.*;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class StatisticsManager {
    private final Locale locale;
    private final String text;
    private Map<String, StatisticsHolder> itemToStatisticsHolderMap;

    public StatisticsManager(Locale locale, String text) {
        this.locale = locale;
        this.text = text;
        this.itemToStatisticsHolderMap = new HashMap<>();
    }

    private ArrayList<String> getSyntaxItems(BreakIterator boundary) {
        ArrayList<String> sentences = new ArrayList<>();
        boundary.setText(text);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            String entry = text.substring(start, end).trim();
            if (!entry.equals("")) {
                sentences.add(entry);
            }
        }
        return sentences;
    }

    private ArrayList<String> getLineItems() {
        ArrayList<String> sentences = new ArrayList<>();
        for (String entry : text.split(System.lineSeparator())) {
            if (!entry.equals(" "))
                sentences.add(entry);
        }
        return sentences;
    }

    public void createStatistics() {
        createSyntaxItemStats("sentences", getSyntaxItems(BreakIterator.getSentenceInstance()));
        createSyntaxItemStats("words", getSyntaxItems(BreakIterator.getWordInstance(locale)));
        createSyntaxItemStats("lines", getLineItems());
        createNumberItemStats("numbers", NumberFormat.getInstance(locale));
        createNumberItemStats("currency", NumberFormat.getCurrencyInstance(locale));
        createDateItemStats(DateFormat.getDateInstance(DateFormat.SHORT, locale));
    }

    private void createSyntaxItemStats(String item, ArrayList<String> sentences) {
        SyntaxStatisticsHolder holder = new SyntaxStatisticsHolder();
        holder.name = item;
        holder.maxLength = 0;
        holder.minLength = text.length();
        HashSet<String> uniqueSentences = new HashSet<>();
        for (String entry : sentences) {
            entry = entry.trim();
            uniqueSentences.add(entry);
            holder.mediumLength += entry.length();
            holder.minLength = min(holder.minLength, entry.length());
            holder.maxLength = max(holder.maxLength, entry.length());
        }
        holder.entryCount = sentences.size();
        holder.uniqueEntryCount = uniqueSentences.size();
        holder.mediumLength /= holder.entryCount;
        holder.minValue = sentences.get(0);
        holder.maxValue = sentences.get(0);
        Collator collator = Collator.getInstance(locale);
        for (String element : uniqueSentences) {
            if (collator.compare(element, holder.minValue) < 0) {
                holder.minValue = element;
            }
            if (collator.compare(element, holder.maxValue) > 0) {
                holder.maxValue = element;
            }
        }
        itemToStatisticsHolderMap.put(item, holder);
    }

    private void createNumberItemStats(String item, NumberFormat nf) {
        NumberStatisticsHolder holder = new NumberStatisticsHolder();
        holder.name = item;
        holder.maxLength = 0;
        holder.minLength = text.length();
        ArrayList<Number> numbers = new ArrayList<>();
        HashSet<Number> uniqueNumbers = new HashSet<>();
        BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(text);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            String entry = text.substring(start, end);
            try {
                entry = entry.trim();
                Number number = nf.parse(entry);
                numbers.add(number);
                uniqueNumbers.add(number);
                holder.mediumLength += entry.length();
                holder.minLength = min(holder.minLength, entry.length());
                holder.maxLength = max(holder.maxLength, entry.length());
            } catch (ParseException ignored) {
            }
        }
        holder.entryCount = numbers.size();
        holder.uniqueEntryCount = uniqueNumbers.size();
        holder.mediumLength /= holder.entryCount;
        if (!numbers.isEmpty()) {
            holder.minValue = numbers.get(0);
            holder.maxValue = numbers.get(0);
            for (Number element : uniqueNumbers) {
                if (element.doubleValue() - holder.minValue.doubleValue() < 0) {
                    holder.minValue = element;
                }
                if (element.doubleValue() - holder.maxValue.doubleValue() > 0) {
                    holder.maxValue = element;
                }
            }
        } else {
            holder.minLength = 0;
        }
        itemToStatisticsHolderMap.put(item, holder);
    }

    private void createDateItemStats(DateFormat df) {
        DateStatisticsHolder holder = new DateStatisticsHolder();
        holder.name = "date";
        holder.maxLength = 0;
        holder.minLength = text.length();
        ArrayList<Date> numbers = new ArrayList<>();
        HashSet<Date> uniqueNumbers = new HashSet<>();
        BreakIterator boundary = BreakIterator.getLineInstance();
        boundary.setText(text);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            String entry = text.substring(start, end);
            try {
                entry = entry.trim();
                Date number = df.parse(entry);
                numbers.add(number);
                uniqueNumbers.add(number);
                holder.mediumLength += entry.length();
                holder.minLength = min(holder.minLength, entry.length());
                holder.maxLength = max(holder.maxLength, entry.length());
            } catch (ParseException ignored) {
            }
        }
        holder.entryCount = numbers.size();
        holder.uniqueEntryCount = uniqueNumbers.size();
        holder.mediumLength /= holder.entryCount;
        if (!numbers.isEmpty()) {
            holder.minValue = numbers.get(0);
            holder.maxValue = numbers.get(0);
            for (Date element : uniqueNumbers) {
                if (element.compareTo(holder.minValue) < 0) {
                    holder.minValue = element;
                }
                if (element.compareTo(holder.maxValue) > 0) {
                    holder.maxValue = element;
                }
            }
        } else {
            holder.minLength = 0;
        }
        itemToStatisticsHolderMap.put("dates", holder);
    }

    public SyntaxStatisticsHolder getSentencesStats() {
        return (SyntaxStatisticsHolder) itemToStatisticsHolderMap.get("sentences");
    }

    public SyntaxStatisticsHolder getWordsStats() {
        return (SyntaxStatisticsHolder) itemToStatisticsHolderMap.get("words");
    }

    public SyntaxStatisticsHolder getLineStats() {
        return (SyntaxStatisticsHolder) itemToStatisticsHolderMap.get("lines");
    }

    public NumberStatisticsHolder getNumberStats() {
        return (NumberStatisticsHolder) itemToStatisticsHolderMap.get("numbers");
    }

    public NumberStatisticsHolder getCurrencyStats() {
        return (NumberStatisticsHolder) itemToStatisticsHolderMap.get("currency");
    }

    public DateStatisticsHolder getDateStats() {
        return (DateStatisticsHolder) itemToStatisticsHolderMap.get("dates");
    }
}

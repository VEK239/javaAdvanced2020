package ru.ifmo.rain.vlasova.i18n;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class TextStatistics {

    public static void main(String[] args) throws ParseException {
        if (args != null && args.length == 4) {
            try {
                String text = getTextFromPath(args[2]);
                Locale outputLocale = new Locale(args[1]), inputLocale;
                String[] localeArgs = args[0].split("[_]");
                if (localeArgs.length == 1) {
                    inputLocale = new Locale(localeArgs[0]);
                    System.err.println("Warning: no currency support without region");
                } else {
                    inputLocale = new Locale(localeArgs[0], localeArgs[1]);
                }
                StatisticsManager manager = new StatisticsManager(inputLocale, text);
                generateHtmlReport(manager, inputLocale, outputLocale, args[2], args[3]);
            } catch (IOException e) {
                System.err.println("Couldn't open input file!");
            }
        } else {
            System.err.println("Expected: inputLocale outputLocale inputPath outputPath");
        }
    }

    private static void generateHtmlReport(StatisticsManager manager, Locale inputLocale, Locale outputLocale,
                                           String inputFileName, String outputFileName) {
        manager.createStatistics();
        StringBuilder report = new StringBuilder("<html>\n" +
                "<head>\n" +
                "<meta charset=utf-8>\n" +
                "</head>\n");

        ResourceBundle reports = ResourceBundle.getBundle("ReportBundle", outputLocale);
        MessageFormat reportFormatter = new MessageFormat(reports.getString("reportPattern"), outputLocale);
        MessageFormat paragraphFormatter = new MessageFormat(reports.getString("paragraphPattern"), outputLocale);
        Object[] arguments = {inputFileName, new Date()};
        report.append(reportFormatter.format(arguments)).append("\n");

        SyntaxStatisticsHolder sentencesHolder = manager.getSentencesStats();
        arguments = sentencesHolder.getArguments();
        arguments[0] = 1;
        arguments[1] = reports.getString("sentencesStats");
        report.append(paragraphFormatter.format(arguments)).append("\n");

        SyntaxStatisticsHolder linesHolder = manager.getLineStats();
        arguments = linesHolder.getArguments();
        arguments[0] = 2;
        arguments[1] = reports.getString("linesStats");
        report.append(paragraphFormatter.format(arguments)).append("\n");

        SyntaxStatisticsHolder wordsHolder = manager.getWordsStats();
        arguments = wordsHolder.getArguments();
        arguments[0] = 3;
        arguments[1] = reports.getString("wordsStats");
        report.append(paragraphFormatter.format(arguments)).append("\n");

        NumberStatisticsHolder numbersHolder = manager.getNumberStats();
        arguments = numbersHolder.getArguments();
        arguments[0] = 4;
        arguments[1] = reports.getString("numbersStats");
        if (arguments[4] == null) {
            arguments[4] = reports.getString("nullValue");
        }
        if (arguments[5] == null) {
            arguments[5] = reports.getString("nullValue");
        }
        report.append(paragraphFormatter.format(arguments)).append("\n");

        NumberStatisticsHolder currencyHolder = manager.getCurrencyStats();
        NumberFormat nf = NumberFormat.getCurrencyInstance(outputLocale);
        try {
            nf.setCurrency(Currency.getInstance(inputLocale));
        } catch (IllegalArgumentException ignored) {}
        arguments = currencyHolder.getArguments();
        arguments[0] = 5;
        arguments[1] = reports.getString("currencyStats");
        arguments[4] = arguments[4] == null ? reports.getString("nullValue") : nf.format(arguments[4]);
        arguments[5] = arguments[5] == null ? reports.getString("nullValue") : nf.format(arguments[5]);
        report.append(paragraphFormatter.format(arguments)).append("\n");

        DateStatisticsHolder datesHolder = manager.getDateStats();
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, outputLocale);
        arguments = datesHolder.getArguments();
        arguments[0] = 6;
        arguments[1] = reports.getString("datesStats");
        arguments[4] = arguments[4] == null ? reports.getString("nullValue") : df.format(arguments[4]);
        arguments[5] = arguments[5] == null ? reports.getString("nullValue") : df.format(arguments[5]);
        report.append(paragraphFormatter.format(arguments)).append("\n");

        report.append("</html>");
        try (FileWriter writer = new FileWriter(outputFileName, false)) {
            writer.write(report.toString());
        } catch (IOException e) {
            System.err.println("Error writing into output file");
        }
    }

    private static String getTextFromPath(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
        }
        return sb.toString();
    }
}

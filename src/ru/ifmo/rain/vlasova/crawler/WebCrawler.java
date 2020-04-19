package ru.ifmo.rain.vlasova.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloaderPool;
    private final ExecutorService extractorPool;

    public WebCrawler(Downloader downloader, int downloaderCount, int extractorCount, int perHost) {
        this.downloader = downloader;
        downloaderPool = Executors.newFixedThreadPool(downloaderCount);
        extractorPool = Executors.newFixedThreadPool(extractorCount);
    }

    public WebCrawler(int downloaderCount, int extractorCount) throws IOException {
        this(new CachingDownloader(), downloaderCount, extractorCount, 0);
    }

    @Override
    public void close() {
        downloaderPool.shutdown();
        extractorPool.shutdown();
    }

    @Override
    public Result download(String url, int depth) {
        final Set<String> downloadedUrls = ConcurrentHashMap.newKeySet();
        final Set<String> usedUrls = ConcurrentHashMap.newKeySet();
        final ConcurrentMap<String, IOException> errors = new ConcurrentHashMap<>();

        Phaser phaser = new Phaser(1);
        usedUrls.add(url);
        downloadPage(url, depth, downloadedUrls, errors, phaser, usedUrls);
        phaser.arriveAndAwaitAdvance();

        return new Result(new ArrayList<>(downloadedUrls), errors);
    }

    private void downloadPage(String url, int depth, Set<String> downloadedUrls, ConcurrentMap<String, IOException> errors,
                              Phaser phaser, Set<String> usedUrls) {
        phaser.register();
        downloaderPool.submit(() -> {
            try {
                final Document page = downloader.download(url);
                downloadedUrls.add(url);
                if (depth > 1) {
                    phaser.register();
                    extractorPool.submit(() -> processPage(page, depth - 1, downloadedUrls, phaser, errors, usedUrls));
                }
            } catch (IOException e) {
                errors.put(url, e);
            } finally {
                phaser.arrive();
            }
        });
    }

    private void processPage(Document page, int depth, Set<String> downloadedUrls, Phaser phaser,
                             ConcurrentMap<String, IOException> errors, Set<String> usedUrls) {
        try {
            page.extractLinks().stream().filter(usedUrls::add)
                    .forEach(link -> downloadPage(link, depth, downloadedUrls, errors, phaser, usedUrls));
        } catch (IOException ignored) {
        } finally {
            phaser.arrive();
        }
    }

    public static void main(String[] args) {
        if (args != null && args.length > 1 && args.length < 6) {
            try {
                int defaultProcessors = Runtime.getRuntime().availableProcessors();
                String url = args[0];
                int depth = Integer.parseInt(args[1]);
                int downloaderCount = (args.length > 2 ? Integer.parseInt(args[2]) : defaultProcessors);
                int extractorCount = (args.length > 3 ? Integer.parseInt(args[3]) : defaultProcessors);
                try (WebCrawler crawler = new WebCrawler(downloaderCount, extractorCount)) {
                    crawler.download(url, depth);
                } catch (IOException e) {
                    System.err.println("Can't create downloader");
                }
            } catch (NumberFormatException e) {
                System.err.println("Can't parse arguments to integer");
            }
        } else {
            System.err.println("Expected: url [depth [downloads [extractors [perHost]]]]");
        }
    }
}
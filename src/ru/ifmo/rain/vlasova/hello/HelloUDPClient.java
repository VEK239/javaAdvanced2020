package ru.ifmo.rain.vlasova.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {
    @Override
    public void run(String host, int port, String prefix, int threadsCount, int requests) {
        ExecutorService threads = Executors.newFixedThreadPool(threadsCount);
        InetSocketAddress address = new InetSocketAddress(host, port);
        for (int i = 0; i < threadsCount; i++) {
            threads.submit(new RequestSender(requests, i, prefix, address));
        }
        threads.shutdown();
        try {
            threads.awaitTermination(requests * threadsCount, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Expected five arguments!");
        } else {
            String host = args[0], prefix = args[2];
            int port = Integer.parseInt(args[1]), threads = Integer.parseInt(args[3]), requests = Integer.parseInt(args[4]);
            new HelloUDPClient().run(host, port, prefix, threads, requests);
        }
    }
}

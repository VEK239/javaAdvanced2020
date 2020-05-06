package ru.ifmo.rain.vlasova.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Integer.max;

public class HelloUDPServer implements HelloServer {
    private ExecutorService senderThreads, listenerThread;
    private DatagramSocket socket;

    @Override
    public void start(int port, int threads) {
        senderThreads = Executors.newFixedThreadPool(max(1, threads));
        listenerThread = Executors.newSingleThreadExecutor();
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(100);
        } catch (SocketException e) {
            System.err.println("Error opening socket");
        }
        listenerThread.submit(this::listen);
    }

    private void listen() {
        try {
            while (!socket.isClosed()) {
                byte[] receivedBytes = new byte[socket.getReceiveBufferSize()];
                DatagramPacket receivedPacket = new DatagramPacket(receivedBytes, receivedBytes.length);
                try {
                    socket.receive(receivedPacket);
                    senderThreads.submit(new ResponseSender(socket, receivedPacket));
                } catch (IOException ignored) {
                    System.err.println("Error receiving datagram");
                }
            }
        } catch (SocketException ignored) {
        }
    }

    @Override
    public void close() {
        socket.close();
        listenerThread.shutdown();
        senderThreads.shutdown();
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("Expected two arguments");
        } else {
            int port = Integer.parseInt(args[0]), threadsCount = Integer.parseInt(args[1]);
            new HelloUDPServer().start(port, threadsCount);
        }
    }
}

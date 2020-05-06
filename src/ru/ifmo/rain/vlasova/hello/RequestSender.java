package ru.ifmo.rain.vlasova.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

class RequestSender implements Runnable {
    private int requests;
    private int threadsCount;
    private final String prefix;
    private final InetSocketAddress address;

    RequestSender(int requests, int threadsCount, String prefix, InetSocketAddress address) {
        this.requests = requests;
        this.threadsCount = threadsCount;
        this.prefix = prefix;
        this.address = address;
    }

    @Override
    public void run() {
        int requestsCount = 0;
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(100);
            String request = null;
            byte[] responseBytes = new byte[socket.getReceiveBufferSize()], requestBytes = null;
            while (!socket.isClosed() && !Thread.currentThread().isInterrupted() && requestsCount < requests) {
                if (request == null) {
                    request = prefix + threadsCount + "_" + requestsCount;
                    requestBytes = request.getBytes(StandardCharsets.UTF_8);
                }
                try {
                    DatagramPacket requestPacket = new DatagramPacket(requestBytes, 0, requestBytes.length, address);
                    socket.send(requestPacket);
                    DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
                    socket.receive(responsePacket);
                    String response = new String(responsePacket.getData(), 0,
                            responsePacket.getLength(), StandardCharsets.UTF_8);
                    if (response.equals("Hello, " + request)) {
                        System.out.println(response);
                        request = null;
                        ++requestsCount;
                    }
                } catch (IOException ignored) {
                    System.err.println("Error sending or receiving data");
                }
            }
        } catch (SocketException ignored) {
            System.err.println("Error opening socket");
        }
    }
}

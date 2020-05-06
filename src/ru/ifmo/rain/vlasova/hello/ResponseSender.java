package ru.ifmo.rain.vlasova.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

class ResponseSender implements Runnable {
    private DatagramSocket socket;
    private DatagramPacket receivedPacket;

    ResponseSender(DatagramSocket socket, DatagramPacket received) {
        this.socket = socket;
        receivedPacket = received;
    }

    @Override
    public void run() {
        try {
            String message = "Hello, " + new String(receivedPacket.getData(), 0, receivedPacket.getLength(), StandardCharsets.UTF_8);
            byte[] sendingBytes = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendPacket = new DatagramPacket(sendingBytes, sendingBytes.length, receivedPacket.getSocketAddress());
            socket.send(sendPacket);
        } catch (IOException e) {
            System.err.println("Error sending response");
        }
    }
}

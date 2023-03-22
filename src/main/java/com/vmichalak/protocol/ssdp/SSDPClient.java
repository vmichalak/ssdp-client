package com.vmichalak.protocol.ssdp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client for discovering UPNP devices with SSDP (Simple Service Discovery Protocol).
 */
public class SSDPClient {
    private SSDPClient() {}
    private static final int DEFAULT_PORT = 1900;
    private static final int DEFAULT_TIMEOUT = 60000;
    private static final String DEFAULT_HOST = "239.255.255.250";
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    private static final String DEFAULT_SEARCH_TARGET = "ssdp:all";

    /**
     * Discover any UPNP device using SSDP (Simple Service Discovery Protocol).
     * Default port is 1900.
     * Default timeout is 5 seconds.
     * Default host is 239.255.255.250
     * Default search target is "ssdp:all".
     *
     * @return List of devices discovered
     * @throws IOException when an I/O error occurs
     * @see <a href="https://en.wikipedia.org/wiki/Simple_Service_Discovery_Protocol">SSDP Wikipedia Page</a>
     */
    public static List<Device> discover() throws IOException {
        return discover(DEFAULT_TIMEOUT, DEFAULT_SEARCH_TARGET);
    }

    /**
     * Discover any UPNP device using SSDP (Simple Service Discovery Protocol).
     * Default port is 1900.
     * Default timeout is 5 seconds.
     * Default host is 255.255.255.255
     * Default search target is "ssdp:all".
     *
     * @return List of devices discovered
     * @throws IOException when an I/O error occurs
     * @see <a href="https://en.wikipedia.org/wiki/Simple_Service_Discovery_Protocol">SSDP Wikipedia Page</a>
     */
    public static List<Device> broadcast() throws IOException {
        return discover(DEFAULT_TIMEOUT, BROADCAST_ADDRESS, DEFAULT_PORT, DEFAULT_SEARCH_TARGET);
    }

    /**
     * Discover any UPNP device using SSDP (Simple Service Discovery Protocol).
     *
     * @param timeout      in milliseconds
     * @param searchTarget if null it uses "ssdp:all"
     * @return List of devices discovered
     * @throws IOException when an I/O error occurs
     * @see <a href="https://en.wikipedia.org/wiki/Simple_Service_Discovery_Protocol">SSDP Wikipedia Page</a>
     */
    public static List<Device> discover(final int timeout, final String searchTarget) throws IOException {
        return discover(timeout, DEFAULT_HOST, DEFAULT_PORT, searchTarget);
    }

    /**
     * Discover any UPNP device using SSDP (Simple Service Discovery Protocol).
     *
     * @param timeout      in milliseconds
     * @param host         the search target host
     * @param port         the search target port
     * @param searchTarget if null it uses "ssdp:all"
     * @return List of devices discovered
     * @throws IOException when an I/O error occurs
     * @see <a href="https://en.wikipedia.org/wiki/Simple_Service_Discovery_Protocol">SSDP Wikipedia Page</a>
     */
    public static List<Device> discover(final int timeout, final String host, final int port, final String searchTarget) throws IOException {
        ArrayList<Device> devices = new ArrayList<>();

        byte[] sendData;
        byte[] receiveData = new byte[1024];

        /* Build the request */
        sendData = mSearchBuilder(timeout, host, port, searchTarget).getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(host), port);


        try (DatagramSocket clientSocket = new DatagramSocket()) {
            long startTime = System.currentTimeMillis();
            clientSocket.setSoTimeout(timeout);
            clientSocket.send(sendPacket);
            do {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(receivePacket);
                    if (searchTarget == null || new String(receivePacket.getData()).contains(searchTarget)) {
                        devices.add(Device.parse(receivePacket));
                    }
                } catch (SocketTimeoutException e) {
                    break;
                }
            } while (System.currentTimeMillis() - startTime < timeout);
        }


        return Collections.unmodifiableList(devices);
    }

    /**
     * Build the search request.
     *
     * @param timeout      in milliseconds
     * @param host         the search target host
     * @param port         the search target port
     * @param searchTarget if null it uses "ssdp:all"
     * @return the search request
     */
    private static String mSearchBuilder(int timeout, String host, int port, String searchTarget) {
        /* Create the search request */
        StringBuilder mSearch = new StringBuilder("M-SEARCH * HTTP/1.1\n");
        mSearch.append("Host: ");
        mSearch.append(host);
        mSearch.append(":");
        mSearch.append(port);
        mSearch.append("\nMAN: ssdp:discover\n");

        if (searchTarget == null) {
            mSearch.append("ST: ssdp:all\n");
        } else {
            mSearch.append("ST: ").append(searchTarget).append("\n");
        }

        if (timeout >= 1100) {
            mSearch.append("MX: ");
            mSearch.append(Math.floor((timeout - 100) / 1000.0));
            mSearch.append("\n");
        }  // gives devices 100ms to respond (in the worst case)

        mSearch.append("\r\n");
        return mSearch.toString();
    }
}
package com.praetoriandroid.cameraremote;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;

public class SsdpClient {

    private static final String WIFI_INTERFACE_NAME = "^w.*[0-9]$";

    private static final int PACKET_BUFFER_SIZE = 1024;
    private static final int DEFAULT_RECEIVE_TIMEOUT = 10000;
    private static final int REPEAT_INTERVAL = 100;

    private static final int SSDP_PORT = 1900;
    private static final int SSDP_MX = 1;
    private static final String SSDP_ADDRESS = "239.255.255.250";
    private static final String SSDP_ST = "urn:schemas-sony-com:service:ScalarWebAPI:1";

    private int timeout = DEFAULT_RECEIVE_TIMEOUT;

//    public SsdpClient() {
//        try {
//            InetAddress localhost = InetAddress.getLocalHost();
//            System.out.println("Local IP Address: " + localhost.getHostAddress());
//            String canonicalHostName = localhost.getCanonicalHostName();
//            System.out.println("Canonical host name: " + canonicalHostName);
//            InetAddress[] allMyIps = InetAddress.getAllByName(canonicalHostName);
//            if (allMyIps != null) {
//                System.out.println("Full list of IP addresses:");
//                for (InetAddress allMyIp : allMyIps) {
//                    System.out.println("    " + allMyIp);
//                }
//            }
//        } catch (UnknownHostException e) {
//            System.out.println(" (error retrieving server host name)");
//        }
//
//        try {
//            System.out.println("Full list of Network Interfaces:");
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//                NetworkInterface netInterface = en.nextElement();
//                System.out.println("    " + netInterface.getName() + " " + netInterface.getDisplayName());
//                for (Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses(); inetAddresses.hasMoreElements(); ) {
//                    InetAddress address = inetAddresses.nextElement();
//                    System.out.println("        " + address);
//                }
//            }
//        } catch (SocketException e) {
//            System.out.println(" (error retrieving network interface list)");
//        }
//    }

    public void setSearchTimeout(int milliseconds) {
        timeout = milliseconds;
    }

    public String getDeviceDescriptionUrl() throws SsdpException {
        DatagramSocket socket = null;
        try {
            SocketAddress localAddress = new InetSocketAddress(getFirstWiFiAddress(), 0);
            socket = new DatagramSocket(localAddress);
            sendMSearchRequest(socket);
            return processResponse(socket);
        } catch (IOException e) {
            throw new SsdpException(e);
        } catch (ParseException e) {
            throw new SsdpException(e);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    private void sendMSearchRequest(DatagramSocket socket) throws IOException {
        String ssdpRequest = "M-SEARCH * HTTP/1.1\r\n"
                + String.format("HOST: %s:%d\r\n", SSDP_ADDRESS, SSDP_PORT)
                + String.format("MAN: \"ssdp:discover\"\r\n")
                + String.format("MX: %d\r\n", SSDP_MX)
                + String.format("ST: %s\r\n", SSDP_ST) + "\r\n";
        byte[] sendData = ssdpRequest.getBytes();

        InetSocketAddress remoteAddress = new InetSocketAddress(SSDP_ADDRESS, SSDP_PORT);
        DatagramPacket request = new DatagramPacket(sendData, sendData.length, remoteAddress);
        socket.send(request);
        try {
            Thread.sleep(REPEAT_INTERVAL);
            socket.send(request);
            Thread.sleep(REPEAT_INTERVAL);
            socket.send(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException();
        }
    }

    private String processResponse(DatagramSocket socket)
            throws IOException, InvalidDataFormatException {
        socket.setSoTimeout(timeout);
        byte[] buffer = new byte[PACKET_BUFFER_SIZE];
        DatagramPacket answer = new DatagramPacket(buffer, buffer.length);
        socket.receive(answer);
        String ssdpReplyMessage = new String(
                answer.getData(), 0,
                answer.getLength());

//        String usn = findHeaderValue(ssdpReplyMessage, "USN"); // could be used as device id
        return findHeaderValue(ssdpReplyMessage, "LOCATION");
    }

    private String findHeaderValue(String ssdpMessage, String parameterName)
            throws InvalidDataFormatException {
        parameterName += ':';
        int start = ssdpMessage.indexOf(parameterName);
        if (start == -1) {
            throw new InvalidDataFormatException("Header not found: " + parameterName);
        }
        start += parameterName.length();

        int end = ssdpMessage.indexOf("\r\n", start);
        if (end == -1) {
            throw new InvalidDataFormatException("Every header should end with '\\r\\n'");
        }

        return ssdpMessage.substring(start, end).trim();
    }

    private InetAddress getFirstWiFiAddress() throws AddressNotFoundException {
        try {
            Enumeration<NetworkInterface> interfaces;
            for (interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.getName().matches(WIFI_INTERFACE_NAME)) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses;
                for (inetAddresses = networkInterface.getInetAddresses(); inetAddresses.hasMoreElements(); ) {
                    InetAddress address = inetAddresses.nextElement();
                    if (!address.isSiteLocalAddress()) {
                        continue;
                    }
                    if (address instanceof Inet4Address) {
                        return address;
                    }
                }
            }
        } catch (SocketException ignored) {
        }

        throw new AddressNotFoundException();
    }

    public static class AddressNotFoundException extends SsdpException {
    }

}

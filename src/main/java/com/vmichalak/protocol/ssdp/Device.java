package com.vmichalak.protocol.ssdp;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Device {
    private final String ip;
    private final String descriptionUrl;
    private final String server;
    private final String serviceType;
    private final String usn;

    public Device(String ip, String descriptionUrl, String server, String serviceType, String usn) {
        this.ip = ip;
        this.descriptionUrl = descriptionUrl;
        this.server = server;
        this.serviceType = serviceType;
        this.usn = usn;
    }

    public static Device parse(final DatagramPacket ssdpResult) {
        if(ssdpResult == null)
            return null;

        final HashMap<String, String> headers = new HashMap<>();
        final Pattern pattern = Pattern.compile("(.*): (.*)");

        final String[] lines = new String(ssdpResult.getData()).split("\r\n");
        for (final String line : lines) {
            final Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                headers.put(matcher.group(1).toUpperCase(), matcher.group(2));
            }
        }

        return new Device(
                ssdpResult.getAddress().getHostAddress(),
                headers.getOrDefault("LOCATION", ""),
                headers.getOrDefault("SERVER", ""),
                headers.getOrDefault("ST", ""),
                headers.getOrDefault("USN", ""));
    }

    public String getIp() {
        return ip;
    }

    public String getDescriptionUrl() {
        return descriptionUrl;
    }

    public String getServer() {
        return server;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getUsn() {
        return usn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Device)) {
            return false;
        }

        Device device = (Device) o;

        return Objects.equals(ip, device.ip)
                && Objects.equals(descriptionUrl, device.descriptionUrl)
                && Objects.equals(server, device.server)
                && Objects.equals(serviceType, device.serviceType)
                && Objects.equals(usn, device.usn);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(ip);
        result = 31 * result + Objects.hashCode(descriptionUrl);
        result = 31 * result + Objects.hashCode(server);
        result = 31 * result + Objects.hashCode(serviceType);
        result = 31 * result + Objects.hashCode(usn);
        return result;
    }

    @Override
    public String toString() {
        return "Device{" +
                "ip='" + ip + '\'' +
                ", descriptionUrl='" + descriptionUrl + '\'' +
                ", server='" + server + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", usn='" + usn + '\'' +
                '}';
    }
}

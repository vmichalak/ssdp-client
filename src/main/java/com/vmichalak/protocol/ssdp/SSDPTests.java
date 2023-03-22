package com.vmichalak.protocol.ssdp;

import org.testng.annotations.Test;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;

public class SSDPTests {

    private static final String IP = "192.168.0.1";
    private static final String DESCRIPTION_URL = "http://192.168.0.1/description.xml";
    private static final String SERVER = "Linux/2.0 UPnP/1.0 MyDevice/1.0";
    private static final String SERVICE_TYPE = "urn:schemas-upnp-org:service:MyService:1";
    private static final String USN = "uuid:1234567890";

    private static final String SSDP_RESULT =
            "HTTP/1.1 200 OK\r\n" +
                    "CACHE-CONTROL: max-age=1800\r\n" +
                    "EXT:\r\n" +
                    "LOCATION: " + DESCRIPTION_URL + "\r\n" +
                    "SERVER: " + SERVER + "\r\n" +
                    "ST: " + SERVICE_TYPE + "\r\n" +
                    "USN: " + USN + "\r\n" +
                    "\r\n";

    @Test
    void testParse() throws UnknownHostException {
        DatagramPacket packet = new DatagramPacket(SSDP_RESULT.getBytes(), SSDP_RESULT.getBytes().length);
        packet.setAddress(InetAddress.getByName(IP));  // Address is not used in Device.parse()

        Device device = Device.parse(packet);

        assertEquals(IP, device.getIp());
        assertEquals(DESCRIPTION_URL, device.getDescriptionUrl());
        assertEquals(SERVER, device.getServer());
        assertEquals(SERVICE_TYPE, device.getServiceType());
        assertEquals(USN, device.getUsn());
    }

    @Test
    void testEquals() {
        Device device1 = new Device(IP, DESCRIPTION_URL, SERVER, SERVICE_TYPE, USN);
        Device device2 = new Device(IP, DESCRIPTION_URL, SERVER, SERVICE_TYPE, USN);
        Device device3 = new Device(IP, DESCRIPTION_URL, SERVER, SERVICE_TYPE, "uuid:0987654321");
        Device device4 = new Device(IP, DESCRIPTION_URL, SERVER, SERVICE_TYPE, null);

        assertEquals(device1, device2);
        assertNotEquals(device1, device3);
        assertNotEquals(device1, device4);
    }

    @Test
    void testHashCode() {
        Device device1 = new Device(IP, DESCRIPTION_URL, SERVER, SERVICE_TYPE, USN);
        Device device2 = new Device(IP, DESCRIPTION_URL, SERVER, SERVICE_TYPE, USN);

        assertEquals(device1.hashCode(), device2.hashCode());
    }

    @Test
    void testToString() {
        Device device = new Device(IP, DESCRIPTION_URL, SERVER, SERVICE_TYPE, USN);
        String expected = "Device{" +
                "ip='" + IP + '\'' +
                ", descriptionUrl='" + DESCRIPTION_URL + '\'' +
                ", server='" + SERVER + '\'' +
                ", serviceType='" + SERVICE_TYPE + '\'' +
                ", usn='" + USN + '\'' +
                '}';
        assertEquals(expected, device.toString());
    }
}

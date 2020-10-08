/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.InputSource;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.PCAPImportFileParser;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test code exercising PCAPImportFileParser
 *
 * @author serpens24
 */
public class PCAPImportFileParserNGTest {

    // Reflection used to view private fields in class under test.
    static PCAPImportFileParser instance = new PCAPImportFileParser();
    static Field private_invalidPCAPField = null;
    static String private_invalidPCAPMsg = "";
    static Method private_byteToInt = null;
    static Method private_byteToProtocol = null;
    static Method private_byteToIPVersion = null;
    static Method private_byteToIPv4HeaderLength = null;
    static Method private_getBytes = null;
    static Method private_bytesToInt = null;
    static Method private_bytesToMacAddressStr = null;
    static Method private_bytesToHexStr = null;
    static Method private_getEtherTypeStr = null;
    static Method private_bytesToIpv4Str = null;
    static Method private_bytesToIpv6Str = null;
    static Method private_getResults = null;
    static Method private_openPcap = null;

    static String[] expectedHeadings = {
        "Frame", "Time",
        "Src MAC", "Src IP", "Src Port", "Src Type",
        "Dest MAC", "Dest IP", "Dest Port", "Dest Type",
        "Ethertype", "Protocol", "Length", "Info"};
    static String[] expectedRow1 = {
        "1", "2005-07-04 09:32:20.839 GMT",
        "00:e0:ed:01:6e:bd", "192.168.1.2", "137", "IPv4 Address",
        "BROADCAST ff:ff:ff:ff:ff:ff", "192.168.1.255", "137", "IPv4 Address",
        "0800", "UDP", "92", ""};
    static String[] expectedIPv6Row1 = {
        "1", "2016-05-30 19:37:47.681 GMT",
        "86:93:23:d3:37:8e", "fc00:2:0:2::1", "43424", "IPv6 Address",
        "22:1a:95:d6:7a:23", "fc00:2:0:1::1", "8080", "IPv6 Address",
        "86dd", "TCP", "94", ""
    };
    static List<String[]> expectedResults = new ArrayList<String[]>();
    
    static String[] expectedDataTruncatedFrame2 = {
        "161", "178", "195", "212", "0", "2", "0", "4", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "255", "255", "0", "0", "0", "1", "66", "201", "2", "36", 
        "0", "12", "206", "144", "0", "0", "0", "92", "0", "0", "0", "92", "255", "255", "255", "255", "255", "255", "0", "224", "237", "1", "110", "189", "8", "0",
        "69", "0", "0", "78", "105", "140", "0", "0", "128", "17", "76", "193", "192", "168", "1", "2",
        "192", "168", "1", "255", "0", "137", "0", "137", "0", "58", "91", "180", "132", "231", "1",
        "16", "0", "1", "0", "0", "0", "0", "0", "0", "32", "69", "70", "69", "68", "69", "74", "70", "80",
        "69", "69", "69", "80", "69", "78", "69", "66", "69", "74", "69", "79", "83", "65", "67", "65", "67",
        "65", "67", "65", "67", "65", "66", "77", "0", "0", "32", "0", "1", "66", "201", "2", "37", "0", "8", "239",
        "148", "0", "0", "0", "92", "0", "0", "0", "92", "255", "255", "255"  
    };

    static String[] protocolMap = {
        "HOPOPT", "ICMP", "IGMP", "GGP", "IPv4", "ST", "TCP", "CBT", "EGP",
        "IGP", "BBN-RCC-MON", "NVP-II", "PUP", "ARGUS", "EMCON", "XNET", "CHAOS",
        "UDP", "MUX", "DCN-MEAS", "HMP", "PRM", "XNS-IDP", "TRUNK-1", "TRUNK-2",
        "LEAF-1", "LEAF-2", "RDP", "IRTP", "ISO-TP4", "NETBLT", "MFE-NSP", "MERIT-INP",
        "DCCP", "3PC", "IDPR", "XTP", "DDP", "IDPR-CMTP", "TP++", "IL", "IPv6",
        "SDRP", "IPv6-Route", "IPv6-Frag", "IDRP", "RSVP", "GRE", "DSR", "BNA",
        "ESP", "AH", "I-NLSP", "SWIPE", "NARP", "MOBILE", "TLSP", "SKIP",
        "IPv6-ICMP", "IPv6-NoNxt", "IPv6-Opts", "0x3d", "CFTP", "0x3f", "SAT-EXPAK",
        "KRYPTOLAN", "RVD", "IPPC", "0x44", "SAT-MON", "VISA", "IPCV", "CPNX",
        "CPHB", "WSN", "PVP", "BR-SAT-MON", "SUN-ND", "WB-MON", "WB-EXPAK",
        "ISO-IP", "VMTP", "SECURE-VMTP", "VINES", "IPTM", "NSFNET-IGP",
        "DGP", "TCF", "EIGRP", "OSPFIGP", "Sprite-RPC", "LARP", "MTP", "AX.25",
        "IPIP", "MICP", "SCC-SP", "ETHERIP", "ENCAP", "0x63", "GMTP", "IFMP",
        "PNNI", "PIM", "ARIS", "SCPS", "QNX", "A/N", "IPComp", "SNP",
        "Compaq-Peer", "IPX-in-IP", "VRRP", "PGM", "0x72", "L2TP", "DDX", "IATP",
        "STP", "SRP", "UTI", "SMP", "SM", "PTP", "ISIS over IPv4", "FIRE", "CRTP",
        "CRUDP", "SSCOPMCE", "IPLT", "SPS", "PIPE", "SCTP", "FC",
        "RSVP-E2E-IGNORE", "Mobility Header", "UDPLite", "MPLS-in-IP", "manet",
        "HIP", "Shim6", "WESP", "ROHC", "Ethernet", "0x90", "0x91", "0x92",
        "0x93", "0x94", "0x95", "0x96", "0x97", "0x98", "0x99", "0x9a", "0x9b",
        "0x9c", "0x9d", "0x9e", "0x9f", "0xa0", "0xa1", "0xa2", "0xa3", "0xa4",
        "0xa5", "0xa6", "0xa7", "0xa8", "0xa9", "0xaa", "0xab", "0xac", "0xad",
        "0xae", "0xaf", "0xb0", "0xb1", "0xb2", "0xb3", "0xb4", "0xb5", "0xb6",
        "0xb7", "0xb8", "0xb9", "0xba", "0xbb", "0xbc", "0xbd", "0xbe", "0xbf",
        "0xc0", "0xc1", "0xc2", "0xc3", "0xc4", "0xc5", "0xc6", "0xc7", "0xc8",
        "0xc9", "0xca", "0xcb", "0xcc", "0xcd", "0xce", "0xcf", "0xd0", "0xd1",
        "0xd2", "0xd3", "0xd4", "0xd5", "0xd6", "0xd7", "0xd8", "0xd9", "0xda",
        "0xdb", "0xdc", "0xdd", "0xde", "0xdf", "0xe0", "0xe1", "0xe2", "0xe3",
        "0xe4", "0xe5", "0xe6", "0xe7", "0xe8", "0xe9", "0xea", "0xeb", "0xec",
        "0xed", "0xee", "0xef", "0xf0", "0xf1", "0xf2", "0xf3", "0xf4", "0xf5",
        "0xf6", "0xf7", "0xf8", "0xf9", "0xfa", "0xfb", "0xfc", "0xfd", "0xfe",
        "0xff"};

    static byte testSrcArray[] = {
        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14,
        (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19};

    static byte testSrcArrayLarge[] = {
        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14,
        (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19,
        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24,
        (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x00};

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Store content of some private strings used in class under test to
        // allow verification of thrown exception content.
        private_invalidPCAPField = PCAPImportFileParser.class.getDeclaredField("WARN_INVALID_PCAP");
        private_invalidPCAPField.setAccessible(true);
        private_invalidPCAPMsg = (String) private_invalidPCAPField.get(instance);

        private_byteToInt = PCAPImportFileParser.class.getDeclaredMethod("byteToInt", byte.class);
        private_byteToInt.setAccessible(true);
        private_byteToProtocol = PCAPImportFileParser.class.getDeclaredMethod("byteToProtocol", byte.class);
        private_byteToProtocol.setAccessible(true);
        private_byteToIPVersion = PCAPImportFileParser.class.getDeclaredMethod("byteToIPVersion", byte.class);
        private_byteToIPVersion.setAccessible(true);
        private_byteToIPv4HeaderLength = PCAPImportFileParser.class.getDeclaredMethod("byteToIPv4HeaderLength", byte.class);
        private_byteToIPv4HeaderLength.setAccessible(true);
        private_getBytes = PCAPImportFileParser.class.getDeclaredMethod("getBytes", byte[].class, int.class, int.class);
        private_getBytes.setAccessible(true);
        private_bytesToInt = PCAPImportFileParser.class.getDeclaredMethod("bytesToInt", byte[].class, int.class, int.class, StringBuilder.class, String.class);
        private_bytesToInt.setAccessible(true);
        private_bytesToMacAddressStr = PCAPImportFileParser.class.getDeclaredMethod("bytesToMacAddressStr", byte[].class, int.class);
        private_bytesToMacAddressStr.setAccessible(true);
        private_bytesToHexStr = PCAPImportFileParser.class.getDeclaredMethod("bytesToHexStr", byte[].class, int.class, int.class);
        private_bytesToHexStr.setAccessible(true);
        private_getEtherTypeStr = PCAPImportFileParser.class.getDeclaredMethod("getEtherTypeStr", int.class);
        private_getEtherTypeStr.setAccessible(true);
        private_bytesToIpv4Str = PCAPImportFileParser.class.getDeclaredMethod("bytesToIpv4Str", byte[].class, int.class);
        private_bytesToIpv4Str.setAccessible(true);
        private_bytesToIpv6Str = PCAPImportFileParser.class.getDeclaredMethod("bytesToIpv6Str", byte[].class, int.class);
        private_bytesToIpv6Str.setAccessible(true);
        private_getResults = PCAPImportFileParser.class.getDeclaredMethod("getResults", InputSource.class, int.class);
        private_getResults.setAccessible(true);
        private_openPcap = PCAPImportFileParser.class.getDeclaredMethod("openPcap", File.class);
        private_openPcap.setAccessible(true);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void checkParseInvalidPCAP() throws InterruptedException {
        // Confirm that attempts to parse invalid PCAP return a clean
        // IOException exception.
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            parser.parse(new InputSource(new File(this.getClass().getResource("./resources/PCAP-invalidContent.pcap").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(private_invalidPCAPMsg));
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkParseTruncatedPCAPHeader() throws InterruptedException {
        // Confirm that attempts to parse invalid PCAP return a clean
        // IOException exception.
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            parser.parse(new InputSource(new File(this.getClass().getResource("./resources/PCAP-truncated_header.pcap").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(private_invalidPCAPMsg));
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkParseTruncatedPCAPFrame1() throws InterruptedException {
        // Confirm that attempts to parse invalid PCAP return a clean
        // IOException exception.
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            List<String[]> results = parser.parse(new InputSource(new File(this.getClass().getResource("./resources/PCAP-truncated_frame1.pcap").getFile())), null);
            Assert.assertEquals(results.size(), 1, "results.size():");
            Assert.assertEquals(expectedHeadings, results.get(0), "results[0]:");
            
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }
    
    @Test
    public void checkParseTruncatedPCAPFrame2() throws InterruptedException {
        // Confirm that attempts to parse invalid PCAP return a clean
        // IOException exception.
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            List<String[]> results = parser.parse(new InputSource(new File(this.getClass().getResource("./resources/PCAP-truncated_frame2.pcap").getFile())), null);
            Assert.assertEquals(results.size(), 2, "results.size():");
            Assert.assertEquals(expectedHeadings, results.get(0), "results[0]:");
            Assert.assertEquals(expectedRow1, results.get(1), "results[1]:");
            
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }
    
    @Test
    public void checkPreviewInvalidPCAP() throws InterruptedException {
        // Confirm that attempts to preview invalid PCAP return a clean
        // IOException exception.
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/PCAP-invalidContent.pcap").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(private_invalidPCAPMsg));
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkPreviewTruncatedPCAPHeader() throws InterruptedException {
        // Confirm that attempts to preview invalid PCAP return a clean
        // IOException exception.
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/PCAP-truncated_header.pcap").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(private_invalidPCAPMsg));
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkPreviewTruncatedPCAPFrame1() throws InterruptedException {
        // Confirm that attempts to preview invalid PCAP return a clean
        // IOException exception.
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            List<String[]> results = parser.preview(new InputSource(new File(this.getClass().getResource("./resources/PCAP-truncated_frame1.pcap").getFile())), null, 100);
            Assert.assertEquals(results.size(), 1, "results.size():");
            Assert.assertEquals(expectedHeadings, results.get(0), "results[0]:");

        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkPreviewTruncatedPCAPFrame2() throws InterruptedException {
        // Confirm that attempts to preview invalid PCAP return a clean
        // IOException exception.
        final PCAPImportFileParser parser = new PCAPImportFileParser();
         List<String[]> results = null;
        try {
            results = parser.preview(new InputSource(new File(this.getClass().getResource("./resources/PCAP-truncated_frame2.pcap").getFile())), null, 100);
            
            Assert.assertEquals(results.size(), 2, "results.size():");
            Assert.assertEquals(results.get(0), expectedHeadings, "results[0]:");
            Assert.assertEquals(results.get(1), expectedRow1, "results[1]:");
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        } 
    }
      
    @Test
    public void checkbyteToInt() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            // Check some sample byte values, ensuring end cases are covered and confirm
            // correct int representation is received
            final byte bytesArray[] = {(byte) 0, (byte) 1, (byte) 127, (byte) 128, (byte) 255};
            final int intsArray[] = {0, 1, 127, 128, 255};

            for (int i = 0; i < bytesArray.length; i++) {
                Assert.assertEquals(intsArray[i], (int) private_byteToInt.invoke(parser, bytesArray[i]));
            }

        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbyteToProtocol() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            // Check some sample byte values, ensuring end cases are covered and confirm
            // correct int representation is received
            for (int i = 0; i < protocolMap.length; i++) {
                Assert.assertEquals(protocolMap[i], (String) private_byteToProtocol.invoke(parser, (byte)i));
            }
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbyteToIPVersion() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            // Check some sample byte values, ensuring end cases are covered and confirm
            // correct int representation is received
            final byte bytesArray[] = {(byte) 0x10, (byte) 0x20, (byte) 0x30, (byte) 0xf0, (byte) 0x4d, (byte) 0xff};
            final int intsArray[] = {1, 2, 3, 15, 4, 15};

            for (int i = 0; i < bytesArray.length; i++) {
                Assert.assertEquals(intsArray[i], (int) private_byteToIPVersion.invoke(parser, bytesArray[i]));
            }
        
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbyteToIPv4HeaderLength() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            // Check some sample byte values, ensuring end cases are covered and confirm
            // correct int representation is received
            final byte bytesArray[] = {(byte) 0x10, (byte) 0x21, (byte) 0x35, (byte) 0xf8, (byte) 0x4d, (byte) 0xff};
            final int intsArray[] = {0, 4, 20, 32, 52, 60};

            for (int i = 0; i < bytesArray.length; i++) {
                Assert.assertEquals(intsArray[i], (int) private_byteToIPv4HeaderLength.invoke(parser, bytesArray[i]));
            }

        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkgetBytes_offsetAssertionMin() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            byte result[] = (byte[]) private_getBytes.invoke(parser, testSrcArray, -1, 1);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkgetBytes_offsetAssertionMax() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            byte result[] = (byte[]) private_getBytes.invoke(parser, testSrcArray, 10, 1);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkgetBytes_sizeAssertion() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            byte result[] = (byte[]) private_getBytes.invoke(parser, testSrcArray, 0, 0);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkgetBytes() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        try {
            byte result[] = (byte[]) private_getBytes.invoke(parser, testSrcArray, 0, 1);
            Assert.assertEquals(result.length, 1, "result.length:");
            byte expected1[] = {0x10};
            Assert.assertEquals(result, expected1, "result");

            result = (byte[]) private_getBytes.invoke(parser, testSrcArray, 0, 5);
            Assert.assertEquals(result.length, 5, "result.length:");
            byte expected2[] = {0x10, 0x11, 0x12, 0x13, 0x14};
            Assert.assertEquals(result, expected2, "result");

            result = (byte[]) private_getBytes.invoke(parser, testSrcArray, 5, 5);
            Assert.assertEquals(result.length, 5, "result.length:");
            byte expected3[] = {0x15, 0x16, 0x17, 0x18, 0x19};
            Assert.assertEquals(result, expected3, "result");

            result = (byte[]) private_getBytes.invoke(parser, testSrcArray, 8, 5);
            Assert.assertEquals(result.length, 5, "result.length:");
            byte expected4[] = {0x18, 0x19, 0x00, 0x00, 0x00};
            Assert.assertEquals(result, expected4, "result");

        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToInt_offsetAssertionMin() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        StringBuilder infoBuilder = new StringBuilder();
        String sampleText = "EXAMPLE";

        try {
            private_bytesToInt.invoke(parser, testSrcArray, -1, 1, infoBuilder, sampleText);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToInt_offsetAssertionMax() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        StringBuilder infoBuilder = new StringBuilder();
        String sampleText = "EXAMPLE";

        try {
            private_bytesToInt.invoke(parser, testSrcArray, 10, 1, infoBuilder, sampleText);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToInt_sizeAssertionMin() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        StringBuilder infoBuilder = new StringBuilder();
        String sampleText = "EXAMPLE";

        try {
            private_bytesToInt.invoke(parser, testSrcArray, 0, 0, infoBuilder, sampleText);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToInt_sizeAssertionMax() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        StringBuilder infoBuilder = new StringBuilder();
        String sampleText = "EXAMPLE";

        try {
            private_bytesToInt.invoke(parser, testSrcArray, 0, 5, infoBuilder, sampleText);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToInt() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        StringBuilder infoBuilder = new StringBuilder();
        String sampleText = "EXAMPLE";

        try {
            int result1 = (int) private_bytesToInt.invoke(parser, testSrcArray, 0, 1, infoBuilder, sampleText);
            Assert.assertEquals(result1, 0x10, "result1");
            Assert.assertEquals(infoBuilder.toString(), "", "result1 infoBuilder");

            int result2 = (int) private_bytesToInt.invoke(parser, testSrcArray, 0, 2, infoBuilder, sampleText);
            Assert.assertEquals(result2, 0x1011, "result2");
            Assert.assertEquals(infoBuilder.toString(), "", "result2 infoBuilder");

            int result3 = (int) private_bytesToInt.invoke(parser, testSrcArray, 0, 3, infoBuilder, sampleText);
            Assert.assertEquals(result3, 0x101112, "result3");
            Assert.assertEquals(infoBuilder.toString(), "", "result3 infoBuilder");

            int result4 = (int) private_bytesToInt.invoke(parser, testSrcArray, 0, 4, infoBuilder, sampleText);
            Assert.assertEquals(result4, 0x10111213, "result4");
            Assert.assertEquals(infoBuilder.toString(), "", "result4 infoBuilder");

            int result5 = (int) private_bytesToInt.invoke(parser, testSrcArray, 8, 1, infoBuilder, sampleText);
            Assert.assertEquals(result5, 0x18, "result5");
            Assert.assertEquals(infoBuilder.toString(), "", "result5 infoBuilder");

            int result6 = (int) private_bytesToInt.invoke(parser, testSrcArray, 8, 2, infoBuilder, sampleText);
            Assert.assertEquals(result6, 0x1819, "result6");
            Assert.assertEquals(infoBuilder.toString(), "", "result6 infoBuilder");

            int result7 = (int) private_bytesToInt.invoke(parser, testSrcArray, 8, 3, infoBuilder, sampleText);
            Assert.assertEquals(result7, 0, "result7");
            Assert.assertEquals(infoBuilder.toString(), sampleText, "result7 infoBuilder");

            int result8 = (int) private_bytesToInt.invoke(parser, testSrcArray, 8, 4, infoBuilder, sampleText);
            Assert.assertEquals(result8, 0, "result8");
            Assert.assertEquals(infoBuilder.toString(), sampleText + sampleText, "result8 infoBuilder");

        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToMacAddressStr_offsetAssertionMin() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();

        try {
            private_bytesToMacAddressStr.invoke(parser, testSrcArray, -1);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToMacAddressStr_offsetAssertionMax() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();

        try {
            private_bytesToMacAddressStr.invoke(parser, testSrcArray, 10);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToMacAddressStr() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        StringBuilder infoBuilder = new StringBuilder();

        try {
            String result1 = (String) private_bytesToMacAddressStr.invoke(parser, testSrcArray, 0);
            Assert.assertEquals(result1, "10:11:12:13:14:15", "result1");
            Assert.assertEquals(infoBuilder.toString(), "", "result1 infoBuilder");

            String result2 = (String) private_bytesToMacAddressStr.invoke(parser, testSrcArray, 1);
            Assert.assertEquals(result2, "11:12:13:14:15:16", "result2");
            Assert.assertEquals(infoBuilder.toString(), "", "result2 infoBuilder");

            String result3 = (String) private_bytesToMacAddressStr.invoke(parser, testSrcArray, 2);
            Assert.assertEquals(result3, "12:13:14:15:16:17", "result3");
            Assert.assertEquals(infoBuilder.toString(), "", "result3 infoBuilder");

            String result4 = (String) private_bytesToMacAddressStr.invoke(parser, testSrcArray, 3);
            Assert.assertEquals(result4, "13:14:15:16:17:18", "result4");
            Assert.assertEquals(infoBuilder.toString(), "", "result4 infoBuilder");

            String result5 = (String) private_bytesToMacAddressStr.invoke(parser, testSrcArray, 4);
            Assert.assertEquals(result5, "14:15:16:17:18:19", "result5");
            Assert.assertEquals(infoBuilder.toString(), "", "result5 infoBuilder");

            String result6 = (String) private_bytesToMacAddressStr.invoke(parser, testSrcArray, 5);
            Assert.assertEquals(result6, "15:16:17:18:19:00", "result6");
            Assert.assertEquals(infoBuilder.toString(), "", "result6 infoBuilder");

            String result7 = (String) private_bytesToMacAddressStr.invoke(parser, testSrcArray, 6);
            Assert.assertEquals(result7, "16:17:18:19:00:00", "result7");
            Assert.assertEquals(infoBuilder.toString(), "", "result7 infoBuilder");

            String result8 = (String) private_bytesToMacAddressStr.invoke(parser, testSrcArray, 7);
            Assert.assertEquals(result8, "17:18:19:00:00:00", "result8");
            Assert.assertEquals(infoBuilder.toString(), "", "result8 infoBuilder");

            byte broadcast[] = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            String result9 = (String) private_bytesToMacAddressStr.invoke(parser, broadcast, 0);
            Assert.assertEquals(result9, "BROADCAST ff:ff:ff:ff:ff:ff");
            Assert.assertEquals(infoBuilder.toString(), "", "result9 infoBuilder");

        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToHexStr_offsetAssertionMin() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();

        try {
            private_bytesToHexStr.invoke(parser, testSrcArray, -1, 1);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToHexStr_offsetAssertionMax() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();

        try {
            private_bytesToHexStr.invoke(parser, testSrcArray, 10, 1);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToHexStr_sizeAssertionMin() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();

        try {
            private_bytesToHexStr.invoke(parser, testSrcArray, 1, 0);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToHexStr() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        StringBuilder infoBuilder = new StringBuilder();

        try {
            String result1 = (String) private_bytesToHexStr.invoke(parser, testSrcArray, 0, 1);
            Assert.assertEquals(result1, "10", "result1");
            Assert.assertEquals(infoBuilder.toString(), "", "result1 infoBuilder");

            String result2 = (String) private_bytesToHexStr.invoke(parser, testSrcArray, 0, 2);
            Assert.assertEquals(result2, "1011", "result2");
            Assert.assertEquals(infoBuilder.toString(), "", "result2 infoBuilder");

            String result3 = (String) private_bytesToHexStr.invoke(parser, testSrcArray, 0, 3);
            Assert.assertEquals(result3, "101112", "result3");
            Assert.assertEquals(infoBuilder.toString(), "", "result3 infoBuilder");

            String result4 = (String) private_bytesToHexStr.invoke(parser, testSrcArray, 0,10);
            Assert.assertEquals(result4, "10111213141516171819", "result4");
            Assert.assertEquals(infoBuilder.toString(), "", "result4 infoBuilder");

            String result5 = (String) private_bytesToHexStr.invoke(parser, testSrcArray, 8, 1);
            Assert.assertEquals(result5, "18", "result5");
            Assert.assertEquals(infoBuilder.toString(), "", "result5 infoBuilder");

            String result6 = (String) private_bytesToHexStr.invoke(parser, testSrcArray, 8, 2);
            Assert.assertEquals(result6, "1819", "result6");
            Assert.assertEquals(infoBuilder.toString(), "", "result6 infoBuilder");

            String result7 = (String) private_bytesToHexStr.invoke(parser, testSrcArray, 8, 3);
            Assert.assertEquals(result7, "181900", "result7");
            Assert.assertEquals(infoBuilder.toString(), "", "result7 infoBuilder");

            String result8 = (String) private_bytesToHexStr.invoke(parser, testSrcArray, 8, 4);
            Assert.assertEquals(result8, "18190000", "result8");
            Assert.assertEquals(infoBuilder.toString(), "", "result8 infoBuilder");

        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkgetEtherTypeStr() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();

        try {
            Assert.assertEquals(private_getEtherTypeStr.invoke(parser, 0x0800), "IPv4 Address");
            Assert.assertEquals(private_getEtherTypeStr.invoke(parser, 0x86DD), "IPv6 Address");
            Assert.assertEquals(private_getEtherTypeStr.invoke(parser, 0x0806), "");
            Assert.assertEquals(private_getEtherTypeStr.invoke(parser, 2), "");
            for (int i = 3; i <= 255; i++) {
                Assert.assertEquals(private_getEtherTypeStr.invoke(parser, i), "");
            }
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToIpv4Str_offsetAssertionMin() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();

        try {
            private_bytesToIpv4Str.invoke(parser, testSrcArray, -1);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToIpv4Str_offsetAssertionMax() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();

        try {
            private_bytesToIpv4Str.invoke(parser, testSrcArray, 10);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToIpv4Str() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();

        try {
            String result1 = (String) private_bytesToIpv4Str.invoke(parser, testSrcArray, 0);
            Assert.assertEquals(result1, "16.17.18.19", "result1");

            String result2 = (String) private_bytesToIpv4Str.invoke(parser, testSrcArray, 1);
            Assert.assertEquals(result2, "17.18.19.20", "result2");

            String result3 = (String) private_bytesToIpv4Str.invoke(parser, testSrcArray, 2);
            Assert.assertEquals(result3, "18.19.20.21", "result3");

            String result4 = (String) private_bytesToIpv4Str.invoke(parser, testSrcArray, 3);
            Assert.assertEquals(result4, "19.20.21.22", "result4");

            String result5 = (String) private_bytesToIpv4Str.invoke(parser, testSrcArray, 4);
            Assert.assertEquals(result5, "20.21.22.23", "result5");

            String result6 = (String) private_bytesToIpv4Str.invoke(parser, testSrcArray, 5);
            Assert.assertEquals(result6, "21.22.23.24", "result6");

            String result7 = (String) private_bytesToIpv4Str.invoke(parser, testSrcArray, 6);
            Assert.assertEquals(result7, "22.23.24.25", "result7");

            String result8 = (String) private_bytesToIpv4Str.invoke(parser, testSrcArray, 7);
            Assert.assertEquals(result8, "23.24.25.0", "result8");

        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToIpv6Str_offsetAssertionMin() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();

        try {
            private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, -1);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToIpv6Str_offsetAssertionMax() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();

        try {
            private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 20);
            Assert.fail("Expected assertion not received");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkbytesToIpv6Str() throws InterruptedException {
        final PCAPImportFileParser parser = new PCAPImportFileParser();

        try {
            String result1 = (String) private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 0);
            Assert.assertEquals(result1, "1011:1213:1415:1617:1819:2021:2223:2425", "result1");

            String result2 = (String) private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 1);
            Assert.assertEquals(result2, "1112:1314:1516:1718:1920:2122:2324:2526", "result2");

            String result3 = (String) private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 2);
            Assert.assertEquals(result3, "1213:1415:1617:1819:2021:2223:2425:2627", "result3");

            String result4 = (String) private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 3);
            Assert.assertEquals(result4, "1314:1516:1718:1920:2122:2324:2526:2728", "result4");

            String result5 = (String) private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 4);
            Assert.assertEquals(result5, "1415:1617:1819:2021:2223:2425:2627:2800", "result5");

            String result6 = (String) private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 5);
            Assert.assertEquals(result6, "1516:1718:1920:2122:2324:2526:2728:0", "result6");

            String result7 = (String) private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 6);
            Assert.assertEquals(result7, "1617:1819:2021:2223:2425:2627:2800:0", "result7");

            String result8 = (String) private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 7);
            Assert.assertEquals(result8, "1718:1920:2122:2324:2526:2728::", "result8");

            String result9 = (String) private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 8);
            Assert.assertEquals(result9, "1819:2021:2223:2425:2627:2800::", "result9");

            String result10 = (String) private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 9);
            Assert.assertEquals(result10, "1920:2122:2324:2526:2728::", "result10");

            String result11 = (String) private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 10);
            Assert.assertEquals(result11, "2021:2223:2425:2627:2800::", "result11");

            String result12 = (String) private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 11);
            Assert.assertEquals(result12, "2122:2324:2526:2728::", "result12");

            String result13 = (String) private_bytesToIpv6Str.invoke(parser, testSrcArrayLarge, 19);
            Assert.assertEquals(result13, "::", "result13");
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }
    }

    @Test
    public void checkGetResultsData() throws InterruptedException {
        // Overall check that getResults() method correctly retrieves
        // and sets all the necessary data
        final PCAPImportFileParser parser = new PCAPImportFileParser();

        try {
            List<String[]> results = (List<String[]>) private_getResults.invoke(parser,
                    new InputSource(new File(this.getClass().getResource("./resources/PCAP-truncated_frame2.pcap").getFile())), 0);
            // Check Frame sets properly
            Assert.assertEquals(results.get(1)[0], expectedRow1[0]);
            // Check DateTime
            Assert.assertEquals(results.get(1)[1], expectedRow1[1]);
            // Check Src MAC address
            Assert.assertEquals(results.get(1)[2], expectedRow1[2]);
            // Check Src IP address
            Assert.assertEquals(results.get(1)[3], expectedRow1[3]);
            // Check TCP Src Port
            Assert.assertEquals(results.get(1)[4], expectedRow1[4]);
            // Check Src Type
            Assert.assertEquals(results.get(1)[5], expectedRow1[5]);
            // Check Dest MAC address
            Assert.assertEquals(results.get(1)[6], expectedRow1[6]);
            // Check Dest IP address
            Assert.assertEquals(results.get(1)[7], expectedRow1[7]);
            // Check TCP Dest Port
            Assert.assertEquals(results.get(1)[8], expectedRow1[8]);
            // Check Dest Type
            Assert.assertEquals(results.get(1)[9], expectedRow1[9]);
            // Check Ethertype
            Assert.assertEquals(results.get(1)[10], expectedRow1[10]);
            // Check Protocol
            Assert.assertEquals(results.get(1)[11], expectedRow1[11]);
            // Check Packet Length
            Assert.assertEquals(results.get(1)[12], expectedRow1[12]);
            // Check Extra Info
            Assert.assertEquals(results.get(1)[13], expectedRow1[13]);
            
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        } 
    }
    
    @Test
    public void checkGetResultsOverall() throws InterruptedException {
        // Does an overall check on the data returned
        // This includes the packet headers 
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        
        expectedResults.add(expectedHeadings);
        expectedResults.add(expectedRow1);

        try {
            List<String[]> results = (List<String[]>) private_getResults.invoke(parser, 
                    new InputSource(new File(this.getClass().getResource("./resources/PCAP-truncated_frame2.pcap").getFile())), 0);                        
            Assert.assertEquals(results, expectedResults);

        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }  
    } 
    
    @Test
    public void checkGetResultsARPRequest() throws InterruptedException {
        // Completes a check into ARP specific packet handelling
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        
        try {
            List<String[]> results = (List<String[]>) private_getResults.invoke(parser, 
                    new InputSource(new File(this.getClass().getResource("./resources/PCAP-ARP_Reply-Response.pcap").getFile())), 0); 
            // Check Info message is correct
            Assert.assertEquals(results.get(1)[13], "Who has 10.10.10.1? Tell 10.10.10.2 ");
            //Check Protocol
            Assert.assertEquals(results.get(1)[11], "ARP");
            
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }  
    }
    
        @Test
    public void checkGetResultsARPReply() throws InterruptedException {
        // Completes a check into ARP specific packet handelling
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        
        try {
            List<String[]> results = (List<String[]>) private_getResults.invoke(parser, 
                    new InputSource(new File(this.getClass().getResource("./resources/PCAP-ARP_Reply-Response.pcap").getFile())), 0); 
            // Check Info message is correct
            Assert.assertEquals(results.get(2)[13], "10.10.10.1 is at 10.10.10.2 ");
            //Check Protocol
            Assert.assertEquals(results.get(2)[11], "ARP");
            
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }  
    }
    
    @Test
    public void checkGetResultsUnsupportedARP() throws InterruptedException {
        // Complete a check into Unsupported ARP packet types
                // Completes a check into ARP specific packet handelling
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        
        try {
            List<String[]> results = (List<String[]>) private_getResults.invoke(parser, 
                    new InputSource(new File(this.getClass().getResource("./resources/PCAP-unsupported_ARP.cap").getFile())), 0); 
            // Check Info message is correct
            Assert.assertEquals(results.get(1)[13], "ARP op code 0x0003 currently not supported. ");
            //Check Protocol
            Assert.assertEquals(results.get(1)[11], "ARP");
            
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }  
    }
    @Test
    public void checkGetResultsIPv6() throws InterruptedException {
        // Completes an overall check into IPv6 packet handelling
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        
        try {
            List<String[]> results = (List<String[]>) private_getResults.invoke(parser, 
                    new InputSource(new File(this.getClass().getResource("./resources/PCAP-IPv6_TCP.pcap").getFile())), 0);
            Assert.assertEquals(results.get(1), expectedIPv6Row1);
            
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        }  
    }
    
    @Test
    public void checkGetResultsIPv6UDP() throws InterruptedException {
        //Completes a check into IPv6 UDP packet handelling
        final PCAPImportFileParser parser = new PCAPImportFileParser();
        
        try {
            List<String[]> results = (List<String[]>) private_getResults.invoke(parser, 
                    new InputSource(new File(this.getClass().getResource("./resources/PCAP-IPv6_UDP.pcap").getFile())), 0);
            // Check UDP Src Port
            Assert.assertEquals(results.get(1)[4], "6363");
            // Check UDP Dest Port
            Assert.assertEquals(results.get(1)[8], "6363");
            // Check Protoc
            Assert.assertEquals(results.get(1)[11], "UDP");
            
        } catch ( Exception ex) {
            Assert.fail("Uxexpected exception received: " + ex.getClass().getName());
        }
    }
    
    @Test
    public void checkGetResultsIPv4UDP() throws InterruptedException {
        //Completes a check into IPv4 UDP packet handelling
       final PCAPImportFileParser parser = new PCAPImportFileParser();
        
        try {
            List<String[]> results = (List<String[]>) private_getResults.invoke(parser, 
                    new InputSource(new File(this.getClass().getResource("./resources/PCAP-IPv4_UDP-TCP.pcap").getFile())), 0);   
            
            // Check UDP Src Port
            Assert.assertEquals(results.get(1)[4], "138");
            // Check UDP Dest Port
            Assert.assertEquals(results.get(1)[8], "138");
            // Check Protocol 
            Assert.assertEquals(results.get(1)[11], "UDP");
            
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received: " + ex.getClass().getName());
        } 
    }
}

/*
 * Copyright 2010-2020 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.plugins.importexport.delimited.parser;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import com.google.common.net.InetAddresses;
import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.buffer.Buffer;
import io.pkts.packet.Packet;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Hex;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * PCAPImportFileParser: Performs basic parsing of PCAP (packet capture) files
 * extracting key values for import. The following columns are populated (where
 * possible): - Frame: The frame number of parsed frame - Time: timestamp of the
 * frame - Source/Destination MAC: Source/Destination MAC addresses -
 * Source/Destination IP: Source/Destination IP addresses - Source/Destination
 * Port: Source/Destination ports - Source/Destination Type: Source/Destination
 * IP address types - Ethertype: 2 byte Hex code identifying protocol
 * encapsulated by the ethernet frame - Protocol: The protocol contained in the
 * ethernet frame (ie TCP/UDP) - Length: Length of the ethernet frame - Info:
 * Miscellaneous information about the ethernet frame
 *
 * NOTE: This decoder does not process beyond the first level transport layer.
 * This differs from Wireshark (for instance) which extracts payload protocols
 * from the application layer. For instance, UDP packets containing NBNS
 * payloads are shown with protocol = UDP in this parser, while Wireshare would
 * show NBNS. NOTE: Wireshark does provide an export to CSV option, this
 * approach can be used as an alternate means to generate CSV files for import
 * into Constellation.
 *
 * @author serpens24
 */
@ServiceProvider(service = ImportFileParser.class)
public class PCAPImportFileParser extends ImportFileParser {

    private static final Logger LOGGER = Logger.getLogger(PCAPImportFileParser.class.getName());

    // Number of bytes in an integer
    private static final int INT_SIZE = 4;

    // Size of IP addresses as stored in IPv4 and IPv6 headers.
    private static final int IPV4_IP_SIZE = 4;
    private static final int IPV6_IP_SIZE = 16;

    /**
     * Ethernet II header format: https://en.wikipedia.org/wiki/Ethernet_frame
     *
     * Offset Size Summary 0 6 Destination MAC address 6 6 Source MAC address 12
     * 2 EtherType
     */
    private static final int HDR_ETHERNET_II_SIZE = 14;
    private static final int HDR_ETHERNET_II_DST_MAC_OFFSET = 0;
    private static final int HDR_ETHERNET_II_SRC_MAC_OFFSET = 6;
    private static final int HDR_ETHERNET_II_TYPE_OFFSET = 12;
    private static final int HDR_ETHERNET_II_MAC_SIZE = 6;
    private static final int HDR_ETHERNET_II_TYPE_SIZE = 2;

    /**
     * ARP header format:
     * https://en.wikipedia.org/wiki/Address_Resolution_Protocol
     * https://en.wikipedia.org/wiki/Address_Resolution_Protocol#Packet_structure
     *
     * Offset Size Summary 0 2 Hardware type 2 2 Protocol type 4 1 Hardware
     * length 5 1 Protocol length 6 2 Operation - currently only support
     * 0001/0002 which are request/reply codes 8 6 Sender MAC address 14 4
     * Sender protocol address 18 6 Target MAC address 24 4 Target protocol
     * address
     */
    private static final int HDR_ARP_PROTOCOL_OFFSET = 2;
    private static final int HDR_ARP_OPERATION_OFFSET = 6;
    private static final int HDR_ARP_SRC_IP_OFFSET = 14;
    private static final int HDR_ARP_DEST_IP_OFFSET = 24;
    private static final int HDR_ARP_PROTOCOL_SIZE = 2;
    private static final int HDR_ARP_OPERATION_SIZE = 2;

    /**
     * IPV4 header format: https://en.wikipedia.org/wiki/IPv4
     * https://en.wikipedia.org/wiki/IPv4#Packet_structure
     *
     * Offset Size Summary 0 1 (bits 0-3) Version, (bits 4-7) Header length - #
     * of 32 bit words 1 1 Priority & type of service 2 2 Total length (header
     * and data), min=20 bytes, max = 65535 bytes 4 2 Identification -
     * differentiate fragments from other datagrams 6 2 Flags, Fragment offset
     * (bit encoded) 8 1 Time to live (datagram lifetime limit) 9 1 Protocol
     * (Defines protocol in use. TCP = 6, UDP = 17) refer:
     * https://en.wikipedia.org/wiki/List_of_IP_protocol_numbers 10 2 Header
     * checksum 12 4 Source IP - one byte per part, i.e.:
     * <part1>.<part2>.<part3>.<part4>
     * 16 4 Destination IP - one byte per part, i.e.:
     * <part1>.<part2>.<part3>.<part4>
     * 20 ? Additional options. From 0 to 32 bytes
     */
    private static final int HDR_IPV4_VERSION_HDRSIZE_OFFSET = 0;
    private static final int HDR_IPV4_PAYLOADLENGTH_OFFSET = 2;
    private static final int HDR_IPV4_PROTOCOL_OFFSET = 9;
    private static final int HDR_IPV4_SRC_IP_OFFSET = 12;
    private static final int HDR_IPV4_DEST_IP_OFFSET = 16;
    private static final int HDR_IPV4_PAYLOADLENGTH_SIZE = 2;
    private static final int HDR_IPV4_PORT_SIZE = 2;

    /**
     * IPV6 header format: https://en.wikipedia.org/wiki/IPv6
     * https://en.wikipedia.org/wiki/IPv6_packet
     *
     * Offset Size Summary 0 1 (bits 0-3) Version, (bits 4-7) Traffic class -
     * the traffic class spills over into bits 8-11 of the byte with offset 1 1
     * 3 (bits 8-11) Traffic class (2nd half - see above), bits (12-21) flow
     * label 4 2 Payload length 6 1 Next header 7 1 Hop limit 8 16 Source
     * Address 24 16 Destination Address
     */
    private static final int HDR_IPV6_SIZE = 40;
    private static final int HDR_IPV6_VERSION_TRAFFCLASS_OFFSET = 0;
    private static final int HDR_IPV6_PROTOCOL_OFFSET = 6;
    private static final int HDR_IPV6_SRC_IP_OFFSET = 8;
    private static final int HDR_IPV6_DEST_IP_OFFSET = 24;
    private static final int HDR_IPV6_PORT_SIZE = 2;

    /**
     * TCP header format:
     * https://en.wikipedia.org/wiki/Transmission_Control_Protocol
     *
     * Offset Size Summary 0 2 Source port 2 2 Destination port 4 4 Sequence
     * number 8 4 Acknowledgement number 12 1 (bits 7-4) Data offset, (bit 0) NS
     * 13 1 8 x Bit masks (CWR,ECE,URG,ACK,PSH,RST,SYN,FIN) 14 2 Window size 16
     * 2 Checksum 18 2 Urgent pointer 20 ? Additional options, between 0 and 40
     * bytes depending on value of data offset value
     */
    private static final int TCP_SRC_PORT_OFFSET = 0;
    private static final int TCP_DEST_PORT_OFFSET = 2;

    /**
     * UDP header format: https://en.wikipedia.org/wiki/User_Datagram_Protocol
     *
     * Offset Size Summary 0 2 Source port 2 2 Destination port 4 2 Length 6 2
     * Checksum
     */
    private static final int UDP_SRC_PORT_OFFSET = 0;
    private static final int UDP_DEST_PORT_OFFSET = 2;

    /**
     * Key Ethernet II header etherType values, these are extracted from
     * etherType field of the Ethernet II header. This field can contain many
     * values, only those handled by this decoder are shown. All other etherType
     * values result in minimal parsing, just extracting source and destination
     * MAC addresses and other data gleaned from the Ethernet II header.
     *
     * Refer to: https://en.wikipedia.org/wiki/EtherType
     */
    private static final int TYPE_IPV4 = 0x0800;
    private static final int TYPE_ARP = 0x0806;
    private static final int TYPE_IPV6 = 0x86DD;

    // MAC address representing a broadcast
    private static final String BROADCAST_MACADDRESS = "ff:ff:ff:ff:ff:ff";

    /**
     * Key IPv4/IPv6 header protocol types. Only those protocols explicitly
     * processed by this class are listed here. Others can be added as required.
     *
     * Refer to: https://en.wikipedia.org/wiki/List_of_IP_protocol_numbers
     */
    private static final int IP_PROT_TCP = 0x06;
    private static final int IP_PROT_UDP = 0x11;

    /**
     * Key ARP operation codes.
     *
     * Refer to:
     * https://www.iana.org/assignments/arp-parameters/arp-parameters.xhtml
     */
    private static final int ARP_REQUEST_ID = 0x0001;
    private static final int ARP_REPLY_ID = 0x0002;

    // Counter keeping track of frame being processed
    private static int frameCounter = 1;

    /**
     * Construct a new JSONImportFileParser with "JSON" label at position 4.
     */
    public PCAPImportFileParser() {
        super("PCAP", 4);
    }

    /**
     * Perform zero padded conversion of a byte to an integer. As an integer
     * uses 4 bytes, 3 leading 0 packed bytes are pre-pended to the supplied
     * byte.
     *
     * @param value the byte to convert to an integer.
     * @return Integer equivalent of supplied value.
     */
    private int byteToInt(final byte value) {
        final byte bytesArray[] = {0, 0, 0, value};
        return ByteBuffer.wrap(bytesArray).getInt();
    }

    /**
     * Take the given byte and return the equivalent protocol string, as defined
     * in the following URL:
     * https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml
     *
     * @param value Byte code of protocol to decode.
     * @return String representation of supplied byte value, or 0x0000 format
     * hex code of the byte if no mapping exists.
     */
    private String byteToProtocol(final byte value) {
        final byte valueArray[] = {value};
        final int intValue = byteToInt(value);
        // Values sourced from https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml
        switch (intValue) {
            case (0):
                return "HOPOPT";
            case (1):
                return "ICMP";
            case (2):
                return "IGMP";
            case (3):
                return "GGP";
            case (4):
                return "IPv4";
            case (5):
                return "ST";
            case (6):
                return "TCP";
            case (7):
                return "CBT";
            case (8):
                return "EGP";
            case (9):
                return "IGP";
            case (10):
                return "BBN-RCC-MON";
            case (11):
                return "NVP-II";
            case (12):
                return "PUP";
            case (13):
                return "ARGUS (deprecated)";
            case (14):
                return "EMCON";
            case (15):
                return "XNET";
            case (16):
                return "CHAOS";
            case (17):
                return "UDP";
            case (18):
                return "MUX";
            case (19):
                return "DCN-MEAS";
            case (20):
                return "HMP";
            case (21):
                return "PRM";
            case (22):
                return "XNS-IDP";
            case (23):
                return "TRUNK-1";
            case (24):
                return "TRUNK-2";
            case (25):
                return "LEAF-1";
            case (26):
                return "LEAF-2";
            case (27):
                return "RDP";
            case (28):
                return "IRTP";
            case (29):
                return "ISO-TP4";
            case (30):
                return "NETBLT";
            case (31):
                return "MFE-NSP";
            case (32):
                return "MERIT-INP";
            case (33):
                return "DCCP";
            case (34):
                return "3PC";
            case (35):
                return "IDPR";
            case (36):
                return "XTP";
            case (37):
                return "DDP";
            case (38):
                return "IDPR-CMTP";
            case (39):
                return "TP++";
            case (40):
                return "IL";
            case (41):
                return "IPv6";
            case (42):
                return "SDRP";
            case (43):
                return "IPv6-Route";
            case (44):
                return "IPv6-Frag";
            case (45):
                return "IDRP";
            case (46):
                return "RSVP";
            case (47):
                return "GRE";
            case (48):
                return "DSR";
            case (49):
                return "BNA";
            case (50):
                return "ESP";
            case (51):
                return "AH";
            case (52):
                return "I-NLSP";
            case (53):
                return "SWIPE (deprecated)";
            case (54):
                return "NARP";
            case (55):
                return "MOBILE";
            case (56):
                return "TLSP";
            case (57):
                return "SKIP";
            case (58):
                return "IPv6-ICMP";
            case (59):
                return "IPv6-NoNxt";
            case (60):
                return "IPv6-Opts";
            case (62):
                return "CFTP";
            case (64):
                return "SAT-EXPAK";
            case (65):
                return "KRYPTOLAN";
            case (66):
                return "RVD";
            case (67):
                return "IPPC";
            case (69):
                return "SAT-MON";
            case (70):
                return "VISA";
            case (71):
                return "IPCV";
            case (72):
                return "CPNX";
            case (73):
                return "CPHB";
            case (74):
                return "WSN";
            case (75):
                return "PVP";
            case (76):
                return "BR-SAT-MON";
            case (77):
                return "SUN-ND";
            case (78):
                return "WB-MON";
            case (79):
                return "WB-EXPAK";
            case (80):
                return "ISO-IP";
            case (81):
                return "VMTP";
            case (82):
                return "SECURE-VMTP";
            case (83):
                return "VINES";
            case (84):
                return "TTP";
            case (85):
                return "NSFNET-IGP";
            case (86):
                return "DGP";
            case (87):
                return "TCF";
            case (88):
                return "EIGRP";
            case (89):
                return "OSPFIGP";
            case (90):
                return "Sprite-RPC";
            case (91):
                return "LARP";
            case (92):
                return "MTP";
            case (93):
                return "AX.25";
            case (94):
                return "IPIP";
            case (95):
                return "MICP (deprecated)";
            case (96):
                return "SCC-SP";
            case (97):
                return "ETHERIP";
            case (98):
                return "ENCAP";
            case (100):
                return "GMTP";
            case (101):
                return "IFMP";
            case (102):
                return "PNNI";
            case (103):
                return "PIM";
            case (104):
                return "ARIS";
            case (105):
                return "SCPS";
            case (106):
                return "QNX";
            case (107):
                return "A/N";
            case (108):
                return "IPComp";
            case (109):
                return "SNP";
            case (110):
                return "Compaq-Peer";
            case (111):
                return "IPX-in-IP";
            case (112):
                return "VRRP";
            case (113):
                return "PGM";
            case (115):
                return "L2TP";
            case (116):
                return "DDX";
            case (117):
                return "IATP";
            case (118):
                return "STP";
            case (119):
                return "SRP";
            case (120):
                return "UTI";
            case (121):
                return "SMP";
            case (122):
                return "SM (deprecated)";
            case (123):
                return "PTP";
            case (124):
                return "ISIS over IPv4";
            case (125):
                return "FIRE";
            case (126):
                return "CRTP";
            case (127):
                return "CRUDP";
            case (128):
                return "SSCOPMCE";
            case (129):
                return "IPLT";
            case (130):
                return "SPS";
            case (131):
                return "PIPE";
            case (132):
                return "SCTP";
            case (133):
                return "FC";
            case (134):
                return "RSVP-E2E-IGNORE";
            case (135):
                return "Mobility Header";
            case (136):
                return "UDPLite";
            case (137):
                return "MPLS-in-IP";
            case (138):
                return "manet";
            case (139):
                return "HIP";
            case (140):
                return "Shim6";
            case (141):
                return "WESP";
            case (142):
                return "ROHC";
            default:
                return ("0x" + Hex.encodeHexString(valueArray));
        }
    }

    /**
     * The first byte of an Ethernet II header for IPv4 and IPv6 uses the most
     * significant 4 bits to identify the version of the protocol. This function
     * sucks out these 4 bits and converts to and integer.
     *
     * @param value Byte to extract version from (using most significant 4 bits)
     * @return Version packed inside most significant 4 bits of supplied byte.
     */
    private int byteToIPVersion(final byte value) {
        return ((value & 0xFF) >> 4);
    }

    /**
     * The first byte of an IPv4 message has the header length (number of 4 bit
     * words) contained in the message. This is packed into least significant 4
     * bits. This function sucks out these 4 bits and converts to an integer.
     *
     * @param value Byte to extract size from (using most significant 4 bits)
     * @return
     */
    private int byteToIPv4HeaderLength(final byte value) {
        return (value & 0x0F) * 4;
    }

    /**
     * Extracts an array of bytes from bytes, of requested size, beginning at
     * the byte with supplied offset. Range exceptions will not be caught,
     * rather they are thrown and allowed to be handled by the calling function.
     *
     * @param bytes Source byte array to extract bytes from
     * @param offset Starting offset of bytes to extract from bytes
     * @param size Number of bytes to extract from bytes
     * @return byte array of requested size starting at offset byte. If this
     * stretches past the end of the bytes array, extra bytes are zeroed
     * @exception IndexOutOfBoundsException if requested offset is out of range
     * of the source bytes array
     * @throws Assertion if requested offset or size to extract is negative
     */
    private byte[] getBytes(final byte[] bytes, int offset, int size) {
        assert (offset >= 0 && size > 0); 
        return Arrays.copyOfRange(bytes, offset, offset + size);
    }

    /**
     * Take the set of bytes (1-4) requested from the source bytes array and
     * convert them into a zero packed integer. This method is private, as such,
     * we can assume that it is called with a valid size value. An assertion is
     * thrown if an invalid integer size is supplied - this would occur due to a
     * coding error.
     *
     * @param bytes Source byte array to extract integer from
     * @param offset The offset into the bytes array (starting from 0) that the
     * integer is to be extracted from
     * @param size Number of bytes to extract (and use for integer) This value
     * should be between 1 and 4.
     * @param infoBuilder The StringBuilder being used to build up and info
     * string. If there are issues extracting the integer due to not being able
     * to extract the correct number of bytes then this string builder is
     * updated with the supplied infoString
     * @param infoString String to add to infoBuilder should the offset + size
     * exceed the array bounds of bytes array
     * @return The integer value extracted from the supplied bytes
     * @throws Assertion if requested offset or size to extract is negative or
     * outside of the allowable range
     */
    private int bytesToInt(final byte[] bytes, int offset, int size, StringBuilder infoBuilder, String infoString) {
        assert (offset >= 0 && size > 0 && size <= INT_SIZE);

        // Ensure that source bytes array is large enough to accomodate the
        // request. If not, set infoBuilder to supplied infoString and return
        // integer = 0.
        if (offset + size <= bytes.length) {
            final byte[] extractedBytes = getBytes(bytes, offset, size);
            final byte bytesArray[] = {0, 0, 0, 0};
            for (int i = 0; i < extractedBytes.length; i++) {
                bytesArray[INT_SIZE + i - extractedBytes.length] = extractedBytes[i];
            }
            return ByteBuffer.wrap(bytesArray).getInt();
        }
        infoBuilder.append(infoString);
        return 0;
    }

    /**
     * Extract a MAC address from the supplied bytes array starting at the given
     * offset.
     *
     * @param bytes Source byte array to extract MAC address from
     * @param offset The offset into the bytes array (starting from 0) that the
     * MAC address will be extracted from
     * @return MAC address string extracted from HDR_ETHERNET_II_MAC_SIZE bytes
     * starting at requested offset byte in bytes array. If this stretches past
     * the end of the bytes array, extra bytes are zeroed prior to being
     * converted to a MAC address
     * @exception IndexOutOfBoundsException if requested offset is out of range
     * of the source bytes array
     * @throws Assertion if requested offset is negative
     */
    private String bytesToMacAddressStr(final byte[] bytes, int offset) {
        assert (offset >= 0);

        final byte[] extractedBytes = getBytes(bytes, offset, HDR_ETHERNET_II_MAC_SIZE);
        final String rawString = Hex.encodeHexString(extractedBytes);
        final StringBuilder builder = new StringBuilder();
        int byteIndex = 0;
        while (byteIndex < HDR_ETHERNET_II_MAC_SIZE) {
            builder.append(rawString.charAt(byteIndex * 2));
            builder.append(rawString.charAt(byteIndex * 2 + 1));
            if (byteIndex < (HDR_ETHERNET_II_MAC_SIZE - 1)) {
                builder.append(":");
            }
            byteIndex++;
        }
        if (BROADCAST_MACADDRESS.equals(builder.toString())) {
            return "BROADCAST ff:ff:ff:ff:ff:ff";
        }
        return builder.toString();
    }

    /**
     * Return a hex string representation of the requested subset of bytes from
     * the source bytes array, starting at given offset and using the specified
     * size.
     *
     * @param bytes Source byte array to extract hex string from
     * @param offset The offset into the bytes array (starting from 0) that the
     * hex string should be extracted from
     * @param size The number of bytes to convert to a hex string representation
     * @return Hex string representation of the given bytes
     * @exception IndexOutOfBoundsException if requested offset is out of range
     * of the source bytes array
     * @throws Assertion if requested offset or size is negative
     */
    private String bytesToHexStr(final byte[] bytes, int offset, int size) {
        assert (offset >= 0 && size > 0);

        return Hex.encodeHexString(getBytes(bytes, offset, size));
    }

    /**
     * Return string representation of given etherType value.
     *
     * @param etherType code to show string representation of
     * @return String representation of supplied etherType or "" if no mapping
     * found
     */
    private String getEtherTypeStr(final int etherType) {
        switch (etherType) {
            case TYPE_IPV4:
                return "IPv4 Address";
            case TYPE_IPV6:
                return "IPv6 Address";
            default:
                return "";
        }
    }

    /**
     * Take 4 bytes from the supplied source bytes array at given offset and
     * extract IPv4 address using one byte per address element.
     *
     * @param bytes Source byte array to extract IPv4 address from
     * @param offset Offset into source byte array to extract IPv4 address from
     * @return IPv4 string representation of supplied bytes.
     * @exception IndexOutOfBoundsException if requested offset is out of range
     * of the source bytes array
     * @throws Assertion if requested offset is negative
     */
    private String bytesToIpv4Str(final byte[] bytes, int offset) {
        assert (offset >= 0);

        final StringBuilder builder = new StringBuilder();
        for (byte element : getBytes(bytes, offset, IPV4_IP_SIZE)) {
            builder.append(String.valueOf((int)(element & 0xff)));
            builder.append(".");
        }
        return builder.toString().substring(0, builder.toString().length() - 1);
    }

    /**
     * Take 12 bytes from the supplied source bytes array at given offset and
     * extract IPv6 address using two bytes per address element.
     *
     * @param bytes Source byte array to extract IPv6 address from
     * @param offset Offset into source byte array to extract IPv6 address from
     * @return IPv6 string representation of supplied bytes.
     * @exception IndexOutOfBoundsException if requested offset is out of range
     * of the source bytes array
     * @throws Assertion if requested offset is negative
     */
    private String bytesToIpv6Str(final byte[] bytes, int offset) {
        assert (offset >= 0);

        // Loop through IPv6 address segments (each are 2 bytes in size)
        // Some shortening done as per rules 1 & 2: https://www.ciscopress.com/articles/article.asp?p=2803866
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < (IPV6_IP_SIZE / 2); i++) {
            final String segmentStr =
                    Hex.encodeHexString(Arrays.copyOfRange(getBytes(bytes, offset, IPV6_IP_SIZE), (2 * i), (2 * (i + 1))));
            builder.append(segmentStr).append(":");
        }
        return InetAddresses.toAddrString(
                InetAddresses.forString(builder.toString().substring(0, builder.toString().length() - 1)));
    }

    /**
     * Designed to perform common decoding as required by the parse and preview
     * functions. Perform common extraction of data from PCAP file. This uses
     * the io.pkts library (where possible) to decode content of PCAP file,
     * looping over contained packets and extracting key information.
     *
     * @param input Input file
     * @param limit Row limit
     * @return a List of String arrays, each of which represents a row in the
     * resulting table.
     * @throws IOException
     */
    private List<String[]> getResults(final InputSource input, final int limit) throws IOException {

        // Define names of 'columns' that will be extracted from PCAP
        final String[] headings = {
            "Frame", "Time",
            "Src MAC", "Src IP", "Src Port", "Src Type",
            "Dest MAC", "Dest IP", "Dest Port", "Dest Type",
            "Ethertype", "Protocol", "Length", "Info"};

        // Define results object to be used and add headings, prior to looping
        // through and adding row data
        final List<String[]> results = new ArrayList<>();
        results.add(headings);

        try {

            // Open the supplied PCAP file and reset frameCounter
            final Pcap pcap = Pcap.openStream(input.getFile());
            frameCounter = 1;

            // Use io.pkts fucntionality to extract rawe packets and iterate
            // over them. Breaking out individual payload fields is not using
            // io.pkts as its quite restrictive, this is instead being done on
            // a bespoke basis. At some point, there may be a case to try and
            // remove all dependencies on io.pkts and manually loop through
            // PCAP data frames
            pcap.loop(new PacketHandler() {
                @Override
                public boolean nextPacket(final Packet packet) throws IOException {

                    // Extract timestamp and frame counter strings
                    final SimpleDateFormat formatter =
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    final Date pktDate = new Date(packet.getArrivalTime() / 1000);
                    final String timestamp = formatter.format(pktDate);

                    // Store string representation of frameCounter so that it
                    // doesnt have to be constantly converted
                    final String frame = Integer.toString(frameCounter);

                    // Use string builder to allow info to be tied to packet
                    // to provide decoding context (when appropriate)
                    StringBuilder infoBuilder = new StringBuilder();

                    // Extract payload ethIIBuffer containing the Ethernet
                    // header as well as plus payload (headers and data).
                    final Buffer ethIIBuffer = packet.getPayload();
                    final byte[] ethIIBufferArray = ethIIBuffer.getArray();

                    // Convert raw header fields of interest to strings ready
                    // for population
                    final String destMacAddress = bytesToMacAddressStr(ethIIBufferArray,
                            HDR_ETHERNET_II_DST_MAC_OFFSET);
                    final String srcMacAddress = bytesToMacAddressStr(ethIIBufferArray,
                            HDR_ETHERNET_II_SRC_MAC_OFFSET);
                    final int etherType = bytesToInt(ethIIBufferArray,
                            HDR_ETHERNET_II_TYPE_OFFSET, HDR_ETHERNET_II_TYPE_SIZE,
                            infoBuilder, "Packet truncated. Unable to extract ethertype.");
                    final String length = Integer.toString(ethIIBuffer.capacity());
                    final String etherTypeStr = bytesToHexStr(ethIIBufferArray,
                            HDR_ETHERNET_II_TYPE_OFFSET, HDR_ETHERNET_II_TYPE_SIZE);
                    int protocolCode = 0;
                    String protocol = "";
                    String srcIP = "";
                    String srcPort = "";
                    String srcType = "";
                    String destIP = "";
                    String destPort = "";
                    String destType = "";

                    try {
                        switch (etherType) {
                            case TYPE_IPV4:
                                // Break off IPV4 packet. Rather than just bite off size based on message content, take
                                // rest of payload. This avoids issues where incorrect message sizes are marked inside
                                // header - which does happen enough in sample PCAPs to consider.
                                final byte[] ipv4BufferArray = getBytes(ethIIBufferArray, HDR_ETHERNET_II_SIZE,
                                        (ethIIBuffer.capacity() - HDR_ETHERNET_II_SIZE));

                                // Extract header length and overall IPv4 packet length. Often there seem to be
                                // issues (especially with older sample PCAPs) of invalid sizes. These are handled
                                // by adding an info message, and by not trusing the overall packet size value.
                                final int ipv4HeaderLength
                                        = byteToIPv4HeaderLength(ipv4BufferArray[HDR_IPV4_VERSION_HDRSIZE_OFFSET]);
                                final int ipv4length = bytesToInt(ipv4BufferArray, HDR_IPV4_PAYLOADLENGTH_OFFSET,
                                        HDR_IPV4_PAYLOADLENGTH_SIZE, infoBuilder,
                                        "Packet truncated (Unable to extract IPv4 length). ");
                                if (ipv4length < ipv4HeaderLength) {
                                    infoBuilder.append("Bogus IP length (").append(ipv4length).append(", less than header length ")
                                            .append(ipv4HeaderLength).append("). ");
                                }

                                // Extract IPv version and confirm its valid. Sample data shows that a lot (particularly
                                // older) PCAPS have incorrect versions set. (Ie IPv4 headers with a version other than
                                // 4. These are just flagged with info entries, but continue to be decoded - assuming that
                                // the etherType value is correct.
                                final int ipv4version = byteToIPVersion(ipv4BufferArray[HDR_IPV4_VERSION_HDRSIZE_OFFSET]);
                                if (ipv4version != 4) {
                                    infoBuilder.append("Bogus IPv4 version (").append(ipv4version).append(", must be 4). ");
                                    LOGGER.log(Level.INFO, "Frame {0}. {1}",
                                            new Object[]{frameCounter, infoBuilder.toString()});
                                }

                                // Extract key output fields based on values contained in IPv4 message
                                srcType = getEtherTypeStr(etherType);
                                destType = srcType;
                                srcIP = bytesToIpv4Str(ipv4BufferArray, HDR_IPV4_SRC_IP_OFFSET);
                                destIP = bytesToIpv4Str(ipv4BufferArray, HDR_IPV4_DEST_IP_OFFSET);
                                protocol = byteToProtocol(ipv4BufferArray[HDR_IPV4_PROTOCOL_OFFSET]);
                                protocolCode = byteToInt(ipv4BufferArray[HDR_IPV4_PROTOCOL_OFFSET]);

                                // Currently the only field we need to extract from IPv4 payload are ports for TCP/UDP.
                                // As we have already stored payload, just grab corresponding bytes offset from start of
                                // the IPv4 message buffer. While both TCP and UDP are at the same offset, individual
                                // constants have been defiend for readability.
                                switch (protocolCode) {
                                    case IP_PROT_TCP:
                                        srcPort = Integer.toString(bytesToInt(ipv4BufferArray,
                                                ipv4HeaderLength + TCP_SRC_PORT_OFFSET,
                                                HDR_IPV4_PORT_SIZE, infoBuilder,
                                                "Packet truncated (Unable to extract src port). "));
                                        destPort = Integer.toString(bytesToInt(ipv4BufferArray,
                                                ipv4HeaderLength + TCP_DEST_PORT_OFFSET,
                                                HDR_IPV4_PORT_SIZE, infoBuilder,
                                                "Packet truncated (Unable to extract dest port). "));
                                        break;
                                    case IP_PROT_UDP:
                                        srcPort = Integer.toString(bytesToInt(ipv4BufferArray,
                                                ipv4HeaderLength + UDP_SRC_PORT_OFFSET,
                                                HDR_IPV4_PORT_SIZE, infoBuilder,
                                                "Packet truncated (Unable to extract src port). "));
                                        destPort = Integer.toString(bytesToInt(ipv4BufferArray,
                                                ipv4HeaderLength + UDP_DEST_PORT_OFFSET,
                                                HDR_IPV4_PORT_SIZE, infoBuilder,
                                                "Packet truncated (Unable to extract dest port). "));
                                        break;
                                }
                                break;

                            case TYPE_IPV6:
                                // Break off IPV6 packet. Rather than just bite off size based on message content, take
                                // rest of payload. This avoids issues where incorrect message sizes are marked inside
                                // header - which does happen enough in sample PCAPs to consider.
                                final byte[] ipv6BufferArray = getBytes(ethIIBufferArray, HDR_ETHERNET_II_SIZE,
                                        (ethIIBuffer.capacity() - HDR_ETHERNET_II_SIZE));

                                // Extract IPv version and confirm its valid. Sample data shows that a lot (particularly
                                // older) PCAPS have incorrect versions set. (Ie IPv6 headers with a version other than
                                // 6. These are just flagged with info entries, but continue to be decoded - assuming that
                                // the etherType value is correct.
                                final int ipv6version
                                        = byteToIPVersion(ipv6BufferArray[HDR_IPV6_VERSION_TRAFFCLASS_OFFSET]);
                                if (ipv6version != 6) {
                                    infoBuilder.append("Bogus IPv6 version (").append(ipv6version).append(", must be 6). ");
                                    LOGGER.log(Level.INFO, "Frame {0}. {1}",
                                            new Object[]{frameCounter, infoBuilder.toString()});
                                }

                                // Extract key output fields based on values contained in IPv6 message
                                srcType = getEtherTypeStr(etherType);
                                destType = srcType;
                                srcIP = bytesToIpv6Str(ipv6BufferArray, HDR_IPV6_SRC_IP_OFFSET);
                                destIP = bytesToIpv6Str(ipv6BufferArray, HDR_IPV6_DEST_IP_OFFSET);
                                protocol = byteToProtocol(ipv6BufferArray[HDR_IPV6_PROTOCOL_OFFSET]);
                                protocolCode = byteToInt(ipv6BufferArray[HDR_IPV6_PROTOCOL_OFFSET]);

                                // Currently the only field we need to extract from IPv6 payload are ports for TCP/UDP.
                                // As we have already stored payload, just grab corresponding bytes offset from start of
                                // the IPv6 message ethIIBuffer. While both TCP and UDP are at the same offset, individual
                                // constants have been defiend for readability.
                                switch (protocolCode) {
                                    case IP_PROT_TCP:
                                        srcPort = Integer.toString(bytesToInt(ipv6BufferArray,
                                                HDR_IPV6_SIZE + TCP_SRC_PORT_OFFSET,
                                                HDR_IPV6_PORT_SIZE, infoBuilder,
                                                "Packet truncated (Unable to extract src port). "));
                                        destPort = Integer.toString(bytesToInt(ipv6BufferArray,
                                                HDR_IPV6_SIZE + TCP_DEST_PORT_OFFSET,
                                                HDR_IPV6_PORT_SIZE, infoBuilder,
                                                "Packet truncated (Unable to extract dest port). "));
                                        break;
                                    case IP_PROT_UDP:
                                        srcPort = Integer.toString(bytesToInt(ipv6BufferArray,
                                                HDR_IPV6_SIZE + UDP_SRC_PORT_OFFSET,
                                                HDR_IPV6_PORT_SIZE, infoBuilder,
                                                "Packet truncated (Unable to extract src port). "));
                                        destPort = Integer.toString(bytesToInt(ipv6BufferArray,
                                                HDR_IPV6_SIZE + UDP_DEST_PORT_OFFSET,
                                                HDR_IPV6_PORT_SIZE, infoBuilder,
                                                "Packet truncated (Unable to extract dest port). "));
                                        break;
                                }
                                break;
                            case TYPE_ARP:

                                // Extract ARP header from payload (in fact get all of packet)
                                final byte[] arpBufferArray = getBytes(ethIIBufferArray, HDR_ETHERNET_II_SIZE,
                                        (ethIIBuffer.capacity() - HDR_ETHERNET_II_SIZE));

                                // xtract key output fields based on values contained in ARP message
                                srcType = getEtherTypeStr(bytesToInt(arpBufferArray, HDR_ARP_PROTOCOL_OFFSET,
                                        HDR_ARP_PROTOCOL_SIZE, infoBuilder,
                                        "Packet truncated (Unable to extract ARP ethertype). "));
                                destType = srcType;
                                srcIP = bytesToIpv4Str(arpBufferArray, HDR_ARP_SRC_IP_OFFSET);
                                destIP = bytesToIpv4Str(arpBufferArray, HDR_ARP_DEST_IP_OFFSET);
                                protocol = "ARP";

                                // Based on operation type, populate the info string
                                final int arpOp = bytesToInt(arpBufferArray, HDR_ARP_OPERATION_OFFSET, HDR_ARP_OPERATION_SIZE,
                                        infoBuilder, "Packet truncated (Unable to extract ARP operation). ");
                                switch (arpOp) {
                                    case ARP_REQUEST_ID:
                                        infoBuilder.append("Who has ").append(destIP).append("? Tell ").append(srcIP).append(" ");
                                        break;
                                    case ARP_REPLY_ID:
                                        infoBuilder.append(srcIP).append(" is at ").append(destIP).append(" ");
                                        break;
                                    default:
                                        byte[] operationBytes
                                                = getBytes(arpBufferArray, HDR_ARP_OPERATION_OFFSET, HDR_ARP_OPERATION_SIZE);
                                        infoBuilder.append("ARP op code 0x").append(Hex.encodeHexString(operationBytes))
                                                .append(" currently not supported. ");
                                }
                                break;

                            default:
                                // The default case covers Ethernet II packets where specified etherType code does
                                // not cover a packet type we explictly covered. New cases can be added to add new
                                // etherType values as required - in line with values discussed in
                                // https://en.wikipedia.org/wiki/EtherType
                                infoBuilder.append("Unknown packet type (").append(etherTypeStr).append("). ");
                                protocol = etherTypeStr;
                        }

                        // Populate data to be sent
                        final String[] row = {
                            frame, timestamp,
                            srcMacAddress, srcIP, srcPort, srcType,
                            destMacAddress, destIP, destPort, destType,
                            etherTypeStr, protocol, length, infoBuilder.toString()};
                        results.add(row);

                    } catch (final AssertionError e) {
                        final String[] row = {
                            frame, timestamp,
                            srcMacAddress, srcIP, srcPort, srcType,
                            destMacAddress, destIP, destPort, destType,
                            etherTypeStr, protocol, length, infoBuilder.toString()};
                        results.add(row);
                        LOGGER.log(Level.INFO, "Unexpected assertion thrown parsing frame {0}, "
                                + "packet content not extracted. {1}", new Object[]{frameCounter, e.toString()});
                    } catch (final Exception e) {
                        final String[] row = {
                            frame, timestamp,
                            srcMacAddress, srcIP, srcPort, srcType,
                            destMacAddress, destIP, destPort, destType,
                            etherTypeStr, protocol, length, infoBuilder.toString()};
                        results.add(row);
                        LOGGER.log(Level.INFO, "Unexpected exception thrown parsing frame {0}, "
                                + "packet content not extracted. {1}", new Object[]{frameCounter, e.toString()});
                    }

                    // Increment frame counter. If limit of frames has been reached no further processing will occur
                    frameCounter++;
                    return (frameCounter <= limit || limit == 0);
                }
            });

            return results;

        } catch (final IndexOutOfBoundsException ex) {
            // io.pkts library doesn't gracefully handle truncated PCAP files and throws an IndexOutOfBoundsException
            // exception
            LOGGER.log(Level.INFO,
                    "Processing of PCAP truncated at row={0}, potentially truncated PCAP file encountered. {1}",
                    new Object[]{frameCounter, ex.toString()});
            return results;
        } catch (final Exception ex) {
            // Unexpected exceptions
            Exceptions.printStackTrace(ex);
            throw (ex);
        }
    }

    /**
     * Reads the entire file and returns a List of String arrays, each of which
     * represents a row in the resulting table.
     *
     * @param input Input file
     * @param parameters the parameters that configure the parse operation.
     * @return a List of String arrays, each of which represents a row in the
     * resulting table.
     * @throws IOException if an error occurred while reading the file.
     */
    @Override
    public List<String[]> parse(final InputSource input, final PluginParameters parameters) throws IOException {
        return getResults(input, 0);
    }

    /**
     * Reads only {@code limit} lines and returns a List of String arrays, each
     * of which represents a row in the resulting table.
     *
     * @param input Input file
     * @param parameters the parameters that configure the parse operation.
     * @param limit Row limit
     * @return a List of String arrays, each of which represents a row in the
     * resulting table.
     * @throws IOException if an error occurred while reading the file.
     */
    @Override
    public List<String[]> preview(final InputSource input, final PluginParameters parameters, final int limit) throws IOException {
        return getResults(input, 0);
    }
}

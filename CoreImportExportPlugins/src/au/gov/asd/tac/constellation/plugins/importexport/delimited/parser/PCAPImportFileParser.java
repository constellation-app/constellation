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
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import com.google.common.net.InetAddresses;
import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.buffer.Buffer;
import io.pkts.packet.Packet;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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

    private static final String WARN_PARSING_PREFIX
            = "Extracting data from PCAP file failed.\n";
    private static final String WARN_INVALID_PCAP
            = WARN_PARSING_PREFIX + "Unable to parse file, invalid PCAP.";
    private static final String INFO_TRUNCATED_SRC_PORT
            = "Packet truncated (Unable to extract source port). ";
    private static final String INFO_TRUNCATED_DEST_PORT
            = "Packet truncated (Unable to extract destination port). ";
    private static final String INFO_TRUNCATED_ETHERTYPE
            = "Packet truncated (Unable to extract ARP ethertype). ";
    private static final String INFO_TRUNCATED_ARPOPERATION
            = "Packet truncated (Unable to extract ARP operation). ";
    
    
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

    /**
     * Map of known IPv4/IPv6 protocol codes encoded in respective packet
     * headers.
     * Refer to:
     * https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml
     */
    private static final String[] PROTOCOL_MAP = {
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
    
    // Counter keeping track of frame being processed
    private int frameCounter = 1;

    /**
     * Construct a new PCAPImportFileParser with "PCAP" label at position 4.
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
        final byte[] bytesArray = {0, 0, 0, value};
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
        return PROTOCOL_MAP[byteToInt(value)];
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
        assert (offset >= 0 && offset < bytes.length && size > 0); 
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
        assert (offset >= 0 && offset < bytes.length && size > 0 && size <= INT_SIZE);

        // Ensure that source bytes array is large enough to accomodate the
        // request. If not, set infoBuilder to supplied infoString and return
        // integer = 0.
        if (offset + size <= bytes.length) {
            final byte[] extractedBytes = getBytes(bytes, offset, size);
            final byte[] bytesArray = {0, 0, 0, 0};
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
        assert (offset >= 0 && offset < bytes.length);

        final byte[] extractedBytes = getBytes(bytes, offset, HDR_ETHERNET_II_MAC_SIZE);
        final String rawString = Hex.encodeHexString(extractedBytes);
        final StringBuilder builder = new StringBuilder();
        int byteIndex = 0;
        while (byteIndex < HDR_ETHERNET_II_MAC_SIZE) {
            builder.append(rawString.charAt(byteIndex * 2));
            builder.append(rawString.charAt(byteIndex * 2 + 1));
            if (byteIndex < (HDR_ETHERNET_II_MAC_SIZE - 1)) {
                builder.append(SeparatorConstants.COLON);
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
        assert (offset >= 0 && offset < bytes.length && size > 0);

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
            builder.append(String.valueOf(element & 0xff));
            builder.append(SeparatorConstants.PERIOD);
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
            builder.append(segmentStr).append(SeparatorConstants.COLON);
        }
        return InetAddresses.toAddrString(
                InetAddresses.forString(builder.toString().substring(0, builder.toString().length() - 1)));
    }

    /**
     * Open and return the supplied PCAP file, with exception handling wrapper
     * to ensure exceptions are caught and propagated in a controlled manner.
     * @param file PCAP file to open
     * @return opened PCAP file
     * @throws IOException 
     */
    private Pcap openPcap(File file) throws IOException {
        try {
                return Pcap.openStream(file);
            } catch (final Exception ex) {
                throw new IOException(WARN_INVALID_PCAP);
            }
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
            Pcap pcap = openPcap(input.getFile());
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
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
                    // Set a default timezone to ensure consistant times across multiple locations
                    // Currently set to GMT +0
                    formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
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
                                                HDR_IPV4_PORT_SIZE, infoBuilder, INFO_TRUNCATED_SRC_PORT));
                                        destPort = Integer.toString(bytesToInt(ipv4BufferArray,
                                                ipv4HeaderLength + TCP_DEST_PORT_OFFSET,
                                                HDR_IPV4_PORT_SIZE, infoBuilder, INFO_TRUNCATED_DEST_PORT));
                                        break;
                                    case IP_PROT_UDP:
                                        srcPort = Integer.toString(bytesToInt(ipv4BufferArray,
                                                ipv4HeaderLength + UDP_SRC_PORT_OFFSET,
                                                HDR_IPV4_PORT_SIZE, infoBuilder, INFO_TRUNCATED_SRC_PORT));
                                        destPort = Integer.toString(bytesToInt(ipv4BufferArray,
                                                ipv4HeaderLength + UDP_DEST_PORT_OFFSET,
                                                HDR_IPV4_PORT_SIZE, infoBuilder, INFO_TRUNCATED_DEST_PORT));
                                        break;
                                    default:
                                        // No ports expected
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
                                                HDR_IPV6_PORT_SIZE, infoBuilder, INFO_TRUNCATED_SRC_PORT));
                                        destPort = Integer.toString(bytesToInt(ipv6BufferArray,
                                                HDR_IPV6_SIZE + TCP_DEST_PORT_OFFSET,
                                                HDR_IPV6_PORT_SIZE, infoBuilder, INFO_TRUNCATED_DEST_PORT));
                                        break;
                                    case IP_PROT_UDP:
                                        srcPort = Integer.toString(bytesToInt(ipv6BufferArray,
                                                HDR_IPV6_SIZE + UDP_SRC_PORT_OFFSET,
                                                HDR_IPV6_PORT_SIZE, infoBuilder, INFO_TRUNCATED_SRC_PORT));
                                        destPort = Integer.toString(bytesToInt(ipv6BufferArray,
                                                HDR_IPV6_SIZE + UDP_DEST_PORT_OFFSET,
                                                HDR_IPV6_PORT_SIZE, infoBuilder, INFO_TRUNCATED_DEST_PORT));
                                        break;
                                    default:
                                        // No ports expected
                                        break;
                                }
                                break;
                            case TYPE_ARP:

                                // Extract ARP header from payload (in fact get all of packet)
                                final byte[] arpBufferArray = getBytes(ethIIBufferArray, HDR_ETHERNET_II_SIZE,
                                        (ethIIBuffer.capacity() - HDR_ETHERNET_II_SIZE));

                                // Extract key output fields based on values contained in ARP message
                                srcType = getEtherTypeStr(bytesToInt(arpBufferArray, HDR_ARP_PROTOCOL_OFFSET,
                                        HDR_ARP_PROTOCOL_SIZE, infoBuilder, INFO_TRUNCATED_ETHERTYPE));
                                destType = srcType;
                                srcIP = bytesToIpv4Str(arpBufferArray, HDR_ARP_SRC_IP_OFFSET);
                                destIP = bytesToIpv4Str(arpBufferArray, HDR_ARP_DEST_IP_OFFSET);
                                protocol = "ARP";

                                // Based on operation type, populate the info string
                                final int arpOp = bytesToInt(arpBufferArray, HDR_ARP_OPERATION_OFFSET,
                                        HDR_ARP_OPERATION_SIZE, infoBuilder, INFO_TRUNCATED_ARPOPERATION);
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
            // exception. Catch this gracefully as its an expected event.
            LOGGER.log(Level.INFO,
                    "Processing of PCAP truncated at row={0}, potentially truncated PCAP file encountered. {1}",
                    new Object[]{frameCounter, ex.toString()});
            return results;
        } catch (final IllegalArgumentException ex) {
            // This exception is thrown by the io.pkts package when receiving an unexpected file type
            throw new IOException(WARN_INVALID_PCAP);
        }
        catch (final Exception ex) {
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
        return getResults(input, limit);
    }
}

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
import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.buffer.Buffer;
import io.pkts.packet.Packet;
import io.pkts.packet.IPPacket;
import io.pkts.packet.TCPPacket;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.Protocol;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author serpens24
 */
@ServiceProvider(service = ImportFileParser.class)
public class PCAPImportFileParser extends ImportFileParser {

    private static final Logger LOGGER = Logger.getLogger(PCAPImportFileParser.class.getName());    
    private final List<String[]> allResults = new ArrayList<>();
    private int frameCounter = 1;
 
    /**
     * Construct a new JSONImportFileParser with "JSON" label at position 4.
     */
    public PCAPImportFileParser() {
        super("PCAP", 4);
    }
        
    private List<String[]> getResults(final InputSource input, final int limit) throws IOException {
        try {
            final String[] headings = {"Frame", "Time", "Source", "Source Port", "Source Type", "Destination", "Destination Port", "Destination Type", "Protocol", "Length"};
            
            // TODO: use classwide allResults to give visibility into nextPacket, is there another way
            allResults.clear();
            allResults.add(headings);
            
            // Open the supplied PCAP file
            final Pcap pcap = Pcap.openStream(input.getFile());
            frameCounter = 1;
            
            pcap.loop(new PacketHandler() {
                @Override
                public boolean nextPacket(final Packet packet) throws IOException {
                    
                    IPPacket ipPacket = null;
                    
                    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    Date timestamp = new Date(packet.getArrivalTime() / 1000);
                    final String frame = Integer.toString(frameCounter);
                    String sourceIP = "";
                    String sourcePort = "";
                    String sourceType = "";
                    String destinationIP = "";
                    String destinationPort = "";
                    String destinationType = "";
                    String protocol = "";
                    String ipVersion = "";
                    Buffer buffer = packet.getPayload();
                    String length = Integer.toString(buffer.capacity());
                         
                    try {               
                        if (packet.hasProtocol(Protocol.TCP)) {
                            protocol = "TCP";
                            TCPPacket tcpPacket = (TCPPacket) packet.getPacket(Protocol.TCP);
                            sourcePort = Integer.toString(tcpPacket.getSourcePort());
                            destinationPort = Integer.toString(tcpPacket.getDestinationPort());
                            ipPacket = tcpPacket.getParentPacket();
                        } else if (packet.hasProtocol(Protocol.UDP)) {
                            protocol = "UDP";
                            UDPPacket udpPacket = (UDPPacket) packet.getPacket(Protocol.UDP);
                            sourcePort = Integer.toString(udpPacket.getSourcePort());
                            destinationPort = Integer.toString(udpPacket.getDestinationPort());
                            ipPacket = udpPacket.getParentPacket();
                        }
                        
                        if (ipPacket != null) {
                            ipVersion = "IPv" + Integer.toString(ipPacket.getVersion());
                            sourceIP = ipPacket.getSourceIP();
                            sourceType = ipVersion + " Address";
                            destinationIP = ipPacket.getDestinationIP();
                            destinationType = ipVersion + " Address";
                        }
                        
                        LOGGER.log(Level.INFO, "{0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}",
                            new Object[]{frame, formatter.format(timestamp),
                            sourceIP, sourcePort, sourceType, destinationIP, destinationPort, destinationType, protocol, length});
                        
                        final String[] row = {frame, formatter.format(timestamp),
                            sourceIP, sourcePort, sourceType, destinationIP, destinationPort, destinationType, protocol, length};
                        
                        allResults.add(row);
                        
                    } catch (final AssertionError e) {
                        LOGGER.log(Level.INFO, "ASSERTION THROWN 1 {0}", e.toString());
                    } catch (final Exception e) {
                        LOGGER.log(Level.INFO, "EXCEPTION THROWN 1 {0}", e.toString());
                    }
                    
                    frameCounter ++;
                    return (frameCounter <= limit || limit == 0);
                }
            });
            
            return allResults;
            
        } catch (final AssertionError e) {
            LOGGER.log(Level.INFO, "ASSERTION THROWN 2 {0}", e.toString());
            return allResults;
        } catch (final Exception ex) {
            // Unexpected exceptions
            LOGGER.log(Level.INFO, "EXCEPTION THROWN 2");
            return allResults;
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

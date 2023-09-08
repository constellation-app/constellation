/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.analytic.concept;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.CompositeNodeStateAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.RawAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.TransactionTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.VertexTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.AnalyticIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.CharacterIconProvider;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * A SchemaConcept for elements which support analysis of a graph.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = SchemaConcept.class, position = 500)
public class AnalyticConcept extends SchemaConcept {

    private static final String DEUTERANOPIA = "Deuteranopia";
    private static final String PROTANOPIA = "Protanopia";
    private static final String TRITANOPIA = "Tritanopia";
    private static final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
    //Retrieve colorblind mode selection preference 
    private static String COLORMODE = prefs.get(ApplicationPreferenceKeys.COLORBLIND_MODE, ApplicationPreferenceKeys.COLORBLIND_MODE_DEFAULT);

    @Override
    public String getName() {
        return "Analysis";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(SchemaConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }

    public static final class VertexAttribute {

        public static final SchemaAttribute COMPOSITE_STATE = new SchemaAttribute.Builder(GraphElementType.VERTEX, CompositeNodeStateAttributeDescription.ATTRIBUTE_NAME, "composite_state")
                .setDescription("State information for a composite node")
                .setDefaultValue(null)
                .build();
        public static final SchemaAttribute TYPE = new SchemaAttribute.Builder(GraphElementType.VERTEX, VertexTypeAttributeDescription.ATTRIBUTE_NAME, "Type")
                .setDescription("The type of this node")
                .create()
                .build();
        public static final SchemaAttribute RAW = new SchemaAttribute.Builder(GraphElementType.VERTEX, RawAttributeDescription.ATTRIBUTE_NAME, "Raw")
                .setDescription("The raw identifier and type of this node")
                .create()
                .build();
        public static final SchemaAttribute WEIGHT = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Weight")
                .setDescription("The weight of this node")
                .build();
        public static final SchemaAttribute SOURCE = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Source")
                .setDescription("The source of this node")
                .create()
                .build();
        public static final SchemaAttribute SEED = new SchemaAttribute.Builder(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "Seed")
                .setDescription("Is this node a seed?")
                .setDefaultValue(false)
                .build();
        public static final SchemaAttribute COMMENT = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Comment")
                .setDescription("A comment about this node")
                .build();
    }

    public static class TransactionAttribute {

        public static final SchemaAttribute TYPE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, TransactionTypeAttributeDescription.ATTRIBUTE_NAME, "Type")
                .setDescription("The type of the transaction")
                .create()
                .build();
        public static final SchemaAttribute ACTIVITY = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Activity")
                .setDescription("The type of activity this transaction represents")
                .create()
                .build();
        public static final SchemaAttribute COUNT = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, IntegerAttributeDescription.ATTRIBUTE_NAME, "Count")
                .setDescription("The number of events this transaction represents")
                .setDefaultValue(1)
                .setLabel(true)
                .build();
        public static final SchemaAttribute WEIGHT = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "Weight")
                .setDescription("The weight of the transaction")
                .build();
        public static final SchemaAttribute SOURCE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Source")
                .setDescription("The source of the transaction")
                .create()
                .build();
        public static final SchemaAttribute COMMENT = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Comment")
                .setDescription("A comment about this transaction")
                .build();
    }

    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> schemaAttributes = new ArrayList<>();
        schemaAttributes.add(VertexAttribute.COMPOSITE_STATE);
        schemaAttributes.add(VertexAttribute.TYPE);
        schemaAttributes.add(VertexAttribute.RAW);
        schemaAttributes.add(VertexAttribute.WEIGHT);
        schemaAttributes.add(VertexAttribute.SOURCE);
        schemaAttributes.add(VertexAttribute.SEED);
        schemaAttributes.add(VertexAttribute.COMMENT);
        schemaAttributes.add(TransactionAttribute.TYPE);
        schemaAttributes.add(TransactionAttribute.ACTIVITY);
        schemaAttributes.add(TransactionAttribute.COUNT);
        schemaAttributes.add(TransactionAttribute.WEIGHT);
        schemaAttributes.add(TransactionAttribute.SOURCE);
        schemaAttributes.add(TransactionAttribute.COMMENT);
        return Collections.unmodifiableCollection(schemaAttributes);
    }

    public static class VertexType {

        static final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        //Retrieve colorblind mode selection preference 
        static final String COLORMODE = prefs.get(ApplicationPreferenceKeys.COLORBLIND_MODE, ApplicationPreferenceKeys.COLORBLIND_MODE_DEFAULT);

        public static final SchemaVertexType TELEPHONE_IDENTIFIER;
        public static final SchemaVertexType MACHINE_IDENTIFIER;
        public static final SchemaVertexType ONLINE_IDENTIFIER;
        public static final SchemaVertexType LOCATION;
        public static final SchemaVertexType DOCUMENT;
        public static final SchemaVertexType EVENT;
        public static final SchemaVertexType PLACEHOLDER;
        public static final SchemaVertexType EMAIL_ADDRESS;

        //Change the color scheme of vertexes with poor contrast based on color blind mode selection 
        static {
            if (COLORMODE.equals(DEUTERANOPIA) || COLORMODE.equals(PROTANOPIA) || COLORMODE.equals(TRITANOPIA)) {
                TELEPHONE_IDENTIFIER = new SchemaVertexType.Builder("Telephone Identifier")
                        .setDescription("A node representing the identifier of a telephony device or service, eg. the phone number +6101234567")
                        .setColor(ConstellationColor.BUTTERMILK)
                        .setForegroundIcon(AnalyticIconProvider.CALL)
                        .setDetectionRegex(Pattern.compile("\\+?\\d{8,15}", Pattern.CASE_INSENSITIVE))
                        .setValidationRegex(Pattern.compile("^\\+?\\d{8,15}$", Pattern.CASE_INSENSITIVE))
                        .build();
                MACHINE_IDENTIFIER = new SchemaVertexType.Builder("Machine Identifier")
                        .setDescription("A node representing the identifier of a physical machine, eg. the MAC address A1:B2:C3:D4:E5:F6")
                        .setColor(ConstellationColor.BLUSH)
                        .setForegroundIcon(AnalyticIconProvider.MICROPROCESSOR)
                        .build();
                ONLINE_IDENTIFIER = new SchemaVertexType.Builder("Online Identifier")
                        .setDescription("A node representing the identifier of an account on a network, eg. the reddit username reddit_user")
                        .setColor(ConstellationColor.MIDNIGHT)
                        .setForegroundIcon(AnalyticIconProvider.PERSON)
                        .build();
                LOCATION = new SchemaVertexType.Builder("Location")
                        .setDescription("A node representing a geographic location, eg. the country Australia")
                        .setColor(ConstellationColor.BLUE)
                        .setForegroundIcon(AnalyticIconProvider.GLOBE)
                        .build();
                DOCUMENT = new SchemaVertexType.Builder("Document")
                        .setDescription("A node representing a document, eg. a text file")
                        .setColor(ConstellationColor.DARK_PURPLE)
                        .setForegroundIcon(AnalyticIconProvider.PAPERCLIP)
                        .build();
                EVENT = new SchemaVertexType.Builder("Event")
                        .setDescription("A node representing an event, eg. a stage show")
                        .setColor(ConstellationColor.BROWN)
                        .setForegroundIcon(AnalyticIconProvider.SIGNAL)
                        .build();
                PLACEHOLDER = new SchemaVertexType.Builder("Placeholder")
                        .setDescription("A node representing a placeholder which can be used for special purposes in CONSTELLATION")
                        .setColor(ConstellationColor.LIME)
                        .setForegroundIcon(AnalyticIconProvider.STAR)
                        .build();
                EMAIL_ADDRESS = new SchemaVertexType.Builder("Email")
                        .setDescription("A node representing an email address")
                        .setColor(ConstellationColor.RED)
                        .setForegroundIcon(AnalyticIconProvider.EMAIL)
                        .setSuperType(ONLINE_IDENTIFIER)
                        .setDetectionRegex(Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" // user component
                                + "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4]\\d)|1\\d\\d|[1-9]?\\d))\\.){3}(?:(2(5[0-5]|[0-4]\\d)|1\\d\\d|[1-9]?\\d)|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])", // domain component 
                                Pattern.CASE_INSENSITIVE))
                        .setValidationRegex(Pattern.compile("^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" // user component
                                + "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4]\\d)|1\\d\\d|[1-9]?\\d))\\.){3}(?:(2(5[0-5]|[0-4]\\d)|1\\d\\d|[1-9]?\\d)|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])$", // domain component 
                                Pattern.CASE_INSENSITIVE))
                        .build();
            } else {
                TELEPHONE_IDENTIFIER = new SchemaVertexType.Builder("Telephone Identifier")
                        .setDescription("A node representing the identifier of a telephony device or service, eg. the phone number +6101234567")
                        .setColor(ConstellationColor.EMERALD)
                        .setForegroundIcon(AnalyticIconProvider.CALL)
                        .setDetectionRegex(Pattern.compile("\\+?\\d{8,15}", Pattern.CASE_INSENSITIVE))
                        .setValidationRegex(Pattern.compile("^\\+?\\d{8,15}$", Pattern.CASE_INSENSITIVE))
                        .build();
                MACHINE_IDENTIFIER = new SchemaVertexType.Builder("Machine Identifier")
                        .setDescription("A node representing the identifier of a physical machine, eg. the MAC address A1:B2:C3:D4:E5:F6")
                        .setColor(ConstellationColor.CHOCOLATE)
                        .setForegroundIcon(AnalyticIconProvider.MICROPROCESSOR)
                        .build();
                ONLINE_IDENTIFIER = new SchemaVertexType.Builder("Online Identifier")
                        .setDescription("A node representing the identifier of an account on a network, eg. the reddit username reddit_user")
                        .setColor(ConstellationColor.AZURE)
                        .setForegroundIcon(AnalyticIconProvider.PERSON)
                        .build();
                LOCATION = new SchemaVertexType.Builder("Location")
                        .setDescription("A node representing a geographic location, eg. the country Australia")
                        .setColor(ConstellationColor.CARROT)
                        .setForegroundIcon(AnalyticIconProvider.GLOBE)
                        .build();
                DOCUMENT = new SchemaVertexType.Builder("Document")
                        .setDescription("A node representing a document, eg. a text file")
                        .setColor(ConstellationColor.BANANA)
                        .setForegroundIcon(AnalyticIconProvider.PAPERCLIP)
                        .build();
                EVENT = new SchemaVertexType.Builder("Event")
                        .setDescription("A node representing an event, eg. a stage show")
                        .setColor(ConstellationColor.PEACH)
                        .setForegroundIcon(AnalyticIconProvider.SIGNAL)
                        .build();
                PLACEHOLDER = new SchemaVertexType.Builder("Placeholder")
                        .setDescription("A node representing a placeholder which can be used for special purposes in CONSTELLATION")
                        .setColor(ConstellationColor.TEAL)
                        .setForegroundIcon(AnalyticIconProvider.STAR)
                        .build();
                EMAIL_ADDRESS = new SchemaVertexType.Builder("Email")
                        .setDescription("A node representing an email address")
                        .setColor(ConstellationColor.MUSK)
                        .setForegroundIcon(AnalyticIconProvider.EMAIL)
                        .setSuperType(ONLINE_IDENTIFIER)
                        .setDetectionRegex(Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" // user component
                                + "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4]\\d)|1\\d\\d|[1-9]?\\d))\\.){3}(?:(2(5[0-5]|[0-4]\\d)|1\\d\\d|[1-9]?\\d)|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])", // domain component 
                                Pattern.CASE_INSENSITIVE))
                        .setValidationRegex(Pattern.compile("^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" // user component
                                + "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4]\\d)|1\\d\\d|[1-9]?\\d))\\.){3}(?:(2(5[0-5]|[0-4]\\d)|1\\d\\d|[1-9]?\\d)|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])$", // domain component 
                                Pattern.CASE_INSENSITIVE))
                        .build();
            }
        }

        public static final SchemaVertexType GRAPH = new SchemaVertexType.Builder(String.format("%s Graph", BrandingUtilities.APPLICATION_NAME))
                .setDescription(String.format("A node representing a %s Graph", BrandingUtilities.APPLICATION_NAME))
                .setColor(ConstellationColor.AZURE)
                .setForegroundIcon(AnalyticIconProvider.STAR)
                .build();
        public static final SchemaVertexType NETWORK_IDENTIFIER = new SchemaVertexType.Builder("Network Identifier")
                .setDescription("A node representing the identifier of equipment on a network, eg. the ip address 192.168.0.1")
                .setColor(ConstellationColor.GREY)
                .setForegroundIcon(AnalyticIconProvider.INTERNET)
                .build();

        public static final SchemaVertexType ONLINE_LOCATION = new SchemaVertexType.Builder("Online Location")
                .setDescription("A node representing a location on a network, eg. the url https://www.some-domain.com")
                .setColor(ConstellationColor.BLUEBERRY)
                .setForegroundIcon(AnalyticIconProvider.UNIFORM_RESOURCE_LOCATOR)
                .build();
        public static final SchemaVertexType PERSON = new SchemaVertexType.Builder("Person")
                .setDescription("A node representing a person, eg. Joe Bloggs")
                .setColor(ConstellationColor.AMETHYST)
                .setForegroundIcon(AnalyticIconProvider.PERSON)
                .build();
        public static final SchemaVertexType ORGANISATION = new SchemaVertexType.Builder("Organisation")
                .setDescription("An node representing an organisation or company, eg. ACME Corporation")
                .setColor(ConstellationColor.AMETHYST).setForegroundIcon(AnalyticIconProvider.GROUP)
                .build();
        public static final SchemaVertexType WORD = new SchemaVertexType.Builder("Word")
                .setDescription("A node representing a word")
                .setColor(ConstellationColor.CYAN)
                .setForegroundIcon(AnalyticIconProvider.PAPERCLIP)
                .build();
        public static final SchemaVertexType HASH = new SchemaVertexType.Builder("Hash")
                .setDescription("A node representing a hash")
                .setColor(ConstellationColor.CYAN)
                .setForegroundIcon(CharacterIconProvider.CHAR_0023)
                .build();
        // network identifier derived types
        public static final SchemaVertexType IP_ADDRESS = new SchemaVertexType.Builder("IP Address")
                .setDescription("A node representing an IP address")
                .setSuperType(NETWORK_IDENTIFIER)
                .build();
        public static final SchemaVertexType IPV4 = new SchemaVertexType.Builder("IPv4 Address")
                .setDescription("A node representing an IP address (in the IPv4 format) on a network, eg. the private ip address 192.168.0.1")
                .setSuperType(IP_ADDRESS)
                .setDetectionRegex(Pattern.compile("(?:(?:25[0-5]|(2[0-4]|1?\\d)?\\d)\\.){3}(?:25[0-5]|(2[0-4]|1?\\d)?\\d)", Pattern.CASE_INSENSITIVE))
                .setValidationRegex(Pattern.compile("^(?:(?:25[0-5]|(2[0-4]|1?\\d)?\\d)\\.){3}(?:25[0-5]|(2[0-4]|1?\\d)?\\d)$", Pattern.CASE_INSENSITIVE))
                .build();
        public static final SchemaVertexType IPV6 = new SchemaVertexType.Builder("IPv6 Address")
                .setDescription("A node representing an IP address (in the IPv6 format) on a network, eg. the ip address 1:2:3:4:5:6:7:8")
                .setSuperType(IP_ADDRESS)
                .setDetectionRegex(Pattern.compile("(?:"
                        + "(?:[0-9a-fA-F]{1,4}:){1,4}:(?:(?:25[0-5]|(?:2[0-4]|1?\\d)?\\d)\\.){3}(?:25[0-5]|(?:2[0-4]|1?\\d)?\\d)|" // 2001:db8:3:4::192.0.2.33 | 64:ff9b::192.0.2.33 (IPv4-Embedded IPv6 Address)
                        + "::(?:[fF]{4}(?::0{1,4})?:)?(?:(?:25[0-5]|(?:2[0-4]|1?\\d)?\\d)\\.){3}(?:25[0-5]|(?:2[0-4]|1?\\d)?\\d)|" // ::255.255.255.255 | ::ffff:255.255.255.255 | ::ffff:0:255.255.255.255 (IPv4-mapped IPv6 addresses and IPv4-translated addresses)
                        + "[fe|FE]80:(?::[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|" // fe80::7:8%eth0 | fe80::7:8%1 (link-local IPv6 addresses with zone index)
                        + ":(?:(?::[0-9a-fA-F]{1,4}){1,7}|:)|" // ::2:3:4:5:6:7:8 | ::2:3:4:5:6:7:8 | ::8 | ::
                        + "[0-9a-fA-F]{1,4}:(?::[0-9a-fA-F]{1,4}){1,6}|" // 1::3:4:5:6:7:8 | 1::3:4:5:6:7:8 | 1::8
                        + "(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}|" // 1::4:5:6:7:8 | 1:2::4:5:6:7:8 | 1:2::8
                        + "(?:[0-9a-fA-F]{1,4}:){1,3}(?::[0-9a-fA-F]{1,4}){1,4}|" // 1::5:6:7:8 | 1:2:3::5:6:7:8 | 1:2:3::8
                        + "(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}|" // 1::6:7:8 | 1:2:3:4::6:7:8 | 1:2:3:4::8
                        + "(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}|" // 1::7:8 | 1:2:3:4:5::7:8 | 1:2:3:4:5::8
                        + "(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" // 1::8 | 1:2:3:4:5:6::8 | 1:2:3:4:5:6::8
                        + "(?:[0-9a-fA-F]{1,4}:){1,7}:|" // 1:: | 1:2:3:4:5:6:7::
                        + "(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}" // 1:2:3:4:5:6:7:8
                        + ")", Pattern.CASE_INSENSITIVE))
                .setValidationRegex(Pattern.compile("^(?:"
                        + "(?:[0-9a-fA-F]{1,4}:){1,4}:(?:(?:25[0-5]|(?:2[0-4]|1?\\d)?\\d)\\.){3}(?:25[0-5]|(?:2[0-4]|1?\\d)?\\d)|" // 2001:db8:3:4::192.0.2.33 | 64:ff9b::192.0.2.33 (IPv4-Embedded IPv6 Address)
                        + "::(?:[fF]{4}(?::0{1,4})?:)?(?:(?:25[0-5]|(?:2[0-4]|1?\\d)?\\d)\\.){3}(?:25[0-5]|(?:2[0-4]|1?\\d)?\\d)|" // ::255.255.255.255 | ::ffff:255.255.255.255 | ::ffff:0:255.255.255.255 (IPv4-mapped IPv6 addresses and IPv4-translated addresses)
                        + "[fe|FE]80:(?::[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|" // fe80::7:8%eth0 | fe80::7:8%1 (link-local IPv6 addresses with zone index)
                        + ":(?:(?::[0-9a-fA-F]{1,4}){1,7}|:)|" // ::2:3:4:5:6:7:8 | ::2:3:4:5:6:7:8 | ::8 | ::
                        + "[0-9a-fA-F]{1,4}:(?::[0-9a-fA-F]{1,4}){1,6}|" // 1::3:4:5:6:7:8 | 1::3:4:5:6:7:8 | 1::8
                        + "(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}|" // 1::4:5:6:7:8 | 1:2::4:5:6:7:8 | 1:2::8
                        + "(?:[0-9a-fA-F]{1,4}:){1,3}(?::[0-9a-fA-F]{1,4}){1,4}|" // 1::5:6:7:8 | 1:2:3::5:6:7:8 | 1:2:3::8
                        + "(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}|" // 1::6:7:8 | 1:2:3:4::6:7:8 | 1:2:3:4::8
                        + "(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}|" // 1::7:8 | 1:2:3:4:5::7:8 | 1:2:3:4:5::8
                        + "(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" // 1::8 | 1:2:3:4:5:6::8 | 1:2:3:4:5:6::8
                        + "(?:[0-9a-fA-F]{1,4}:){1,7}:|" // 1:: | 1:2:3:4:5:6:7::
                        + "(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}" // 1:2:3:4:5:6:7:8
                        + ")$", Pattern.CASE_INSENSITIVE))
                .build();
        // online location derived types
        public static final SchemaVertexType USER_NAME = new SchemaVertexType.Builder("User Name")
                .setDescription("A node representing an online user name")
                .setSuperType(ONLINE_IDENTIFIER)
                .build();
        public static final SchemaVertexType HOST_NAME = new SchemaVertexType.Builder("Host Name")
                .setDescription("A node representing a hostname")
                .setSuperType(ONLINE_LOCATION)
                .setDetectionRegex(Pattern.compile("(?:(?:[0-9a-zA-Z][0-9a-zA-Z\\-]{0,61}[0-9a-zA-Z]|[0-9a-zA-Z])\\.)+(?:[0-9a-zA-Z][0-9a-zA-Z\\-]{0,61}[0-9a-zA-Z]|[0-9a-zA-Z])", Pattern.CASE_INSENSITIVE))
                .setValidationRegex(Pattern.compile("^(?:(?:[0-9a-z][0-9a-z\\-]{0,61}[0-9a-zA-Z]|[0-9a-zA-Z])\\.)+(?:[0-9a-zA-Z][0-9a-zA-Z\\-]{0,61}[0-9a-zA-Z]|[0-9a-zA-Z])$", Pattern.CASE_INSENSITIVE))
                .build();
        public static final SchemaVertexType URL = new SchemaVertexType.Builder("URL")
                .setDescription("A node representing a URL")
                .setSuperType(ONLINE_LOCATION)
                .setDetectionRegex(Pattern.compile("(?:(?<scheme>[a-zA-Z][a-zA-Z\\d\\+\\-\\.]*):)?" // protocol
                        + "(?:"
                        + "(?:"
                        + "(?:\\/\\/"
                        + "(?:"
                        + "(?:(?:(?:[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=%]*)(?::(?:[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=:%]*))?)@)?" // user info
                        + "(?:(?:[a-zA-Z\\d\\.\\-%]+)|(?:\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(?:\\[(?:[a-fA-F\\d\\.:]+)\\]))?" // host ip
                        + "(?::(\\d*))?" // port
                        + ")"
                        + ")"
                        + "(?:(?:\\/[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=:@%]*)*)" // slash path
                        + ")"
                        + "|(?:\\/(?:(?:[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=:@%]+(?:\\/[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=:@%]*)*))?)" // slash path
                        //                        + "|(?:[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=:@%]+(?:\\/[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=:@%]*)*)" // path - TODO: this component is too general
                        + ")"
                        + "(?:\\?(?:[\\w\\(\\)\\+\\?\\$\\.\\-\\/~!&'*,;=:@%]*))?" // query string
                        + "(?:\\#(?:[\\w\\(\\)\\+\\?\\$\\.\\-\\/~!&'*,;=:@%]*))?", // fragment
                        Pattern.CASE_INSENSITIVE))
                .setValidationRegex(Pattern.compile("^(?:([a-zA-Z][a-zA-Z\\d\\+\\-\\.]*):)?" // protocol
                        + "(?:"
                        + "(?:"
                        + "(?:\\/\\/"
                        + "(?:"
                        + "(?:(?:(?:[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=%]*)(?::(?:[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=:%]*))?)@)?" // user info
                        + "(?:(?:[a-zA-Z\\d\\.\\-%]+)|(?:\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(?:\\[(?:[a-fA-F\\d\\.:]+)\\]))?" // host ip
                        + "(?::(\\d*))?" // port
                        + ")"
                        + ")"
                        + "(?:(?:\\/[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=:@%]*)*)" // slash path
                        + ")"
                        + "|(?:\\/(?:(?:[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=:@%]+(?:\\/[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=:@%]*)*))?)" // slash path
                        //                        + "|(?:[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=:@%]+(?:\\/[\\w\\(\\)\\+\\$\\.\\-~!&'*,;=:@%]*)*)" // path - TODO: this component is too general
                        + ")"
                        + "(?:\\?(?:[\\w\\(\\)\\+\\?\\$\\.\\-\\/~!&'*,;=:@%]*))?" // query string
                        + "(?:\\#(?:[\\w\\(\\)\\+\\?\\$\\.\\-\\/~!&'*,;=:@%]*))?$", // fragment
                        Pattern.CASE_INSENSITIVE))
                .build();
        // location derived types
        public static final SchemaVertexType COUNTRY = new SchemaVertexType.Builder("Country")
                .setDescription("A node representing the name of a country")
                .setSuperType(LOCATION)
                .setValidationRegex(Pattern.compile("^[a-zA-Z '\\-\\(\\)Åçé]{2,50}$", Pattern.CASE_INSENSITIVE))
                .build();
        public static final SchemaVertexType GEOHASH = new SchemaVertexType.Builder("Geohash")
                .setDescription("A node representing a geohash")
                .setSuperType(LOCATION)
                .setValidationRegex(Pattern.compile("^[0-9a-z=_]{1,12}$", Pattern.CASE_INSENSITIVE))
                .build();
        public static final SchemaVertexType MGRS = new SchemaVertexType.Builder("MGRS")
                .setDescription("A node representing a Military Grid Reference System coordinate")
                .setSuperType(LOCATION)
                .setValidationRegex(Pattern.compile("^(?:[1-9]|[1-5]\\d|60)[^abioyz][^io]{2}(?:\\d\\d){0,5}$", Pattern.CASE_INSENSITIVE))
                .build();
        // hash derived types
        public static final SchemaVertexType MD5 = new SchemaVertexType.Builder("MD5 Hash")
                .setDescription("A node representing an MD5 hash")
                .setForegroundIcon(AnalyticIconProvider.MD5)
                .setSuperType(HASH)
                .setDetectionRegex(Pattern.compile("[0-9a-fA-F]{32}", Pattern.CASE_INSENSITIVE))
                .setValidationRegex(Pattern.compile("^[0-9a-f]{32}$", Pattern.CASE_INSENSITIVE))
                .build();
        public static final SchemaVertexType SHA1 = new SchemaVertexType.Builder("SHA1 Hash")
                .setDescription("A node representing an SHA1 hash")
                .setForegroundIcon(AnalyticIconProvider.SHA1)
                .setSuperType(HASH)
                .setDetectionRegex(Pattern.compile("[0-9a-fA-F]{40}", Pattern.CASE_INSENSITIVE))
                .setValidationRegex(Pattern.compile("^[0-9a-f]{40}$", Pattern.CASE_INSENSITIVE))
                .build();
        public static final SchemaVertexType SHA256 = new SchemaVertexType.Builder("SHA256 Hash")
                .setDescription("A node representing an SHA256 hash")
                .setForegroundIcon(AnalyticIconProvider.SHA256)
                .setSuperType(HASH)
                .setDetectionRegex(Pattern.compile("[0-9a-fA-F]{64}", Pattern.CASE_INSENSITIVE))
                .setValidationRegex(Pattern.compile("^[0-9a-f]{64}$", Pattern.CASE_INSENSITIVE))
                .build();
    }

    @Override
    public List<SchemaVertexType> getSchemaVertexTypes() {
        final List<SchemaVertexType> schemaVertexTypes = new ArrayList<>();
        schemaVertexTypes.add(VertexType.GRAPH);
        schemaVertexTypes.add(VertexType.TELEPHONE_IDENTIFIER);
        schemaVertexTypes.add(VertexType.MACHINE_IDENTIFIER);
        schemaVertexTypes.add(VertexType.NETWORK_IDENTIFIER);
        schemaVertexTypes.add(VertexType.ONLINE_IDENTIFIER);
        schemaVertexTypes.add(VertexType.ONLINE_LOCATION);
        schemaVertexTypes.add(VertexType.LOCATION);
        schemaVertexTypes.add(VertexType.PERSON);
        schemaVertexTypes.add(VertexType.ORGANISATION);
        schemaVertexTypes.add(VertexType.DOCUMENT);
        schemaVertexTypes.add(VertexType.WORD);
        schemaVertexTypes.add(VertexType.HASH);
        schemaVertexTypes.add(VertexType.EVENT);
        schemaVertexTypes.add(VertexType.PLACEHOLDER);
        schemaVertexTypes.add(VertexType.IP_ADDRESS);
        schemaVertexTypes.add(VertexType.IPV4);
        schemaVertexTypes.add(VertexType.IPV6);
        schemaVertexTypes.add(VertexType.EMAIL_ADDRESS);
        schemaVertexTypes.add(VertexType.USER_NAME);
        schemaVertexTypes.add(VertexType.HOST_NAME);
        schemaVertexTypes.add(VertexType.URL);
        schemaVertexTypes.add(VertexType.COUNTRY);
        schemaVertexTypes.add(VertexType.GEOHASH);
        schemaVertexTypes.add(VertexType.MGRS);
        schemaVertexTypes.add(VertexType.MD5);
        schemaVertexTypes.add(VertexType.SHA1);
        schemaVertexTypes.add(VertexType.SHA256);
        return Collections.unmodifiableList(schemaVertexTypes);
    }

    @Override
    public SchemaVertexType getDefaultSchemaVertexType() {
        return SchemaVertexType.unknownType();
    }

    public static class TransactionType {

        public static final SchemaTransactionType COMMUNICATION;
        public static final SchemaTransactionType CORRELATION;
        public static final SchemaTransactionType LOCATION;
        public static final SchemaTransactionType NETWORK;
        public static final SchemaTransactionType RELATIONSHIP;
        public static final SchemaTransactionType BEHAVIOUR;
        public static final SchemaTransactionType SIMILARITY;
        public static final SchemaTransactionType CREATED;
        public static final SchemaTransactionType REFERENCED;

        static {
            if (COLORMODE.equals(DEUTERANOPIA) || COLORMODE.equals(PROTANOPIA) || COLORMODE.equals(TRITANOPIA)) {
                COMMUNICATION = new SchemaTransactionType.Builder("Communication")
                        .setDescription("A transaction representing a communication between two entities, eg. a phone made a call to another phone")
                        .setColor(ConstellationColor.EMERALD)
                        .build();
                CORRELATION = new SchemaTransactionType.Builder("Correlation")
                        .setDescription("A transaction representing a two entities which are part of the same larger entity, eg. a person is correlated to their online identifier")
                        .setColor(ConstellationColor.AZURE)
                        .setDirected(false)
                        .build();
                LOCATION = new SchemaTransactionType.Builder("Location")
                        .setDescription("A transaction representing an entity having a location, eg. a person is located in a country")
                        .setColor(ConstellationColor.NAVY)
                        .build();
                NETWORK = new SchemaTransactionType.Builder("Network")
                        .setDescription("A transaction representing a network connection, eg. an computer sent a request to a server")
                        .setColor(ConstellationColor.BANANA)
                        .build();
                RELATIONSHIP = new SchemaTransactionType.Builder("Relationship")
                        .setDescription("A transaction representing a relationship between two entities, eg. a person is the mother of another person")
                        .setColor(ConstellationColor.AMETHYST)
                        .setDirected(false)
                        .build();
                BEHAVIOUR = new SchemaTransactionType.Builder("Behaviour")
                        .setDescription("A transaction representing an entity exhibiting a behaviour, eg. an online identifier added a friend")
                        .setColor(ConstellationColor.MUSK)
                        .build();
                SIMILARITY = new SchemaTransactionType.Builder("Similarity")
                        .setDescription("An transaction representing two entities which are similar (often with an associated similarity score), eg. an online identifier has a similar set of friends to another online identifier")
                        .setColor(ConstellationColor.TURQUOISE)
                        .setDirected(false)
                        .build();
                CREATED = new SchemaTransactionType.Builder("Created")
                        .setDescription("A transaction representing an entity creating another entity, eg. a person created a document")
                        .setColor(ConstellationColor.CHOCOLATE)
                        .build();
                REFERENCED = new SchemaTransactionType.Builder("Referenced")
                        .setDescription("A transaction representing an entity referencing another entity, eg. a document referenced its author")
                        .setColor(ConstellationColor.CHOCOLATE)
                        .setStyle(LineStyle.DASHED)
                        .build();
            } else {
                COMMUNICATION = new SchemaTransactionType.Builder("Communication")
                        .setDescription("A transaction representing a communication between two entities, eg. a phone made a call to another phone")
                        .setColor(ConstellationColor.EMERALD)
                        .build();
                CORRELATION = new SchemaTransactionType.Builder("Correlation")
                        .setDescription("A transaction representing a two entities which are part of the same larger entity, eg. a person is correlated to their online identifier")
                        .setColor(ConstellationColor.AZURE)
                        .setDirected(false)
                        .build();
                LOCATION = new SchemaTransactionType.Builder("Location")
                        .setDescription("A transaction representing an entity having a location, eg. a person is located in a country")
                        .setColor(ConstellationColor.CARROT)
                        .build();
                NETWORK = new SchemaTransactionType.Builder("Network")
                        .setDescription("A transaction representing a network connection, eg. an computer sent a request to a server")
                        .setColor(ConstellationColor.BANANA)
                        .build();
                RELATIONSHIP = new SchemaTransactionType.Builder("Relationship")
                        .setDescription("A transaction representing a relationship between two entities, eg. a person is the mother of another person")
                        .setColor(ConstellationColor.AMETHYST)
                        .setDirected(false)
                        .build();
                BEHAVIOUR = new SchemaTransactionType.Builder("Behaviour")
                        .setDescription("A transaction representing an entity exhibiting a behaviour, eg. an online identifier added a friend")
                        .setColor(ConstellationColor.MUSK)
                        .build();
                SIMILARITY = new SchemaTransactionType.Builder("Similarity")
                        .setDescription("An transaction representing two entities which are similar (often with an associated similarity score), eg. an online identifier has a similar set of friends to another online identifier")
                        .setColor(ConstellationColor.TURQUOISE)
                        .setDirected(false)
                        .build();
                CREATED = new SchemaTransactionType.Builder("Created")
                        .setDescription("A transaction representing an entity creating another entity, eg. a person created a document")
                        .setColor(ConstellationColor.BROWN)
                        .build();
                REFERENCED = new SchemaTransactionType.Builder("Referenced")
                        .setDescription("A transaction representing an entity referencing another entity, eg. a document referenced its author")
                        .setColor(ConstellationColor.BROWN)
                        .setStyle(LineStyle.DASHED)
                        .build();
            }
        }
    }

    @Override
    public List<SchemaTransactionType> getSchemaTransactionTypes() {
        final List<SchemaTransactionType> schemaTransactionTypes = new ArrayList<>();
        schemaTransactionTypes.add(TransactionType.COMMUNICATION);
        schemaTransactionTypes.add(TransactionType.CORRELATION);
        schemaTransactionTypes.add(TransactionType.LOCATION);
        schemaTransactionTypes.add(TransactionType.NETWORK);
        schemaTransactionTypes.add(TransactionType.RELATIONSHIP);
        schemaTransactionTypes.add(TransactionType.BEHAVIOUR);
        schemaTransactionTypes.add(TransactionType.SIMILARITY);
        schemaTransactionTypes.add(TransactionType.CREATED);
        schemaTransactionTypes.add(TransactionType.REFERENCED);
        return Collections.unmodifiableList(schemaTransactionTypes);
    }

    @Override
    public SchemaTransactionType getDefaultSchemaTransactionType() {
        return SchemaTransactionType.unknownType();
    }
}

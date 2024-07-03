/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.geospatial;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An enum describing countries known by ISO 3166-1.
 *
 * @author cygnus_x-1
 */
public enum Country {

    AFGHANISTAN("AF", "AFG", "Afghan", "Afghanistan"),
    ALAND_ISLANDS("AX", "ALA", null, "Aland Islands"),
    ALBANIA("AL", "ALB", "Albanian", "Albania"),
    ALGERIA("DZ", "DZA", "Algerian", "Algeria"),
    AMERICAN_SAMOA("AS", "ASM", "American Samoan", "American Samoa"),
    ANDORRA("AD", "AND", "Andorran", "Andorra"),
    ANGOLA("AO", "AGO", "Angolan", "Angola"),
    ANGUILLA("AI", "AIA", "Anguillan", "Anguilla"),
    ANTARCTICA("AQ", "ATA", null, "Antarctica"),
    ANTIGUA_AND_BARBUDA("AG", "ATG", "Antiguan or Barbudan", "Antigua and Barbuda"),
    ARGENTINA("AR", "ARG", "Argentine", "Argentina"),
    ARMENIA("AM", "ARM", "Armenian", "Armenia"),
    ARUBA("AW", "ABW", "Aruban", "Aruba"),
    AUSTRALIA("AU", "AUS", "Australian", "Australia"),
    AUSTRIA("AT", "AUT", "Austrian", "Austria"),
    AZERBAIJAN("AZ", "AZE", "Azerbaijani", "Azerbaijan"),
    BAHAMAS("BS", "BHS", "Bahamian", "Bahamas", "The Bahamas"),
    BAHRAIN("BH", "BHR", "Bahraini", "Bahrain"),
    BANGLADESH("BD", "BGD", "Bangladeshi", "Bangladesh"),
    BARBADOS("BB", "BRB", "Barbadian", "Barbados"),
    BELARUS("BY", "BLR", "Belarusian", "Belarus"),
    BELGIUM("BE", "BEL", "Belgian", "Belgium"),
    BELIZE("BZ", "BLZ", "Belizean", "Belize"),
    BENIN("BJ", "BEN", "Beninese", "Benin"),
    BERMUDA("BM", "BMU", "Bermudan", "Bermuda"),
    BHUTAN("BT", "BTN", "Bhutanese", "Bhutan"),
    BOLIVIA("BO", "BOL", "Bolivian", "Bolivia", "Plurinational State of Bolivia"),
    BONAIRE_SINT_EUSTATIUS_AND_SABA("BQ", "BES", null, "Bonaire, Sint Eustatius and Saba"),
    BOSNIA_AND_HERZEGOVINA("BA", "BIH", "Bosnian or Herzegovinian", "Bosnia and Herzegovina"),
    BOTSWANA("BW", "BWA", "Botswanan", "Botswana"),
    BOUVET_ISLAND("BV", "BVT", null, "Bouvet Island"),
    BRAZIL("BR", "BRA", "Brazilian", "Brazil"),
    BRITISH_INDIAN_OCEAN_TERRITORY("IO", "IOT", "British", "British Indian Ocean Territory"),
    BRUNEI_DARUSSALAM("BN", "BRN", "Bruneian", "Brunei Darussalam"),
    BULGARIA("BG", "BGR", "Bulgarian", "Bulgaria"),
    BURKINA_FASO("BF", "BFA", "Burkinabe", "Burkina Faso"),
    BURUNDI("BI", "BDI", "Burundian", "Burundi"),
    CABO_VERDE("CV", "CPV", "Cabo Verdean", "Cabo Verde", "Cape Verde"),
    CAMBODIA("KH", "KHM", "Cambodian", "Cambodia"),
    CAMEROON("CM", "CMR", "Cameroonian", "Cameroon"),
    CANADA("CA", "CAN", "Canadian", "Canada"),
    CAYMAN_ISLANDS("KY", "CYM", "Caymanian", "Cayman Islands"),
    CENTRAL_AFRICAN_REPUBLIC("CF", "CAF", "Central African", "Central African Republic"),
    CHAD("TD", "TCD", "Chadian", "Chad"),
    CHILE("CL", "CHL", "Chilean", "Chile"),
    CHINA("CN", "CHN", "Chinese", "China"),
    CHRISTMAS_ISLAND("CX", "CXR", null, "Christmas Island"),
    COCOS_ISLANDS("CC", "CCK", null, "Cocos Islands", "Keeling Islands"),
    COLOMBIA("CO", "COL", "Colombian", "Colombia"),
    COMOROS("KM", "COM", "Comorian", "Comoros"),
    CONGO("CG", "COG", "Congolese", "Congo", "The Congo", "The Republic of the Congo"),
    CONGO_DEMOCRATIC_REPUBLIC("CD", "COD", "Congolese", "Congo, the Democratic Republic of the"),
    COOK_ISLANDS("CK", "COK", "Cook Island", "Cook Islands"),
    COSTA_RICA("CR", "CRI", "Costa Rican", "Costa Rica"),
    CROATIA("HR", "HRV", "Croatian", "Croatia", "Hrvatska"),
    CUBA("CU", "CUB", "Cuban", "Cuba"),
    CURACAO("CW", "CUW", null, "Curacao"),
    CYPRUS("CY", "CYP", "Cypriot", "Cyprus"),
    CZECH_REPUBLIC("CZ", "CZE", "Czech", "Czechia", "Czech Republic"),
    COTE_DIVOIRE("CI", "CIV", "Ivorian", "Cote D'Ivoire"),
    DENMARK("DK", "DNK", "Danish", "Denmark"),
    DJIBOUTI("DJ", "DJI", "Djiboutian", "Djibouti"),
    DOMINICA("DM", "DMA", "Dominican", "Dominica"),
    DOMINICAN_REPUBLIC("DO", "DOM", "Dominican", "Dominican Republic"),
    ECUADOR("EC", "ECU", "Ecuadorian", "Ecuador"),
    EGYPT("EG", "EGY", "Egyptian", "Egypt"),
    EL_SALVADOR("SV", "SLV", "Salvadoran", "El Salvador"),
    EQUATORIAL_GUINEA("GQ", "GNQ", "Equatorial Guinean", "Equatorial Guinea"),
    ERITREA("ER", "ERI", "Eritrean", "Eritrea"),
    ESTONIA("EE", "EST", "Estonian", "Estonia"),
    ESWATINI("SZ", "SWZ", "Swazi", "Eswatini", "The Kingdom of Eswatini", "Swaziland"),
    ETHIOPIA("ET", "ETH", "Ethiopian", "Ethiopia"),
    FALKLAND_ISLANDS("FK", "FLK", null, "Falkland Islands", "Malvinas"),
    FAROE_ISLANDS("FO", "FRO", "Faroese", "Faroe Islands"),
    FIJI("FJ", "FJI", "Fijian", "Fiji"),
    FINLAND("FI", "FIN", "Finnish", "Finland"),
    FRANCE("FR", "FRA", "French", "France"),
    FRENCH_GUIANA("GF", "GUF", "French Guianese", "French Guiana"),
    FRENCH_POLYNESIA("PF", "PYF", "French Polynesian", "French Polynesia"),
    FRENCH_SOUTHERN_TERRITORIES("TF", "ATF", null, "French Southern Territories"),
    GABON("GA", "GAB", "Gabonese", "Gabon"),
    GAMBIA("GM", "GMB", "Gambian", "Gambia", "The Gambia"),
    GEORGIA("GE", "GEO", "Georgian", "Georgia"),
    GERMANY("DE", "DEU", "German", "Germany"),
    GHANA("GH", "GHA", "Ghanaian", "Ghana"),
    GIBRALTAR("GI", "GIB", "Gibraltar", "Gibraltar"),
    GREECE("GR", "GRC", "Greek", "Greece"),
    GREENLAND("GL", "GRL", "Greenlandic", "Greenland"),
    GRENADA("GD", "GRD", "Grenadian", "Grenada"),
    GUADELOUPE("GP", "GLP", "Guadeloupe", "Guadeloupe"),
    GUAM("GU", "GUM", "Guamanian", "Guam"),
    GUATEMALA("GT", "GTM", "Guatemalan", "Guatemala"),
    GUERNSEY("GG", "GGY", null, "Guernsey", "Bailiwick of Guernsey"),
    GUINEA("GN", "GIN", "Guinean", "Guinea"),
    GUINEA_BISSAU("GW", "GNB", "Guinean", "Guinea-Bissau"),
    GUYANA("GY", "GUY", "Guyanese", "Guyana"),
    HAITI("HT", "HTI", "Haitian", "Haiti"),
    HEARD_ISLAND_AND_MCDONALD_ISLANDS("HM", "HMD", null, "Heard Island and McDonald Islands"),
    HOLY_SEE("VA", "VAT", null, "Holy See", "Vatican City State"),
    HONDURAS("HN", "HND", "Honduran", "Honduras"),
    HONG_KONG("HK", "HKG", "Hong Kong", "Hong Kong"),
    HUNGARY("HU", "HUN", "Hungarian", "Hungary"),
    ICELAND("IS", "ISL", "Icelandic", "Iceland"),
    INDIA("IN", "IND", "Indian", "India"),
    INDONESIA("ID", "IDN", "Indonesian", "Indonesia"),
    IRAN("IR", "IRN", "Iranian", "Iran", "Islamic Republic of Iran"),
    IRAQ("IQ", "IRQ", "Iraqi", "Iraq"),
    IRELAND("IE", "IRL", "Irish", "Ireland"),
    ISLE_OF_MAN("IM", "IMN", null, "Isle of Man"),
    ISRAEL("IL", "ISR", "Israeli", "Israel"),
    ITALY("IT", "ITA", "Italian", "Italy"),
    JAMAICA("JM", "JAM", "Jamaican", "Jamaica"),
    JAPAN("JP", "JPN", "Japanese", "Japan"),
    JERSEY("JE", "JEY", null, "Jersey", "Bailiwick of Jersey"),
    JORDAN("JO", "JOR", "Jordanian", "Jordan"),
    KAZAKHSTAN("KZ", "KAZ", "Kazakh", "Kazakhstan"),
    KENYA("KE", "KEN", "Kenyan", "Kenya"),
    KIRIBATI("KI", "KIR", "I-Kiribati", "Kiribati"),
    KOREA_NORTH("KP", "PRK", "North Korean", "North Korea", "Democratic People's Republic of Korea"),
    KOREA_SOUTH("KR", "KOR", "South Korean", "South Korea", "Republic of Korea"),
    KOSOVO("XK", "XKK", "Kosovan", "Kosovo"),
    KUWAIT("KW", "KWT", "Kuwaiti", "Kuwait"),
    KYRGYZSTAN("KG", "KGZ", "Kyrgyzstani", "Kyrgyzstan"),
    LAOS("LA", "LAO", "Laotian", "Laos", "Lao People's Democratic Republic"),
    LATVIA("LV", "LVA", "Latvian", "Latvia"),
    LEBANON("LB", "LBN", "Lebanese", "Lebanon"),
    LESOTHO("LS", "LSO", "Basotho", "Lesotho"),
    LIBERIA("LR", "LBR", "Liberian", "Liberia"),
    LIBYA("LY", "LBY", "Libyan", "Libya"),
    LIECHTENSTEIN("LI", "LIE", "Liechtenstein", "Liechtenstein"),
    LITHUANIA("LT", "LTU", "Lithuanian", "Lithuania"),
    LUXEMBOURG("LU", "LUX", "Luxembourg", "Luxembourg"),
    MACAU("MO", "MAC", "Macanese", "Macau", "Macao"),
    MACEDONIA("MK", "MKD", "Macedonian", "North Macedonia", "Macedonia", "The Former Yugoslav Republic of Macedonia"),
    MADAGASCAR("MG", "MDG", "Malagasy", "Madagascar"),
    MALAWI("MW", "MWI", "Malawian", "Malawi"),
    MALAYSIA("MY", "MYS", "Malaysian", "Malaysia"),
    MALDIVES("MV", "MDV", "Maldivian", "Maldives"),
    MALI("ML", "MLI", "Malian", "Mali"),
    MALTA("MT", "MLT", "Maltese", "Malta"),
    MARSHALL_ISLANDS("MH", "MHL", "Marshallese", "Marshall Islands"),
    MARTINIQUE("MQ", "MTQ", "Martiniquais", "Martinique"),
    MAURITANIA("MR", "MRT", "Mauritanian", "Mauritania"),
    MAURITIUS("MU", "MUS", "Mauritian", "Mauritius"),
    MAYOTTE("YT", "MYT", null, "Mayotte"),
    MEXICO("MX", "MEX", "Mexican", "Mexico"),
    MICRONESIA("FM", "FSM", "Micronesian", "Federated States of Micronesia"),
    MOLDOVA("MD", "MDA", "Moldovan", "Moldova", "Republic of Moldova"),
    MONACO("MC", "MCO", "Monegasque", "Monaco"),
    MONGOLIA("MN", "MNG", "Mongolian", "Mongolia"),
    MONTENEGRO("ME", "MNE", "Montenegrin", "Montenegro"),
    MONTSERRAT("MS", "MSR", "Montserratian", "Montserrat"),
    MOROCCO("MA", "MAR", "Moroccan", "Morocco"),
    MOZAMBIQUE("MZ", "MOZ", "Mozambican", "Mozambique"),
    MYANMAR("MM", "MMR", "Burmese", "Myanmar"),
    NAMIBIA("NA", "NAM", "Namibian", "Namibia"),
    NAURU("NR", "MRU", "Nauruan", "Nauru"),
    NEPAL("NP", "NPL", "Nepalese", "Nepal"),
    NETHERLANDS("NL", "NLD", "Dutch", "The Netherlands", "Netherlands"),
    NEW_CALEDONIA("NC", "NCL", "New Caledonian", "New Caledonia"),
    NEW_ZEALAND("NZ", "NZL", "New Zealand", "New Zealand", "Aotearoa"),
    NICARAGUA("NI", "NIC", "Nicaraguan", "Nicaragua"),
    NIGER("NE", "NER", "Nigerien", "Niger"),
    NIGERIA("NG", "NGA", "Nigerian", "Nigeria"),
    NIUE("NU", "NIU", "Niuean", "Niue"),
    NORFOLK_ISLAND("NF", "NFK", null, "Norfolk Island"),
    NORTHERN_MARIANA_ISLANDS("MP", "MNP", "Northern Marianan", "Northern Mariana Islands"),
    NORWAY("NO", "NOR", "Norwegian", "Norway"),
    OMAN("OM", "OMN", "Omani", "Oman"),
    PAKISTAN("PK", "PAK", "Pakistani", "Pakistan"),
    PALAU("PW", "PLW", "Palauan", "Palau"),
    PALESTINE("PS", "PSE", "Palestinian", "Palestine", "State of Palestine"),
    PANAMA("PA", "PAN", "Panamanian", "Panama"),
    PAPUA_NEW_GUINEA("PG", "PNG", "Papua New Guinean", "Papua New Guinea"),
    PARAGUAY("PY", "PRY", "Paraguayan", "Paraguay"),
    PERU("PE", "PER", "Peruvian", "Peru"),
    PHILIPPINES("PH", "PHL", "Filipino", "Philippines"),
    PITCAIRN("PN", "PCN", null, "Pitcairn"),
    POLAND("PL", "POL", "Polish", "Poland"),
    PORTUGAL("PT", "PRT", "Portuguese", "Portugal"),
    PUERTO_RICO("PR", "PRI", "Puerto Rican", "Puerto Rico"),
    QATAR("QA", "QAT", "Qatari", "Qatar"),
    REUNION("RE", "REU", "Reunionese or Mahoran", "Reunion"),
    ROMANIA("RO", "ROU", "Romanian", "Romania"),
    RUSSIA("RU", "RUS", "Russian", "Russia", "Russian Federation"),
    RWANDA("RW", "RWA", "Rwandan", "Rwanda"),
    SAINT_BARTHELEMY("BL", "BLM", null, "Saint Barthelemy"),
    SAINT_HELENA_ASCENSION_AND_TRISTAN_DA_CUNHA("SH", "SHN", "Saint Helenian", "Saint Helena, Ascension and Tristan da Cunha"),
    SAINT_KITTS_AND_NEVIS("KN", "KNA", "Kittitian or Nevisian", "Saint Kitts and Nevis"),
    SAINT_LUCIA("LC", "LCA", "Saint Lucian", "Saint Lucia"),
    SAINT_MARTIN("MF", "MAF", null, "Saint Martin", "Saint Martin (French Part)"),
    SAINT_PIERRE_AND_MIQUELON("PM", "SPM", "Saint-Pierrais or Miquelonnais", "Saint Pierre and Miquelon"),
    SAINT_VINCENT_AND_THE_GRENADINES("VC", "VCT", "Saint Vincentian", "Saint Vincent and the Grenadines"),
    SAMOA("WS", "WSM", "Samoan", "Samoa"),
    SAN_MARINO("SM", "SMR", "Sammarinese", "San Marino"),
    SAO_TOME_AND_PRINCIPE("ST", "STP", "Santomean", "Sao Tome and Principe"),
    SAUDI_ARABIA("SA", "SAU", "Saudi", "Saudi Arabia"),
    SENEGAL("SN", "SEN", "Senegalese", "Senegal"),
    SERBIA("RS", "SRB", "Serbian", "Serbia"),
    SEYCHELLES("SC", "SYC", "Seychellois", "Seychelles"),
    SIERRA_LEONE("SL", "SLE", "Sierra Leonean", "Sierra Leone"),
    SINGAPORE("SG", "SGP", "Singaporean", "Singapore"),
    SINT_MAARTEN("SX", "SXM", "Sint Maartenese", "Sint Maarten", "Sint Maarten (Dutch Part)"),
    SLOVAKIA("SK", "SVK", "Slovak", "Slovakia"),
    SLOVENIA("SI", "SVN", "Slovenian", "Slovenia"),
    SOLOMON_ISLANDS("SB", "SLB", "Solomon Island", "Solomon Islands"),
    SOMALIA("SO", "SOM", "Somali", "Somalia"),
    SOUTH_AFRICA("ZA", "ZAF", "South African", "South Africa"),
    SOUTH_GEORGIA_AND_THE_SOUTH_SANDWICH_ISLANDS("GS", "SGS", null, "South Georgia and the South Sandwich Islands"),
    SOUTH_SUDAN("SS", "SSD", "Sudanese", "South Sudan"),
    SPAIN("ES", "ESP", "Spanish", "Spain"),
    SRI_LANKA("LK", "LKA", "Sri Lankan", "Sri Lanka"),
    SUDAN("SD", "SDN", "Sudanese", "Sudan"),
    SURINAME("SR", "SUR", "Surinamese", "Suriname"),
    SVALBARD_AND_JAN_MAYEN("SJ", "SJM", "Norwegian", "Svalbard and Jan Mayen"),
    SWEDEN("SE", "SWE", "Swedish", "Sweden"),
    SWITZERLAND("CH", "CHE", "Swiss", "Switzerland"),
    SYRIA("SY", "SYR", "Syrian", "Syrian Arab Republic", "Syria"),
    TAIWAN("TW", "TWN", "Taiwanese", "Taiwan"),
    TAJIKISTAN("TJ", "TJK", "Tajikistani", "Tajikistan"),
    TANZANIA("TZ", "TZA", "Tanzanian", "United Republic of Tanzania", "Tanzania"),
    THAILAND("TH", "THA", "Thai", "Thailand"),
    TIMOR_LESTE("TL", "TLS", "Timorese", "Timor-Leste", "East Timor"),
    TOGO("TG", "TGO", "Togolese", "Togo"),
    TOKELAU("TK", "TKL", "Tokelauan", "Tokelau"),
    TONGA("TO", "TON", "Tongan", "Tonga"),
    TRINIDAD_AND_TOBAGO("TT", "TTO", "Trinidadian or Tobagonian", "Trinidad and Tobago"),
    TUNISIA("TN", "TUN", "Tunisian", "Tunisia"),
    TURKEY("TR", "TUR", "Turkish", "Republic of Turkiye", "Turkiye", "Turkey"),
    TURKMENISTAN("TM", "TKM", "Turkmen", "Turkmenistan"),
    TURKS_AND_CAICOS_ISLANDS("TC", "TCA", "Turks and Caicos Island", "Turks and Caicos Islands"),
    TUVALU("TV", "TUV", "Tuvaluan", "Tuvalu"),
    UGANDA("UG", "UGA", "Ugandan", "Uganda"),
    UKRAINE("UA", "UKR", "Ukrainian", "Ukraine"),
    UNITED_ARAB_EMIRATES("AE", "ARE", "Emirati", "United Arab Emirates"),
    UNITED_KINGDOM("GB", "GBR", "British", "United Kingdom", "United Kingdom of Great Britain and Northern Ireland"),
    UNITED_STATES("US", "USA", "American", "United States of America", "United States"),
    UNITED_STATES_MINOR_OUTLYING_ISLANDS("UM", "UMI", null, "United States Minor Outlying Islands"),
    UNKNOWN_RESERVED_OR_PRIVATE("XX", "XXX", null, "Unknown"),
    URUGUAY("UY", "URY", "Uruguayan", "Uruguay"),
    UZBEKISTAN("UZ", "UZB", "Uzbekistani", "Uzbekistan"),
    VANUATU("VU", "VUT", "Vanuatuan", "Vanuatu"),
    VENEZUELA("VE", "VEN", "Venezuelan", "Bolivarian Republic of Venezuela"),
    VIETNAM("VN", "VNM", "Vietnamese", "Vietnam"),
    VIRGIN_ISLANDS_BRITISH("VG", "VGB", null, "Virgin Islands (British)", "British Virgin Islands"),
    VIRGIN_ISLANDS_US("VI", "VIR", null, "Virgin Islands (U.S.)", "U.S. Virgin Islands"),
    WALLIS_AND_FUTUNA("WF", "WLF", "Wallisian or Futunan", "Wallis and Futuna"),
    WESTERN_SAHARA("EH", "ESH", null, "Western Sahara"),
    YEMEN("YE", "YEM", "Yemeni", "Yemen"),
    ZAMBIA("ZM", "ZMB", "Zambian", "Zambia"),
    ZIMBABWE("ZW", "ZWE", "Zimbabwean", "Zimbabwe");

    private static final Logger LOGGER = Logger.getLogger(Country.class.getName());

    private final String digraph;
    private final String trigraph;
    private final String demonym;
    private final String displayName;
    private final String[] alternateNames;

    private Country(final String digraph, final String trigraph, final String demonym, final String displayName, final String... alternateNames) {
        if (digraph != null && digraph.length() != 2) {
            throw new IllegalArgumentException("Country digraph must be 2 characters long: " + digraph);
        }
        if (trigraph != null && trigraph.length() != 3) {
            throw new IllegalArgumentException("Country trigraph must be 3 characters long: " + trigraph);
        }
        if (displayName == null) {
            throw new IllegalArgumentException("Display name must be specified");
        }
        this.digraph = digraph;
        this.trigraph = trigraph;
        this.demonym = demonym;
        this.displayName = displayName;
        this.alternateNames = alternateNames;
    }

    public String getDigraph() {
        return digraph;
    }

    public String getTrigraph() {
        return trigraph;
    }

    public String getDemonym() {
        return demonym;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String[] getAlternateNames() {
        return alternateNames != null ? Arrays.copyOf(alternateNames, alternateNames.length) : null;
    }

    private static final Map<String, Country> displayNameToCountry = new HashMap<>();
    private static final Map<String, Country> digraphToCountry = new HashMap<>();
    private static final Map<String, Country> trigraphToCountry = new HashMap<>();

    static {
        for (final Country country : Country.values()) {
            if (country.getDisplayName() != null) {
                if (displayNameToCountry.containsKey(country.getDisplayName())) {
                    LOGGER.log(Level.SEVERE, "Country display name maps to multiple countries: {0}", country.getDisplayName());
                    throw new IllegalStateException("Country display name maps to multiple countries: " + country.getDigraph());
                }
                displayNameToCountry.put(country.getDisplayName(), country);
            }
            final String[] alternateNamesCopy = country.getAlternateNames();
            if (alternateNamesCopy != null) {
                for (final String alternateName : alternateNamesCopy) {
                    if (displayNameToCountry.containsKey(alternateName)) {
                        LOGGER.log(Level.SEVERE, "Country alternative name maps to multiple countries: {0}", alternateName);
                        throw new IllegalStateException("Country alternative name maps to multiple countries: " + country.getDigraph());
                    }
                    displayNameToCountry.put(alternateName, country);
                }
            }
            if (country.getDigraph() != null) {
                if (digraphToCountry.containsKey(country.getDigraph())) {
                    LOGGER.log(Level.SEVERE, "Country digraph maps to multiple countries: {0}", country.getDigraph());
                    throw new IllegalStateException("Country digraph maps to multiple countries: " + country.getDigraph());
                }
                digraphToCountry.put(country.getDigraph().toUpperCase(), country);
            }
            if (country.getTrigraph() != null) {
                if (trigraphToCountry.containsKey(country.getTrigraph())) {
                    LOGGER.log(Level.SEVERE, "Country trigraph maps to multiple countries: {0}", country.getTrigraph());
                    throw new IllegalStateException("Country trigraph maps to multiple countries: " + country.getTrigraph());
                }
                trigraphToCountry.put(country.getTrigraph().toUpperCase(), country);
            }
        }
    }

    /**
     * Return the country for the specified country display name, or null if the
     * display name was not recognised.
     *
     * @param displayName the display name of the country.
     * @return the country for the specified country name, or null if the name
     * was not recognised.
     */
    public static synchronized Country lookupCountryDisplayName(final String displayName) {
        if (displayName == null) {
            return null;
        }
        return displayNameToCountry.get(displayName);
    }

    /**
     * Return the country for the specified country digraph, or null if the
     * digraph was not recognised.
     *
     * @param digraph the country digraph.
     * @return the country for the specified country digraph, or null if the
     * digraph was not recognised.
     */
    public static synchronized Country lookupCountryDigraph(final String digraph) {
        if (digraph == null) {
            return null;
        }
        return digraphToCountry.get(digraph.toUpperCase());
    }

    /**
     * Return the country for the specified country trigraph, or null if the
     * trigraph was not recognised.
     *
     * @param trigraph the country trigraph.
     * @return the country for the specified country trigraph, or null if the
     * trigraph was not recognised.
     */
    public static synchronized Country lookupCountryTrigraph(final String trigraph) {
        if (trigraph == null) {
            return null;
        }
        return trigraphToCountry.get(trigraph.toUpperCase());
    }
}

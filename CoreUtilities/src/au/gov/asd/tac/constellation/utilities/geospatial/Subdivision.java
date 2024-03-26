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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An enum of country subdivisions based on ISO 3166-2.
 *
 * @author cygnus_x-1
 */
public enum Subdivision {

    BADAKHSHAN(Country.AFGHANISTAN, "BDS", "Badakhshān"),
    BADGHIS(Country.AFGHANISTAN, "BDG", "Bādghīs"),
    BAGHLAN(Country.AFGHANISTAN, "BGL", "Baghlān"),
    BALKH(Country.AFGHANISTAN, "BAL", "Balkh"),
    BAMYAN(Country.AFGHANISTAN, "BAM", "Bāmyān"),
    DAYKUNDI(Country.AFGHANISTAN, "DAY", "Dāykundī"),
    FARAH(Country.AFGHANISTAN, "FRA", "Farāh"),
    FARYAB(Country.AFGHANISTAN, "FYB", "Fāryāb"),
    GHAZNI(Country.AFGHANISTAN, "GHA", "Ghaznī"),
    GHOR(Country.AFGHANISTAN, "GHO", "Ghōr"),
    HELMAND(Country.AFGHANISTAN, "HEL", "Helmand"),
    HERAT(Country.AFGHANISTAN, "HER", "Herāt"),
    JOWZJAN(Country.AFGHANISTAN, "JOW", "Jowzjān"),
    KABUL(Country.AFGHANISTAN, "KAB", "Kābul"),
    KANDAHAR(Country.AFGHANISTAN, "KAN", "Kandahār"),
    KAPISA(Country.AFGHANISTAN, "KAP", "Kāpīsā"),
    KHOST(Country.AFGHANISTAN, "KHO", "Khōst"),
    KUNAR(Country.AFGHANISTAN, "KNR", "Kunar"),
    KUNDUZ(Country.AFGHANISTAN, "KDZ", "Kunduz"),
    LAGHMAN(Country.AFGHANISTAN, "LAG", "Laghmān"),
    LOGAR(Country.AFGHANISTAN, "LOG", "Lōgar"),
    NANGARHAR(Country.AFGHANISTAN, "NAN", "Nangarhār"),
    NIMROZ(Country.AFGHANISTAN, "NIM", "Nīmrōz"),
    NURISTAN(Country.AFGHANISTAN, "NUR", "Nūristān"),
    PAKTIKA(Country.AFGHANISTAN, "PKA", "Paktīkā"),
    PAKTIYA(Country.AFGHANISTAN, "PIA", "Paktiyā"),
    PANJSHAYR(Country.AFGHANISTAN, "PAN", "Panjshayr"),
    PARWAN(Country.AFGHANISTAN, "PAR", "Parwān"),
    SAMANGAN(Country.AFGHANISTAN, "SAM", "Samangān"),
    SAR_E_PUL(Country.AFGHANISTAN, "SAR", "Sar-e Pul"),
    TAKHAR(Country.AFGHANISTAN, "TAK", "Takhār"),
    URUZGAN(Country.AFGHANISTAN, "URU", "Uruzgān"),
    WARDUK(Country.AFGHANISTAN, "WAR", "Wardak"),
    ZABUL(Country.AFGHANISTAN, "ZAB", "Zābul"),
    NEW_SOUTH_WALES(Country.AUSTRALIA, "NSW", "New South Wales"),
    QUEENSLAND(Country.AUSTRALIA, "QLD", "Queensland"),
    SOUTH_AUSTRALIA(Country.AUSTRALIA, "SA", "South Australia"),
    TASMANIA(Country.AUSTRALIA, "TAS", "Tasmania"),
    VICTORIA(Country.AUSTRALIA, "VIC", "Victoria"),
    WESTERN_AUSTRALIA(Country.AUSTRALIA, "WA", "Western Australia"),
    AUSTRALIAN_CAPITAL_TERRITORY(Country.AUSTRALIA, "ACT", "Australian Capital Territory"),
    NORTHERN_TERRITORY(Country.AUSTRALIA, "NT", "Northern Territory"),
    ABKHAZIA(Country.GEORGIA, "AB", "Abkhazia"),
    AJARIA(Country.GEORGIA, "AJ", "Ajaria"),
    TBILISI(Country.GEORGIA, "TB", "Tbilisi"),
    GURIA(Country.GEORGIA, "GU", "Guria"),
    IMERETI(Country.GEORGIA, "IM", "Imereti"),
    KAKHETI(Country.GEORGIA, "KA", "K'akheti"),
    KVEMO_KARTLI(Country.GEORGIA, "KK", "Kvemo Kartli"),
    MTSKHETA_MTIANETI(Country.GEORGIA, "MM", "Mtskheta-Mtianeti"),
    RACH_A_LECHKHUMI_KVEMO_SVANETI(Country.GEORGIA, "RL", "Rach'a-Lechkhumi-Kvemo Svaneti"),
    SAMEGRELO_ZEMO_SVANETI(Country.GEORGIA, "SZ", "Samegrelo-Zemo Svaneti"),
    SAMTSKHE_JAVAKHETI(Country.GEORGIA, "SJ", "Samtskhe-Javakheti"),
    SHIDA_KARTLII(Country.GEORGIA, "SK", "Shida Kartlii"),
    DRENTHE(Country.NETHERLANDS, "DR", "Drenthe"),
    FLEVOLAND(Country.NETHERLANDS, "FL", "Flevoland"),
    FRYSLAN(Country.NETHERLANDS, "FR", "Fryslân"),
    GELDERLAND(Country.NETHERLANDS, "GE", "Gelderland"),
    GRONINGEN(Country.NETHERLANDS, "GR", "Groningen"),
    LIMBURG(Country.NETHERLANDS, "LI", "Limburg"),
    NOORD_BRABANT(Country.NETHERLANDS, "NB", "Noord-Brabant"),
    NOORD_HOLLAND(Country.NETHERLANDS, "NH", "Noord-Holland"),
    OVERIJSSEL(Country.NETHERLANDS, "OV", "Overijssel"),
    UTRECHT(Country.NETHERLANDS, "UT", "Utrecht"),
    ZEELAND(Country.NETHERLANDS, "ZE", "Zeeland"),
    ZUID_HOLLAND(Country.NETHERLANDS, "ZH", "Zuid-Holland"),
    ARUBA(Country.NETHERLANDS, "AW", "Aruba"),
    CURACAO(Country.NETHERLANDS, "CW", "Curaçao"),
    SINT_MAARTEN(Country.NETHERLANDS, "SX", "Sint Maarten"),
    BONAIRE(Country.NETHERLANDS, "BQ1", "Bonaire"),
    SABA(Country.NETHERLANDS, "BQ2", "Saba"),
    SINT_EUSTATIUS(Country.NETHERLANDS, "BQ3", "Sint Eustatius"),
    ABU_DHABI(Country.UNITED_ARAB_EMIRATES, "AZ", "Abu Dhabi"),
    AJMAN(Country.UNITED_ARAB_EMIRATES, "AJ", "Ajman"),
    FUJAIRAH(Country.UNITED_ARAB_EMIRATES, "FU", "Fujairah"),
    SHARJAH(Country.UNITED_ARAB_EMIRATES, "SH", "Sharjah"),
    DUBAI(Country.UNITED_ARAB_EMIRATES, "DU", "Dubai"),
    RAS_AL_KHAIMAH(Country.UNITED_ARAB_EMIRATES, "RK", "Ras al-Khaimah"),
    UMM_AL_QUWAIN(Country.UNITED_ARAB_EMIRATES, "UQ", "Umm al-Quwain"),
    UNKNOWN_RESERVED_OR_PRIVATE(Country.UNKNOWN_RESERVED_OR_PRIVATE, "XX", "Unknown");

    private static final Logger LOGGER = Logger.getLogger(Subdivision.class.getName());
    private static final String CODE_SEPARATOR = "-";

    private Country country;
    private String code;
    private String displayName;

    private Subdivision(final Country country, final String code, final String displayName) {
        if (country == null) {
            throw new NullPointerException("Country must be specified");
        }
        if (code == null) {
            throw new NullPointerException("Code must be specified");
        }
        if (displayName == null) {
            throw new NullPointerException("Display name must be specified");
        }
        this.country = country;
        this.code = code;
        this.displayName = displayName;
    }

    public Country getCountry() {
        return country;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    private static final Map<String, Subdivision> displayNameToSubdivision = new HashMap<>();
    private static final Map<String, Subdivision> codeToSubdivision = new HashMap<>();

    static {
        for (final Subdivision subdivision : Subdivision.values()) {
            if (subdivision.getDisplayName() != null) {
                if (displayNameToSubdivision.containsKey(subdivision.getDisplayName())) {
                    LOGGER.log(Level.SEVERE, "Subdivision display name maps to multiple subdivisions: {0}", subdivision.getDisplayName());
                    throw new IllegalStateException("Subdivision display name maps to multiple subdivisions: " + subdivision.getDisplayName());
                }
                displayNameToSubdivision.put(subdivision.getDisplayName().toLowerCase(), subdivision);
            }
            if (subdivision.getCountry() != null && subdivision.getCode() != null) {
                final String fullyQualifiedCode = subdivision.getCountry().getDigraph() + CODE_SEPARATOR + subdivision.getCode().toUpperCase();
                if (codeToSubdivision.containsKey(fullyQualifiedCode)) {
                    LOGGER.log(Level.SEVERE, "Subdivision code maps to multiple subdivisions: {0}", fullyQualifiedCode);
                    throw new IllegalStateException("Subdivision code maps to multiple subdivisions: " + fullyQualifiedCode);
                }
                codeToSubdivision.put(fullyQualifiedCode, subdivision);
            }
        }
    }

    /**
     * Return the subdivision for the specified subdivision display name, or
     * null if the display name was not recognised.
     *
     * @param displayName the display name of the subdivision.
     * @return the subdivision for the specified subdivision name, or null if
     * the name was not recognised.
     */
    public static synchronized Subdivision lookupSubdivisionDisplayName(final String displayName) {
        if (displayName == null) {
            return null;
        }
        return displayNameToSubdivision.get(displayName.toLowerCase());
    }

    /**
     * Return the subdivision for the specified subdivision code, or null if the
     * code was not recognised.
     *
     * @param country the country to which the subdivision belongs.
     * @param code the subdivision code.
     * @return the subdivision for the specified subdivision code, or null if
     * the code was not recognised.
     */
    public static synchronized Subdivision lookupSubdivisionCode(final Country country, final String code) {
        if (country == null || code == null) {
            return null;
        }
        return codeToSubdivision.get(country.getDigraph() + CODE_SEPARATOR + code.toUpperCase());
    }
}

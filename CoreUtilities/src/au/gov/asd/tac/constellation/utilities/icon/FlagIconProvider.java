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
package au.gov.asd.tac.constellation.utilities.icon;

import au.gov.asd.tac.constellation.utilities.geospatial.Country;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * An IconProvider defining flag icons.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = ConstellationIconProvider.class)
public class FlagIconProvider implements ConstellationIconProvider {

    private static final Logger LOGGER = Logger.getLogger(FlagIconProvider.class.getName());

    private static final String CODE_NAME_BASE = "au.gov.asd.tac.constellation.utilities";

    private static final String FLAG_CATEGORY = "Flag";

    public static final ConstellationIcon AFGHANISTAN = new ConstellationIcon.Builder("Afghanistan", new FileIconData("modules/ext/icons/flags/afghanistan.png", CODE_NAME_BASE))
            .addAlias(Country.AFGHANISTAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ALBANIA = new ConstellationIcon.Builder("Albania", new FileIconData("modules/ext/icons/flags/albania.png", CODE_NAME_BASE))
            .addAlias(Country.ALBANIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ALGERIA = new ConstellationIcon.Builder("Algeria", new FileIconData("modules/ext/icons/flags/algeria.png", CODE_NAME_BASE))
            .addAlias(Country.ALGERIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ANDORRA = new ConstellationIcon.Builder("Andorra", new FileIconData("modules/ext/icons/flags/andorra.png", CODE_NAME_BASE))
            .addAlias(Country.ANDORRA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ANTIGUA_AND_BARBUDA = new ConstellationIcon.Builder("Antigua and Barbuda", new FileIconData("modules/ext/icons/flags/antigua_and_barbuda.png", CODE_NAME_BASE))
            .addAlias(Country.ANTIGUA_AND_BARBUDA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ARGENTINA = new ConstellationIcon.Builder("Argentina", new FileIconData("modules/ext/icons/flags/argentina.png", CODE_NAME_BASE))
            .addAlias(Country.ARGENTINA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ARMENIA = new ConstellationIcon.Builder("Armenia", new FileIconData("modules/ext/icons/flags/armenia.png", CODE_NAME_BASE))
            .addAlias(Country.ARMENIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon AUSTRALIA = new ConstellationIcon.Builder("Australia", new FileIconData("modules/ext/icons/flags/australia.png", CODE_NAME_BASE))
            .addAlias(Country.AUSTRALIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon AUSTRIA = new ConstellationIcon.Builder("Austria", new FileIconData("modules/ext/icons/flags/austria.png", CODE_NAME_BASE))
            .addAlias(Country.AUSTRIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon AZERBAIJAN = new ConstellationIcon.Builder("Azerbaijan", new FileIconData("modules/ext/icons/flags/azerbaijan.png", CODE_NAME_BASE))
            .addAlias(Country.AZERBAIJAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BAHAMAS = new ConstellationIcon.Builder("Bahamas", new FileIconData("modules/ext/icons/flags/bahamas.png", CODE_NAME_BASE))
            .addAlias(Country.BAHAMAS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BAHRAIN = new ConstellationIcon.Builder("Bahrain", new FileIconData("modules/ext/icons/flags/bahrain.png", CODE_NAME_BASE))
            .addAlias(Country.BAHRAIN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BANGLADESH = new ConstellationIcon.Builder("Bangladesh", new FileIconData("modules/ext/icons/flags/bangladesh.png", CODE_NAME_BASE))
            .addAlias(Country.BANGLADESH.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BARBADOS = new ConstellationIcon.Builder("Barbados", new FileIconData("modules/ext/icons/flags/barbados.png", CODE_NAME_BASE))
            .addAlias(Country.BARBADOS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BELARUS = new ConstellationIcon.Builder("Belarus", new FileIconData("modules/ext/icons/flags/belarus.png", CODE_NAME_BASE))
            .addAlias(Country.BELARUS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BELGIUM = new ConstellationIcon.Builder("Belgium", new FileIconData("modules/ext/icons/flags/belgium.png", CODE_NAME_BASE))
            .addAlias(Country.BELGIUM.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BELIZE = new ConstellationIcon.Builder("Belize", new FileIconData("modules/ext/icons/flags/belize.png", CODE_NAME_BASE))
            .addAlias(Country.BELIZE.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BENIN = new ConstellationIcon.Builder("Benin", new FileIconData("modules/ext/icons/flags/benin.png", CODE_NAME_BASE))
            .addAlias(Country.BENIN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BHUTAN = new ConstellationIcon.Builder("Bhutan", new FileIconData("modules/ext/icons/flags/bhutan.png", CODE_NAME_BASE))
            .addAlias(Country.BHUTAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BOLIVIA = new ConstellationIcon.Builder("Bolivia", new FileIconData("modules/ext/icons/flags/bolivia.png", CODE_NAME_BASE))
            .addAlias(Country.BOLIVIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BOSNIA_AND_HERZEGOVINA = new ConstellationIcon.Builder("Bosnia and Herzegovina", new FileIconData("modules/ext/icons/flags/bosnia_and_herzegovina.png", CODE_NAME_BASE))
            .addAlias(Country.BOSNIA_AND_HERZEGOVINA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BOTSWANA = new ConstellationIcon.Builder("Botswana", new FileIconData("modules/ext/icons/flags/botswana.png", CODE_NAME_BASE))
            .addAlias(Country.BOTSWANA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BRAZIL = new ConstellationIcon.Builder("Brazil", new FileIconData("modules/ext/icons/flags/brazil.png", CODE_NAME_BASE))
            .addAlias(Country.BRAZIL.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BRUNEI = new ConstellationIcon.Builder("Brunei", new FileIconData("modules/ext/icons/flags/brunei.png", CODE_NAME_BASE))
            .addAlias(Country.BRUNEI_DARUSSALAM.getDigraph())
            .addAlias("Brunei Darussalam")
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BULGARIA = new ConstellationIcon.Builder("Bulgaria", new FileIconData("modules/ext/icons/flags/bulgaria.png", CODE_NAME_BASE))
            .addAlias(Country.BULGARIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BURKINA_FASO = new ConstellationIcon.Builder("Burkina Faso", new FileIconData("modules/ext/icons/flags/burkina_faso.png", CODE_NAME_BASE))
            .addAlias(Country.BURKINA_FASO.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BURUNDAI = new ConstellationIcon.Builder("Burundi", new FileIconData("modules/ext/icons/flags/burundi.png", CODE_NAME_BASE))
            .addAlias(Country.BURUNDI.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CAMBODIA = new ConstellationIcon.Builder("Cambodia", new FileIconData("modules/ext/icons/flags/cambodia.png", CODE_NAME_BASE))
            .addAlias(Country.CAMBODIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CAMEROON = new ConstellationIcon.Builder("Cameroon", new FileIconData("modules/ext/icons/flags/cameroon.png", CODE_NAME_BASE))
            .addAlias(Country.CAMEROON.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CANADA = new ConstellationIcon.Builder("Canada", new FileIconData("modules/ext/icons/flags/canada.png", CODE_NAME_BASE))
            .addAlias(Country.CANADA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CAPE_VERDE = new ConstellationIcon.Builder("Cape Verde", new FileIconData("modules/ext/icons/flags/cape_verde.png", CODE_NAME_BASE))
            .addAlias(Country.CABO_VERDE.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CENTRAL_AFRICAN_REPUBLIC = new ConstellationIcon.Builder("Central African Republic", new FileIconData("modules/ext/icons/flags/central_african_republic.png", CODE_NAME_BASE))
            .addAlias(Country.CENTRAL_AFRICAN_REPUBLIC.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CHAD = new ConstellationIcon.Builder("Chad", new FileIconData("modules/ext/icons/flags/chad.png", CODE_NAME_BASE))
            .addAlias(Country.CHAD.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CHILE = new ConstellationIcon.Builder("Chile", new FileIconData("modules/ext/icons/flags/chile.png", CODE_NAME_BASE))
            .addAlias(Country.CHILE.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CHINA = new ConstellationIcon.Builder("China", new FileIconData("modules/ext/icons/flags/china.png", CODE_NAME_BASE))
            .addAlias(Country.CHINA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon COLOMBIA = new ConstellationIcon.Builder("Colombia", new FileIconData("modules/ext/icons/flags/colombia.png", CODE_NAME_BASE))
            .addAlias(Country.COLOMBIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon COMOROS = new ConstellationIcon.Builder("Comoros", new FileIconData("modules/ext/icons/flags/comoros.png", CODE_NAME_BASE))
            .addAlias(Country.COMOROS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CONGO_DEMOCRATIC = new ConstellationIcon.Builder("Congo (Democratic)", new FileIconData("modules/ext/icons/flags/congo_democratic.png", CODE_NAME_BASE))
            .addAlias(Country.CONGO_DEMOCRATIC_REPUBLIC.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CONGO_REPUBLIC = new ConstellationIcon.Builder("Congo (Republic)", new FileIconData("modules/ext/icons/flags/congo_republic.png", CODE_NAME_BASE))
            .addAlias(Country.CONGO.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon COSTA_RICA = new ConstellationIcon.Builder("Costa Rica", new FileIconData("modules/ext/icons/flags/costa_rica.png", CODE_NAME_BASE))
            .addAlias(Country.COSTA_RICA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon COTE_DIVOIRE = new ConstellationIcon.Builder("Cote d'Ivoire", new FileIconData("modules/ext/icons/flags/cote_d'ivoire.png", CODE_NAME_BASE))
            .addAlias(Country.COTE_DIVOIRE.getDigraph())
            .addAlias("Cote D'Ivoire")
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CROATIA = new ConstellationIcon.Builder("Croatia", new FileIconData("modules/ext/icons/flags/croatia.png", CODE_NAME_BASE))
            .addAlias(Country.CROATIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CUBA = new ConstellationIcon.Builder("Cuba", new FileIconData("modules/ext/icons/flags/cuba.png", CODE_NAME_BASE))
            .addAlias(Country.CUBA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CYPRUS = new ConstellationIcon.Builder("Cyprus", new FileIconData("modules/ext/icons/flags/cyprus.png", CODE_NAME_BASE))
            .addAlias(Country.CYPRUS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CZECH_REPUBLIC = new ConstellationIcon.Builder("Czech Republic", new FileIconData("modules/ext/icons/flags/czech_republic.png", CODE_NAME_BASE))
            .addAlias(Country.CZECH_REPUBLIC.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon DENMARK = new ConstellationIcon.Builder("Denmark", new FileIconData("modules/ext/icons/flags/denmark.png", CODE_NAME_BASE))
            .addAlias(Country.DENMARK.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon DJIBOUTI = new ConstellationIcon.Builder("Djibouti", new FileIconData("modules/ext/icons/flags/djibouti.png", CODE_NAME_BASE))
            .addAlias(Country.DJIBOUTI.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon DOMINICA = new ConstellationIcon.Builder("Dominica", new FileIconData("modules/ext/icons/flags/dominica.png", CODE_NAME_BASE))
            .addAlias(Country.DOMINICA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon DOMINICAN_REPUBLIC = new ConstellationIcon.Builder("Dominican Republic", new FileIconData("modules/ext/icons/flags/dominican_republic.png", CODE_NAME_BASE))
            .addAlias(Country.DOMINICAN_REPUBLIC.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon EAST_TIMOR = new ConstellationIcon.Builder("East Timor", new FileIconData("modules/ext/icons/flags/east_timor.png", CODE_NAME_BASE))
            .addAlias(Country.TIMOR_LESTE.getDigraph())
            .addAlias("Timor-Leste")
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ECUADOR = new ConstellationIcon.Builder("Ecuador", new FileIconData("modules/ext/icons/flags/ecuador.png", CODE_NAME_BASE))
            .addAlias(Country.ECUADOR.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon EGYPT = new ConstellationIcon.Builder("Egypt", new FileIconData("modules/ext/icons/flags/egypt.png", CODE_NAME_BASE))
            .addAlias(Country.EGYPT.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon EL_SALVADOR = new ConstellationIcon.Builder("El Salvador", new FileIconData("modules/ext/icons/flags/el_salvador.png", CODE_NAME_BASE))
            .addAlias(Country.EL_SALVADOR.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon EQUATORIAL_GUINEA = new ConstellationIcon.Builder("Equatorial Guinea", new FileIconData("modules/ext/icons/flags/equatorial_guinea.png", CODE_NAME_BASE))
            .addAlias(Country.EQUATORIAL_GUINEA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ERITREA = new ConstellationIcon.Builder("Eritrea", new FileIconData("modules/ext/icons/flags/eritrea.png", CODE_NAME_BASE))
            .addAlias(Country.ERITREA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ESTONIA = new ConstellationIcon.Builder("Estonia", new FileIconData("modules/ext/icons/flags/estonia.png", CODE_NAME_BASE))
            .addAlias(Country.ESTONIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ETHIOPIA = new ConstellationIcon.Builder("Ethiopia", new FileIconData("modules/ext/icons/flags/ethiopia.png", CODE_NAME_BASE))
            .addAlias(Country.ETHIOPIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon FIJI = new ConstellationIcon.Builder("Fiji", new FileIconData("modules/ext/icons/flags/fiji.png", CODE_NAME_BASE))
            .addAlias(Country.FIJI.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon FINLAND = new ConstellationIcon.Builder("Finland", new FileIconData("modules/ext/icons/flags/finland.png", CODE_NAME_BASE))
            .addAlias(Country.FINLAND.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon FRANCE = new ConstellationIcon.Builder("France", new FileIconData("modules/ext/icons/flags/france.png", CODE_NAME_BASE))
            .addAlias(Country.FRANCE.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon GABON = new ConstellationIcon.Builder("Gabon", new FileIconData("modules/ext/icons/flags/gabon.png", CODE_NAME_BASE))
            .addAlias(Country.GABON.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon GAMBIA = new ConstellationIcon.Builder("Gambia", new FileIconData("modules/ext/icons/flags/gambia.png", CODE_NAME_BASE))
            .addAlias(Country.GAMBIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon GEORGIA = new ConstellationIcon.Builder("Georgia", new FileIconData("modules/ext/icons/flags/georgia.png", CODE_NAME_BASE))
            .addAlias(Country.GEORGIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon GERMANY = new ConstellationIcon.Builder("Germany", new FileIconData("modules/ext/icons/flags/germany.png", CODE_NAME_BASE))
            .addAlias(Country.GERMANY.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon GHANA = new ConstellationIcon.Builder("Ghana", new FileIconData("modules/ext/icons/flags/ghana.png", CODE_NAME_BASE))
            .addAlias(Country.GHANA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon GREECE = new ConstellationIcon.Builder("Greece", new FileIconData("modules/ext/icons/flags/greece.png", CODE_NAME_BASE))
            .addAlias(Country.GREECE.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon GRENADA = new ConstellationIcon.Builder("Grenada", new FileIconData("modules/ext/icons/flags/grenada.png", CODE_NAME_BASE))
            .addAlias(Country.GRENADA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon GUATEMALA = new ConstellationIcon.Builder("Guatemala", new FileIconData("modules/ext/icons/flags/guatemala.png", CODE_NAME_BASE))
            .addAlias(Country.GUATEMALA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon GUINEA = new ConstellationIcon.Builder("Guinea", new FileIconData("modules/ext/icons/flags/guinea.png", CODE_NAME_BASE))
            .addAlias(Country.GUINEA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon GUINEA_BISSAU = new ConstellationIcon.Builder("Guinea Bissau", new FileIconData("modules/ext/icons/flags/guinea_bissau.png", CODE_NAME_BASE))
            .addAlias(Country.GUINEA_BISSAU.getDigraph())
            .addAlias("Guinea-Bissau")
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon GUYANA = new ConstellationIcon.Builder("Guyana", new FileIconData("modules/ext/icons/flags/guyana.png", CODE_NAME_BASE))
            .addAlias(Country.GUYANA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon HAITI = new ConstellationIcon.Builder("Haiti", new FileIconData("modules/ext/icons/flags/haiti.png", CODE_NAME_BASE))
            .addAlias(Country.HAITI.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon HONDURAS = new ConstellationIcon.Builder("Honduras", new FileIconData("modules/ext/icons/flags/honduras.png", CODE_NAME_BASE))
            .addAlias(Country.HONDURAS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon HUNGARY = new ConstellationIcon.Builder("Hungary", new FileIconData("modules/ext/icons/flags/hungary.png", CODE_NAME_BASE))
            .addAlias(Country.HUNGARY.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ICELAND = new ConstellationIcon.Builder("Iceland", new FileIconData("modules/ext/icons/flags/iceland.png", CODE_NAME_BASE))
            .addAlias(Country.ICELAND.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon INDIA = new ConstellationIcon.Builder("India", new FileIconData("modules/ext/icons/flags/india.png", CODE_NAME_BASE))
            .addAlias(Country.INDIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon INDONESIA = new ConstellationIcon.Builder("Indonesia", new FileIconData("modules/ext/icons/flags/indonesia.png", CODE_NAME_BASE))
            .addAlias(Country.INDONESIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon IRAN = new ConstellationIcon.Builder("Iran", new FileIconData("modules/ext/icons/flags/iran.png", CODE_NAME_BASE))
            .addAlias(Country.IRAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon IRAQ = new ConstellationIcon.Builder("Iraq", new FileIconData("modules/ext/icons/flags/iraq.png", CODE_NAME_BASE))
            .addAlias(Country.IRAQ.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon IRELAND = new ConstellationIcon.Builder("Ireland", new FileIconData("modules/ext/icons/flags/ireland.png", CODE_NAME_BASE))
            .addAlias(Country.IRELAND.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ISRAEL = new ConstellationIcon.Builder("Israel", new FileIconData("modules/ext/icons/flags/israel.png", CODE_NAME_BASE))
            .addAlias(Country.ISRAEL.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ITALY = new ConstellationIcon.Builder("Italy", new FileIconData("modules/ext/icons/flags/italy.png", CODE_NAME_BASE))
            .addAlias(Country.ITALY.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon JAMAICA = new ConstellationIcon.Builder("Jamaica", new FileIconData("modules/ext/icons/flags/jamaica.png", CODE_NAME_BASE))
            .addAlias(Country.JAMAICA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon JAPAN = new ConstellationIcon.Builder("Japan", new FileIconData("modules/ext/icons/flags/japan.png", CODE_NAME_BASE))
            .addAlias(Country.JAPAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon JORDAN = new ConstellationIcon.Builder("Jordan", new FileIconData("modules/ext/icons/flags/jordan.png", CODE_NAME_BASE))
            .addAlias(Country.JORDAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon KAZAKHSTAN = new ConstellationIcon.Builder("Kazakhstan", new FileIconData("modules/ext/icons/flags/kazakhstan.png", CODE_NAME_BASE))
            .addAlias(Country.KAZAKHSTAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon KENYA = new ConstellationIcon.Builder("Kenya", new FileIconData("modules/ext/icons/flags/kenya.png", CODE_NAME_BASE))
            .addAlias(Country.KENYA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon KIRIBATI = new ConstellationIcon.Builder("Kiribati", new FileIconData("modules/ext/icons/flags/kiribati.png", CODE_NAME_BASE))
            .addAlias(Country.KIRIBATI.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon KOREA_NORTH = new ConstellationIcon.Builder("North Koread", new FileIconData("modules/ext/icons/flags/korea_north.png", CODE_NAME_BASE))
            .addAlias(Country.KOREA_NORTH.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon KOREA_SOUTH = new ConstellationIcon.Builder("South Korea", new FileIconData("modules/ext/icons/flags/korea_south.png", CODE_NAME_BASE))
            .addAlias(Country.KOREA_SOUTH.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon KOSOVO = new ConstellationIcon.Builder("Kosovo", new FileIconData("modules/ext/icons/flags/kosovo.png", CODE_NAME_BASE))
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon KUWAIT = new ConstellationIcon.Builder("Kuwait", new FileIconData("modules/ext/icons/flags/kuwait.png", CODE_NAME_BASE))
            .addAlias(Country.KUWAIT.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon KYRGYZSTAN = new ConstellationIcon.Builder("Kyrgyzstan", new FileIconData("modules/ext/icons/flags/kyrgyzstan.png", CODE_NAME_BASE))
            .addAlias(Country.KYRGYZSTAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon LAOS = new ConstellationIcon.Builder("Laos", new FileIconData("modules/ext/icons/flags/laos.png", CODE_NAME_BASE))
            .addAlias(Country.LAOS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon LATVIA = new ConstellationIcon.Builder("Latvia", new FileIconData("modules/ext/icons/flags/latvia.png", CODE_NAME_BASE))
            .addAlias(Country.LATVIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon LEBANON = new ConstellationIcon.Builder("Lebanon", new FileIconData("modules/ext/icons/flags/lebanon.png", CODE_NAME_BASE))
            .addAlias(Country.LEBANON.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon LESOTHO = new ConstellationIcon.Builder("Lesotho", new FileIconData("modules/ext/icons/flags/lesotho.png", CODE_NAME_BASE))
            .addAlias(Country.LESOTHO.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon LIBERIA = new ConstellationIcon.Builder("Liberia", new FileIconData("modules/ext/icons/flags/liberia.png", CODE_NAME_BASE))
            .addAlias(Country.LIBERIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon LIBYA = new ConstellationIcon.Builder("Libya", new FileIconData("modules/ext/icons/flags/libya.png", CODE_NAME_BASE))
            .addAlias(Country.LIBYA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon LIECHTENSTEIN = new ConstellationIcon.Builder("Liechtenstein", new FileIconData("modules/ext/icons/flags/liechtenstein.png", CODE_NAME_BASE))
            .addAlias(Country.LIECHTENSTEIN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon LITHUANIA = new ConstellationIcon.Builder("Lithuania", new FileIconData("modules/ext/icons/flags/lithuania.png", CODE_NAME_BASE))
            .addAlias(Country.LITHUANIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon LUXEMBOURG = new ConstellationIcon.Builder("Luxembourg", new FileIconData("modules/ext/icons/flags/luxembourg.png", CODE_NAME_BASE))
            .addAlias(Country.LUXEMBOURG.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MACEDONIA = new ConstellationIcon.Builder("Macedonia", new FileIconData("modules/ext/icons/flags/macedonia.png", CODE_NAME_BASE))
            .addAlias(Country.MACEDONIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MADAGASCAR = new ConstellationIcon.Builder("Madagascar", new FileIconData("modules/ext/icons/flags/madagascar.png", CODE_NAME_BASE))
            .addAlias(Country.MADAGASCAR.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MALAWI = new ConstellationIcon.Builder("Malawi", new FileIconData("modules/ext/icons/flags/malawi.png", CODE_NAME_BASE))
            .addAlias(Country.MALAWI.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MALAYSIA = new ConstellationIcon.Builder("Malaysia", new FileIconData("modules/ext/icons/flags/malaysia.png", CODE_NAME_BASE))
            .addAlias(Country.MALAYSIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MALDIVES = new ConstellationIcon.Builder("Maldives", new FileIconData("modules/ext/icons/flags/maldives.png", CODE_NAME_BASE))
            .addAlias(Country.MALDIVES.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MALI = new ConstellationIcon.Builder("Mali", new FileIconData("modules/ext/icons/flags/mali.png", CODE_NAME_BASE))
            .addAlias(Country.MALI.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MALTA = new ConstellationIcon.Builder("Malta", new FileIconData("modules/ext/icons/flags/malta.png", CODE_NAME_BASE))
            .addAlias(Country.MALTA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MARSHALL_ISLANDS = new ConstellationIcon.Builder("Marshall Islands", new FileIconData("modules/ext/icons/flags/marshall_islands.png", CODE_NAME_BASE))
            .addAlias(Country.MARSHALL_ISLANDS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MAURITANIA = new ConstellationIcon.Builder("Mauritania", new FileIconData("modules/ext/icons/flags/mauritania.png", CODE_NAME_BASE))
            .addAlias(Country.MAURITANIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MAURITIUS = new ConstellationIcon.Builder("Mauritius", new FileIconData("modules/ext/icons/flags/mauritius.png", CODE_NAME_BASE))
            .addAlias(Country.MAURITIUS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MEXICO = new ConstellationIcon.Builder("Mexico", new FileIconData("modules/ext/icons/flags/mexico.png", CODE_NAME_BASE))
            .addAlias(Country.MEXICO.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MICRONESIA = new ConstellationIcon.Builder("Micronesia", new FileIconData("modules/ext/icons/flags/micronesia_federated.png", CODE_NAME_BASE))
            .addAlias(Country.MICRONESIA.getDigraph())
            .addAlias("Micronesia, Federated States of")
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MOLDOVA = new ConstellationIcon.Builder("Moldova", new FileIconData("modules/ext/icons/flags/moldova.png", CODE_NAME_BASE))
            .addAlias(Country.MOLDOVA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MONACO = new ConstellationIcon.Builder("Monaco", new FileIconData("modules/ext/icons/flags/monaco.png", CODE_NAME_BASE))
            .addAlias(Country.MONACO.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MONGOLIA = new ConstellationIcon.Builder("Mongolia", new FileIconData("modules/ext/icons/flags/mongolia.png", CODE_NAME_BASE))
            .addAlias(Country.MONGOLIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MONTENEGRO = new ConstellationIcon.Builder("Montenegro", new FileIconData("modules/ext/icons/flags/montenegro.png", CODE_NAME_BASE))
            .addAlias(Country.MONTENEGRO.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MOROCCO = new ConstellationIcon.Builder("Morocco", new FileIconData("modules/ext/icons/flags/morocco.png", CODE_NAME_BASE))
            .addAlias(Country.MOROCCO.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MOZAMBIQUE = new ConstellationIcon.Builder("Mozambique", new FileIconData("modules/ext/icons/flags/mozambique.png", CODE_NAME_BASE))
            .addAlias(Country.MOZAMBIQUE.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MYANMAR = new ConstellationIcon.Builder("Myanmar", new FileIconData("modules/ext/icons/flags/myanmar.png", CODE_NAME_BASE))
            .addAlias(Country.MYANMAR.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon NAMIBIA = new ConstellationIcon.Builder("Namibia", new FileIconData("modules/ext/icons/flags/namibia.png", CODE_NAME_BASE))
            .addAlias(Country.NAMIBIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon NAURU = new ConstellationIcon.Builder("Nauru", new FileIconData("modules/ext/icons/flags/nauru.png", CODE_NAME_BASE))
            .addAlias(Country.NAURU.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon NEPAL = new ConstellationIcon.Builder("Nepal", new FileIconData("modules/ext/icons/flags/nepal.png", CODE_NAME_BASE))
            .addAlias(Country.NEPAL.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon NETHERLANDS = new ConstellationIcon.Builder("Netherlands", new FileIconData("modules/ext/icons/flags/netherlands.png", CODE_NAME_BASE))
            .addAlias(Country.NETHERLANDS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon NEW_ZEALAND = new ConstellationIcon.Builder("New Zealand", new FileIconData("modules/ext/icons/flags/new_zealand.png", CODE_NAME_BASE))
            .addAlias(Country.NEW_ZEALAND.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon NICARAGUA = new ConstellationIcon.Builder("Nicaragua", new FileIconData("modules/ext/icons/flags/nicaragua.png", CODE_NAME_BASE))
            .addAlias(Country.NICARAGUA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon NIGER = new ConstellationIcon.Builder("Niger", new FileIconData("modules/ext/icons/flags/niger.png", CODE_NAME_BASE))
            .addAlias(Country.NIGER.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon NIGERIA = new ConstellationIcon.Builder("Nigeria", new FileIconData("modules/ext/icons/flags/nigeria.png", CODE_NAME_BASE))
            .addAlias(Country.NIGERIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon NORWAY = new ConstellationIcon.Builder("Norway", new FileIconData("modules/ext/icons/flags/norway.png", CODE_NAME_BASE))
            .addAlias(Country.NORWAY.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon OMAN = new ConstellationIcon.Builder("Oman", new FileIconData("modules/ext/icons/flags/oman.png", CODE_NAME_BASE))
            .addAlias(Country.OMAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon PAKISTAN = new ConstellationIcon.Builder("Pakistan", new FileIconData("modules/ext/icons/flags/pakistan.png", CODE_NAME_BASE))
            .addAlias(Country.PAKISTAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon PALAU = new ConstellationIcon.Builder("Palau", new FileIconData("modules/ext/icons/flags/palau.png", CODE_NAME_BASE))
            .addAlias(Country.PALAU.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon PANAMA = new ConstellationIcon.Builder("Panama", new FileIconData("modules/ext/icons/flags/panama.png", CODE_NAME_BASE))
            .addAlias(Country.PANAMA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon PAPUA_NEW_GUINEA = new ConstellationIcon.Builder("Papua New Guinea", new FileIconData("modules/ext/icons/flags/papua_new_guinea.png", CODE_NAME_BASE))
            .addAlias(Country.PAPUA_NEW_GUINEA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon PARAGUAY = new ConstellationIcon.Builder("Paraguay", new FileIconData("modules/ext/icons/flags/paraguay.png", CODE_NAME_BASE))
            .addAlias(Country.PARAGUAY.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon PERU = new ConstellationIcon.Builder("Peru", new FileIconData("modules/ext/icons/flags/peru.png", CODE_NAME_BASE))
            .addAlias(Country.PERU.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon PHILIPPINES = new ConstellationIcon.Builder("Philippines", new FileIconData("modules/ext/icons/flags/philippines.png", CODE_NAME_BASE))
            .addAlias(Country.PHILIPPINES.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon POLAND = new ConstellationIcon.Builder("Poland", new FileIconData("modules/ext/icons/flags/poland.png", CODE_NAME_BASE))
            .addAlias(Country.POLAND.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon PORTUGAL = new ConstellationIcon.Builder("Portugal", new FileIconData("modules/ext/icons/flags/portugal.png", CODE_NAME_BASE))
            .addAlias(Country.PORTUGAL.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon QATAR = new ConstellationIcon.Builder("Qatar", new FileIconData("modules/ext/icons/flags/qatar.png", CODE_NAME_BASE))
            .addAlias(Country.QATAR.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ROMANIA = new ConstellationIcon.Builder("Romania", new FileIconData("modules/ext/icons/flags/romania.png", CODE_NAME_BASE))
            .addAlias(Country.ROMANIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon RUSSIA = new ConstellationIcon.Builder("Russia", new FileIconData("modules/ext/icons/flags/russia.png", CODE_NAME_BASE))
            .addAlias(Country.RUSSIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon RWANDA = new ConstellationIcon.Builder("Rwanda", new FileIconData("modules/ext/icons/flags/rwanda.png", CODE_NAME_BASE))
            .addAlias(Country.RWANDA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SAINT_KITTS_AND_NEVIS = new ConstellationIcon.Builder("Saint Kitts and Nevis", new FileIconData("modules/ext/icons/flags/saint_kitts_and_nevis.png", CODE_NAME_BASE))
            .addAlias(Country.SAINT_KITTS_AND_NEVIS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SAINT_LUCIA = new ConstellationIcon.Builder("Saint Lucia", new FileIconData("modules/ext/icons/flags/saint_lucia.png", CODE_NAME_BASE))
            .addAlias(Country.SAINT_LUCIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SAINT_VINCENT_AND_THE_GRENADINES = new ConstellationIcon.Builder("Saint Vincent and the Grenadines", new FileIconData("modules/ext/icons/flags/saint_vincent_and_the_grenadines.png", CODE_NAME_BASE))
            .addAlias(Country.SAINT_VINCENT_AND_THE_GRENADINES.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SAMOA = new ConstellationIcon.Builder("Samoa", new FileIconData("modules/ext/icons/flags/samoa.png", CODE_NAME_BASE))
            .addAlias(Country.SAMOA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SAN_MARINO = new ConstellationIcon.Builder("San Marino", new FileIconData("modules/ext/icons/flags/san_marino.png", CODE_NAME_BASE))
            .addAlias(Country.SAN_MARINO.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SAO_TOME_AND_PRINCIPE = new ConstellationIcon.Builder("Sao Tome and Principe", new FileIconData("modules/ext/icons/flags/sao_tome_and_principe.png", CODE_NAME_BASE))
            .addAlias(Country.SAO_TOME_AND_PRINCIPE.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SAUDI_ARABIA = new ConstellationIcon.Builder("Saudi Arabia", new FileIconData("modules/ext/icons/flags/saudi_arabia.png", CODE_NAME_BASE))
            .addAlias(Country.SAUDI_ARABIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SENEGAL = new ConstellationIcon.Builder("Senegal", new FileIconData("modules/ext/icons/flags/senegal.png", CODE_NAME_BASE))
            .addAlias(Country.SENEGAL.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SERBIA = new ConstellationIcon.Builder("Serbia", new FileIconData("modules/ext/icons/flags/serbia.png", CODE_NAME_BASE))
            .addAlias(Country.SERBIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SEYCHELLES = new ConstellationIcon.Builder("Seychelles", new FileIconData("modules/ext/icons/flags/seychelles.png", CODE_NAME_BASE))
            .addAlias(Country.SEYCHELLES.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SIERRA_LEONE = new ConstellationIcon.Builder("Sierra Leone", new FileIconData("modules/ext/icons/flags/sierra_leone.png", CODE_NAME_BASE))
            .addAlias(Country.SIERRA_LEONE.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SINGAPORE = new ConstellationIcon.Builder("Singapore", new FileIconData("modules/ext/icons/flags/singapore.png", CODE_NAME_BASE))
            .addAlias(Country.SINGAPORE.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SLOVAKIA = new ConstellationIcon.Builder("Slovakia", new FileIconData("modules/ext/icons/flags/slovakia.png", CODE_NAME_BASE))
            .addAlias(Country.SLOVAKIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SLOVENIA = new ConstellationIcon.Builder("Slovenia", new FileIconData("modules/ext/icons/flags/slovenia.png", CODE_NAME_BASE))
            .addAlias(Country.SLOVENIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SOLOMON_ISLANDS = new ConstellationIcon.Builder("Solomon Islands", new FileIconData("modules/ext/icons/flags/solomon_islands.png", CODE_NAME_BASE))
            .addAlias(Country.SOLOMON_ISLANDS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SOMALIA = new ConstellationIcon.Builder("Somalia", new FileIconData("modules/ext/icons/flags/somalia.png", CODE_NAME_BASE))
            .addAlias(Country.SOMALIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SOUTH_AFRICA = new ConstellationIcon.Builder("South Africa", new FileIconData("modules/ext/icons/flags/south_africa.png", CODE_NAME_BASE))
            .addAlias(Country.SOUTH_AFRICA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SOUTH_SUDAN = new ConstellationIcon.Builder("South Sudan", new FileIconData("modules/ext/icons/flags/south_sudan.png", CODE_NAME_BASE))
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SPAIN = new ConstellationIcon.Builder("Spain", new FileIconData("modules/ext/icons/flags/spain.png", CODE_NAME_BASE))
            .addAlias(Country.SPAIN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SRI_LANKA = new ConstellationIcon.Builder("Sri Lanka", new FileIconData("modules/ext/icons/flags/sri_lanka.png", CODE_NAME_BASE))
            .addAlias(Country.SRI_LANKA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SUDAN = new ConstellationIcon.Builder("Sudan", new FileIconData("modules/ext/icons/flags/sudan.png", CODE_NAME_BASE))
            .addAlias(Country.SUDAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SURINAME = new ConstellationIcon.Builder("Suriname", new FileIconData("modules/ext/icons/flags/suriname.png", CODE_NAME_BASE))
            .addAlias(Country.SURINAME.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ESWATINI = new ConstellationIcon.Builder("Swaziland", new FileIconData("modules/ext/icons/flags/swaziland.png", CODE_NAME_BASE))
            .addAlias(Country.ESWATINI.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SWEDEN = new ConstellationIcon.Builder("Sweden", new FileIconData("modules/ext/icons/flags/sweden.png", CODE_NAME_BASE))
            .addAlias(Country.SWEDEN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SWITZERLAND = new ConstellationIcon.Builder("Switzerland", new FileIconData("modules/ext/icons/flags/switzerland.png", CODE_NAME_BASE))
            .addAlias(Country.SWITZERLAND.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon SYRIA = new ConstellationIcon.Builder("Syria", new FileIconData("modules/ext/icons/flags/syria.png", CODE_NAME_BASE))
            .addAlias(Country.SYRIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon TAIWAN = new ConstellationIcon.Builder("Taiwan", new FileIconData("modules/ext/icons/flags/taiwan.png", CODE_NAME_BASE))
            .addAlias(Country.TAIWAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon TAJIKISTAN = new ConstellationIcon.Builder("Tajikistan", new FileIconData("modules/ext/icons/flags/tajikistan.png", CODE_NAME_BASE))
            .addAlias(Country.TAJIKISTAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon TANZANIA = new ConstellationIcon.Builder("Tanzania", new FileIconData("modules/ext/icons/flags/tanzania.png", CODE_NAME_BASE))
            .addAlias(Country.TANZANIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon THAILAND = new ConstellationIcon.Builder("Thailand", new FileIconData("modules/ext/icons/flags/thailand.png", CODE_NAME_BASE))
            .addAlias(Country.THAILAND.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon TOGO = new ConstellationIcon.Builder("Togo", new FileIconData("modules/ext/icons/flags/togo.png", CODE_NAME_BASE))
            .addAlias(Country.TOGO.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon TONGA = new ConstellationIcon.Builder("Tonga", new FileIconData("modules/ext/icons/flags/tonga.png", CODE_NAME_BASE))
            .addAlias(Country.TONGA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon TRINIDAD_AND_TOBAGO = new ConstellationIcon.Builder("Trinidad and Tobago", new FileIconData("modules/ext/icons/flags/trinidad_and_tobago.png", CODE_NAME_BASE))
            .addAlias(Country.TRINIDAD_AND_TOBAGO.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon TUNISIA = new ConstellationIcon.Builder("Tunisia", new FileIconData("modules/ext/icons/flags/tunisia.png", CODE_NAME_BASE))
            .addAlias(Country.TUNISIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon TURKEY = new ConstellationIcon.Builder("Turkey", new FileIconData("modules/ext/icons/flags/turkey.png", CODE_NAME_BASE))
            .addAlias(Country.TURKEY.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon TURKMENISTAN = new ConstellationIcon.Builder("Turkmenistan", new FileIconData("modules/ext/icons/flags/turkmenistan.png", CODE_NAME_BASE))
            .addAlias(Country.TURKMENISTAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon TUVALU = new ConstellationIcon.Builder("Tuvalu", new FileIconData("modules/ext/icons/flags/tuvalu.png", CODE_NAME_BASE))
            .addAlias(Country.TUVALU.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon UGANDA = new ConstellationIcon.Builder("Uganda", new FileIconData("modules/ext/icons/flags/uganda.png", CODE_NAME_BASE))
            .addAlias(Country.UGANDA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon UKRAINE = new ConstellationIcon.Builder("Ukraine", new FileIconData("modules/ext/icons/flags/ukraine.png", CODE_NAME_BASE))
            .addAlias(Country.UKRAINE.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon UNITED_ARAB_EMIRATES = new ConstellationIcon.Builder("United Arab Emirates", new FileIconData("modules/ext/icons/flags/united_arab_emirates.png", CODE_NAME_BASE))
            .addAlias(Country.UNITED_ARAB_EMIRATES.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon UNITED_KINGDOM = new ConstellationIcon.Builder("United Kingdom", new FileIconData("modules/ext/icons/flags/united_kingdom.png", CODE_NAME_BASE))
            .addAlias(Country.UNITED_KINGDOM.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon UNITED_STATES = new ConstellationIcon.Builder("United States", new FileIconData("modules/ext/icons/flags/united_states_of_america.png", CODE_NAME_BASE))
            .addAlias(Country.UNITED_STATES.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon URUGUAY = new ConstellationIcon.Builder("Uruguay", new FileIconData("modules/ext/icons/flags/uruguay.png", CODE_NAME_BASE))
            .addAlias(Country.URUGUAY.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon UZBEKISTAN = new ConstellationIcon.Builder("Uzbekistan", new FileIconData("modules/ext/icons/flags/uzbekistan.png", CODE_NAME_BASE))
            .addAlias(Country.UZBEKISTAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon VANUATU = new ConstellationIcon.Builder("Vanuatu", new FileIconData("modules/ext/icons/flags/vanuatu.png", CODE_NAME_BASE))
            .addAlias(Country.VANUATU.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon VATICAN_CITY = new ConstellationIcon.Builder("Vatican City", new FileIconData("modules/ext/icons/flags/vatican_city.png", CODE_NAME_BASE))
            .addAlias(Country.HOLY_SEE.getDigraph())
            .addAlias("Vatican City State")
            .addAlias("Holy See")
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon VENEZUELA = new ConstellationIcon.Builder("Venezuela", new FileIconData("modules/ext/icons/flags/venezuela.png", CODE_NAME_BASE))
            .addAlias(Country.VENEZUELA.getDigraph())
            .addAlias("Venezuela, Bolivarian Republic of")
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon VIETNAM = new ConstellationIcon.Builder("Vietnam", new FileIconData("modules/ext/icons/flags/vietnam.png", CODE_NAME_BASE))
            .addAlias(Country.VIETNAM.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon YEMEN = new ConstellationIcon.Builder("Yemen", new FileIconData("modules/ext/icons/flags/yemen.png", CODE_NAME_BASE))
            .addAlias(Country.YEMEN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ZAMBIA = new ConstellationIcon.Builder("Zambia", new FileIconData("modules/ext/icons/flags/zambia.png", CODE_NAME_BASE))
            .addAlias(Country.ZAMBIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ZIMBABWE = new ConstellationIcon.Builder("Zimbabwe", new FileIconData("modules/ext/icons/flags/zimbabwe.png", CODE_NAME_BASE))
            .addAlias(Country.ZIMBABWE.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();

    public static final ConstellationIcon ALAND_ISLANDS = new ConstellationIcon.Builder("Aland Islands", new FileIconData("modules/ext/icons/flags/aland.png", CODE_NAME_BASE))
            .addAlias(Country.ALAND_ISLANDS.getDigraph())
            .addAlias("Aland")
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ANGOLA = new ConstellationIcon.Builder("Angola", new FileIconData("modules/ext/icons/flags/angola.png", CODE_NAME_BASE))
            .addAlias(Country.ANGOLA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ANGUILLA = new ConstellationIcon.Builder("Anguilla", new FileIconData("modules/ext/icons/flags/anguilla.png", CODE_NAME_BASE))
            .addAlias(Country.ANGUILLA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ARUBA = new ConstellationIcon.Builder("Aruba", new FileIconData("modules/ext/icons/flags/aruba.png", CODE_NAME_BASE))
            .addAlias(Country.ARUBA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon BERMUDA = new ConstellationIcon.Builder("Bermuda", new FileIconData("modules/ext/icons/flags/bermuda.png", CODE_NAME_BASE))
            .addAlias(Country.BERMUDA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon VIRGIN_ISLANDS_BRITISH = new ConstellationIcon.Builder("British Virgin Islands", new FileIconData("modules/ext/icons/flags/british_virgin_islands.png", CODE_NAME_BASE))
            .addAlias(Country.VIRGIN_ISLANDS_BRITISH.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CAYMAN_ISLANDS = new ConstellationIcon.Builder("Cayman Islands", new FileIconData("modules/ext/icons/flags/cayman_islands.png", CODE_NAME_BASE))
            .addAlias(Country.CAYMAN_ISLANDS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon COOK_ISLANDS = new ConstellationIcon.Builder("Cook Islands", new FileIconData("modules/ext/icons/flags/cook_islands.png", CODE_NAME_BASE))
            .addAlias(Country.COOK_ISLANDS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon CURACAO = new ConstellationIcon.Builder("Curacao", new FileIconData("modules/ext/icons/flags/curacao.png", CODE_NAME_BASE))
            .addAlias(Country.CURACAO.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon FALKLAND_ISLANDS = new ConstellationIcon.Builder("Falkland Islands", new FileIconData("modules/ext/icons/flags/falkland_islands.png", CODE_NAME_BASE))
            .addAlias(Country.FALKLAND_ISLANDS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon GIBRALTAR = new ConstellationIcon.Builder("Gibraltar", new FileIconData("modules/ext/icons/flags/gibraltar.png", CODE_NAME_BASE))
            .addAlias(Country.GIBRALTAR.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon GUERNSEY = new ConstellationIcon.Builder("Guernsey", new FileIconData("modules/ext/icons/flags/guernsey.png", CODE_NAME_BASE))
            .addAlias(Country.GUERNSEY.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon HONG_KONG = new ConstellationIcon.Builder("Hong Kong", new FileIconData("modules/ext/icons/flags/hong_kong.png", CODE_NAME_BASE))
            .addAlias(Country.HONG_KONG.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon ISLE_OF_MAN = new ConstellationIcon.Builder("Isle of Man", new FileIconData("modules/ext/icons/flags/isle_of_man.png", CODE_NAME_BASE))
            .addAlias(Country.ISLE_OF_MAN.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon JERSEY = new ConstellationIcon.Builder("Jersey", new FileIconData("modules/ext/icons/flags/jersey.png", CODE_NAME_BASE))
            .addAlias(Country.JERSEY.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MACAU = new ConstellationIcon.Builder("Macau", new FileIconData("modules/ext/icons/flags/macau.png", CODE_NAME_BASE))
            .addAlias(Country.MACAU.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon MONTSERRAT = new ConstellationIcon.Builder("Montserrat", new FileIconData("modules/ext/icons/flags/montserrat.png", CODE_NAME_BASE))
            .addAlias(Country.MONTSERRAT.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon NEW_CALEDONIA = new ConstellationIcon.Builder("New Caledonia", new FileIconData("modules/ext/icons/flags/new_caledonia.png", CODE_NAME_BASE))
            .addAlias(Country.NEW_CALEDONIA.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon NIUE = new ConstellationIcon.Builder("Niue", new FileIconData("modules/ext/icons/flags/niue.png", CODE_NAME_BASE))
            .addAlias(Country.NIUE.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon NORTHERN_MARIANA_ISLANDS = new ConstellationIcon.Builder("Northern Mariana Islands", new FileIconData("modules/ext/icons/flags/northern_mariana_islands.png", CODE_NAME_BASE))
            .addAlias(Country.NORTHERN_MARIANA_ISLANDS.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon PALESTINE = new ConstellationIcon.Builder("Palestine", new FileIconData("modules/ext/icons/flags/palestine.png", CODE_NAME_BASE))
            .addAlias(Country.PALESTINE.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon PUERTO_RICO = new ConstellationIcon.Builder("Puerto Rico", new FileIconData("modules/ext/icons/flags/puerto_rico.png", CODE_NAME_BASE))
            .addAlias(Country.PUERTO_RICO.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();
    public static final ConstellationIcon TOKELAU = new ConstellationIcon.Builder("Tokelau", new FileIconData("modules/ext/icons/flags/tokelau.png", CODE_NAME_BASE))
            .addAlias(Country.TOKELAU.getDigraph())
            .addCategory(FLAG_CATEGORY)
            .build();

    @Override
    public List<ConstellationIcon> getIcons() {
        final List<ConstellationIcon> flagIcons = new ArrayList<>();

        // Iterate over country enums
        for (Country c : Country.values()) {
            try {
                final ConstellationIcon country_icon = new ConstellationIcon.Builder(c.getDisplayName(), new FileIconData("modules/ext/icons/flags/" + c.getDisplayName().replaceAll(" ", "_").toLowerCase() + ".png", CODE_NAME_BASE))
                        .addAlias(c.getDigraph())
                        .addAlias(c.getTrigraph())
                        .addAliases(Arrays.asList(c.getAlternateNames()))
                        .addCategory(FLAG_CATEGORY)
                        .build();
                flagIcons.add(country_icon);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Unable to find file: {0}", "modules/ext/icons/flags/" + c.getDisplayName().replaceAll(" ", "_").toLowerCase() + ".png");
            }
        }

        return flagIcons;

//        flagIcons.add(AFGHANISTAN);
//        flagIcons.add(ALAND_ISLANDS);
//        flagIcons.add(ALBANIA);
//        flagIcons.add(ALGERIA);
//        flagIcons.add(ANDORRA);
//        flagIcons.add(ANGOLA);
//        flagIcons.add(ANGUILLA);
//        flagIcons.add(ANTIGUA_AND_BARBUDA);
//        flagIcons.add(ARGENTINA);
//        flagIcons.add(ARMENIA);
//        flagIcons.add(ARUBA);
//        flagIcons.add(AUSTRALIA);
//        flagIcons.add(AUSTRIA);
//        flagIcons.add(AZERBAIJAN);
//        flagIcons.add(BAHAMAS);
//        flagIcons.add(BAHRAIN);
//        flagIcons.add(BANGLADESH);
//        flagIcons.add(BARBADOS);
//        flagIcons.add(BELARUS);
//        flagIcons.add(BELGIUM);
//        flagIcons.add(BELIZE);
//        flagIcons.add(BENIN);
//        flagIcons.add(BERMUDA);
//        flagIcons.add(BHUTAN);
//        flagIcons.add(BOLIVIA);
//        flagIcons.add(BOSNIA_AND_HERZEGOVINA);
//        flagIcons.add(BOTSWANA);
//        flagIcons.add(BRAZIL);
//        flagIcons.add(BRUNEI);
//        flagIcons.add(BULGARIA);
//        flagIcons.add(BURKINA_FASO);
//        flagIcons.add(BURUNDAI);
//        flagIcons.add(CAMBODIA);
//        flagIcons.add(CAMEROON);
//        flagIcons.add(CANADA);
//        flagIcons.add(CAPE_VERDE);
//        flagIcons.add(CAYMAN_ISLANDS);
//        flagIcons.add(CENTRAL_AFRICAN_REPUBLIC);
//        flagIcons.add(CHAD);
//        flagIcons.add(CHILE);
//        flagIcons.add(CHINA);
//        flagIcons.add(COLOMBIA);
//        flagIcons.add(COMOROS);
//        flagIcons.add(CONGO_DEMOCRATIC);
//        flagIcons.add(CONGO_REPUBLIC);
//        flagIcons.add(COOK_ISLANDS);
//        flagIcons.add(COSTA_RICA);
//        flagIcons.add(COTE_DIVOIRE);
//        flagIcons.add(CROATIA);
//        flagIcons.add(CUBA);
//        flagIcons.add(CURACAO);
//        flagIcons.add(CYPRUS);
//        flagIcons.add(CZECH_REPUBLIC);
//        flagIcons.add(DENMARK);
//        flagIcons.add(DJIBOUTI);
//        flagIcons.add(DOMINICA);
//        flagIcons.add(DOMINICAN_REPUBLIC);
//        flagIcons.add(EAST_TIMOR);
//        flagIcons.add(ECUADOR);
//        flagIcons.add(EGYPT);
//        flagIcons.add(EL_SALVADOR);
//        flagIcons.add(EQUATORIAL_GUINEA);
//        flagIcons.add(ERITREA);
//        flagIcons.add(ESTONIA);
//        flagIcons.add(ETHIOPIA);
//        flagIcons.add(FALKLAND_ISLANDS);
//        flagIcons.add(FIJI);
//        flagIcons.add(FINLAND);
//        flagIcons.add(FRANCE);
//        flagIcons.add(GABON);
//        flagIcons.add(GAMBIA);
//        flagIcons.add(GEORGIA);
//        flagIcons.add(GERMANY);
//        flagIcons.add(GHANA);
//        flagIcons.add(GIBRALTAR);
//        flagIcons.add(GREECE);
//        flagIcons.add(GRENADA);
//        flagIcons.add(GUATEMALA);
//        flagIcons.add(GUERNSEY);
//        flagIcons.add(GUINEA);
//        flagIcons.add(GUINEA_BISSAU);
//        flagIcons.add(GUYANA);
//        flagIcons.add(HAITI);
//        flagIcons.add(HONDURAS);
//        flagIcons.add(HONG_KONG);
//        flagIcons.add(HUNGARY);
//        flagIcons.add(ICELAND);
//        flagIcons.add(INDIA);
//        flagIcons.add(INDONESIA);
//        flagIcons.add(IRAN);
//        flagIcons.add(IRAQ);
//        flagIcons.add(IRELAND);
//        flagIcons.add(ISLE_OF_MAN);
//        flagIcons.add(ISRAEL);
//        flagIcons.add(ITALY);
//        flagIcons.add(JAMAICA);
//        flagIcons.add(JAPAN);
//        flagIcons.add(JERSEY);
//        flagIcons.add(JORDAN);
//        flagIcons.add(KAZAKHSTAN);
//        flagIcons.add(KENYA);
//        flagIcons.add(KIRIBATI);
//        flagIcons.add(KOREA_NORTH);
//        flagIcons.add(KOREA_SOUTH);
//        flagIcons.add(KOSOVO);
//        flagIcons.add(KUWAIT);
//        flagIcons.add(KYRGYZSTAN);
//        flagIcons.add(LAOS);
//        flagIcons.add(LATVIA);
//        flagIcons.add(LEBANON);
//        flagIcons.add(LESOTHO);
//        flagIcons.add(LIBERIA);
//        flagIcons.add(LIBYA);
//        flagIcons.add(LIECHTENSTEIN);
//        flagIcons.add(LITHUANIA);
//        flagIcons.add(LUXEMBOURG);
//        flagIcons.add(MACAU);
//        flagIcons.add(MACEDONIA);
//        flagIcons.add(MADAGASCAR);
//        flagIcons.add(MALAWI);
//        flagIcons.add(MALAYSIA);
//        flagIcons.add(MALDIVES);
//        flagIcons.add(MALI);
//        flagIcons.add(MALTA);
//        flagIcons.add(MARSHALL_ISLANDS);
//        flagIcons.add(MAURITANIA);
//        flagIcons.add(MAURITIUS);
//        flagIcons.add(MEXICO);
//        flagIcons.add(MICRONESIA);
//        flagIcons.add(MOLDOVA);
//        flagIcons.add(MONACO);
//        flagIcons.add(MONGOLIA);
//        flagIcons.add(MONTENEGRO);
//        flagIcons.add(MONTSERRAT);
//        flagIcons.add(MOROCCO);
//        flagIcons.add(MOZAMBIQUE);
//        flagIcons.add(MYANMAR);
//        flagIcons.add(NAMIBIA);
//        flagIcons.add(NAURU);
//        flagIcons.add(NEPAL);
//        flagIcons.add(NETHERLANDS);
//        flagIcons.add(NEW_CALEDONIA);
//        flagIcons.add(NEW_ZEALAND);
//        flagIcons.add(NICARAGUA);
//        flagIcons.add(NIGER);
//        flagIcons.add(NIGERIA);
//        flagIcons.add(NIUE);
//        flagIcons.add(NORTHERN_MARIANA_ISLANDS);
//        flagIcons.add(NORWAY);
//        flagIcons.add(OMAN);
//        flagIcons.add(PAKISTAN);
//        flagIcons.add(PALAU);
//        flagIcons.add(PALESTINE);
//        flagIcons.add(PANAMA);
//        flagIcons.add(PAPUA_NEW_GUINEA);
//        flagIcons.add(PARAGUAY);
//        flagIcons.add(PERU);
//        flagIcons.add(PHILIPPINES);
//        flagIcons.add(POLAND);
//        flagIcons.add(PORTUGAL);
//        flagIcons.add(PUERTO_RICO);
//        flagIcons.add(QATAR);
//        flagIcons.add(ROMANIA);
//        flagIcons.add(RUSSIA);
//        flagIcons.add(RWANDA);
//        flagIcons.add(SAINT_KITTS_AND_NEVIS);
//        flagIcons.add(SAINT_LUCIA);
//        flagIcons.add(SAINT_VINCENT_AND_THE_GRENADINES);
//        flagIcons.add(SAMOA);
//        flagIcons.add(SAN_MARINO);
//        flagIcons.add(SAO_TOME_AND_PRINCIPE);
//        flagIcons.add(SAUDI_ARABIA);
//        flagIcons.add(SENEGAL);
//        flagIcons.add(SERBIA);
//        flagIcons.add(SEYCHELLES);
//        flagIcons.add(SIERRA_LEONE);
//        flagIcons.add(SINGAPORE);
//        flagIcons.add(SLOVAKIA);
//        flagIcons.add(SLOVENIA);
//        flagIcons.add(SOLOMON_ISLANDS);
//        flagIcons.add(SOMALIA);
//        flagIcons.add(SOUTH_AFRICA);
//        flagIcons.add(SOUTH_SUDAN);
//        flagIcons.add(SPAIN);
//        flagIcons.add(SRI_LANKA);
//        flagIcons.add(SUDAN);
//        flagIcons.add(SURINAME);
//        flagIcons.add(ESWATINI);
//        flagIcons.add(SWEDEN);
//        flagIcons.add(SWITZERLAND);
//        flagIcons.add(SYRIA);
//        flagIcons.add(TAIWAN);
//        flagIcons.add(TAJIKISTAN);
//        flagIcons.add(TANZANIA);
//        flagIcons.add(THAILAND);
//        flagIcons.add(TOGO);
//        flagIcons.add(TOKELAU);
//        flagIcons.add(TONGA);
//        flagIcons.add(TRINIDAD_AND_TOBAGO);
//        flagIcons.add(TUNISIA);
//        flagIcons.add(TURKEY);
//        flagIcons.add(TURKMENISTAN);
//        flagIcons.add(TUVALU);
//        flagIcons.add(UGANDA);
//        flagIcons.add(UKRAINE);
//        flagIcons.add(UNITED_ARAB_EMIRATES);
//        flagIcons.add(UNITED_KINGDOM);
//        flagIcons.add(UNITED_STATES);
//        flagIcons.add(URUGUAY);
//        flagIcons.add(UZBEKISTAN);
//        flagIcons.add(VANUATU);
//        flagIcons.add(VATICAN_CITY);
//        flagIcons.add(VENEZUELA);
//        flagIcons.add(VIETNAM);
//        flagIcons.add(VIRGIN_ISLANDS_BRITISH);
//        flagIcons.add(YEMEN);
//        flagIcons.add(ZAMBIA);
//        flagIcons.add(ZIMBABWE);
//
//        return flagIcons;
    }
}

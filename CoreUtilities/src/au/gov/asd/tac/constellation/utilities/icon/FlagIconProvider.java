/*
 * Copyright 2010-2019 Australian Signals Directorate
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
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * An IconProvider defining flag icons.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = ConstellationIconProvider.class)
public class FlagIconProvider implements ConstellationIconProvider {

    public static final ConstellationIcon AFGHANISTAN = new ConstellationIcon.Builder("Afghanistan", new FileIconData("modules/ext/icons/flags/afghanistan.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.AFGHANISTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ALBANIA = new ConstellationIcon.Builder("Albania", new FileIconData("modules/ext/icons/flags/albania.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ALBANIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ALGERIA = new ConstellationIcon.Builder("Algeria", new FileIconData("modules/ext/icons/flags/algeria.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ALGERIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ANDORRA = new ConstellationIcon.Builder("Andorra", new FileIconData("modules/ext/icons/flags/andorra.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ANDORRA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ANTIGUA_AND_BARBUDA = new ConstellationIcon.Builder("Antigua and Barbuda", new FileIconData("modules/ext/icons/flags/antigua_and_barbuda.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ANTIGUA_AND_BARBUDA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ARGENTINA = new ConstellationIcon.Builder("Argentina", new FileIconData("modules/ext/icons/flags/argentina.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ARGENTINA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ARMENIA = new ConstellationIcon.Builder("Armenia", new FileIconData("modules/ext/icons/flags/armenia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ARMENIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon AUSTRALIA = new ConstellationIcon.Builder("Australia", new FileIconData("modules/ext/icons/flags/australia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.AUSTRALIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon AUSTRIA = new ConstellationIcon.Builder("Austria", new FileIconData("modules/ext/icons/flags/austria.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.AUSTRIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon AZERBAIJAN = new ConstellationIcon.Builder("Azerbaijan", new FileIconData("modules/ext/icons/flags/azerbaijan.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.AZERBAIJAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BAHAMAS = new ConstellationIcon.Builder("Bahamas", new FileIconData("modules/ext/icons/flags/bahamas.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BAHAMAS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BAHRAIN = new ConstellationIcon.Builder("Bahrain", new FileIconData("modules/ext/icons/flags/bahrain.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BAHRAIN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BANGLADESH = new ConstellationIcon.Builder("Bangladesh", new FileIconData("modules/ext/icons/flags/bangladesh.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BANGLADESH.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BARBADOS = new ConstellationIcon.Builder("Barbados", new FileIconData("modules/ext/icons/flags/barbados.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BARBADOS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BELARUS = new ConstellationIcon.Builder("Belarus", new FileIconData("modules/ext/icons/flags/belarus.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BELARUS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BELGIUM = new ConstellationIcon.Builder("Belgium", new FileIconData("modules/ext/icons/flags/belgium.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BELGIUM.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BELIZE = new ConstellationIcon.Builder("Belize", new FileIconData("modules/ext/icons/flags/belize.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BELIZE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BENIN = new ConstellationIcon.Builder("Benin", new FileIconData("modules/ext/icons/flags/benin.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BENIN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BHUTAN = new ConstellationIcon.Builder("Bhutan", new FileIconData("modules/ext/icons/flags/bhutan.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BHUTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BOLIVIA = new ConstellationIcon.Builder("Bolivia", new FileIconData("modules/ext/icons/flags/bolivia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BOLIVIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BOSNIA_AND_HERZEGOVINA = new ConstellationIcon.Builder("Bosnia and Herzegovina", new FileIconData("modules/ext/icons/flags/bosnia_and_herzegovina.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BOSNIA_AND_HERZEGOVINA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BOTSWANA = new ConstellationIcon.Builder("Botswana", new FileIconData("modules/ext/icons/flags/botswana.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BOTSWANA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BRAZIL = new ConstellationIcon.Builder("Brazil", new FileIconData("modules/ext/icons/flags/brazil.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BRAZIL.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BRUNEI = new ConstellationIcon.Builder("Brunei", new FileIconData("modules/ext/icons/flags/brunei.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BRUNEI_DARUSSALAM.getDigraph())
            .addAlias("Brunei Darussalam")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BULGARIA = new ConstellationIcon.Builder("Bulgaria", new FileIconData("modules/ext/icons/flags/bulgaria.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BULGARIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BURKINA_FASO = new ConstellationIcon.Builder("Burkina Faso", new FileIconData("modules/ext/icons/flags/burkina_faso.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BURKINA_FASO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BURUNDAI = new ConstellationIcon.Builder("Burundi", new FileIconData("modules/ext/icons/flags/burundi.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BURUNDI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CAMBODIA = new ConstellationIcon.Builder("Cambodia", new FileIconData("modules/ext/icons/flags/cambodia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CAMBODIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CAMEROON = new ConstellationIcon.Builder("Cameroon", new FileIconData("modules/ext/icons/flags/cameroon.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CAMEROON.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CANADA = new ConstellationIcon.Builder("Canada", new FileIconData("modules/ext/icons/flags/canada.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CANADA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CAPE_VERDE = new ConstellationIcon.Builder("Cape Verde", new FileIconData("modules/ext/icons/flags/cape_verde.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CABO_VERDE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CENTRAL_AFRICAN_REPUBLIC = new ConstellationIcon.Builder("Central African Republic", new FileIconData("modules/ext/icons/flags/central_african_republic.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CENTRAL_AFRICAN_REPUBLIC.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CHAD = new ConstellationIcon.Builder("Chad", new FileIconData("modules/ext/icons/flags/chad.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CHAD.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CHILE = new ConstellationIcon.Builder("Chile", new FileIconData("modules/ext/icons/flags/chile.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CHILE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CHINA = new ConstellationIcon.Builder("China", new FileIconData("modules/ext/icons/flags/china.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CHINA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon COLOMBIA = new ConstellationIcon.Builder("Colombia", new FileIconData("modules/ext/icons/flags/colombia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.COLOMBIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon COMOROS = new ConstellationIcon.Builder("Comoros", new FileIconData("modules/ext/icons/flags/comoros.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.COMOROS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CONGO_DEMOCRATIC = new ConstellationIcon.Builder("Congo (Democratic)", new FileIconData("modules/ext/icons/flags/congo_democratic.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CONGO_DEMOCRATIC_REPUBLIC.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CONGO_REPUBLIC = new ConstellationIcon.Builder("Congo (Republic)", new FileIconData("modules/ext/icons/flags/congo_republic.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CONGO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon COSTA_RICA = new ConstellationIcon.Builder("Costa Rica", new FileIconData("modules/ext/icons/flags/costa_rica.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.COSTA_RICA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon COTE_DIVOIRE = new ConstellationIcon.Builder("Cote d'Ivoire", new FileIconData("modules/ext/icons/flags/cote_d'ivoire.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.COTE_DIVOIRE.getDigraph())
            .addAlias("Cote D'Ivoire")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CROATIA = new ConstellationIcon.Builder("Croatia", new FileIconData("modules/ext/icons/flags/croatia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CROATIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CUBA = new ConstellationIcon.Builder("Cuba", new FileIconData("modules/ext/icons/flags/cuba.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CUBA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CYPRUS = new ConstellationIcon.Builder("Cyprus", new FileIconData("modules/ext/icons/flags/cyprus.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CYPRUS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CZECH_REPUBLIC = new ConstellationIcon.Builder("Czech Republic", new FileIconData("modules/ext/icons/flags/czech_republic.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CZECH_REPUBLIC.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon DENMARK = new ConstellationIcon.Builder("Denmark", new FileIconData("modules/ext/icons/flags/denmark.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.DENMARK.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon DJIBOUTI = new ConstellationIcon.Builder("Djibouti", new FileIconData("modules/ext/icons/flags/djibouti.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.DJIBOUTI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon DOMINICA = new ConstellationIcon.Builder("Dominica", new FileIconData("modules/ext/icons/flags/dominica.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.DOMINICA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon DOMINICAN_REPUBLIC = new ConstellationIcon.Builder("Dominican Republic", new FileIconData("modules/ext/icons/flags/dominican_republic.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.DOMINICAN_REPUBLIC.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon EAST_TIMOR = new ConstellationIcon.Builder("East Timor", new FileIconData("modules/ext/icons/flags/east_timor.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.TIMOR_LESTE.getDigraph())
            .addAlias("Timor-Leste")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ECUADOR = new ConstellationIcon.Builder("Ecuador", new FileIconData("modules/ext/icons/flags/ecuador.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ECUADOR.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon EGYPT = new ConstellationIcon.Builder("Egypt", new FileIconData("modules/ext/icons/flags/egypt.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.EGYPT.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon EL_SALVADOR = new ConstellationIcon.Builder("El Salvador", new FileIconData("modules/ext/icons/flags/el_salvador.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.EL_SALVADOR.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon EQUATORIAL_GUINEA = new ConstellationIcon.Builder("Equatorial Guinea", new FileIconData("modules/ext/icons/flags/equatorial_guinea.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.EQUATORIAL_GUINEA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ERITREA = new ConstellationIcon.Builder("Eritrea", new FileIconData("modules/ext/icons/flags/eritrea.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ERITREA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ESTONIA = new ConstellationIcon.Builder("Estonia", new FileIconData("modules/ext/icons/flags/estonia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ESTONIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ETHIOPIA = new ConstellationIcon.Builder("Ethiopia", new FileIconData("modules/ext/icons/flags/ethiopia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ETHIOPIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon FIJI = new ConstellationIcon.Builder("Fiji", new FileIconData("modules/ext/icons/flags/fiji.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.FIJI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon FINLAND = new ConstellationIcon.Builder("Finland", new FileIconData("modules/ext/icons/flags/finland.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.FINLAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon FRANCE = new ConstellationIcon.Builder("France", new FileIconData("modules/ext/icons/flags/france.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.FRANCE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GABON = new ConstellationIcon.Builder("Gabon", new FileIconData("modules/ext/icons/flags/gabon.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.GABON.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GAMBIA = new ConstellationIcon.Builder("Gambia", new FileIconData("modules/ext/icons/flags/gambia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.GAMBIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GEORGIA = new ConstellationIcon.Builder("Georgia", new FileIconData("modules/ext/icons/flags/georgia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.GEORGIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GERMANY = new ConstellationIcon.Builder("Germany", new FileIconData("modules/ext/icons/flags/germany.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.GERMANY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GHANA = new ConstellationIcon.Builder("Ghana", new FileIconData("modules/ext/icons/flags/ghana.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.GHANA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GREECE = new ConstellationIcon.Builder("Greece", new FileIconData("modules/ext/icons/flags/greece.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.GREECE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GRENADA = new ConstellationIcon.Builder("Grenada", new FileIconData("modules/ext/icons/flags/grenada.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.GRENADA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GUATEMALA = new ConstellationIcon.Builder("Guatemala", new FileIconData("modules/ext/icons/flags/guatemala.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.GUATEMALA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GUINEA = new ConstellationIcon.Builder("Guinea", new FileIconData("modules/ext/icons/flags/guinea.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.GUINEA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GUINEA_BISSAU = new ConstellationIcon.Builder("Guinea Bissau", new FileIconData("modules/ext/icons/flags/guinea_bissau.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.GUINEA_BISSAU.getDigraph())
            .addAlias("Guinea-Bissau")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GUYANA = new ConstellationIcon.Builder("Guyana", new FileIconData("modules/ext/icons/flags/guyana.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.GUYANA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon HAITI = new ConstellationIcon.Builder("Haiti", new FileIconData("modules/ext/icons/flags/haiti.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.HAITI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon HONDURAS = new ConstellationIcon.Builder("Honduras", new FileIconData("modules/ext/icons/flags/honduras.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.HONDURAS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon HUNGARY = new ConstellationIcon.Builder("Hungary", new FileIconData("modules/ext/icons/flags/hungary.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.HUNGARY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ICELAND = new ConstellationIcon.Builder("Iceland", new FileIconData("modules/ext/icons/flags/iceland.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ICELAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon INDIA = new ConstellationIcon.Builder("India", new FileIconData("modules/ext/icons/flags/india.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.INDIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon INDONESIA = new ConstellationIcon.Builder("Indonesia", new FileIconData("modules/ext/icons/flags/indonesia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.INDONESIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon IRAN = new ConstellationIcon.Builder("Iran", new FileIconData("modules/ext/icons/flags/iran.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.IRAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon IRAQ = new ConstellationIcon.Builder("Iraq", new FileIconData("modules/ext/icons/flags/iraq.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.IRAQ.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon IRELAND = new ConstellationIcon.Builder("Ireland", new FileIconData("modules/ext/icons/flags/ireland.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.IRELAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ISRAEL = new ConstellationIcon.Builder("Israel", new FileIconData("modules/ext/icons/flags/israel.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ISRAEL.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ITALY = new ConstellationIcon.Builder("Italy", new FileIconData("modules/ext/icons/flags/italy.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ITALY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon JAMAICA = new ConstellationIcon.Builder("Jamaica", new FileIconData("modules/ext/icons/flags/jamaica.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.JAMAICA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon JAPAN = new ConstellationIcon.Builder("Japan", new FileIconData("modules/ext/icons/flags/japan.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.JAPAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon JORDAN = new ConstellationIcon.Builder("Jordan", new FileIconData("modules/ext/icons/flags/jordan.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.JORDAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KAZAKHSTAN = new ConstellationIcon.Builder("Kazakhstan", new FileIconData("modules/ext/icons/flags/kazakhstan.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.KAZAKHSTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KENYA = new ConstellationIcon.Builder("Kenya", new FileIconData("modules/ext/icons/flags/kenya.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.KENYA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KIRIBATI = new ConstellationIcon.Builder("Kiribati", new FileIconData("modules/ext/icons/flags/kiribati.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.KIRIBATI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KOREA_NORTH = new ConstellationIcon.Builder("North Koread", new FileIconData("modules/ext/icons/flags/korea_north.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.KOREA_NORTH.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KOREA_SOUTH = new ConstellationIcon.Builder("South Korea", new FileIconData("modules/ext/icons/flags/korea_south.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.KOREA_SOUTH.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KOSOVO = new ConstellationIcon.Builder("Kosovo", new FileIconData("modules/ext/icons/flags/kosovo.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KUWAIT = new ConstellationIcon.Builder("Kuwait", new FileIconData("modules/ext/icons/flags/kuwait.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.KUWAIT.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KYRGYZSTAN = new ConstellationIcon.Builder("Kyrgyzstan", new FileIconData("modules/ext/icons/flags/kyrgyzstan.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.KYRGYZSTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LAOS = new ConstellationIcon.Builder("Laos", new FileIconData("modules/ext/icons/flags/laos.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.LAOS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LATVIA = new ConstellationIcon.Builder("Latvia", new FileIconData("modules/ext/icons/flags/latvia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.LATVIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LEBANON = new ConstellationIcon.Builder("Lebanon", new FileIconData("modules/ext/icons/flags/lebanon.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.LEBANON.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LESOTHO = new ConstellationIcon.Builder("Lesotho", new FileIconData("modules/ext/icons/flags/lesotho.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.LESOTHO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LIBERIA = new ConstellationIcon.Builder("Liberia", new FileIconData("modules/ext/icons/flags/liberia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.LIBERIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LIBYA = new ConstellationIcon.Builder("Libya", new FileIconData("modules/ext/icons/flags/libya.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.LIBYA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LIECHTENSTEIN = new ConstellationIcon.Builder("Liechtenstein", new FileIconData("modules/ext/icons/flags/liechtenstein.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.LIECHTENSTEIN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LITHUANIA = new ConstellationIcon.Builder("Lithuania", new FileIconData("modules/ext/icons/flags/lithuania.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.LITHUANIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LUXEMBOURG = new ConstellationIcon.Builder("Luxembourg", new FileIconData("modules/ext/icons/flags/luxembourg.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.LUXEMBOURG.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MACEDONIA = new ConstellationIcon.Builder("Macedonia", new FileIconData("modules/ext/icons/flags/macedonia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MACEDONIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MADAGASCAR = new ConstellationIcon.Builder("Madagascar", new FileIconData("modules/ext/icons/flags/madagascar.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MADAGASCAR.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MALAWI = new ConstellationIcon.Builder("Malawi", new FileIconData("modules/ext/icons/flags/malawi.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MALAWI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MALAYSIA = new ConstellationIcon.Builder("Malaysia", new FileIconData("modules/ext/icons/flags/malaysia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MALAYSIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MALDIVES = new ConstellationIcon.Builder("Maldives", new FileIconData("modules/ext/icons/flags/maldives.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MALDIVES.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MALI = new ConstellationIcon.Builder("Mali", new FileIconData("modules/ext/icons/flags/mali.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MALI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MALTA = new ConstellationIcon.Builder("Malta", new FileIconData("modules/ext/icons/flags/malta.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MALTA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MARSHALL_ISLANDS = new ConstellationIcon.Builder("Marshall Islands", new FileIconData("modules/ext/icons/flags/marshall_islands.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MARSHALL_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MAURITANIA = new ConstellationIcon.Builder("Mauritania", new FileIconData("modules/ext/icons/flags/mauritania.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MAURITANIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MAURITIUS = new ConstellationIcon.Builder("Mauritius", new FileIconData("modules/ext/icons/flags/mauritius.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MAURITIUS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MEXICO = new ConstellationIcon.Builder("Mexico", new FileIconData("modules/ext/icons/flags/mexico.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MEXICO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MICRONESIA = new ConstellationIcon.Builder("Micronesia", new FileIconData("modules/ext/icons/flags/micronesia_federated.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MICRONESIA.getDigraph())
            .addAlias("Micronesia, Federated States of")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MOLDOVA = new ConstellationIcon.Builder("Moldova", new FileIconData("modules/ext/icons/flags/moldova.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MOLDOVA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MONACO = new ConstellationIcon.Builder("Monaco", new FileIconData("modules/ext/icons/flags/monaco.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MONACO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MONGOLIA = new ConstellationIcon.Builder("Mongolia", new FileIconData("modules/ext/icons/flags/mongolia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MONGOLIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MONTENEGRO = new ConstellationIcon.Builder("Montenegro", new FileIconData("modules/ext/icons/flags/montenegro.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MONTENEGRO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MOROCCO = new ConstellationIcon.Builder("Morocco", new FileIconData("modules/ext/icons/flags/morocco.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MOROCCO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MOZAMBIQUE = new ConstellationIcon.Builder("Mozambique", new FileIconData("modules/ext/icons/flags/mozambique.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MOZAMBIQUE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MYANMAR = new ConstellationIcon.Builder("Myanmar", new FileIconData("modules/ext/icons/flags/myanmar.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MYANMAR.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NAMIBIA = new ConstellationIcon.Builder("Namibia", new FileIconData("modules/ext/icons/flags/namibia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.NAMIBIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NAURU = new ConstellationIcon.Builder("Nauru", new FileIconData("modules/ext/icons/flags/nauru.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.NAURU.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NEPAL = new ConstellationIcon.Builder("Nepal", new FileIconData("modules/ext/icons/flags/nepal.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.NEPAL.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NETHERLANDS = new ConstellationIcon.Builder("Netherlands", new FileIconData("modules/ext/icons/flags/netherlands.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.NETHERLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NEW_ZEALAND = new ConstellationIcon.Builder("New Zealand", new FileIconData("modules/ext/icons/flags/new_zealand.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.NEW_ZEALAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NICARAGUA = new ConstellationIcon.Builder("Nicaragua", new FileIconData("modules/ext/icons/flags/nicaragua.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.NICARAGUA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NIGER = new ConstellationIcon.Builder("Niger", new FileIconData("modules/ext/icons/flags/niger.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.NIGER.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NIGERIA = new ConstellationIcon.Builder("Nigeria", new FileIconData("modules/ext/icons/flags/nigeria.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.NIGERIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NORWAY = new ConstellationIcon.Builder("Norway", new FileIconData("modules/ext/icons/flags/norway.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.NORWAY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon OMAN = new ConstellationIcon.Builder("Oman", new FileIconData("modules/ext/icons/flags/oman.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.OMAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PAKISTAN = new ConstellationIcon.Builder("Pakistan", new FileIconData("modules/ext/icons/flags/pakistan.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.PAKISTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PALAU = new ConstellationIcon.Builder("Palau", new FileIconData("modules/ext/icons/flags/palau.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.PALAU.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PANAMA = new ConstellationIcon.Builder("Panama", new FileIconData("modules/ext/icons/flags/panama.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.PANAMA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PAPUA_NEW_GUINEA = new ConstellationIcon.Builder("Papua New Guinea", new FileIconData("modules/ext/icons/flags/papua_new_guinea.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.PAPUA_NEW_GUINEA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PARAGUAY = new ConstellationIcon.Builder("Paraguay", new FileIconData("modules/ext/icons/flags/paraguay.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.PARAGUAY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PERU = new ConstellationIcon.Builder("Peru", new FileIconData("modules/ext/icons/flags/peru.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.PERU.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PHILIPPINES = new ConstellationIcon.Builder("Philippines", new FileIconData("modules/ext/icons/flags/philippines.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.PHILIPPINES.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon POLAND = new ConstellationIcon.Builder("Poland", new FileIconData("modules/ext/icons/flags/poland.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.POLAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PORTUGAL = new ConstellationIcon.Builder("Portugal", new FileIconData("modules/ext/icons/flags/portugal.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.PORTUGAL.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon QATAR = new ConstellationIcon.Builder("Qatar", new FileIconData("modules/ext/icons/flags/qatar.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.QATAR.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ROMANIA = new ConstellationIcon.Builder("Romania", new FileIconData("modules/ext/icons/flags/romania.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ROMANIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon RUSSIA = new ConstellationIcon.Builder("Russia", new FileIconData("modules/ext/icons/flags/russia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.RUSSIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon RWANDA = new ConstellationIcon.Builder("Rwanda", new FileIconData("modules/ext/icons/flags/rwanda.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.RWANDA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAINT_KITTS_AND_NEVIS = new ConstellationIcon.Builder("Saint Kitts and Nevis", new FileIconData("modules/ext/icons/flags/saint_kitts_and_nevis.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SAINT_KITTS_AND_NEVIS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAINT_LUCIA = new ConstellationIcon.Builder("Saint Lucia", new FileIconData("modules/ext/icons/flags/saint_lucia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SAINT_LUCIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAINT_VINCENT_AND_THE_GRENADINES = new ConstellationIcon.Builder("Saint Vincent and the Grenadines", new FileIconData("modules/ext/icons/flags/saint_vincent_and_the_grenadines.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SAINT_VINCENT_AND_THE_GRENADINES.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAMOA = new ConstellationIcon.Builder("Samoa", new FileIconData("modules/ext/icons/flags/samoa.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SAMOA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAN_MARINO = new ConstellationIcon.Builder("San Marino", new FileIconData("modules/ext/icons/flags/san_marino.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SAN_MARINO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAO_TOME_AND_PRINCIPE = new ConstellationIcon.Builder("Sao Tome and Principe", new FileIconData("modules/ext/icons/flags/sao_tome_and_principe.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SAO_TOME_AND_PRINCIPE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAUDI_ARABIA = new ConstellationIcon.Builder("Saudi Arabia", new FileIconData("modules/ext/icons/flags/saudi_arabia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SAUDI_ARABIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SENEGAL = new ConstellationIcon.Builder("Senegal", new FileIconData("modules/ext/icons/flags/senegal.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SENEGAL.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SERBIA = new ConstellationIcon.Builder("Serbia", new FileIconData("modules/ext/icons/flags/serbia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SERBIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SEYCHELLES = new ConstellationIcon.Builder("Seychelles", new FileIconData("modules/ext/icons/flags/seychelles.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SEYCHELLES.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SIERRA_LEONE = new ConstellationIcon.Builder("Sierra Leone", new FileIconData("modules/ext/icons/flags/sierra_leone.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SIERRA_LEONE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SINGAPORE = new ConstellationIcon.Builder("Singapore", new FileIconData("modules/ext/icons/flags/singapore.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SINGAPORE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SLOVAKIA = new ConstellationIcon.Builder("Slovakia", new FileIconData("modules/ext/icons/flags/slovakia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SLOVAKIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SLOVENIA = new ConstellationIcon.Builder("Slovenia", new FileIconData("modules/ext/icons/flags/slovenia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SLOVENIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SOLOMON_ISLANDS = new ConstellationIcon.Builder("Solomon Islands", new FileIconData("modules/ext/icons/flags/solomon_islands.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SOLOMON_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SOMALIA = new ConstellationIcon.Builder("Somalia", new FileIconData("modules/ext/icons/flags/somalia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SOMALIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SOUTH_AFRICA = new ConstellationIcon.Builder("South Africa", new FileIconData("modules/ext/icons/flags/south_africa.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SOUTH_AFRICA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SOUTH_SUDAN = new ConstellationIcon.Builder("South Sudan", new FileIconData("modules/ext/icons/flags/south_sudan.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SPAIN = new ConstellationIcon.Builder("Spain", new FileIconData("modules/ext/icons/flags/spain.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SPAIN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SRI_LANKA = new ConstellationIcon.Builder("Sri Lanka", new FileIconData("modules/ext/icons/flags/sri_lanka.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SRI_LANKA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SUDAN = new ConstellationIcon.Builder("Sudan", new FileIconData("modules/ext/icons/flags/sudan.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SUDAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SURINAME = new ConstellationIcon.Builder("Suriname", new FileIconData("modules/ext/icons/flags/suriname.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SURINAME.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SWAZILAND = new ConstellationIcon.Builder("Swaziland", new FileIconData("modules/ext/icons/flags/swaziland.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SWAZILAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SWEDEN = new ConstellationIcon.Builder("Sweden", new FileIconData("modules/ext/icons/flags/sweden.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SWEDEN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SWITZERLAND = new ConstellationIcon.Builder("Switzerland", new FileIconData("modules/ext/icons/flags/switzerland.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SWITZERLAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SYRIA = new ConstellationIcon.Builder("Syria", new FileIconData("modules/ext/icons/flags/syria.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.SYRIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TAIWAN = new ConstellationIcon.Builder("Taiwan", new FileIconData("modules/ext/icons/flags/taiwan.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.TAIWAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TAJIKISTAN = new ConstellationIcon.Builder("Tajikistan", new FileIconData("modules/ext/icons/flags/tajikistan.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.TAJIKISTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TANZANIA = new ConstellationIcon.Builder("Tanzania", new FileIconData("modules/ext/icons/flags/tanzania.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.TANZANIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon THAILAND = new ConstellationIcon.Builder("Thailand", new FileIconData("modules/ext/icons/flags/thailand.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.THAILAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TOGO = new ConstellationIcon.Builder("Togo", new FileIconData("modules/ext/icons/flags/togo.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.TOGO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TONGA = new ConstellationIcon.Builder("Tonga", new FileIconData("modules/ext/icons/flags/tonga.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.TONGA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TRINIDAD_AND_TOBAGO = new ConstellationIcon.Builder("Trinidad and Tobago", new FileIconData("modules/ext/icons/flags/trinidad_and_tobago.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.TRINIDAD_AND_TOBAGO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TUNISIA = new ConstellationIcon.Builder("Tunisia", new FileIconData("modules/ext/icons/flags/tunisia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.TUNISIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TURKEY = new ConstellationIcon.Builder("Turkey", new FileIconData("modules/ext/icons/flags/turkey.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.TURKEY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TURKMENISTAN = new ConstellationIcon.Builder("Turkmenistan", new FileIconData("modules/ext/icons/flags/turkmenistan.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.TURKMENISTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TUVALU = new ConstellationIcon.Builder("Tuvalu", new FileIconData("modules/ext/icons/flags/tuvalu.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.TUVALU.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon UGANDA = new ConstellationIcon.Builder("Uganda", new FileIconData("modules/ext/icons/flags/uganda.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.UGANDA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon UKRAINE = new ConstellationIcon.Builder("Ukraine", new FileIconData("modules/ext/icons/flags/ukraine.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.UKRAINE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon UNITED_ARAB_EMIRATES = new ConstellationIcon.Builder("United Arab Emirates", new FileIconData("modules/ext/icons/flags/united_arab_emirates.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.UNITED_ARAB_EMIRATES.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon UNITED_KINGDOM = new ConstellationIcon.Builder("United Kingdom", new FileIconData("modules/ext/icons/flags/united_kingdom.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.UNITED_KINGDOM.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon UNITED_STATES = new ConstellationIcon.Builder("United States", new FileIconData("modules/ext/icons/flags/united_states_of_america.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.UNITED_STATES.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon URUGUAY = new ConstellationIcon.Builder("Uruguay", new FileIconData("modules/ext/icons/flags/uruguay.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.URUGUAY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon UZBEKISTAN = new ConstellationIcon.Builder("Uzbekistan", new FileIconData("modules/ext/icons/flags/uzbekistan.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.UZBEKISTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon VANUATU = new ConstellationIcon.Builder("Vanuatu", new FileIconData("modules/ext/icons/flags/vanuatu.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.VANUATU.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon VATICAN_CITY = new ConstellationIcon.Builder("Vatican City", new FileIconData("modules/ext/icons/flags/vatican_city.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.HOLY_SEE.getDigraph())
            .addAlias("Vatican City State")
            .addAlias("Holy See")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon VENEZUELA = new ConstellationIcon.Builder("Venezuela", new FileIconData("modules/ext/icons/flags/venezuela.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.VENEZUELA.getDigraph())
            .addAlias("Venezuela, Bolivarian Republic of")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon VIETNAM = new ConstellationIcon.Builder("Vietnam", new FileIconData("modules/ext/icons/flags/vietnam.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.VIETNAM.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon YEMEN = new ConstellationIcon.Builder("Yemen", new FileIconData("modules/ext/icons/flags/yemen.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.YEMEN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ZAMBIA = new ConstellationIcon.Builder("Zambia", new FileIconData("modules/ext/icons/flags/zambia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ZAMBIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ZIMBABWE = new ConstellationIcon.Builder("Zimbabwe", new FileIconData("modules/ext/icons/flags/zimbabwe.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ZIMBABWE.getDigraph())
            .addCategory("Flag")
            .build();

    public static final ConstellationIcon ALAND_ISLANDS = new ConstellationIcon.Builder("Aland Islands", new FileIconData("modules/ext/icons/flags/aland.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ALAND_ISLANDS.getDigraph())
            .addAlias("Aland")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ANGOLA = new ConstellationIcon.Builder("Angola", new FileIconData("modules/ext/icons/flags/angola.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ANGOLA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ANGUILLA = new ConstellationIcon.Builder("Anguilla", new FileIconData("modules/ext/icons/flags/anguilla.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ANGUILLA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ARUBA = new ConstellationIcon.Builder("Aruba", new FileIconData("modules/ext/icons/flags/aruba.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ARUBA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BERMUDA = new ConstellationIcon.Builder("Bermuda", new FileIconData("modules/ext/icons/flags/bermuda.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.BERMUDA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon VIRGIN_ISLANDS_BRITISH = new ConstellationIcon.Builder("British Virgin Islands", new FileIconData("modules/ext/icons/flags/british_virgin_islands.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.VIRGIN_ISLANDS_BRITISH.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CAYMAN_ISLANDS = new ConstellationIcon.Builder("Cayman Islands", new FileIconData("modules/ext/icons/flags/cayman_islands.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CAYMAN_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon COOK_ISLANDS = new ConstellationIcon.Builder("Cook Islands", new FileIconData("modules/ext/icons/flags/cook_islands.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.COOK_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CURACAO = new ConstellationIcon.Builder("Curacao", new FileIconData("modules/ext/icons/flags/curacao.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.CURACAO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon FALKLAND_ISLANDS = new ConstellationIcon.Builder("Falkland Islands", new FileIconData("modules/ext/icons/flags/falkland_islands.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.FALKLAND_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GIBRALTAR = new ConstellationIcon.Builder("Gibraltar", new FileIconData("modules/ext/icons/flags/gibraltar.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.GIBRALTAR.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GUERNSEY = new ConstellationIcon.Builder("Guernsey", new FileIconData("modules/ext/icons/flags/guernsey.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.GUERNSEY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon HONG_KONG = new ConstellationIcon.Builder("Hong Kong", new FileIconData("modules/ext/icons/flags/hong_kong.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.HONG_KONG.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ISLE_OF_MAN = new ConstellationIcon.Builder("Isle of Man", new FileIconData("modules/ext/icons/flags/isle_of_man.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.ISLE_OF_MAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon JERSEY = new ConstellationIcon.Builder("Jersey", new FileIconData("modules/ext/icons/flags/jersey.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.JERSEY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MACAU = new ConstellationIcon.Builder("Macau", new FileIconData("modules/ext/icons/flags/macau.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MACAU.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MONTSERRAT = new ConstellationIcon.Builder("Montserrat", new FileIconData("modules/ext/icons/flags/montserrat.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.MONTSERRAT.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NEW_CALEDONIA = new ConstellationIcon.Builder("New Caledonia", new FileIconData("modules/ext/icons/flags/new_caledonia.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.NEW_CALEDONIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NIUE = new ConstellationIcon.Builder("Niue", new FileIconData("modules/ext/icons/flags/niue.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.NIUE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NORTHERN_MARIANA_ISLANDS = new ConstellationIcon.Builder("Northern Mariana Islands", new FileIconData("modules/ext/icons/flags/northern_mariana_islands.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.NORTHERN_MARIANA_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PALESTINE = new ConstellationIcon.Builder("Palestine", new FileIconData("modules/ext/icons/flags/palestine.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.PALESTINE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PUERTO_RICO = new ConstellationIcon.Builder("Puerto Rico", new FileIconData("modules/ext/icons/flags/puerto_rico.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.PUERTO_RICO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TOKELAU = new ConstellationIcon.Builder("Tokelau", new FileIconData("modules/ext/icons/flags/tokelau.png", "au.gov.asd.tac.constellation.utilities"))
            .addAlias(Country.TOKELAU.getDigraph())
            .addCategory("Flag")
            .build();

    @Override
    public List<ConstellationIcon> getIcons() {
        final List<ConstellationIcon> flagIcons = new ArrayList<>();

        flagIcons.add(AFGHANISTAN);
        flagIcons.add(ALAND_ISLANDS);
        flagIcons.add(ALBANIA);
        flagIcons.add(ALGERIA);
        flagIcons.add(ANDORRA);
        flagIcons.add(ANGOLA);
        flagIcons.add(ANGUILLA);
        flagIcons.add(ANTIGUA_AND_BARBUDA);
        flagIcons.add(ARGENTINA);
        flagIcons.add(ARMENIA);
        flagIcons.add(ARUBA);
        flagIcons.add(AUSTRALIA);
        flagIcons.add(AUSTRIA);
        flagIcons.add(AZERBAIJAN);
        flagIcons.add(BAHAMAS);
        flagIcons.add(BAHRAIN);
        flagIcons.add(BANGLADESH);
        flagIcons.add(BARBADOS);
        flagIcons.add(BELARUS);
        flagIcons.add(BELGIUM);
        flagIcons.add(BELIZE);
        flagIcons.add(BENIN);
        flagIcons.add(BERMUDA);
        flagIcons.add(BHUTAN);
        flagIcons.add(BOLIVIA);
        flagIcons.add(BOSNIA_AND_HERZEGOVINA);
        flagIcons.add(BOTSWANA);
        flagIcons.add(BRAZIL);
        flagIcons.add(BRUNEI);
        flagIcons.add(BULGARIA);
        flagIcons.add(BURKINA_FASO);
        flagIcons.add(BURUNDAI);
        flagIcons.add(CAMBODIA);
        flagIcons.add(CAMEROON);
        flagIcons.add(CANADA);
        flagIcons.add(CAPE_VERDE);
        flagIcons.add(CAYMAN_ISLANDS);
        flagIcons.add(CENTRAL_AFRICAN_REPUBLIC);
        flagIcons.add(CHAD);
        flagIcons.add(CHILE);
        flagIcons.add(CHINA);
        flagIcons.add(COLOMBIA);
        flagIcons.add(COMOROS);
        flagIcons.add(CONGO_DEMOCRATIC);
        flagIcons.add(CONGO_REPUBLIC);
        flagIcons.add(COOK_ISLANDS);
        flagIcons.add(COSTA_RICA);
        flagIcons.add(COTE_DIVOIRE);
        flagIcons.add(CROATIA);
        flagIcons.add(CUBA);
        flagIcons.add(CURACAO);
        flagIcons.add(CYPRUS);
        flagIcons.add(CZECH_REPUBLIC);
        flagIcons.add(DENMARK);
        flagIcons.add(DJIBOUTI);
        flagIcons.add(DOMINICA);
        flagIcons.add(DOMINICAN_REPUBLIC);
        flagIcons.add(EAST_TIMOR);
        flagIcons.add(ECUADOR);
        flagIcons.add(EGYPT);
        flagIcons.add(EL_SALVADOR);
        flagIcons.add(EQUATORIAL_GUINEA);
        flagIcons.add(ERITREA);
        flagIcons.add(ESTONIA);
        flagIcons.add(ETHIOPIA);
        flagIcons.add(FALKLAND_ISLANDS);
        flagIcons.add(FIJI);
        flagIcons.add(FINLAND);
        flagIcons.add(FRANCE);
        flagIcons.add(GABON);
        flagIcons.add(GAMBIA);
        flagIcons.add(GEORGIA);
        flagIcons.add(GERMANY);
        flagIcons.add(GHANA);
        flagIcons.add(GIBRALTAR);
        flagIcons.add(GREECE);
        flagIcons.add(GRENADA);
        flagIcons.add(GUATEMALA);
        flagIcons.add(GUERNSEY);
        flagIcons.add(GUINEA);
        flagIcons.add(GUINEA_BISSAU);
        flagIcons.add(GUYANA);
        flagIcons.add(HAITI);
        flagIcons.add(HONDURAS);
        flagIcons.add(HONG_KONG);
        flagIcons.add(HUNGARY);
        flagIcons.add(ICELAND);
        flagIcons.add(INDIA);
        flagIcons.add(INDONESIA);
        flagIcons.add(IRAN);
        flagIcons.add(IRAQ);
        flagIcons.add(IRELAND);
        flagIcons.add(ISLE_OF_MAN);
        flagIcons.add(ISRAEL);
        flagIcons.add(ITALY);
        flagIcons.add(JAMAICA);
        flagIcons.add(JAPAN);
        flagIcons.add(JERSEY);
        flagIcons.add(JORDAN);
        flagIcons.add(KAZAKHSTAN);
        flagIcons.add(KENYA);
        flagIcons.add(KIRIBATI);
        flagIcons.add(KOREA_NORTH);
        flagIcons.add(KOREA_SOUTH);
        flagIcons.add(KOSOVO);
        flagIcons.add(KUWAIT);
        flagIcons.add(KYRGYZSTAN);
        flagIcons.add(LAOS);
        flagIcons.add(LATVIA);
        flagIcons.add(LEBANON);
        flagIcons.add(LESOTHO);
        flagIcons.add(LIBERIA);
        flagIcons.add(LIBYA);
        flagIcons.add(LIECHTENSTEIN);
        flagIcons.add(LITHUANIA);
        flagIcons.add(LUXEMBOURG);
        flagIcons.add(MACAU);
        flagIcons.add(MACEDONIA);
        flagIcons.add(MADAGASCAR);
        flagIcons.add(MALAWI);
        flagIcons.add(MALAYSIA);
        flagIcons.add(MALDIVES);
        flagIcons.add(MALI);
        flagIcons.add(MALTA);
        flagIcons.add(MARSHALL_ISLANDS);
        flagIcons.add(MAURITANIA);
        flagIcons.add(MAURITIUS);
        flagIcons.add(MEXICO);
        flagIcons.add(MICRONESIA);
        flagIcons.add(MOLDOVA);
        flagIcons.add(MONACO);
        flagIcons.add(MONGOLIA);
        flagIcons.add(MONTENEGRO);
        flagIcons.add(MONTSERRAT);
        flagIcons.add(MOROCCO);
        flagIcons.add(MOZAMBIQUE);
        flagIcons.add(MYANMAR);
        flagIcons.add(NAMIBIA);
        flagIcons.add(NAURU);
        flagIcons.add(NEPAL);
        flagIcons.add(NETHERLANDS);
        flagIcons.add(NEW_CALEDONIA);
        flagIcons.add(NEW_ZEALAND);
        flagIcons.add(NICARAGUA);
        flagIcons.add(NIGER);
        flagIcons.add(NIGERIA);
        flagIcons.add(NIUE);
        flagIcons.add(NORTHERN_MARIANA_ISLANDS);
        flagIcons.add(NORWAY);
        flagIcons.add(OMAN);
        flagIcons.add(PAKISTAN);
        flagIcons.add(PALAU);
        flagIcons.add(PALESTINE);
        flagIcons.add(PANAMA);
        flagIcons.add(PAPUA_NEW_GUINEA);
        flagIcons.add(PARAGUAY);
        flagIcons.add(PERU);
        flagIcons.add(PHILIPPINES);
        flagIcons.add(POLAND);
        flagIcons.add(PORTUGAL);
        flagIcons.add(PUERTO_RICO);
        flagIcons.add(QATAR);
        flagIcons.add(ROMANIA);
        flagIcons.add(RUSSIA);
        flagIcons.add(RWANDA);
        flagIcons.add(SAINT_KITTS_AND_NEVIS);
        flagIcons.add(SAINT_LUCIA);
        flagIcons.add(SAINT_VINCENT_AND_THE_GRENADINES);
        flagIcons.add(SAMOA);
        flagIcons.add(SAN_MARINO);
        flagIcons.add(SAO_TOME_AND_PRINCIPE);
        flagIcons.add(SAUDI_ARABIA);
        flagIcons.add(SENEGAL);
        flagIcons.add(SERBIA);
        flagIcons.add(SEYCHELLES);
        flagIcons.add(SIERRA_LEONE);
        flagIcons.add(SINGAPORE);
        flagIcons.add(SLOVAKIA);
        flagIcons.add(SLOVENIA);
        flagIcons.add(SOLOMON_ISLANDS);
        flagIcons.add(SOMALIA);
        flagIcons.add(SOUTH_AFRICA);
        flagIcons.add(SOUTH_SUDAN);
        flagIcons.add(SPAIN);
        flagIcons.add(SRI_LANKA);
        flagIcons.add(SUDAN);
        flagIcons.add(SURINAME);
        flagIcons.add(SWAZILAND);
        flagIcons.add(SWEDEN);
        flagIcons.add(SWITZERLAND);
        flagIcons.add(SYRIA);
        flagIcons.add(TAIWAN);
        flagIcons.add(TAJIKISTAN);
        flagIcons.add(TANZANIA);
        flagIcons.add(THAILAND);
        flagIcons.add(TOGO);
        flagIcons.add(TOKELAU);
        flagIcons.add(TONGA);
        flagIcons.add(TRINIDAD_AND_TOBAGO);
        flagIcons.add(TUNISIA);
        flagIcons.add(TURKEY);
        flagIcons.add(TURKMENISTAN);
        flagIcons.add(TUVALU);
        flagIcons.add(UGANDA);
        flagIcons.add(UKRAINE);
        flagIcons.add(UNITED_ARAB_EMIRATES);
        flagIcons.add(UNITED_KINGDOM);
        flagIcons.add(UNITED_STATES);
        flagIcons.add(URUGUAY);
        flagIcons.add(UZBEKISTAN);
        flagIcons.add(VANUATU);
        flagIcons.add(VATICAN_CITY);
        flagIcons.add(VENEZUELA);
        flagIcons.add(VIETNAM);
        flagIcons.add(VIRGIN_ISLANDS_BRITISH);
        flagIcons.add(YEMEN);
        flagIcons.add(ZAMBIA);
        flagIcons.add(ZIMBABWE);

        return flagIcons;
    }
}

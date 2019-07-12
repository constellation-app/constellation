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
package au.gov.asd.tac.constellation.visual.icons;

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

    public static final ConstellationIcon AFGHANISTAN = new ConstellationIcon.Builder("Afghanistan", new FileIconData("modules/ext/icons/afghanistan.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.AFGHANISTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ALBANIA = new ConstellationIcon.Builder("Albania", new FileIconData("modules/ext/icons/albania.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ALBANIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ALGERIA = new ConstellationIcon.Builder("Algeria", new FileIconData("modules/ext/icons/algeria.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ALGERIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ANDORRA = new ConstellationIcon.Builder("Andorra", new FileIconData("modules/ext/icons/andorra.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ANDORRA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ANTIGUA_AND_BARBUDA = new ConstellationIcon.Builder("Antigua and Barbuda", new FileIconData("modules/ext/icons/antigua_and_barbuda.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ANTIGUA_AND_BARBUDA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ARGENTINA = new ConstellationIcon.Builder("Argentina", new FileIconData("modules/ext/icons/argentina.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ARGENTINA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ARMENIA = new ConstellationIcon.Builder("Armenia", new FileIconData("modules/ext/icons/armenia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ARMENIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon AUSTRALIA = new ConstellationIcon.Builder("Australia", new FileIconData("modules/ext/icons/australia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.AUSTRALIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon AUSTRIA = new ConstellationIcon.Builder("Austria", new FileIconData("modules/ext/icons/austria.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.AUSTRIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon AZERBAIJAN = new ConstellationIcon.Builder("Azerbaijan", new FileIconData("modules/ext/icons/azerbaijan.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.AZERBAIJAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BAHAMAS = new ConstellationIcon.Builder("Bahamas", new FileIconData("modules/ext/icons/bahamas.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BAHAMAS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BAHRAIN = new ConstellationIcon.Builder("Bahrain", new FileIconData("modules/ext/icons/bahrain.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BAHRAIN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BANGLADESH = new ConstellationIcon.Builder("Bangladesh", new FileIconData("modules/ext/icons/bangladesh.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BANGLADESH.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BARBADOS = new ConstellationIcon.Builder("Barbados", new FileIconData("modules/ext/icons/barbados.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BARBADOS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BELARUS = new ConstellationIcon.Builder("Belarus", new FileIconData("modules/ext/icons/belarus.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BELARUS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BELGIUM = new ConstellationIcon.Builder("Belgium", new FileIconData("modules/ext/icons/belgium.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BELGIUM.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BELIZE = new ConstellationIcon.Builder("Belize", new FileIconData("modules/ext/icons/belize.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BELIZE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BENIN = new ConstellationIcon.Builder("Benin", new FileIconData("modules/ext/icons/benin.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BENIN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BHUTAN = new ConstellationIcon.Builder("Bhutan", new FileIconData("modules/ext/icons/bhutan.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BHUTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BOLIVIA = new ConstellationIcon.Builder("Bolivia", new FileIconData("modules/ext/icons/bolivia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BOLIVIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BOSNIA_AND_HERZEGOVINA = new ConstellationIcon.Builder("Bosnia and Herzegovina", new FileIconData("modules/ext/icons/bosnia_and_herzegovina.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BOSNIA_AND_HERZEGOVINA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BOTSWANA = new ConstellationIcon.Builder("Botswana", new FileIconData("modules/ext/icons/botswana.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BOTSWANA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BRAZIL = new ConstellationIcon.Builder("Brazil", new FileIconData("modules/ext/icons/brazil.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BRAZIL.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BRUNEI = new ConstellationIcon.Builder("Brunei", new FileIconData("modules/ext/icons/brunei.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BRUNEI_DARUSSALAM.getDigraph())
            .addAlias("Brunei Darussalam")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BULGARIA = new ConstellationIcon.Builder("Bulgaria", new FileIconData("modules/ext/icons/bulgaria.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BULGARIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BURKINA_FASO = new ConstellationIcon.Builder("Burkina Faso", new FileIconData("modules/ext/icons/burkina_faso.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BURKINA_FASO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BURUNDAI = new ConstellationIcon.Builder("Burundi", new FileIconData("modules/ext/icons/burundi.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BURUNDI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CAMBODIA = new ConstellationIcon.Builder("Cambodia", new FileIconData("modules/ext/icons/cambodia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CAMBODIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CAMEROON = new ConstellationIcon.Builder("Cameroon", new FileIconData("modules/ext/icons/cameroon.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CAMEROON.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CANADA = new ConstellationIcon.Builder("Canada", new FileIconData("modules/ext/icons/canada.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CANADA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CAPE_VERDE = new ConstellationIcon.Builder("Cape Verde", new FileIconData("modules/ext/icons/cape_verde.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CABO_VERDE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CENTRAL_AFRICAN_REPUBLIC = new ConstellationIcon.Builder("Central African Republic", new FileIconData("modules/ext/icons/central_african_republic.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CENTRAL_AFRICAN_REPUBLIC.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CHAD = new ConstellationIcon.Builder("Chad", new FileIconData("modules/ext/icons/chad.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CHAD.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CHILE = new ConstellationIcon.Builder("Chile", new FileIconData("modules/ext/icons/chile.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CHILE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CHINA = new ConstellationIcon.Builder("China", new FileIconData("modules/ext/icons/china.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CHINA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon COLOMBIA = new ConstellationIcon.Builder("Colombia", new FileIconData("modules/ext/icons/colombia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.COLOMBIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon COMOROS = new ConstellationIcon.Builder("Comoros", new FileIconData("modules/ext/icons/comoros.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.COMOROS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CONGO_DEMOCRATIC = new ConstellationIcon.Builder("Congo (Democratic)", new FileIconData("modules/ext/icons/congo_democratic.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CONGO_DEMOCRATIC_REPUBLIC.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CONGO_REPUBLIC = new ConstellationIcon.Builder("Congo (Republic)", new FileIconData("modules/ext/icons/congo_republic.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CONGO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon COSTA_RICA = new ConstellationIcon.Builder("Costa Rica", new FileIconData("modules/ext/icons/costa_rica.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.COSTA_RICA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon COTE_DIVOIRE = new ConstellationIcon.Builder("Cote d'Ivoire", new FileIconData("modules/ext/icons/cote_d'ivoire.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.COTE_DIVOIRE.getDigraph())
            .addAlias("Cote D'Ivoire")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CROATIA = new ConstellationIcon.Builder("Croatia", new FileIconData("modules/ext/icons/croatia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CROATIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CUBA = new ConstellationIcon.Builder("Cuba", new FileIconData("modules/ext/icons/cuba.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CUBA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CYPRUS = new ConstellationIcon.Builder("Cyprus", new FileIconData("modules/ext/icons/cyprus.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CYPRUS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CZECH_REPUBLIC = new ConstellationIcon.Builder("Czech Republic", new FileIconData("modules/ext/icons/czech_republic.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CZECH_REPUBLIC.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon DENMARK = new ConstellationIcon.Builder("Denmark", new FileIconData("modules/ext/icons/denmark.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.DENMARK.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon DJIBOUTI = new ConstellationIcon.Builder("Djibouti", new FileIconData("modules/ext/icons/djibouti.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.DJIBOUTI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon DOMINICA = new ConstellationIcon.Builder("Dominica", new FileIconData("modules/ext/icons/dominica.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.DOMINICA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon DOMINICAN_REPUBLIC = new ConstellationIcon.Builder("Dominican Republic", new FileIconData("modules/ext/icons/dominican_republic.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.DOMINICAN_REPUBLIC.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon EAST_TIMOR = new ConstellationIcon.Builder("East Timor", new FileIconData("modules/ext/icons/east_timor.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.TIMOR_LESTE.getDigraph())
            .addAlias("Timor-Leste")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ECUADOR = new ConstellationIcon.Builder("Ecuador", new FileIconData("modules/ext/icons/ecuador.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ECUADOR.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon EGYPT = new ConstellationIcon.Builder("Egypt", new FileIconData("modules/ext/icons/egypt.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.EGYPT.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon EL_SALVADOR = new ConstellationIcon.Builder("El Salvador", new FileIconData("modules/ext/icons/el_salvador.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.EL_SALVADOR.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon EQUATORIAL_GUINEA = new ConstellationIcon.Builder("Equatorial Guinea", new FileIconData("modules/ext/icons/equatorial_guinea.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.EQUATORIAL_GUINEA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ERITREA = new ConstellationIcon.Builder("Eritrea", new FileIconData("modules/ext/icons/eritrea.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ERITREA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ESTONIA = new ConstellationIcon.Builder("Estonia", new FileIconData("modules/ext/icons/estonia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ESTONIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ETHIOPIA = new ConstellationIcon.Builder("Ethiopia", new FileIconData("modules/ext/icons/ethiopia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ETHIOPIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon FIJI = new ConstellationIcon.Builder("Fiji", new FileIconData("modules/ext/icons/fiji.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.FIJI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon FINLAND = new ConstellationIcon.Builder("Finland", new FileIconData("modules/ext/icons/finland.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.FINLAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon FRANCE = new ConstellationIcon.Builder("France", new FileIconData("modules/ext/icons/france.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.FRANCE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GABON = new ConstellationIcon.Builder("Gabon", new FileIconData("modules/ext/icons/gabon.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.GABON.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GAMBIA = new ConstellationIcon.Builder("Gambia", new FileIconData("modules/ext/icons/gambia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.GAMBIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GEORGIA = new ConstellationIcon.Builder("Georgia", new FileIconData("modules/ext/icons/georgia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.GEORGIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GERMANY = new ConstellationIcon.Builder("Germany", new FileIconData("modules/ext/icons/germany.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.GERMANY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GHANA = new ConstellationIcon.Builder("Ghana", new FileIconData("modules/ext/icons/ghana.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.GHANA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GREECE = new ConstellationIcon.Builder("Greece", new FileIconData("modules/ext/icons/greece.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.GREECE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GRENADA = new ConstellationIcon.Builder("Grenada", new FileIconData("modules/ext/icons/grenada.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.GRENADA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GUATEMALA = new ConstellationIcon.Builder("Guatemala", new FileIconData("modules/ext/icons/guatemala.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.GUATEMALA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GUINEA = new ConstellationIcon.Builder("Guinea", new FileIconData("modules/ext/icons/guinea.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.GUINEA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GUINEA_BISSAU = new ConstellationIcon.Builder("Guinea Bissau", new FileIconData("modules/ext/icons/guinea_bissau.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.GUINEA_BISSAU.getDigraph())
            .addAlias("Guinea-Bissau")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GUYANA = new ConstellationIcon.Builder("Guyana", new FileIconData("modules/ext/icons/guyana.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.GUYANA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon HAITI = new ConstellationIcon.Builder("Haiti", new FileIconData("modules/ext/icons/haiti.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.HAITI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon HONDURAS = new ConstellationIcon.Builder("Honduras", new FileIconData("modules/ext/icons/honduras.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.HONDURAS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon HUNGARY = new ConstellationIcon.Builder("Hungary", new FileIconData("modules/ext/icons/hungary.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.HUNGARY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ICELAND = new ConstellationIcon.Builder("Iceland", new FileIconData("modules/ext/icons/iceland.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ICELAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon INDIA = new ConstellationIcon.Builder("India", new FileIconData("modules/ext/icons/india.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.INDIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon INDONESIA = new ConstellationIcon.Builder("Indonesia", new FileIconData("modules/ext/icons/indonesia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.INDONESIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon IRAN = new ConstellationIcon.Builder("Iran", new FileIconData("modules/ext/icons/iran.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.IRAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon IRAQ = new ConstellationIcon.Builder("Iraq", new FileIconData("modules/ext/icons/iraq.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.IRAQ.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon IRELAND = new ConstellationIcon.Builder("Ireland", new FileIconData("modules/ext/icons/ireland.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.IRELAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ISRAEL = new ConstellationIcon.Builder("Israel", new FileIconData("modules/ext/icons/israel.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ISRAEL.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ITALY = new ConstellationIcon.Builder("Italy", new FileIconData("modules/ext/icons/italy.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ITALY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon JAMAICA = new ConstellationIcon.Builder("Jamaica", new FileIconData("modules/ext/icons/jamaica.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.JAMAICA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon JAPAN = new ConstellationIcon.Builder("Japan", new FileIconData("modules/ext/icons/japan.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.JAPAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon JORDAN = new ConstellationIcon.Builder("Jordan", new FileIconData("modules/ext/icons/jordan.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.JORDAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KAZAKHSTAN = new ConstellationIcon.Builder("Kazakhstan", new FileIconData("modules/ext/icons/kazakhstan.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.KAZAKHSTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KENYA = new ConstellationIcon.Builder("Kenya", new FileIconData("modules/ext/icons/kenya.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.KENYA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KIRIBATI = new ConstellationIcon.Builder("Kiribati", new FileIconData("modules/ext/icons/kiribati.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.KIRIBATI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KOREA_NORTH = new ConstellationIcon.Builder("North Koread", new FileIconData("modules/ext/icons/korea_north.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.KOREA_NORTH.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KOREA_SOUTH = new ConstellationIcon.Builder("South Korea", new FileIconData("modules/ext/icons/korea_south.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.KOREA_SOUTH.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KOSOVO = new ConstellationIcon.Builder("Kosovo", new FileIconData("modules/ext/icons/kosovo.png", "au.gov.asd.tac.constellation.visual"))
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KUWAIT = new ConstellationIcon.Builder("Kuwait", new FileIconData("modules/ext/icons/kuwait.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.KUWAIT.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon KYRGYZSTAN = new ConstellationIcon.Builder("Kyrgyzstan", new FileIconData("modules/ext/icons/kyrgyzstan.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.KYRGYZSTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LAOS = new ConstellationIcon.Builder("Laos", new FileIconData("modules/ext/icons/laos.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.LAOS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LATVIA = new ConstellationIcon.Builder("Latvia", new FileIconData("modules/ext/icons/latvia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.LATVIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LEBANON = new ConstellationIcon.Builder("Lebanon", new FileIconData("modules/ext/icons/lebanon.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.LEBANON.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LESOTHO = new ConstellationIcon.Builder("Lesotho", new FileIconData("modules/ext/icons/lesotho.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.LESOTHO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LIBERIA = new ConstellationIcon.Builder("Liberia", new FileIconData("modules/ext/icons/liberia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.LIBERIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LIBYA = new ConstellationIcon.Builder("Libya", new FileIconData("modules/ext/icons/libya.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.LIBYA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LIECHTENSTEIN = new ConstellationIcon.Builder("Liechtenstein", new FileIconData("modules/ext/icons/liechtenstein.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.LIECHTENSTEIN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LITHUANIA = new ConstellationIcon.Builder("Lithuania", new FileIconData("modules/ext/icons/lithuania.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.LITHUANIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon LUXEMBOURG = new ConstellationIcon.Builder("Luxembourg", new FileIconData("modules/ext/icons/luxembourg.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.LUXEMBOURG.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MACEDONIA = new ConstellationIcon.Builder("Macedonia", new FileIconData("modules/ext/icons/macedonia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MACEDONIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MADAGASCAR = new ConstellationIcon.Builder("Madagascar", new FileIconData("modules/ext/icons/madagascar.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MADAGASCAR.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MALAWI = new ConstellationIcon.Builder("Malawi", new FileIconData("modules/ext/icons/malawi.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MALAWI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MALAYSIA = new ConstellationIcon.Builder("Malaysia", new FileIconData("modules/ext/icons/malaysia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MALAYSIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MALDIVES = new ConstellationIcon.Builder("Maldives", new FileIconData("modules/ext/icons/maldives.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MALDIVES.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MALI = new ConstellationIcon.Builder("Mali", new FileIconData("modules/ext/icons/mali.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MALI.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MALTA = new ConstellationIcon.Builder("Malta", new FileIconData("modules/ext/icons/malta.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MALTA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MARSHALL_ISLANDS = new ConstellationIcon.Builder("Marshall Islands", new FileIconData("modules/ext/icons/marshall_islands.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MARSHALL_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MAURITANIA = new ConstellationIcon.Builder("Mauritania", new FileIconData("modules/ext/icons/mauritania.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MAURITANIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MAURITIUS = new ConstellationIcon.Builder("Mauritius", new FileIconData("modules/ext/icons/mauritius.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MAURITIUS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MEXICO = new ConstellationIcon.Builder("Mexico", new FileIconData("modules/ext/icons/mexico.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MEXICO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MICRONESIA = new ConstellationIcon.Builder("Micronesia", new FileIconData("modules/ext/icons/micronesia_federated.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MICRONESIA.getDigraph())
            .addAlias("Micronesia, Federated States of")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MOLDOVA = new ConstellationIcon.Builder("Moldova", new FileIconData("modules/ext/icons/moldova.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MOLDOVA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MONACO = new ConstellationIcon.Builder("Monaco", new FileIconData("modules/ext/icons/monaco.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MONACO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MONGOLIA = new ConstellationIcon.Builder("Mongolia", new FileIconData("modules/ext/icons/mongolia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MONGOLIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MONTENEGRO = new ConstellationIcon.Builder("Montenegro", new FileIconData("modules/ext/icons/montenegro.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MONTENEGRO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MOROCCO = new ConstellationIcon.Builder("Morocco", new FileIconData("modules/ext/icons/morocco.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MOROCCO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MOZAMBIQUE = new ConstellationIcon.Builder("Mozambique", new FileIconData("modules/ext/icons/mozambique.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MOZAMBIQUE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MYANMAR = new ConstellationIcon.Builder("Myanmar", new FileIconData("modules/ext/icons/myanmar.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MYANMAR.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NAMIBIA = new ConstellationIcon.Builder("Namibia", new FileIconData("modules/ext/icons/namibia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.NAMIBIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NAURU = new ConstellationIcon.Builder("Nauru", new FileIconData("modules/ext/icons/nauru.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.NAURU.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NEPAL = new ConstellationIcon.Builder("Nepal", new FileIconData("modules/ext/icons/nepal.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.NEPAL.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NETHERLANDS = new ConstellationIcon.Builder("Netherlands", new FileIconData("modules/ext/icons/netherlands.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.NETHERLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NEW_ZEALAND = new ConstellationIcon.Builder("New Zealand", new FileIconData("modules/ext/icons/new_zealand.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.NEW_ZEALAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NICARAGUA = new ConstellationIcon.Builder("Nicaragua", new FileIconData("modules/ext/icons/nicaragua.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.NICARAGUA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NIGER = new ConstellationIcon.Builder("Niger", new FileIconData("modules/ext/icons/niger.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.NIGER.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NIGERIA = new ConstellationIcon.Builder("Nigeria", new FileIconData("modules/ext/icons/nigeria.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.NIGERIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NORWAY = new ConstellationIcon.Builder("Norway", new FileIconData("modules/ext/icons/norway.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.NORWAY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon OMAN = new ConstellationIcon.Builder("Oman", new FileIconData("modules/ext/icons/oman.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.OMAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PAKISTAN = new ConstellationIcon.Builder("Pakistan", new FileIconData("modules/ext/icons/pakistan.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.PAKISTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PALAU = new ConstellationIcon.Builder("Palau", new FileIconData("modules/ext/icons/palau.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.PALAU.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PANAMA = new ConstellationIcon.Builder("Panama", new FileIconData("modules/ext/icons/panama.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.PANAMA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PAPUA_NEW_GUINEA = new ConstellationIcon.Builder("Papua New Guinea", new FileIconData("modules/ext/icons/papua_new_guinea.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.PAPUA_NEW_GUINEA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PARAGUAY = new ConstellationIcon.Builder("Paraguay", new FileIconData("modules/ext/icons/paraguay.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.PARAGUAY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PERU = new ConstellationIcon.Builder("Peru", new FileIconData("modules/ext/icons/peru.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.PERU.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PHILIPPINES = new ConstellationIcon.Builder("Philippines", new FileIconData("modules/ext/icons/philippines.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.PHILIPPINES.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon POLAND = new ConstellationIcon.Builder("Poland", new FileIconData("modules/ext/icons/poland.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.POLAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PORTUGAL = new ConstellationIcon.Builder("Portugal", new FileIconData("modules/ext/icons/portugal.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.PORTUGAL.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon QATAR = new ConstellationIcon.Builder("Qatar", new FileIconData("modules/ext/icons/qatar.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.QATAR.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ROMANIA = new ConstellationIcon.Builder("Romania", new FileIconData("modules/ext/icons/romania.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ROMANIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon RUSSIA = new ConstellationIcon.Builder("Russia", new FileIconData("modules/ext/icons/russia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.RUSSIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon RWANDA = new ConstellationIcon.Builder("Rwanda", new FileIconData("modules/ext/icons/rwanda.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.RWANDA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAINT_KITTS_AND_NEVIS = new ConstellationIcon.Builder("Saint Kitts and Nevis", new FileIconData("modules/ext/icons/saint_kitts_and_nevis.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SAINT_KITTS_AND_NEVIS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAINT_LUCIA = new ConstellationIcon.Builder("Saint Lucia", new FileIconData("modules/ext/icons/saint_lucia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SAINT_LUCIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAINT_VINCENT_AND_THE_GRENADINES = new ConstellationIcon.Builder("Saint Vincent and the Grenadines", new FileIconData("modules/ext/icons/saint_vincent_and_the_grenadines.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SAINT_VINCENT_AND_THE_GRENADINES.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAMOA = new ConstellationIcon.Builder("Samoa", new FileIconData("modules/ext/icons/samoa.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SAMOA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAN_MARINO = new ConstellationIcon.Builder("San Marino", new FileIconData("modules/ext/icons/san_marino.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SAN_MARINO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAO_TOME_AND_PRINCIPE = new ConstellationIcon.Builder("Sao Tome and Principe", new FileIconData("modules/ext/icons/sao_tome_and_principe.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SAO_TOME_AND_PRINCIPE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SAUDI_ARABIA = new ConstellationIcon.Builder("Saudi Arabia", new FileIconData("modules/ext/icons/saudi_arabia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SAUDI_ARABIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SENEGAL = new ConstellationIcon.Builder("Senegal", new FileIconData("modules/ext/icons/senegal.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SENEGAL.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SERBIA = new ConstellationIcon.Builder("Serbia", new FileIconData("modules/ext/icons/serbia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SERBIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SEYCHELLES = new ConstellationIcon.Builder("Seychelles", new FileIconData("modules/ext/icons/seychelles.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SEYCHELLES.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SIERRA_LEONE = new ConstellationIcon.Builder("Sierra Leone", new FileIconData("modules/ext/icons/sierra_leone.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SIERRA_LEONE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SINGAPORE = new ConstellationIcon.Builder("Singapore", new FileIconData("modules/ext/icons/singapore.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SINGAPORE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SLOVAKIA = new ConstellationIcon.Builder("Slovakia", new FileIconData("modules/ext/icons/slovakia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SLOVAKIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SLOVENIA = new ConstellationIcon.Builder("Slovenia", new FileIconData("modules/ext/icons/slovenia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SLOVENIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SOLOMON_ISLANDS = new ConstellationIcon.Builder("Solomon Islands", new FileIconData("modules/ext/icons/solomon_islands.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SOLOMON_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SOMALIA = new ConstellationIcon.Builder("Somalia", new FileIconData("modules/ext/icons/somalia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SOMALIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SOUTH_AFRICA = new ConstellationIcon.Builder("South Africa", new FileIconData("modules/ext/icons/south_africa.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SOUTH_AFRICA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SOUTH_SUDAN = new ConstellationIcon.Builder("South Sudan", new FileIconData("modules/ext/icons/south_sudan.png", "au.gov.asd.tac.constellation.visual"))
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SPAIN = new ConstellationIcon.Builder("Spain", new FileIconData("modules/ext/icons/spain.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SPAIN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SRI_LANKA = new ConstellationIcon.Builder("Sri Lanka", new FileIconData("modules/ext/icons/sri_lanka.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SRI_LANKA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SUDAN = new ConstellationIcon.Builder("Sudan", new FileIconData("modules/ext/icons/sudan.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SUDAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SURINAME = new ConstellationIcon.Builder("Suriname", new FileIconData("modules/ext/icons/suriname.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SURINAME.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SWAZILAND = new ConstellationIcon.Builder("Swaziland", new FileIconData("modules/ext/icons/swaziland.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SWAZILAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SWEDEN = new ConstellationIcon.Builder("Sweden", new FileIconData("modules/ext/icons/sweden.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SWEDEN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SWITZERLAND = new ConstellationIcon.Builder("Switzerland", new FileIconData("modules/ext/icons/switzerland.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SWITZERLAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon SYRIA = new ConstellationIcon.Builder("Syria", new FileIconData("modules/ext/icons/syria.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.SYRIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TAIWAN = new ConstellationIcon.Builder("Taiwan", new FileIconData("modules/ext/icons/taiwan.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.TAIWAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TAJIKISTAN = new ConstellationIcon.Builder("Tajikistan", new FileIconData("modules/ext/icons/tajikistan.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.TAJIKISTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TANZANIA = new ConstellationIcon.Builder("Tanzania", new FileIconData("modules/ext/icons/tanzania.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.TANZANIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon THAILAND = new ConstellationIcon.Builder("Thailand", new FileIconData("modules/ext/icons/thailand.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.THAILAND.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TOGO = new ConstellationIcon.Builder("Togo", new FileIconData("modules/ext/icons/togo.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.TOGO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TONGA = new ConstellationIcon.Builder("Tonga", new FileIconData("modules/ext/icons/tonga.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.TONGA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TRINIDAD_AND_TOBAGO = new ConstellationIcon.Builder("Trinidad and Tobago", new FileIconData("modules/ext/icons/trinidad_and_tobago.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.TRINIDAD_AND_TOBAGO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TUNISIA = new ConstellationIcon.Builder("Tunisia", new FileIconData("modules/ext/icons/tunisia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.TUNISIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TURKEY = new ConstellationIcon.Builder("Turkey", new FileIconData("modules/ext/icons/turkey.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.TURKEY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TURKMENISTAN = new ConstellationIcon.Builder("Turkmenistan", new FileIconData("modules/ext/icons/turkmenistan.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.TURKMENISTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TUVALU = new ConstellationIcon.Builder("Tuvalu", new FileIconData("modules/ext/icons/tuvalu.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.TUVALU.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon UGANDA = new ConstellationIcon.Builder("Uganda", new FileIconData("modules/ext/icons/uganda.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.UGANDA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon UKRAINE = new ConstellationIcon.Builder("Ukraine", new FileIconData("modules/ext/icons/ukraine.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.UKRAINE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon UNITED_ARAB_EMIRATES = new ConstellationIcon.Builder("United Arab Emirates", new FileIconData("modules/ext/icons/united_arab_emirates.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.UNITED_ARAB_EMIRATES.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon UNITED_KINGDOM = new ConstellationIcon.Builder("United Kingdom", new FileIconData("modules/ext/icons/united_kingdom.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.UNITED_KINGDOM.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon UNITED_STATES = new ConstellationIcon.Builder("United States", new FileIconData("modules/ext/icons/united_states_of_america.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.UNITED_STATES.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon URUGUAY = new ConstellationIcon.Builder("Uruguay", new FileIconData("modules/ext/icons/uruguay.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.URUGUAY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon UZBEKISTAN = new ConstellationIcon.Builder("Uzbekistan", new FileIconData("modules/ext/icons/uzbekistan.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.UZBEKISTAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon VANUATU = new ConstellationIcon.Builder("Vanuatu", new FileIconData("modules/ext/icons/vanuatu.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.VANUATU.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon VATICAN_CITY = new ConstellationIcon.Builder("Vatican City", new FileIconData("modules/ext/icons/vatican_city.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.HOLY_SEE.getDigraph())
            .addAlias("Vatican City State")
            .addAlias("Holy See")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon VENEZUELA = new ConstellationIcon.Builder("Venezuela", new FileIconData("modules/ext/icons/venezuela.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.VENEZUELA.getDigraph())
            .addAlias("Venezuela, Bolivarian Republic of")
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon VIETNAM = new ConstellationIcon.Builder("Vietnam", new FileIconData("modules/ext/icons/vietnam.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.VIETNAM.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon YEMEN = new ConstellationIcon.Builder("Yemen", new FileIconData("modules/ext/icons/yemen.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.YEMEN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ZAMBIA = new ConstellationIcon.Builder("Zambia", new FileIconData("modules/ext/icons/zambia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ZAMBIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ZIMBABWE = new ConstellationIcon.Builder("Zimbabwe", new FileIconData("modules/ext/icons/zimbabwe.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ZIMBABWE.getDigraph())
            .addCategory("Flag")
            .build();

    public static final ConstellationIcon ALAND_ISLANDS = new ConstellationIcon.Builder("Aland Islands", new FileIconData("modules/ext/icons/aland.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ALAND_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ALAND = new ConstellationIcon.Builder("Aland", new FileIconData("modules/ext/icons/aland.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ALAND_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ANGOLA = new ConstellationIcon.Builder("Angola", new FileIconData("modules/ext/icons/angola.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ANGOLA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ANGUILLA = new ConstellationIcon.Builder("Anguilla", new FileIconData("modules/ext/icons/anguilla.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ANGUILLA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ARUBA = new ConstellationIcon.Builder("Aruba", new FileIconData("modules/ext/icons/aruba.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ARUBA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon BERMUDA = new ConstellationIcon.Builder("Bermuda", new FileIconData("modules/ext/icons/bermuda.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.BERMUDA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon VIRGIN_ISLANDS_BRITISH = new ConstellationIcon.Builder("British Virgin Islands", new FileIconData("modules/ext/icons/british_virgin_islands.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.VIRGIN_ISLANDS_BRITISH.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CAYMAN_ISLANDS = new ConstellationIcon.Builder("Cayman Islands", new FileIconData("modules/ext/icons/cayman_islands.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CAYMAN_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon COOK_ISLANDS = new ConstellationIcon.Builder("Cook Islands", new FileIconData("modules/ext/icons/cook_islands.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.COOK_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon CURACAO = new ConstellationIcon.Builder("Curacao", new FileIconData("modules/ext/icons/curacao.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.CURACAO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon FALKLAND_ISLANDS = new ConstellationIcon.Builder("Falkland Islands", new FileIconData("modules/ext/icons/falkland_islands.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.FALKLAND_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GIBRALTAR = new ConstellationIcon.Builder("Gibraltar", new FileIconData("modules/ext/icons/gibraltar.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.GIBRALTAR.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon GUERNSEY = new ConstellationIcon.Builder("Guernsey", new FileIconData("modules/ext/icons/guernsey.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.GUERNSEY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon HONG_KONG = new ConstellationIcon.Builder("Hong Kong", new FileIconData("modules/ext/icons/hong_kong.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.HONG_KONG.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon ISLE_OF_MAN = new ConstellationIcon.Builder("Isle of Man", new FileIconData("modules/ext/icons/isle_of_man.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.ISLE_OF_MAN.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon JERSEY = new ConstellationIcon.Builder("Jersey", new FileIconData("modules/ext/icons/jersey.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.JERSEY.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MACAU = new ConstellationIcon.Builder("Macau", new FileIconData("modules/ext/icons/macau.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MACAU.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon MONTSERRAT = new ConstellationIcon.Builder("Montserrat", new FileIconData("modules/ext/icons/montserrat.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.MONTSERRAT.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NEW_CALEDONIA = new ConstellationIcon.Builder("New Caledonia", new FileIconData("modules/ext/icons/new_caledonia.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.NEW_CALEDONIA.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NIUE = new ConstellationIcon.Builder("Niue", new FileIconData("modules/ext/icons/niue.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.NIUE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon NORTHERN_MARIANA_ISLANDS = new ConstellationIcon.Builder("Northern Mariana Islands", new FileIconData("modules/ext/icons/northern_mariana_islands.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.NORTHERN_MARIANA_ISLANDS.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PALESTINE = new ConstellationIcon.Builder("Palestine", new FileIconData("modules/ext/icons/palestine.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.PALESTINE.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon PUERTO_RICO = new ConstellationIcon.Builder("Puerto Rico", new FileIconData("modules/ext/icons/puerto_rico.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.PUERTO_RICO.getDigraph())
            .addCategory("Flag")
            .build();
    public static final ConstellationIcon TOKELAU = new ConstellationIcon.Builder("Tokelau", new FileIconData("modules/ext/icons/tokelau.png", "au.gov.asd.tac.constellation.visual"))
            .addAlias(Country.TOKELAU.getDigraph())
            .addCategory("Flag")
            .build();

    @Override
    public List<ConstellationIcon> getIcons() {
        List<ConstellationIcon> flagIcons = new ArrayList<>();
        flagIcons.add(AFGHANISTAN);
        flagIcons.add(ALBANIA);
        flagIcons.add(ALGERIA);
        flagIcons.add(ANDORRA);
        flagIcons.add(ANTIGUA_AND_BARBUDA);
        flagIcons.add(ARGENTINA);
        flagIcons.add(ARMENIA);
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
        flagIcons.add(CENTRAL_AFRICAN_REPUBLIC);
        flagIcons.add(CHAD);
        flagIcons.add(CHILE);
        flagIcons.add(CHINA);
        flagIcons.add(COLOMBIA);
        flagIcons.add(COMOROS);
        flagIcons.add(CONGO_DEMOCRATIC);
        flagIcons.add(CONGO_REPUBLIC);
        flagIcons.add(COSTA_RICA);
        flagIcons.add(COTE_DIVOIRE);
        flagIcons.add(CROATIA);
        flagIcons.add(CUBA);
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
        flagIcons.add(FIJI);
        flagIcons.add(FINLAND);
        flagIcons.add(FRANCE);
        flagIcons.add(GABON);
        flagIcons.add(GAMBIA);
        flagIcons.add(GEORGIA);
        flagIcons.add(GERMANY);
        flagIcons.add(GHANA);
        flagIcons.add(GREECE);
        flagIcons.add(GRENADA);
        flagIcons.add(GUATEMALA);
        flagIcons.add(GUINEA);
        flagIcons.add(GUINEA_BISSAU);
        flagIcons.add(GUYANA);
        flagIcons.add(HAITI);
        flagIcons.add(HONDURAS);
        flagIcons.add(HUNGARY);
        flagIcons.add(ICELAND);
        flagIcons.add(INDIA);
        flagIcons.add(INDONESIA);
        flagIcons.add(IRAN);
        flagIcons.add(IRAQ);
        flagIcons.add(IRELAND);
        flagIcons.add(ISRAEL);
        flagIcons.add(ITALY);
        flagIcons.add(JAMAICA);
        flagIcons.add(JAPAN);
        flagIcons.add(JORDAN);
        flagIcons.add(KAZAKHSTAN);
        flagIcons.add(KENYA);
        flagIcons.add(KIRIBATI);
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
        flagIcons.add(MOROCCO);
        flagIcons.add(MOZAMBIQUE);
        flagIcons.add(MYANMAR);
        flagIcons.add(NAMIBIA);
        flagIcons.add(NAURU);
        flagIcons.add(NEPAL);
        flagIcons.add(NETHERLANDS);
        flagIcons.add(NEW_ZEALAND);
        flagIcons.add(NICARAGUA);
        flagIcons.add(NIGER);
        flagIcons.add(NIGERIA);
        flagIcons.add(NORWAY);
        flagIcons.add(OMAN);
        flagIcons.add(PAKISTAN);
        flagIcons.add(PALAU);
        flagIcons.add(PANAMA);
        flagIcons.add(PAPUA_NEW_GUINEA);
        flagIcons.add(PARAGUAY);
        flagIcons.add(PERU);
        flagIcons.add(PHILIPPINES);
        flagIcons.add(POLAND);
        flagIcons.add(PORTUGAL);
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
        flagIcons.add(YEMEN);
        flagIcons.add(ZAMBIA);
        flagIcons.add(ZIMBABWE);

        flagIcons.add(ALAND_ISLANDS);
        flagIcons.add(ANGOLA);
        flagIcons.add(ANGUILLA);
        flagIcons.add(ARUBA);
        flagIcons.add(BERMUDA);
        flagIcons.add(VIRGIN_ISLANDS_BRITISH);
        flagIcons.add(CAYMAN_ISLANDS);
        flagIcons.add(COOK_ISLANDS);
        flagIcons.add(CURACAO);
        flagIcons.add(FALKLAND_ISLANDS);
        flagIcons.add(GIBRALTAR);
        flagIcons.add(GUERNSEY);
        flagIcons.add(HONG_KONG);
        flagIcons.add(ISLE_OF_MAN);
        flagIcons.add(JERSEY);
        flagIcons.add(MACAU);
        flagIcons.add(MONTSERRAT);
        flagIcons.add(NEW_CALEDONIA);
        flagIcons.add(NIUE);
        flagIcons.add(NORTHERN_MARIANA_ISLANDS);
        flagIcons.add(PALESTINE);
        flagIcons.add(PUERTO_RICO);
        flagIcons.add(TOKELAU);
        return flagIcons;
    }
}

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

    
    private final List<ConstellationIcon> flagIcons = new ArrayList<>();
     
    public FlagIconProvider(){
        // Iterate over country enums and add flag to list
        for (Country c : Country.values()) {
            try {
                final ConstellationIcon countryIcon = new ConstellationIcon.Builder(c.getDisplayName(), new FileIconData("modules/ext/icons/flags/" + c.getDisplayName().replaceAll(" ", "_").replaceAll(",", "").toLowerCase() + ".png", CODE_NAME_BASE))
                        .addAlias(c.getDigraph())
                        .addAlias(c.getTrigraph())
                        .addAliases(Arrays.asList(c.getAlternateNames()))
                        .addCategory(FLAG_CATEGORY)
                        .build();
                flagIcons.add(countryIcon);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Unable to find file: {0}", "modules/ext/icons/flags/" + c.getDisplayName().replaceAll(" ", "_").toLowerCase() + ".png");
            }
        }
    }

    @Override
    public List<ConstellationIcon> getIcons() {
        return flagIcons;
    }
}

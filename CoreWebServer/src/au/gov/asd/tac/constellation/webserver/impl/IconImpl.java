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
package au.gov.asd.tac.constellation.webserver.impl;

import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author algol
 */
public class IconImpl {

    /**
     * A list of available icons.
     *
     * @param editable If true the result set will be filtered to only editable
     * icons, if false the results set will be filtered to only built-in icons,
     * if null all icons will be returned.
     *
     * @param out An OutputStream to write the response to.
     *
     * @throws IOException
     */
    public static void get_list(final Boolean editable, final OutputStream out) throws IOException {
        final List<String> names = new ArrayList<>(IconManager.getIconNames(editable));
        names.sort(String::compareToIgnoreCase);

        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode root = mapper.createArrayNode();
        names.forEach(name -> root.add(name));

        mapper.writeValue(out, root);
    }

    /**
     * Get a particular icon.
     *
     * @param name The name of the icon to get.
     * @param out An OutputStream to write the response to.
     *
     * @throws IOException
     */
    public static void get_get(final String name, final OutputStream out) throws IOException {
        final ConstellationIcon icon = IconManager.getIcon(name);
        out.write(icon.buildByteArray());
    }
}

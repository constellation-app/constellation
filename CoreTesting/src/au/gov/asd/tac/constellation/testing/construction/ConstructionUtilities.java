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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author algol
 */
public class ConstructionUtilities {

    private ConstructionUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Load a sequence of icons into a list of bytes.
     * <p>
     * Each string in <tt>fnams</tt> is the case-sensitive name of an icon which
     * is drawn in a graph. Icons are loaded by using <tt><i>fnam</i></tt> to
     * create a resource name <tt>icons/<i>fnam</i>.png</tt>, and loading the
     * bytes in resource using
     * <tt>getResourceAsStream()</tt> relative to the specified class.
     *
     * @param cls The class relative to which the icons are loaded from.
     * @param names The filenames of the icons.
     *
     * @return A List&lt;byte[]&gt; containing the contents of the resources in
     * the same order as the filenames.
     *
     * @throws IOException is an error occurs while reading the icon resources.
     */
    public static Map<String, byte[]> loadIcons(final Class<?> cls, final String... names) throws IOException {
        final HashMap<String, byte[]> iconBytes = new HashMap<>();

        for (String name : names) {
            final String resourceName = String.format("icons/%s.png", name);
            final ByteArrayOutputStream out;
            try (InputStream in = cls.getResourceAsStream(resourceName)) {
                if (in == null) {
                    throw new IOException("Can't find icon file '" + resourceName + "' relative to class " + cls.getCanonicalName());
                }
                out = new ByteArrayOutputStream();
                final byte[] buf = new byte[1024];
                while (true) {
                    final int len = in.read(buf);
                    if (len == -1) {
                        break;
                    }
                    out.write(buf, 0, len);
                }
            }
            out.close();

            iconBytes.put(name, out.toByteArray());
        }

        return iconBytes;
    }

    public static void setxyz(final GraphWriteMethods graph, final int nodeId, final int xattr, final int yattr, final int zattr, final float x, final float y, final float z) {
        graph.setFloatValue(xattr, nodeId, x);
        graph.setFloatValue(yattr, nodeId, y);
        graph.setFloatValue(zattr, nodeId, z);
    }
}

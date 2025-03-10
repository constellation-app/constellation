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
package au.gov.asd.tac.constellation.utilities.svg;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Common testing methods for svg utility classes
 * 
 * @author capricornunicorn123
 */
public class SVGTestUtilities {
    
    public static void testLoadedData(SVGData graph){   
        SVGData background = graph.getChild("background");
        SVGData content = graph.getChild("content");
        SVGData header = graph.getChild("header");
        SVGData node0 = graph.getChild("Node-0");

        assertTrue(graph.getAllChildren().contains(background));
        assertTrue(graph.getAllChildren().contains(content));
        assertTrue(graph.getAllChildren().contains(header));
        assertFalse(graph.getAllChildren().contains(node0));
    }
    
    public static String getString(final SVGData svgObject) {
        final StringBuilder sb = new StringBuilder();
        for (final String line : svgObject.toLines()){
            sb.append(line);
        }
        return sb.toString();
    }
}

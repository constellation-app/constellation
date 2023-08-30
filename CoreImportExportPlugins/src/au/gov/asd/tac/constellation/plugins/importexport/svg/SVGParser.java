/*
* Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.svg;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * The SVGParser facilitates the translation of SVGfiles into
 * an in memory format for easy manipulation and translation. 
 * In contrast to other Constellation import parsers, this import parser does not 
 * assume data is in a tabular format and as such will not extend the InportFileParser
 * 
 * @author capricornunicorn123
 */
public class SVGParser {
    
    private SVGParser() {
        throw new IllegalStateException("Utility class");
    }
    /**
     * Takes an input stream and translates it to an SVG object. 
     * Will only translate to an SVG object if the file data is valid.
     * 
     * Does not support multi-line tags.
     * 
     * @param inputStream
     * @return
     * @throws IOException 
     */
    public static SVGObject parse(InputStream inputStream) throws IOException {
        
        SVGObject currentElement = null; 
        final Collection<SVGObject> roots = new HashSet<>();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                final String svgElement = SVGParser.isolateSVGElement(line);
                boolean openTag = SVGParser.isOpenTag(svgElement);
                boolean closeTag = SVGParser.isCloseTag(svgElement);
                
                // Create a new SVGObject with the current SVGObject as the parent 
                if (openTag){
                    SVGObject newObject = new SVGObject(SVGParser.getElementType(svgElement), currentElement);
                    newObject.setAttributes(SVGParser.getElementAttributes(svgElement));
                    currentElement = newObject;
                }
                // Move back up one level to the curren objects parent
                if (closeTag){
                    currentElement = currentElement.getParent();
                } 
                // This parser curently requires all lines with be an SVG tag as it does not support multi line tags.
                if (!openTag && !closeTag){
                    throw new UnsupportedOperationException(String.format("This line could not be interpreted: %s", svgElement));
                }
                
                if (currentElement != null && currentElement.getParent() == null){
                    roots.add(currentElement);
                }
            }
        }
        if (roots.size() != 1){
            throw new UnsupportedOperationException(String.format("The SVG file has %s outer elements.", roots.size()));
        } else{
            return (SVGObject) roots.toArray()[0];
        }
    }
        
    /**
     * Takes an SVG element and returns the tag type of the element.
     * 
     * @param svgString
     * @return 
     */
    private static String getElementType(final String svgString){
        return svgString.split(SeparatorConstants.BLANKSPACE)[0].replaceAll("<", "").replaceAll(">", "");
    }
   
    private static Map<String,String> getElementAttributes(final String svgString) {
        Map<String,String> attributes = new HashMap<>();
        String[] components = svgString.split(SeparatorConstants.BLANKSPACE);
        for (String component : components) {
            if (component.contains("=")) {
                String[] attribute = component.split("=");
                if (attribute.length == 2){
                    String key = attribute[0];
                    String value = attribute[1].replaceAll(SeparatorConstants.QUOTE, "");
                    attributes.put(key, value);
                } else {
                    throw new UnsupportedOperationException(String.format("This line could not be interpreted: %s", component));
                }
            }
        }
        return attributes;
    }

    /**
     * Takes a String and returns the SVG element contained in that string.
     * White spaces and foreign characters external to the tag are removed.
     * 
     * @param line
     * @return 
     */
    private static String isolateSVGElement(final String line) {
        String svgElement = line.substring(line.indexOf("<"), line.indexOf(">") + 1);
        if (svgElement.length() < 2){
            throw new UnsupportedOperationException("SVG Element wrong");
        }
        return svgElement;
    }

    /**
     * checks if the current element is a close tag.
     * A close tag will contain the characters "&lt/" or "/&gt"
     * 
     * @param line
     * @return 
     */
    private static boolean isCloseTag(final String line) {
        return line.contains("/>") || line.contains("</");
    }

    /**
     * Checks if the current element is an open Tag.
     * An open tag will contain the characters "&lt" unless it also contains "<&lt/"
     * @param line
     * @return 
     */
    private static boolean isOpenTag(final String line) {
        return line.contains("<") && !line.contains("</");
    }
}

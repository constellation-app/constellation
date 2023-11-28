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
package au.gov.asd.tac.constellation.utilities.svg;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The SVGParser facilitates the translation of SVGfiles into
 * an in memory format for easy manipulation and translation. 
 * In contrast to other Constellation import parsers, this import parser does not 
 * assume data is in a tabular format and as such will not extend the InportFileParser
 * 
 * @author capricornunicorn123
 */
public class SVGParser {

    static final String NON_LATIN_CHARACTER_OMMMISION_TEXT = " (omitted non-latin characters)"; 
    
    private SVGParser() {
        throw new IllegalStateException("Utility class");
    }
    
    private static boolean isHeaderTag(final String line) {
        return line.contains("<?") && line.contains("?>");
    }

    /**
     * Takes an input stream and translates it to an SVG object. 
     * Will only translate to an SVG object if the file data is valid.
     * Does not support multi-line tags.
     * @param inputStream
     * @return SVGData
     * @throws IOException 
     */
    public static SVGData parse(final InputStream inputStream) throws IOException {
        
        if (inputStream == null) {
            throw new IOException("An input stream has not been provided");
        }
        
        SVGData currentElement = null; 
        final Collection<SVGData> roots = new HashSet<>();
        
        final InputStreamReader isr = new InputStreamReader(inputStream);
        final BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            final String svgElement = SVGParser.isolateSVGElement(line);
            if (svgElement != null) {
                final boolean openTag = SVGParser.isOpenTag(svgElement);
                final boolean closeTag = SVGParser.isCloseTag(svgElement);

                // This parser curently requires all lines within an SVG tag as it does not support multi line tags.
                if (!openTag && !closeTag && !SVGParser.isHeaderTag(svgElement)) {
                    throw new IllegalStateException(String.format("This line could not be interpreted: %s", svgElement));
                }

                // Create a new SVGData with the current SVGData as the parent 
                if (openTag) {
                    final SVGData newObject = new SVGData(
                            SVGParser.getElementType(svgElement), 
                            currentElement, 
                            SVGParser.getElementAttributes(svgElement)
                    );
                    currentElement = newObject;
                }

                if (currentElement != null && currentElement.getParent() == null && !roots.contains(currentElement)) {
                    roots.add(currentElement);
                }                

                // Move back up one level to the current objects parent
                if (closeTag) {
                    currentElement = currentElement.getParent();
                } 
            }
        }
        
        if (roots.size() != 1) {
            throw new IllegalStateException(String.format("The SVG file has %s outer elements.", roots.size()));
        } else {
            return (SVGData) roots.toArray()[0];
        }
    }

    /**
     * Ensures plan text string can be rendered by browsers.
     * @param text
     * @return 
     */
    public static String sanitisePlanText(final String text) {
        final String validString = replaceInvalidCharacters(text);
        return removeNonLatinCharacters(validString);
    }
    
    /**
     * Replaces <&amp;>, <&gt;>, <&lt;>, " and ' with XML entity reference.
     * @param text
     * @return 
     */
    private static String replaceInvalidCharacters(final String text) {
        return text.replace("&", "&amp;").replace(">", "&gt;").replace("<", "&lt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
    
    /**
     * Removes ASCII characters above 126.
     * Appends a disclaimer that non-Latin characters have been omitted.
     * @param text
     * @return 
     */
    private static String removeNonLatinCharacters(final String text) {
        final StringBuilder builder = new StringBuilder();
        final char[] charArray = text.toCharArray();
        for (int i = 0 ; i < charArray.length ; i++) {
            if (charArray[i] < 127 && charArray[i] > 0 && charArray[i] != 12) {
                builder.append(charArray[i]);
            }
        }
        if (builder.length() < text.length()) {
            builder.append(SVGParser.NON_LATIN_CHARACTER_OMMMISION_TEXT);
        }
        return builder.toString();
    }
          
    /**
     * Takes an SVG element and returns the tag type of the element.
     * @param svgString
     * @return 
     */
    private static SVGTypeConstants getElementType(final String svgString) {
        final String typeString = svgString.split(SeparatorConstants.BLANKSPACE)[0].replaceAll("<", "").replaceAll(">", "");
        return SVGTypeConstants.getType(typeString);
    }
   
    /**
     * Retrieves a map of SVG attribute key value pairs.
     * uses regular expressions to interpret potential attributes from the SVG string.
     * Cases may exist where this regular expression does not satisfy attribute requirements.
     * @param svgString
     * @return 
     */
    private static Map<String,String> getElementAttributes(final String svgString) {
        final Map<String,String> extractedAttributes = new LinkedHashMap<>();
        final String regex = "[-:a-zA-Z0-9]*=\"[,\\-/:%#\\s.a-zA-Z0-9]*\"";
        final Pattern svgAttributeAssignmentRegex = Pattern.compile(regex);
        final Matcher svgMatcher = svgAttributeAssignmentRegex.matcher(svgString);
        while (svgMatcher.find()) {
            final String potentialAttribute = svgMatcher.group();
            final String[] attribute = potentialAttribute.split("=");
            final String foundKey = attribute[0];
            final String foundValue = attribute[1].replaceAll(SeparatorConstants.QUOTE, "");
            extractedAttributes.put(foundKey, foundValue);
        }        
        return extractedAttributes;
    }

    /**
     * Takes a String and returns the SVG element contained in that string.
     * White spaces and foreign characters external to the tag are removed.
     * @param line
     * @return 
     */
    private static String isolateSVGElement(final String line) throws UnsupportedOperationException{
        final List<String> svgElements = new ArrayList<>();
        final String regex = "<.*>";
        final Pattern svgAttributeAssignmentRegex = Pattern.compile(regex);
        final Matcher svgMatcher = svgAttributeAssignmentRegex.matcher(line);
        int foundElements = 0;
        while (svgMatcher.find()) {
            final String potentialElement = svgMatcher.group();
            svgElements.add(potentialElement);
            foundElements++;
        }
        if (foundElements < 1) {
            return null;
        } else {
            return svgElements.get(0);
        }
    }

    /**
     * checks if the current element is a close tag.
     * A close tag will contain the characters "&lt/" or "/&gt"
     * @param line
     * @return 
     */
    private static boolean isCloseTag(final String line) {
        return (line.contains("/>") || line.contains("</") && !line.contains("?>"));
    }

    /**
     * Checks if the current element is an open Tag.
     * An open tag will contain the characters "&lt" unless it also contains "<&lt/"
     * @param line
     * @return 
     */
    private static boolean isOpenTag(final String line) {
        return line.contains("<") && !line.contains("</") && !line.contains("<?");
    }    
}

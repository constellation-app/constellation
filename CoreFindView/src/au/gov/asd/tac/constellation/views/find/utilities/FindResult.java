/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.find.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This class is the holder class for an individual vertex, transaction, edge or
 * link that has been found to be a match to one or more find criteria.
 * <p>
 * Any results delivered from advanced or quick queries are returned in an
 * <code>ArrayList&lt;FindResult&gt;</code>.
 *
 * @see ArrayList
 *
 * @author betelgeuse
 */
public class FindResult {

    public static final String SEPARATOR = " : ";
    public static final String LEFT_BRACKET = "\u276a"; // bold left parenthesis
    public static final String RIGHT_BRACKET = "\u276b"; // bold right parenthesis
    public static final String SMALL_SPACE = "\u2005";
    public static final String CIRCLED_D = LEFT_BRACKET + "\uff24" + RIGHT_BRACKET + SMALL_SPACE; // (D) - prefix for Data Access Plugin results
    public static final String CIRCLED_E = LEFT_BRACKET + "\uff25" + RIGHT_BRACKET + SMALL_SPACE; // (E) - prefix for EDGE results
    public static final String CIRCLED_H = LEFT_BRACKET + "\uff28" + RIGHT_BRACKET + SMALL_SPACE; // (H) - prefix for HELP results
    public static final String CIRCLED_L = LEFT_BRACKET + "\uff2c" + RIGHT_BRACKET + SMALL_SPACE; // (L) - prefix for LINK results
    public static final String CIRCLED_N = LEFT_BRACKET + "\uff2e" + RIGHT_BRACKET + SMALL_SPACE; // (N) - prefix for NODE results
    public static final String CIRCLED_T = LEFT_BRACKET + "\uff34" + RIGHT_BRACKET + SMALL_SPACE; // (T) - prefix for TRANSACTION results 
    public static final String LH_SUB_BRACKET = "\u208d"; // subscript left bracket
    public static final String RH_SUB_BRACKET = "\u208e"; // subscript right bracket

    private int id;
    private long uid;
    private GraphElementType type;
    private String attributeName;
    private Object value;
    private static final String SELECTED = "selected";
    private static final String DEFAULT_VALUE = "found";
    private final String graphId;

    /**
     * Constructs a new <code>FindResult</code> with minimum applicable content.
     *
     * @param id The ID of the Graph Element this <code>FindResult</code>
     * represents.
     * @param type The type of result. May be VERTEX, LINK, EDGE or TRANSACTION.
     */
    public FindResult(final int id, final long uid, final GraphElementType type, final String graphId) {
        this.id = id;
        this.uid = uid;
        this.type = type;
        this.attributeName = SELECTED;
        this.value = DEFAULT_VALUE;
        this.graphId = graphId;
    }

    /**
     * Constructs a new <code>FindResult</code>.
     *
     * @param id Graph ID of the search result.
     * @param type Type of result. May be VERTEX, LINK, EDGE or TRANSACTION.
     * @param attributeName The name of the result's attribute type.
     * @param value The content of the given VERTEX, LINK, EDGE or TRANSACTION.
     */
    public FindResult(final int id, final long uid, final GraphElementType type,
            final String attributeName, final Object value, final String graphId) {
        this.id = id;
        this.uid = uid;
        this.type = type;
        this.attributeName = attributeName;
        this.value = value;
        this.graphId = graphId;
    }

    /**
     * Returns the ID of the given GraphElement.
     *
     * @return The ID of the given GraphElement.
     */
    public int getID() {
        return id;
    }

    /**
     * Sets the ID of the given GraphElement.
     *
     * @param id ID to set this FindResult to.
     */
    public void setID(final int id) {
        this.id = id;
    }

    /**
     * Returns the UID of the given GraphElement.
     *
     * @return The UID of the given GraphElement.
     */
    public long getUID() {
        return uid;
    }

    /**
     * Sets the UID of the given GraphElement.
     *
     * @param uid UID to set this FindResult to.
     */
    public void setUID(final long uid) {
        this.uid = uid;
    }

    /**
     * Gets the type of the given GraphElement.
     *
     * @return type.
     */
    public GraphElementType getType() {
        return type;
    }

    /**
     * Sets the type of the given GraphElement.
     *
     * @param type Type of the GraphElement.
     */
    public void setType(final GraphElementType type) {
        this.type = type;
    }

    /**
     * Gets the name of the attribute for the given GraphElement.
     *
     * @return AttributeName.
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Sets the attribute name.
     *
     * @param attributeName The name of the attribute.
     */
    public void setAttributeName(final String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * Returns the stored attribute value.
     *
     * @return attribute value.
     */
    public Object getAttributeValue() {
        return value;
    }

    /**
     * Sets the value of this result to the given value.
     *
     * @param value Value of the GraphElement.
     */
    public void setAttributeValue(final Object value) {
        this.value = value;
    }

    /**
     * Gets the id of the graph this result is found in
     * 
     * @return graphId
     */
    public String getGraphId() {
        return graphId;
    }

    public static String buildSubscriptFromID(final String idData) {
        final StringBuilder subscriptId = new StringBuilder();
        for (int i = 0; i < idData.length(); i++) {
            char currentChar = idData.charAt(i);
            switch (currentChar) {
                case '0':
                    subscriptId.append('\u2080');
                    break;
                case '1':
                    subscriptId.append('\u2081');
                    break;
                case '2':
                    subscriptId.append('\u2082');
                    break;
                case '3':
                    subscriptId.append('\u2083');
                    break;
                case '4':
                    subscriptId.append('\u2084');
                    break;
                case '5':
                    subscriptId.append('\u2085');
                    break;
                case '6':
                    subscriptId.append('\u2086');
                    break;
                case '7':
                    subscriptId.append('\u2087');
                    break;
                case '8':
                    subscriptId.append('\u2088');
                    break;
                case '9':
                    subscriptId.append('\u2089');
                    break;
            }
        }
        return subscriptId.toString();
    }
    
    public static String buildIDFromSubscript(final String idSubscriptData) {
        final StringBuilder subscriptId = new StringBuilder();
        for (int i = 0; i < idSubscriptData.length(); i++) {
            char currentChar = idSubscriptData.charAt(i);
            switch (currentChar) {
                case '\u2080':
                    subscriptId.append('0');
                    break;
                case '\u2081':
                    subscriptId.append('1');
                    break;
                case '\u2082':
                    subscriptId.append('2');
                    break;
                case '\u2083':
                    subscriptId.append('3');
                    break;
                case '\u2084':
                    subscriptId.append('4');
                    break;
                case '\u2085':
                    subscriptId.append('5');
                    break;
                case '\u2086':
                    subscriptId.append('6');
                    break;
                case '\u2087':
                    subscriptId.append('7');
                    break;
                case '\u2088':
                    subscriptId.append('8');
                    break;
                case '\u2089':
                    subscriptId.append('9');
                    break;
            }
        }
        return subscriptId.toString();
    }

    /**
     * Returns the string representation of this FindResult.
     *
     * @return The current item's value and GraphElementType.
     */
    @Override
    public String toString() {
        return value.toString() + SEPARATOR + attributeName;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FindResult)) {
            return false;
        }
        final FindResult other = (FindResult) obj;

        return this.id == other.id && this.graphId.equals(other.graphId);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.id;
        hash = 97 * hash + (int) (this.uid ^ (this.uid >>> 32));
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.attributeName);
        hash = 97 * hash + Objects.hashCode(this.value);
        hash = 97 * hash + Objects.hashCode(this.graphId);
        return hash;
    }
}

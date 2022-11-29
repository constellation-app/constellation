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
package au.gov.asd.tac.constellation.views.attributeeditor;

import java.util.HashSet;
import java.util.Set;

/**
 * This class contains a number of constants that are the keys for various
 * display preferences in CONSTELLATION's attribute editor.
 *
 * @author twinkle2_little
 */
public class AttributePreferenceKey {

    /*
     attribute hiding preference
     */
    public static final char SPLIT_CHAR = ';';
    protected static final char[] META_CHARS = {SPLIT_CHAR};
    protected static final Set<Character> SPLIT_CHAR_SET = new HashSet<>();
    public static final String HIDDEN_ATTRIBUTES = "hiddenAttribute";
    public static final String GRAPH_SHOW_ALL = "graphShowAll";
    public static final String NODE_SHOW_ALL = "nodeShowAll";
    public static final String TRANSACTION_SHOW_ALL = "transactionShowAll";
    public static final String PRIMARY_KEY_ATTRIBUTE_COLOR = "primaryKeyAttributeColor";
    public static final String CUSTOM_ATTRIBUTE_COLOR = "customAttributeColor";
    public static final String SCHEMA_ATTRIBUTE_COLOR = "schemaAttributeColor";
    public static final String HIDDEN_ATTRIBUTE_COLOR = "hiddenAttributeColor";
    protected static final Set<String> ATTRIBUTE_COLOR_PREFS = new HashSet<>();

    static {
        SPLIT_CHAR_SET.add(SPLIT_CHAR);
        ATTRIBUTE_COLOR_PREFS.add(PRIMARY_KEY_ATTRIBUTE_COLOR);
        ATTRIBUTE_COLOR_PREFS.add(CUSTOM_ATTRIBUTE_COLOR);
        ATTRIBUTE_COLOR_PREFS.add(SCHEMA_ATTRIBUTE_COLOR);
        ATTRIBUTE_COLOR_PREFS.add(HIDDEN_ATTRIBUTE_COLOR);
    }
}

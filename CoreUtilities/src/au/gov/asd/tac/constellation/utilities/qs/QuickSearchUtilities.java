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
package au.gov.asd.tac.constellation.utilities.qs;

/**
 * Netbeans has issues dealing with quick search results that contain:
 * angled brackets - greater than, less than - (it thinks they are html tags)
 * and round brackets - parentheses - (it thinks they denote a search category)
 * 
 * This class will modify the search results sent back to the Quick Search system
 * Interchanging the brackets with the equivalent unicode "full width" alternatives
 *
 * @author OrionsGuardian
 */
public class QuickSearchUtilities {

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
    
    // Substitution characters for angled brackets and round brackets, used to address a Netbeans issue
    private static final String LT_FULL = "\uff1c"; // <
    private static final String GT_FULL = "\uff1e"; // >
    private static final String OB_FULL = "\uff08"; // (
    private static final String CB_FULL = "\uff09"; // )
    
    private QuickSearchUtilities(){
        // Should not be instantiated. Should only use the static methods.
        throw new IllegalStateException("Utility Class");
    }
    
    public static String replaceBrackets(final String source) {
        return source.replace("<", LT_FULL).replace(">", GT_FULL).replace("(", OB_FULL).replace(")", CB_FULL);
    }

    public static String restoreBrackets(final String source) {
        return source.replace(LT_FULL, "<").replace(GT_FULL, ">").replace(OB_FULL, "(").replace(CB_FULL, ")");
    }

    public static String buildSubscriptFromID(final String idData) {
        final StringBuilder subscriptId = new StringBuilder();
        for (int i = 0; i < idData.length(); i++) {
            final char currentChar = idData.charAt(i);
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
                default:
                    break;
            }
        }
        return subscriptId.toString();
    }
    
    public static String buildIDFromSubscript(final String idSubscriptData) {
        final StringBuilder subscriptId = new StringBuilder();
        for (int i = 0; i < idSubscriptData.length(); i++) {
            final char currentChar = idSubscriptData.charAt(i);
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
                default:
                    break;
            }
        }
        return subscriptId.toString();
    }
    
}

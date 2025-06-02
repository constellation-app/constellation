/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.tableview.components;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Custom Comparator for the Data Strings in the Table View
 * This will sort strings numerically if they only contains numbers,
 * or do a substring numerical sort if the strings have a common prefix, but end in different numbers,
 * otherwise it will do a standard alphabetical sort on the strings.
 *
 * @author OrionsGuardian
 */
public class TableDataComparator implements Comparator<String>, Serializable{
    private static final long serialVersionUID = 1001;

    @Override
    public int compare(final String str0, final String str1) {
        if (str0 == null) {
            return -1;
        } else if (str1 == null) {
            return 1;
        }
        
        try {
            // if both strings are numeric we can do a numeric comparison
            final Double dbl0 = Double.valueOf(str0);
            final Double dbl1 = Double.valueOf(str1);
            return dbl0.compareTo(dbl1);
        } catch (final NumberFormatException nfe) {
            // when one or both of the strings are not numeric,
            // proceed to the next step in the comparator
        }
        // if both strings start with the same letter, we can strip the first character and sort on the remaining string,
        // which may be entirely numeric, in which case they can be sorted numerically
        if (str0.length() > 1 && str1.length() > 1 && str0.startsWith(str1.substring(0, 1))) {
            // this is a recursive call to the compare function, using a smaller substring in each call
            return compare(str0.substring(1), str1.substring(1));
        }
        // default functionality: do a standard string comparison
        return str0.compareTo(str1);
    }    
}

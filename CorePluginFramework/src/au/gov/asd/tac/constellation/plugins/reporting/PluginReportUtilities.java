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
package au.gov.asd.tac.constellation.plugins.reporting;

/**
 *
 * @author capricornunicorn123
 */
public class PluginReportUtilities {
    
    private PluginReportUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Generates a string containing an integer and the correct plurality of a proceeding noun. 
     * @param count
     * @param singularForm
     * @param pluralForm
     */
    public static final String toString(final int count, final String singularForm, final String pluralForm) {
        if (count == 1) {
            return count + " " + singularForm;
        } else {
            return count + " " + pluralForm;
        }
    }
    
    /**
     * Generates a string containing an integer and the correct plurality of the word Node. 
     * @param count
     */
    public static final String getNodeCountString(final int count){
        return PluginReportUtilities.toString(count, "node", "Nodes");
    }
    
    /**
     * Generates a string containing an integer and the correct plurality of the word Transaction. 
     * @param count
     */
    public static final String getTransactionCountString(final int count){
        return PluginReportUtilities.toString(count, "transaction", "transactions");
    }
    
    /**
     * Generates a string containing an integer and the correct plurality of the word Entity. 
     * @param count
     */
    public static final String getEntityCountString(final int count){
        return PluginReportUtilities.toString(count, "entity", "entities");
    }
    
    /**
     * Generates a string containing an integer and the correct plurality of the word File. 
     * @param count
     */
    public static final String getFileCountString(final int count){
        return PluginReportUtilities.toString(count, "file", "files");
    }
    
    /**
     * Generates a string containing an integer and the correct plurality of the word Attribute. 
     * @param count
     */
    public static final String getAttributeCountString(final int count){
        return PluginReportUtilities.toString(count, "attribute", "attributes");
    }
}

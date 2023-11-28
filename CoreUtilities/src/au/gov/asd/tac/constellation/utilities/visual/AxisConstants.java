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
package au.gov.asd.tac.constellation.utilities.visual;

/**
 * Class to help control axis information.
 * @author capricornunicorn123
 */
public enum AxisConstants {
    X_POSITIVE("X Axis", false), 
    Y_POSITIVE("Y Axis", false), 
    Z_POSITIVE("Z Axis", false), 
    X_NEGATIVE("-X Axis", true), 
    Y_NEGATIVE("-Y Axis", true), 
    Z_NEGATIVE("-Z Axis", true);
    
    private final String name;
    private final boolean negative;
    
    private AxisConstants(final String name, final boolean negative){
        this.name = name;
        this.negative = negative;
    }
    
    public static AxisConstants getReference(final String name){
        if (name != null){
            for (AxisConstants value : AxisConstants.values()){
                if (name.equals(value.toString())){
                    return value;
                }
            }
        }
        return null;
    }
    
    public boolean isNegative(){
        return negative;
    }
    
    @Override 
    public String toString(){
        return name;
    }
    
}

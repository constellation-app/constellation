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
package au.gov.asd.tac.constellation.utilities.visual;

import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;

/**
 * Class to help control axis information.
 * @author capricornunicorn123
 */
public enum AxisConstants {
    X_POSITIVE("X Axis", false, new Vector3f(1,0,0), new Vector3f(0,1,0)), 
    Y_POSITIVE("Y Axis", false, new Vector3f(0,1,0), new Vector3f(0,0,-1)), 
    Z_POSITIVE("Z Axis", false, new Vector3f(0,0,1), new Vector3f(0,1,0)), 
    X_NEGATIVE("-X Axis", true, new Vector3f(-1,0,0), new Vector3f(0,1,0)), 
    Y_NEGATIVE("-Y Axis", true, new Vector3f(0,-1,0), new Vector3f(0,0,1)), 
    Z_NEGATIVE("-Z Axis", true, new Vector3f(0,0,-1), new Vector3f(0,1,0));
    
    private final String name;
    private final boolean negative;
    private final Vector3f forward;
    private final Vector3f up;
    
    private AxisConstants(final String name, final boolean negative, final Vector3f forward, final Vector3f up) {
        this.name = name;
        this.negative = negative;
        this.forward = forward;        
        this.up = up;
    }
    
    public static AxisConstants getReference(final String name) {
        if (name != null) {
            for (final AxisConstants value : AxisConstants.values()) {
                if (name.equals(value.toString())) {
                    return value;
                }
            }
        }
        return null;
    }
    
    public boolean isNegative() {
        return negative;
    }
    
    public Vector3f getUp() {
        return new Vector3f(up);
    }
    
    public Vector3f getForward() {
        return new Vector3f(forward);
    }
    
    @Override 
    public String toString() {
        return name;
    }
    
}

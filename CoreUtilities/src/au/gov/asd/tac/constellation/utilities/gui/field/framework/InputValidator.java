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
package au.gov.asd.tac.constellation.utilities.gui.field.framework;

/**
 * An Interface for objects that was to validate extensions of {@link ConstellationInput}.
 * Implementation of this class can be registered to a {@link ConstellationInput} and will be 
 * consulted every time the value of the {@link ConstellationInput} is changed.
 * 
 * @author capricornunicorn123
 */
public interface InputValidator {
    
   public String validateString(final String s);
    
}

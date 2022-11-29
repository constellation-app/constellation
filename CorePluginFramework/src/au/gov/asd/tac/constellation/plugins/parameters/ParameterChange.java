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
package au.gov.asd.tac.constellation.plugins.parameters;

/**
 * A ParameterChange describes the different ways in which a parameter value can
 * change, either by user interaction or programmatic access.
 *
 * @author sirius
 */
public enum ParameterChange {

    /**
     * The actual value of the parameter has changed.
     */
    VALUE,
    /**
     * The parameter has changed categories.
     */
    CATEGORY,
    /**
     * The name of the parameter has changed.
     */
    NAME,
    /**
     * The description of the parameter has changed.
     */
    DESCRIPTION,
    /**
     * The icon of the parameter has changed.
     */
    ICON,
    /**
     * The error/valid state of the parameter has changed.
     */
    ERROR,
    /**
     * The parameter has changed from visible to invisible or vice-versa.
     */
    VISIBLE,
    /**
     * The parameter has changed from enabled to disabled or vice-versa.
     */
    ENABLED,
    /**
     * A custom property of the parameter has changed.
     */
    PROPERTY,
    // For when we don't want to propagate changes.
    // The action type (which doesn't have a value) technically doesn't change: it's just a button.
    // Therefore, even though it used a new object to fire its change event (and cause the controller action to be executed),
    // we don't want to propagate the change any further.
    // In particular, we don't want a plugin being turned on just because of a button press.
    NO_CHANGE
}

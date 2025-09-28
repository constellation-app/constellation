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
package au.gov.asd.tac.constellation.views.scripting;

/**
 * The provider for scripting actions to be used by the ScriptingTopComponent.
 *
 * @author algol
 */
public interface ScriptingAbstractAction {

    /**
     * The label to display on the action menu dropdown.
     *
     * @return The label to display on the action menu dropdown.
     */
    public String getLabel();

    /**
     * The action to perform.
     *
     * @param scriptingPane The {@link ScriptingViewPane} in which to perform
     * the action.
     */
    public void performAction(final ScriptingViewPane scriptingPane);
}

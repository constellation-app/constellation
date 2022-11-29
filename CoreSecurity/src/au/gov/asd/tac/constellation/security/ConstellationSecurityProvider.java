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
package au.gov.asd.tac.constellation.security;

import java.util.List;

/**
 * A ConstellationSecurityProvider is responsible for providing one or more
 * ConstellationSecurityContexts to the application.
 *
 * @author sirius
 */
public interface ConstellationSecurityProvider {

    /**
     * A name for this provider that is suitable for the user to see.
     *
     * @return The name of this security provider.
     */
    public String getName();

    /**
     * A description of this provider that is suitable for the user to see.
     *
     * @return The description of this provider.
     */
    public String getDescription();

    /**
     * A list of ConstellationSecurityContexts that this provider can supply.
     * Contexts are used from the top of the list down.
     *
     * @return A list of ConstellationSecurityContexts.
     */
    public List<ConstellationSecurityContext> getContexts();

    /**
     * The current status of this provider. The status should be updated after
     * getContexts() has been called to describe the outcome of that operation.
     *
     * @return The current status of this provider.
     */
    public String getStatus();
}

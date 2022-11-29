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

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * A ConstellationSecurityContext defines how security should be handled by the
 * application.
 *
 * @author sirius
 */
public interface ConstellationSecurityContext {

    /**
     * Returns the ConstellationSecurityProvider that created this context.
     *
     * @return the ConstellationSecurityProvider that created this context.
     */
    public ConstellationSecurityProvider getProvider();

    /**
     * Returns a name for this context that is suitable for the user to see.
     *
     * @return a name for this context that is suitable for the user to see.
     */
    public String getName();

    /**
     * Returns a description of this context that is suitable for the user to
     * see.
     *
     * @return a description of this context that is suitable for the user to
     * see.
     */
    public String getDescription();

    /**
     * Returns the distinguished name to use while using this context.
     *
     * @return the distinguished name to use while using this context.
     */
    public String getDistinguishedName();

    /**
     * Returns the SSL context to use while this ConstellationSecurityContext is
     * current.
     *
     * @return the SSL context to use while this ConstellationSecurityContext is
     * current.
     */
    public SSLContext getSSLContext();

    public TrustManager[] getTrustManagers();

    public KeyManager[] getKeyManagers();

}

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
package au.gov.asd.tac.constellation.security.proxy;

/**
 * Preference keys for proxy settings.
 *
 * @author algol
 */
class ProxyPreferenceKeys {

    protected static final String USE_DEFAULTS = "proxy.use_defaults";
    protected static final boolean USE_DEFAULTS_DEFAULT = true;
    
    protected static final String DEFAULT = "proxy.default";
    protected static final String DEFAULT_DEFAULT = ConstellationHttpProxy.getDefault().getDefaultProxyString();
    
    protected static final String ADDITIONAL = "proxy.proxies";
    protected static final String ADDITIONAL_DEFAULT = ConstellationHttpProxy.getDefault().getAdditionalProxiesString();
    
    protected static final String BYPASS = "proxy.local_hosts";
    protected static final String BYPASS_DEFAULT = ConstellationHttpProxy.getDefault().getBypassProxyHostsString();
}

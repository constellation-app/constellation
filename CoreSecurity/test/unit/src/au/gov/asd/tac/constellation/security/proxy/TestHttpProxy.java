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

import java.util.Arrays;
import java.util.List;
import javafx.util.Pair;

/**
 * An implementation of ConstellationHttpProxy to be used for testing purposes
 *
 * @author antares
 */
public class TestHttpProxy implements ConstellationHttpProxy {

    @Override
    public Pair<String, Integer> getDefaultProxy() {
        return new Pair<>("my-proxy.default", 8080);
    }

    @Override
    public List<Pair<String, Pair<String, Integer>>> getAdditionalProxies() {
        final String additionalProxies = ".madeup.site = my.proxy.additional:8080";
        return ProxyUtilities.parseProxies(additionalProxies, true);
    }

    @Override
    public List<String> getBypassProxyHosts() {
        return Arrays.asList(".test");
    }  
}

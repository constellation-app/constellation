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
package au.gov.asd.tac.constellation.security.proxy;

import java.util.Collections;
import java.util.List;
import javafx.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of ConstellationHttpProxy specifying no proxy.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = ConstellationHttpProxy.class, position = Integer.MAX_VALUE)
public class NoProxyDefaults implements ConstellationHttpProxy {

    @Override
    public Pair<String, Integer> getDefaultProxy() {
        return NO_PROXY;
    }

    @Override
    public List<Pair<String, Pair<String, Integer>>> getAdditionalProxies() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getBypassProxyHosts() {
        return Collections.emptyList();
    }
}

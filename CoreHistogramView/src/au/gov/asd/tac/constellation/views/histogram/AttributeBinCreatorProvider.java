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
package au.gov.asd.tac.constellation.views.histogram;

import org.openide.util.Lookup;

/**
 * An AttributeBinCreatorProvider provides a mechanism for new BinCreators to be
 * registered with the histogram.
 *
 * @author cygnus_x-1
 */
public abstract class AttributeBinCreatorProvider {

    private static final boolean IS_INIT = false;

    public static synchronized void init() {
        if (!IS_INIT) {
            for (AttributeBinCreatorProvider attributeBinCreatorProvider : Lookup.getDefault().lookupAll(AttributeBinCreatorProvider.class)) {
                attributeBinCreatorProvider.register();
            }
        }
    }

    public abstract void register();

}

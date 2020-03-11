/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.utilities.banner;

import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Banner;
import java.util.ArrayList;
import org.openide.util.Lookup;

/**
 * Abstract class for the getting a list of predefined banners
 *
 * @author altair
 */
public abstract class PredefinedBanners {

    /**
     * Return a list of predefined banners.
     *
     * @return A list of predefined banners.
     */
    public abstract ArrayList<Banner> getBanners();

    /**
     * return the current instance of the class
     *
     * @return a class instance
     */
    public static PredefinedBanners getDefault() {
        PredefinedBanners value = Lookup.getDefault().lookup(PredefinedBanners.class);
        if (value == null) {
            value = new DefaultPredefinedBanners();
        }
        return value;
    }
}

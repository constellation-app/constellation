/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview2;

import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author altair1673
 */
@TopComponent.Description(
        preferredID = "MapViewTopComponent2",
        iconBase = "au/gov/asd/tac/constellation/views/mapview/resources/treasure-map.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Views", position = 4000),
    @ActionReference(path = "Shortcuts", name = "CA-M")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MapView2Action",
        preferredID = "MapViewTopComponent2"
)
@NbBundle.Messages({
    "CTL_MapView2Action=Map View",
    "CTL_MapViewTopComponent2=Map View",
    "HINT_MapViewTopComponent2=Map View"
})
public final class MapViewTopComponent extends JavaFxTopComponent<MapViewPane> {

    private final MapViewPane mapViewPane;

    public MapViewTopComponent() {
        setName(Bundle.CTL_MapViewTopComponent2());
        setToolTipText(Bundle.HINT_MapViewTopComponent2());

        mapViewPane = new MapViewPane(this);
        initContent();

    }

    @Override
    protected String createStyle() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return "";
    }

    @Override
    protected MapViewPane createContent() {
        return mapViewPane;
    }

}

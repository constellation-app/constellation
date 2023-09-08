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
package au.gov.asd.tac.constellation.views.schemaview;

import au.gov.asd.tac.constellation.utilities.threadpool.ConstellationGlobalThreadPool;
import au.gov.asd.tac.constellation.views.schemaview.providers.SchemaViewNodeProvider;
import java.util.Collection;
import java.util.concurrent.Executor;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.openide.util.Lookup;

/**
 *
 * @author cygnus_x-1
 */
public class SchemaViewPane extends BorderPane {

    private Collection<? extends SchemaViewNodeProvider> schemaViewProviders;
    private final TabPane schemaViewTabPane;

    public SchemaViewPane() {
        schemaViewProviders = null;

        schemaViewTabPane = new TabPane();
        schemaViewTabPane.setStyle("-fx-background-color: #333333;");
        schemaViewTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        setCenter(schemaViewTabPane);
    }

    public void populate() {
        Platform.runLater(() -> {
            final Executor pool = ConstellationGlobalThreadPool.getThreadPool().getFixedThreadPool();
            schemaViewProviders = Lookup.getDefault().lookupAll(SchemaViewNodeProvider.class);
            schemaViewProviders.stream().forEach(provider -> {
                final Tab tab = new Tab(provider.getText());
                schemaViewTabPane.getTabs().add(tab);

                pool.execute(() -> provider.setContent(tab));
            });
        });
    }

    public void clear() {
        Platform.runLater(() -> {
            schemaViewTabPane.getTabs().clear();

            if (schemaViewProviders != null) {
                schemaViewProviders.stream().forEach(provider -> provider.discardNode());
                schemaViewProviders = null;
            }
        });
    }
}

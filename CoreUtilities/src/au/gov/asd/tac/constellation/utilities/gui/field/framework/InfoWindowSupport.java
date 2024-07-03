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
package au.gov.asd.tac.constellation.utilities.gui.field.framework;

import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInput;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldListener;
import java.io.Serializable;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * An Interface for {@link ConstellationInput} extensions.
 * Provides required functionality to build an info window for insertion to an input extenesion.
 * 
 * @author capricornunicorn123
 */
public interface InfoWindowSupport{
    
    public abstract InfoWindow getInfoWindow();
    
    public abstract class InfoWindow extends StackPane implements ConstellationInputFieldListener<Serializable> {
        public final ConstellationInput parent;

        public InfoWindow(ConstellationInput parent){
            this.parent = parent;
            setPadding(new Insets(0,6,0,0));
            setAlignment(Pos.CENTER);
            Platform.runLater(()->{
                refreshWindow();
            });
            parent.addListener(this);
        }
        
        public void setWindowContents(Node content){
            this.getChildren().add(content);
        }

        protected abstract void refreshWindow();

        @Override
        public void changed(Serializable newValue) {
            refreshWindow();
        }
    }
}

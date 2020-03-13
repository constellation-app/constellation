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
package au.gov.asd.tac.constellation.utilities.tooltip;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.Pane;
import org.openide.util.Lookup;

/**
 *
 * @author sirius
 */
public abstract class TooltipProvider {

    private static List<TooltipProvider> TOOLTIP_PROVIDERS = null;

    /**
     * Requests a TooltipDefinition from all registered TooltipProviders and
     * returns a list of any results.
     *
     * @param content the content the mouse is currently over.
     * @param activePosition the position of the character that the mouse is
     * currently over.
     * @return a list of any TooltipDefinitions discovered.
     */
    public static List<TooltipDefinition> getTooltips(String content, int activePosition) {
        init();
        List<TooltipDefinition> definitions = new ArrayList<>();
        for (TooltipProvider provider : TOOLTIP_PROVIDERS) {
            TooltipDefinition definition = provider.createTooltip(content, activePosition);
            if (definition != null) {
                definitions.add(definition);
            }
        }
        return definitions;
    }

    public static List<TooltipDefinition> getAllTooltips(String content) {
        init();
        List<TooltipDefinition> definitions = new ArrayList<>();
        for (TooltipProvider provider : TOOLTIP_PROVIDERS) {
            TooltipDefinition definition = provider.createTooltips(content);
            if (definition != null) {
                definitions.add(definition);
            }
        }
        return definitions;
    }

    private static synchronized void init() {
        if (TOOLTIP_PROVIDERS == null) {
            TOOLTIP_PROVIDERS = new ArrayList<>(Lookup.getDefault().lookupAll(TooltipProvider.class));
        }
    }

    public static class TooltipDefinition {

        private final Pane pane;
        private int start = -1, finish = -1;

        public TooltipDefinition(Pane pane) {
            this.pane = pane;
        }

        public Pane getNode() {
            return pane;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getFinish() {
            return finish;
        }

        public void setFinish(int finish) {
            this.finish = finish;
        }
    }

    public TooltipDefinition createTooltips(String content) {
        return null;
    }

    public TooltipDefinition createTooltip(String content, int activePosition) {
        return null;
    }
}

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

    private static List<TooltipProvider> tooltipProviders = null;

    /**
     * Requests a TooltipDefinition from all registered TooltipProviders and
     * returns a list of any results.
     *
     * @param content the content the mouse is currently over.
     * @param activePosition the position of the character that the mouse is
     * currently over.
     * @return a list of any TooltipDefinitions discovered.
     */
    public static List<TooltipDefinition> getTooltips(final String content, final int activePosition) {
        init();
        final List<TooltipDefinition> definitions = new ArrayList<>();
        for (final TooltipProvider provider : getTooltipProviders()) {
            final TooltipDefinition definition = provider.createTooltip(content, activePosition);
            if (definition != null) {
                definitions.add(definition);
            }
        }
        return definitions;
    }

    public static List<TooltipDefinition> getAllTooltips(final String content) {
        init();
        final List<TooltipDefinition> definitions = new ArrayList<>();
        for (final TooltipProvider provider : getTooltipProviders()) {
            final TooltipDefinition definition = provider.createTooltips(content);
            if (definition != null) {
                definitions.add(definition);
            }
        }
        return definitions;
    }

    protected static synchronized void init() {
        if (tooltipProviders == null) {
            tooltipProviders = new ArrayList<>(Lookup.getDefault().lookupAll(TooltipProvider.class));
        }
    }
    
    protected static List<TooltipProvider> getTooltipProviders(){
        return tooltipProviders;
    }
    
    public abstract TooltipDefinition createTooltips(final String content);

    public abstract TooltipDefinition createTooltip(final String content, final int activePosition);

    public static class TooltipDefinition {

        private final Pane pane;
        private int start = -1;
        private int finish = -1;

        public TooltipDefinition(final Pane pane) {
            this.pane = pane;
        }

        public Pane getNode() {
            return pane;
        }

        public int getStart() {
            return start;
        }

        public void setStart(final int start) {
            this.start = start;
        }

        public int getFinish() {
            return finish;
        }

        public void setFinish(final int finish) {
            this.finish = finish;
        }
    }
}

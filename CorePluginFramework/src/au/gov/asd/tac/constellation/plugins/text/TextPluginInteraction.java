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
package au.gov.asd.tac.constellation.plugins.text;

import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.logging.Logger;

/**
 * A text implementation of {@link PluginInteraction}
 * <p>
 * This would be useful in unit tests
 *
 * @author arcturus
 */
public class TextPluginInteraction implements PluginInteraction {

    private static final Logger LOGGER = Logger.getLogger(TextPluginInteraction.class.getName());

    private String currentMessage;

    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public void setBusy(String graphId, boolean busy) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCurrentMessage() {
        return currentMessage;
    }

    @Override
    public void setProgress(int currentStep, int totalSteps, String message, boolean cancellable) throws InterruptedException {
        currentMessage = message;
        LOGGER.info(String.format("currentStep=%d totalSteps=%d message=%s", currentStep, totalSteps, message));
    }

    @Override
    public void notify(PluginNotificationLevel level, String message) {
        currentMessage = message;
        LOGGER.info(String.format("level=%s message=%s", level, message));
    }

    @Override
    public boolean confirm(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean prompt(String promptName, PluginParameters parameters) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

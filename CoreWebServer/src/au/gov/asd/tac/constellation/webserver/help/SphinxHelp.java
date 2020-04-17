/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.webserver.help;

import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.javahelp.Help;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service = Help.class, position = 1)
public class SphinxHelp extends Help {
    private static final Logger LOGGER = Logger.getLogger(SphinxHelp.class.getName());

    @Override
    public Boolean isValidID(final String id, final boolean force) {
        LOGGER.info(String.format("isValidId %s %s", id, force));

        return true;
    }

    @Override
    public void showHelp(final HelpCtx helpCtx, final boolean showmaster) {
        LOGGER.info(String.format("showHelp %s %s", helpCtx, showmaster));

        // Here, we examine the ID and if it starts with "PREFIX", we find our implementation
        // of HelpCtx.Displayer using the Lookup API and use it to display our help.
        if (true) {//helpCtx.getHelpID().startsWith("PREFIX")) {

            final HelpCtx.Displayer displayer =
                    Lookup.getDefault().lookup(HelpCtx.Displayer.class);

            displayer.display(helpCtx);
        }

        // If we don't know what do with the ID, we find all the implementations of Help and
        // pass the HelpCtx along to the first one we find that isn't this class.
        else {
            final Collection<? extends Help> helps =
                    Lookup.getDefault().lookupAll(Help.class);

            for(final Help help : helps) {
                LOGGER.info(String.format("Possible help: %s", help));
            }

            for(final Help help : helps) {
                if (help != this) {
                    help.showHelp(helpCtx, showmaster);

                    break;
                }
            }
        }
    }

    @Override
    public void addChangeListener(final ChangeListener cl) {
        LOGGER.info(String.format("addChangeListener %s", cl));
    }

    @Override
    public void removeChangeListener(final ChangeListener cl) {
        LOGGER.info(String.format("removeChangeListener %s", cl));
    }

}

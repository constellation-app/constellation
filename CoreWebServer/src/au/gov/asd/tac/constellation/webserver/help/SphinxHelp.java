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
//    private static final Logger LOGGER = Logger.getLogger(SphinxHelp.class.getName());

    @Override
    public Boolean isValidID(final String id, final boolean force) {

        return true;
    }

    @Override
    public void showHelp(final HelpCtx helpCtx, final boolean showmaster) {
        final HelpCtx.Displayer displayer = Lookup.getDefault().lookup(HelpCtx.Displayer.class);
        displayer.display(helpCtx);
    }

    @Override
    public void addChangeListener(final ChangeListener cl) {
    }

    @Override
    public void removeChangeListener(final ChangeListener cl) {
    }
}

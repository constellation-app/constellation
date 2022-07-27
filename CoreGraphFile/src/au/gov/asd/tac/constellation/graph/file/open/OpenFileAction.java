/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package au.gov.asd.tac.constellation.graph.file.open;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.file.GraphFilePluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 * Action which allows user to open a file.
 *
 * @author sol695510
 */
@ActionRegistration(
        displayName = "#LBL_openFile",
        iconBase = "au/gov/asd/tac/constellation/graph/file/open/resources/openFile.png",
        iconInMenu = false)
@ActionID(category = "File", id = "au.gov.asd.tac.constellation.graph.file.open.OpenFileAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 300),
    @ActionReference(path = "Shortcuts", name = "C-O")})
public class OpenFileAction implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(OpenFileAction.class.getName());

    /**
     * {@inheritDoc} Displays a file chooser dialog and opens the selected
     * files.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final StoreGraph sg = new StoreGraph();

        try {
            PluginExecution.withPlugin(GraphFilePluginRegistry.OPEN_FILE).executeNow(sg);
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            Thread.currentThread().interrupt();
        } catch (final PluginException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }
}

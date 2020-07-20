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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

 /*
 * Po-Ting Wu: Copy from studio-plugin/src/org/netbeans/modules/j2ee/sun/ide/j2ee/LogViewerSupport.java
 */
package au.gov.asd.tac.constellation.utilities.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Connects the output stream of a file to the IDE output window.
 *
 * @author ludo
 */
public class LogViewerSupport implements Runnable {

    private static final RequestProcessor RP = new RequestProcessor(LogViewerSupport.class);
    boolean shouldStop = false;
    FileInputStream filestream = null;
    BufferedReader ins;
    InputOutput io;
    File fileName;
    String ioName;
    int lines;
    Ring ring;
    private final RequestProcessor.Task task = RP.create(this);

    /**
     * Connects a given process to the output window. Returns immediately, but
     * threads are started that copy streams of the process to/from the output
     * window.
     *
     * @param fileName
     * @param ioName name of the output window tab to use
     */
    public LogViewerSupport(final File fileName, final String ioName) {

        this.fileName = fileName;
        this.ioName = ioName;
    }

    private void init() {
        final int LINES = 2000;
        final int OLD_LINES = 2000;
        ring = new Ring(OLD_LINES);
        String line;

        // Read the log file without
        // displaying everything
        try {
            while ((line = ins.readLine()) != null) {
                ring.add(line);
            } // end of while ((line = ins.readLine()) != null)
        } catch (IOException e) {
            Logger.getLogger(LogViewerSupport.class.getName()).log(Level.INFO, null, e);
        } // end of try-catch // end of try-catch

        // Now show the last OLD_LINES
        lines = ring.output();
        ring.setMaxCount(LINES);
    }

    public void run() {
        final int MAX_LINES = 10000;
        String line;

        shouldStop = io.isClosed();

        if (!shouldStop) {
            try {
                if (lines >= MAX_LINES) {
                    io.getOut().reset();
                    lines = ring.output();
                } // end of if (lines >= MAX_LINES)

                while ((line = ins.readLine()) != null) {
                    if ((line = ring.add(line)) != null) {
                        io.getOut().println(line);
                        lines++;
                    } // end of if ((line = ring.add(line)) != null)
                }

            } catch (IOException e) {
                Logger.getLogger(LogViewerSupport.class.getName()).log(Level.INFO, null, e);
            }
            task.schedule(1000);
        } else {
            ///System.out.println("end of infinite loop for log viewer\n\n\n\n");
            stopUpdatingLogViewer();
        }
    }

    /* display the log viewer dialog
     *
     **/
    public void showLogViewer() throws IOException {
        shouldStop = false;
        io = IOProvider.getDefault().getIO(ioName, false);
        io.getOut().reset();
        io.select();
        filestream = new FileInputStream(fileName);
        ins = new BufferedReader(new InputStreamReader(filestream, StandardCharsets.UTF_8.name()));
        RP.post(() -> {
            init();
            task.schedule(0);
        });
    }

    /* stop to update  the log viewer dialog
     *
     **/
    public void stopUpdatingLogViewer() {
        try {
            ins.close();
            filestream.close();
            io.closeInputOutput();
            io.setOutputVisible(false);
        } catch (IOException e) {
            Logger.getLogger(LogViewerSupport.class.getName()).log(Level.INFO, null, e);
        }
    }

    private class Ring {

        private int maxCount;
        private int count;
        private LinkedList<String> anchor;

        public Ring(int max) {
            maxCount = max;
            count = 0;
            anchor = new LinkedList<>();
        }

        public String add(String line) {
            if (StringUtils.isBlank(line)) { // NOI18N
                return null;
            } // end of if (line == null || line.equals(""))

            while (count >= maxCount) {
                anchor.removeFirst();
                count--;
            } // end of while (count >= maxCount)

            anchor.addLast(line);
            count++;

            return line;
        }

        public void setMaxCount(int newMax) {
            maxCount = newMax;
        }

        public int output() {
            int i = 0;
            for (String s : anchor) {
                io.getOut().println(s);
                i++;
            }

            return i;
        }

        public void reset() {
            anchor = new LinkedList<>();
        }
    }
}

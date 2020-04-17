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

import au.gov.asd.tac.constellation.webserver.WebServer;
import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service=HelpCtx.Displayer.class, position=9999)
public class SphinxHelpDisplayer implements HelpCtx.Displayer {
    private static final Logger LOGGER = Logger.getLogger(SphinxHelpDisplayer.class.getName());

    // The help zip must contain a text file mapping helpIds to page paths.
    //
    private static final String HELP_MAP = "/help_map.txt";

    // Big temporary hack just to get it working.
    //
    private static final String HELP_ZIP = "D:/tmp/help.zip";

    // The mapping of helpIds to page paths.
    //
    private static Map<String, String> helpMap_;

    private static synchronized Map<String, String> getHelpMap() {
        if(helpMap_==null) {
            try {
                final Path p = Paths.get(HELP_ZIP);
                final FileSystem fs = FileSystems.newFileSystem(p, null);

                final Path path = fs.getPath(HELP_MAP);
                final List<String> lines = Files.readAllLines(path);
                helpMap_ = new HashMap<>();
                lines.forEach(line -> {
                    final int ix = line.indexOf(',');
                    final String helpId = line.substring(0, ix).strip();
                    if(!helpId.isEmpty() && !helpId.startsWith("#")) {
                        final String helpPath = line.substring(ix+1);
                        helpMap_.put(helpId, helpPath);
                    }
                });
            } catch(final IOException ex) {
                Exceptions.printStackTrace(ex);

                // If we couldn't read the file the first time,
                // it won't magically work the next time, so stop trying.
                //
                helpMap_ = Map.of();
            }
        }

        return helpMap_;
    }

    public static void copyFile(final String filepath, final OutputStream out) throws IOException {
        final Path p = Paths.get(HELP_ZIP);
        final FileSystem fs = FileSystems.newFileSystem(p, null);

        final Path path = fs.getPath(filepath);
        Files.copy(path, out);
    }

    @Override
    public boolean display(final HelpCtx helpCtx) {
        final String helpId = helpCtx.getHelpID();
        LOGGER.info(String.format("display '%s'", helpId));

        final int port = WebServer.start();

        final Map<String, String> helpMap = getHelpMap();
        final String part = helpMap.containsKey(helpId) ? helpMap.get(helpId) : "index";
        final String url = String.format("http://localhost:%d/help/html/%s.html", port, part);
        LOGGER.info(String.format("url %s", url));

//        SwingUtilities.invokeLater(() -> {
//            JOptionPane.showMessageDialog(
//                    WindowManager.getDefault().getMainWindow(),
//                    helpCtx.getHelpID()+"\n"+url);
//        });

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));

                    return true;
            }
        } catch(final URISyntaxException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }

            return false;
    }
}

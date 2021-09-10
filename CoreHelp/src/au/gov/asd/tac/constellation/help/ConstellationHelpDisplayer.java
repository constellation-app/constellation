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
package au.gov.asd.tac.constellation.help;

import au.gov.asd.tac.constellation.help.utilities.Generator;
import au.gov.asd.tac.constellation.help.utilities.HelpMapper;
import com.github.rjeschke.txtmark.Processor;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service = HelpCtx.Displayer.class, position = 9999)
public class ConstellationHelpDisplayer implements HelpCtx.Displayer {

    private static final Logger LOGGER = Logger.getLogger(ConstellationHelpDisplayer.class.getName());

    // This system property controls where the help is displayed from.
    //
    // * If it is undefined, help is read from getResourceAsStream().
    // * If it is defined as a (zip) file, help is read from that zip file.
    // * If it is defined as a web URL, help is read from that URL.
    //
    // Define it in this module's project.properties file or another
    // appropriate place as (for example):
    //
    // run.args.extra="-J-Dconstellation.help=D:/tmp/help.zip"
    // run.args.extra="-J-Dconstellation.help=http://localhost:8000"
    // run.args.extra="-J-Dconstellation.help=https://example.com"
    //
    // Set the value to the following github url to have the pages served from readthedocs.io
    //
    // run.args.extra="-J-Dconstellation.help=https://github.com/constellation-app/constellation/raw/master/docs"
    //
    private static final String CONSTELLATION_HELP = "constellation.help";

    // The help zip must contain a text file mapping helpIds to page paths.
    //
    private static final String HELP_MAP = "help_map.txt";

    // The name of the internal help.zip resource.
    //
    private static final String HELP_ZIP = "help.zip";

    // Maximum size of buffer for reading help files.
    //
    private static final int BUFSIZ = 10 * 1024 * 1024;

    // The mapping of helpIds to page paths.
    //
    private static Map<String, String> helpMap;

    // A special case to use the readthedocs.io website if the HELP_MAP file is
    // in the official GitHub repository
    //
    private static final String OFFICIAL_GITHUB_REPOSITORY = "https://github.com/constellation-app/constellation";
    private static final String READ_THE_DOCS = "https://constellation.readthedocs.io/en/latest/%s";

    protected static int currentPort = 0;

    public static void copy(final String filepath, final OutputStream out) throws IOException {
        final Path path = Paths.get(filepath.substring(3));
        final InputStream input = new FileInputStream(path.toString());

        // only add the html path when the file isnt a css file
        if (filepath.contains(".css") || filepath.contains(".js") || filepath.contains(".png") || filepath.contains(".jpg")) {
            out.write(input.readAllBytes());
            return;
        }
        final String startCol = " <div class='row'> <div class='col-4 col-sm-3'>";
        final String endFirstCol = "</div> <div class='col-8 col-sm-9'>";
        final String endCol = "</div> </div> </div>";
        final String sep = File.separator;
        final Path tocFilePath = Paths.get(Generator.baseDirectory + sep + Generator.tocDirectory);

        // TODO: Check for issues with the ../ relative paths in css
        // If the filepath is the toc then don't append the toc again when outputted
        if (filepath.contains("toc.md")) {
            final File file = new File(Generator.baseDirectory + sep + "constellation/bootstrap/css/bootstrap.css");
            final URL fileUrl = file.toURI().toURL();
            final String css = "<link href=\"\\" + fileUrl.toString() + "\" rel='stylesheet'></link>";
            final String jquery = "<script src=\"https://code.jquery.com/jquery-3.4.1.slim.min.js\" integrity=\"sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n\" crossorigin=\"anonymous\"></script>";
            final String popper = "<script src=\"https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js\" integrity=\"sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo\" crossorigin=\"anonymous\"></script>";

            final File jsFile = new File(Generator.baseDirectory + sep + "constellation/bootstrap/js/bootstrap.js");
            final URL jsFileUrl = jsFile.toURI().toURL();
            final String boostrapjs = "<script type=\"text/javascript\" src=\"\\" + jsFileUrl + "\" ></script>";

            final String html = css + "\n" + jquery + "\n" + popper + "\n" + boostrapjs + "\n" + Processor.process(input);
            out.write(html.getBytes());
        } else {
            final File file = new File(Generator.baseDirectory + sep + "constellation/bootstrap/css/bootstrap.css");
            final URL fileUrl = file.toURI().toURL();
            final String css = "<link href=\"\\" + fileUrl.toString() + "\" rel='stylesheet'></link>";
            final String jquery = "<script src=\"https://code.jquery.com/jquery-3.4.1.slim.min.js\" integrity=\"sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n\" crossorigin=\"anonymous\"></script>";
            final String popper = "<script src=\"https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js\" integrity=\"sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo\" crossorigin=\"anonymous\"></script>";

            final File jsFile = new File(Generator.baseDirectory + sep + "constellation/bootstrap/js/bootstrap.js");
            final URL jsFileUrl = jsFile.toURI().toURL();
            final String boostrapjs = "<script type=\"text/javascript\" src=\"\\" + jsFileUrl + "\" ></script>";
            final InputStream tocInput = new FileInputStream(tocFilePath.toFile());
            final String html = css + "\n" + jquery + "\n" + popper + "\n" + boostrapjs + "\n" + startCol + Processor.process(tocInput) + endFirstCol + Processor.process(input) + endCol;
            out.write(html.getBytes());
        }
    }

    @Override
    public boolean display(final HelpCtx helpCtx) {
        final String helpId = helpCtx.getHelpID();
        LOGGER.log(Level.INFO, "display help for: {0}}", helpId);

        // Given the helpId, get the corresponding help page path.
        // If it doesn't exist (maybe because someone forgot to put the helpId
        // in their .rst file), go to the root page.
        final String sep = File.separator;

        // Switched base help page to About Constellation
        //String helpTOCPath = "constellation" + sep + "toc.md";
        String helpTOCPath = "constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/about-constellation.md";

        // use the requested help file, or the table of contents if it doesnt exist
        final String helpLink = StringUtils.isNotEmpty(HelpMapper.getHelpAddress(helpId)) ? HelpMapper.getHelpAddress(helpId).substring(2) : helpTOCPath;

        if (!helpLink.isEmpty()) {
            try {
                final File file = new File(Generator.baseDirectory + sep + helpLink);
                final URL fileUrl = file.toURI().toURL();
                currentPort = HelpWebServer.start();
                String url = String.format("http://localhost:%d/%s", currentPort, fileUrl);

                /* if (helpSource == null || !helpSource.startsWith("http")) {
                // The help source is an internal zipped resource or an actual zip file,
                // therefore we need to invoke the internal web server's help servlet,
                // so insert /help into the path. The servlet will call copy() to get
                // the file from the resource/file.
                //

                } */ /* else if (helpSource.startsWith(OFFICIAL_GITHUB_REPOSITORY)) {
                // If the helpSource points to github contellation-app/constellation then
                // we are going to use read the docs to serve the help pages
                // url = String.format(READ_THE_DOCS, part);
                } else {
                // The help source is an external web server, so we just
                // assemble the URL and disavow all knowledge.
                //
                // url = String.format("%s/html/%s", helpSource, part);
                } */

                LOGGER.log(Level.INFO, "help url {0}", url);
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    // Run in a different thread, not the JavaFX thread
                    new Thread(() -> {
                        Thread.currentThread().setName("Browse Help");
                        try {
                            Desktop.getDesktop().browse(new URI(url));
                        } catch (URISyntaxException | IOException ex) {
                            LOGGER.log(Level.SEVERE, "Tried to browse a url.", ex);
                            final String msg = "Unable to browse to that location.";
                            /* NotificationDisplayer.getDefault().notify("Help displayer",
                            UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.RED.getJavaColor()),
                            msg,
                            null
                            ); */
                        }
                    }).start();

                    return true;
                }
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            final String msg = "Help not available; see logs for the reason.";
            /* NotificationDisplayer.getDefault().notify("Help displayer",
                    UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.RED.getJavaColor()),
                    msg,
                    null
            ); */
        }

        return false;
    }

    /**
     * Use a lookup to get all of the help pages
     *
     * @return help pages
     */
    public List<String> getHelpPages() {
        final HelpPageProvider helpFiles = Lookup.getDefault().lookup(HelpPageProvider.class);

        List<String> pages = helpFiles.getHelpPages();
        return pages;
    }
}

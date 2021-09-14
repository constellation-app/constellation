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

import au.gov.asd.tac.constellation.help.preferences.HelpPreferenceKeys;
import au.gov.asd.tac.constellation.help.utilities.Generator;
import au.gov.asd.tac.constellation.help.utilities.HelpMapper;
import com.github.rjeschke.txtmark.Processor;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
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
    // A special case to use the readthedocs.io website if the HELP_MAP file is
    // in the official GitHub repository
    //
    private static final String OFFICIAL_GITHUB_REPOSITORY = "https://github.com/constellation-app/constellation";
    private static final String OFFICIAL_CONSTELLATION_WEBSITE = "https://www.constellation-app.com/";
    private static final String READ_THE_DOCS = "https://constellation.readthedocs.io/en/latest/%s";

    protected static int currentPort = 0;

    public static void copy(final String filePath, final OutputStream out) throws IOException {
        final String sep = File.separator;
        final InputStream input = getInputStream(filePath.substring(3));
        final InputStream tocInput = getInputStream(Generator.baseDirectory + sep + Generator.tocDirectory);

        if (input == null || tocInput == null) {
            // files could not be found, don't progress.
            return;
        }

        // avoid parsing utility files into html
        if (filePath.contains(".css") || filePath.contains(".js") || filePath.contains(".png") || filePath.contains(".jpg")) {
            out.write(input.readAllBytes());
            return;
        }

        // Generate html for output
        final String html = generateHTMLOutput(tocInput, input);

        out.write(html.getBytes());
    }

    private static InputStream getInputStream(final String filePath) throws FileNotFoundException {
        final Path path = Paths.get(filePath);
        return new FileInputStream(path.toString());
    }

    private static String generateHTMLOutput(final InputStream tocInput, final InputStream pageInput) throws MalformedURLException, IOException {
        final StringBuilder html = new StringBuilder();

        // HTML elements
        final String startRowDiv = "<div class='row'>";
        final String endDiv = "</div>";
        final String startColDiv = "<div class='col-4 col-sm-3'>";
        final String startInnerColDiv = "<div class='col-8 col-sm-9'>";

        final String sep = File.separator;

        final File cssFile = new File(Generator.baseDirectory + sep + "constellation/bootstrap/assets/css/app.css");
        final URL cssURL = cssFile.toURI().toURL();
        final String css = String.format("<link href=\"\\%s\" rel='stylesheet'></link>", cssURL.toString());

        final File noScriptCss = new File(Generator.baseDirectory + sep + "constellation/bootstrap/assets/css/noscript.css");
        final URL noScriptURL = noScriptCss.toURI().toURL();
        final String noScript = String.format("<link href=\"\\%s\" rel='stylesheet'></link>", noScriptURL.toString());

        final File bootstrapCSS = new File(Generator.baseDirectory + sep + "constellation/bootstrap/css/bootstrap.css");
        final URL bootstrapCSSURL = bootstrapCSS.toURI().toURL();
        final String cssBootstrap = String.format("<link href=\"\\%s\" rel='stylesheet'></link>", bootstrapCSSURL.toString());

        final File jqueryFile = new File(Generator.baseDirectory + sep + "constellation/bootstrap/assets/js/jquery.min.js");
        final URL jqueryURL = jqueryFile.toURI().toURL();
        final String jquery = String.format("<script src=\"\\%s\" ></script>", jqueryURL.toString());

        final File jqueryDropotronFile = new File(Generator.baseDirectory + sep + "constellation/bootstrap/assets/js/jquery.dropotron.min.js");
        final URL jqueryDropotronURL = jqueryDropotronFile.toURI().toURL();
        final String dropotron = String.format("<script type=\"text/javascript\" src=\"\\%s\" ></script>", jqueryDropotronURL.toString());

        final File jqueryScrollyFile = new File(Generator.baseDirectory + sep + "constellation/bootstrap/assets/js/jquery.scrolly.min.js");
        final URL jqueryScrollyURL = jqueryScrollyFile.toURI().toURL();
        final String scrolly = String.format("<script type=\"text/javascript\" src=\"\\%s\" ></script>", jqueryScrollyURL.toString());

        final File jqueryScrollexFile = new File(Generator.baseDirectory + sep + "constellation/bootstrap/assets/js/jquery.scrollex.min.js");
        final URL jqueryScrollexURL = jqueryScrollexFile.toURI().toURL();
        final String scrollex = String.format("<script type=\"text/javascript\" src=\"\\%s\" ></script>", jqueryScrollexURL.toString());

        final File browserFile = new File(Generator.baseDirectory + sep + "constellation/bootstrap/assets/js/browser.min.js");
        final URL browserURL = browserFile.toURI().toURL();
        final String browser = String.format("<script type=\"text/javascript\" src=\"\\%s\" ></script>", browserURL.toString());

        final File breakpointsFile = new File(Generator.baseDirectory + sep + "constellation/bootstrap/assets/js/breakpoints.min.js");
        final URL breakpointsURL = breakpointsFile.toURI().toURL();
        final String breakpoints = String.format("<script type=\"text/javascript\" src=\"\\%s\" ></script>", breakpointsURL.toString());

        final File appFile = new File(Generator.baseDirectory + sep + "constellation/bootstrap/assets/js/app.js");
        final URL appURL = appFile.toURI().toURL();
        final String appJS = String.format("<script type=\"text/javascript\" src=\"\\%s\" ></script>", appURL.toString());

        final File bootstrapJS = new File(Generator.baseDirectory + sep + "constellation/bootstrap/js/bootstrap.js");
        final URL bootstrapJSURL = bootstrapJS.toURI().toURL();
        final String boostrapjs = String.format("<script type=\"text/javascript\" src=\"\\%s\" ></script>", bootstrapJSURL);


        // Add items to StringBuilder
        html.append(css);
        html.append("\n");
        html.append(noScript);
        html.append("\n");
        html.append(cssBootstrap);
        html.append("\n");
        html.append(jquery);
        html.append("\n");
        html.append(dropotron);
        html.append("\n");
        html.append(scrolly);
        html.append("\n");
        html.append(scrollex);
        html.append("\n");
        html.append(browser);
        html.append("\n");
        html.append(breakpoints);
        html.append("\n");
        html.append(appJS);
        html.append("\n");
        html.append(boostrapjs);
        html.append("\n");
        html.append(startRowDiv);
        html.append("\n");
        html.append(startColDiv);
        html.append("\n");
        html.append(Processor.process(tocInput));
        html.append("\n");
        html.append(endDiv);
        html.append("\n");
        html.append(startInnerColDiv);
        html.append("\n");
        html.append(Processor.process(pageInput));
        html.append("\n");
        html.append(endDiv);
        html.append("\n");
        html.append(endDiv);
        html.append("\n");
        html.append(endDiv);

        return html.toString();
    }

    @Override
    public boolean display(final HelpCtx helpCtx) {
        final String helpId = helpCtx.getHelpID();
        LOGGER.log(Level.INFO, "display help for: {0}}", helpId);

        final String sep = File.separator;

        // Switched base help page to About Constellation
        //String helpTOCPath = "constellation" + sep + "toc.md";
        String helpTOCPath = "constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/about-constellation.md";

        // use the requested help file, or the table of contents if it doesnt exist
        final String helpLink = StringUtils.isNotEmpty(HelpMapper.getHelpAddress(helpId)) ? HelpMapper.getHelpAddress(helpId).substring(2) : helpTOCPath;

        if (!helpLink.isEmpty()) {
            try {
                final Preferences prefs = NbPreferences.forModule(HelpPreferenceKeys.class);
                final boolean isOnline = prefs.getBoolean(HelpPreferenceKeys.HELP_KEY, HelpPreferenceKeys.ONLINE_HELP);

                final String url;
                if (!isOnline) {
                    final File file = new File(Generator.baseDirectory + sep + helpLink);
                    final URL fileUrl = file.toURI().toURL();
                    currentPort = HelpWebServer.start();
                    url = String.format("http://localhost:%d/%s", currentPort, fileUrl);
                } else {

                    url = OFFICIAL_CONSTELLATION_WEBSITE;

                    // Uncomment below when pages are online
                    // url = String.format("https://www.constellation-app.com/%1$s/%2$s/%3$s/%4$s/", "docs" ,"v2_4" ,"constellation", fileUrl);
                }

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

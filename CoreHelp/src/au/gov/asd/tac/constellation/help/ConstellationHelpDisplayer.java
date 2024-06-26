/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author algol
 * @author Delphinus8821
 * @author aldebaran30701
 */
@ServiceProvider(service = HelpCtx.Displayer.class, position = 9999)
public class ConstellationHelpDisplayer implements HelpCtx.Displayer {

    private static final Logger LOGGER = Logger.getLogger(ConstellationHelpDisplayer.class.getName());

    private static final String OFFICIAL_CONSTELLATION_WEBSITE = "https://www.constellation-app.com/help";
    private static final String NEWLINE = "\n";
    
    // Run in a different thread, not the JavaFX thread
    private static final ExecutorService pluginExecutor = Executors.newCachedThreadPool();

    public static void copy(final String filePath, final OutputStream out) throws IOException {
        final String sep = File.separator;
        final InputStream pageInput = getInputStream(filePath);
        final InputStream tocInput = getInputStream(Generator.getBaseDirectory() + sep + Generator.getTOCDirectory());

        // avoid parsing utility files or images into html
        if (filePath.contains(".css") || filePath.contains(".js") || filePath.contains(".png") || filePath.contains(".jpg")) {
            out.write(pageInput.readAllBytes());
            return;
        }

        out.write(generateHTMLOutput(sep, tocInput, pageInput).getBytes());
    }

    protected static InputStream getInputStream(final String filePath) throws FileNotFoundException {
        final Path path = Paths.get(getPathString(filePath));
        return new FileInputStream(path.toString());
    }

    protected static String getFileURLString(final String fileSeparator, final String baseDirectory, final String relativePath) throws MalformedURLException {
        final File file = new File(baseDirectory + fileSeparator + relativePath);
        final URL url = file.toURI().toURL();
        return url.toString();
    }

    protected static String getPathString(final String helpFilePath) {
        final String errorPathIdentifier = "src/au/gov/constellation";
        final String errorPathEnd = "src/au/gov";
        if (helpFilePath.contains(errorPathIdentifier)) {
            final int lastIndex = helpFilePath.indexOf(errorPathEnd) + 11;
            final String removedFirstHalf = helpFilePath.substring(lastIndex, helpFilePath.length());
            final String coreRepo = "constellation";
            final String localPath = Generator.getBaseDirectory().substring(0, Generator.getBaseDirectory().indexOf(coreRepo));
            return localPath + removedFirstHalf;
        } else {
            return helpFilePath;
        }
    }

    /**
     * Generate a String which represents the table of contents, the currently displayed page, and the necessary html
     * tags for formatting.
     *
     * @param separator
     * @param tocInput
     * @param pageInput
     * @return
     * @throws IOException
     */
    protected static String generateHTMLOutput(final String separator, final InputStream tocInput, final InputStream pageInput) throws IOException {
        final StringBuilder html = new StringBuilder();

        // HTML elements
        final String startRowDiv = "<div class='row'>";
        final String endDiv = "</div>";
        final String startColDiv = "<div class='col-4 col-sm-3'>";
        final String startInnerColDiv = "<div class='col-8 col-sm-9'>";
        final String stylesheetLink = "<link href=\"\\%s\" rel='stylesheet'></link>";
        final String javascriptText = "<script type=\"text/javascript\" src=\"\\%s\" ></script>";

        final String css = String.format(stylesheetLink, getFileURLString(separator, Generator.getBaseDirectory(), "ext/bootstrap/assets/css/app.css"));
        final String noScript = String.format(stylesheetLink, getFileURLString(separator, Generator.getBaseDirectory(), "ext/bootstrap/assets/css/noscript.css"));
        final String cssBootstrap = String.format(stylesheetLink, getFileURLString(separator, Generator.getBaseDirectory(), "ext/bootstrap/css/bootstrap.css"));
        final String jquery = String.format("<script src=\"\\%s\" ></script>", getFileURLString(separator, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/jquery.min.js"));
        final String dropotron = String.format(javascriptText, getFileURLString(separator, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/jquery.dropotron.min.js"));
        final String scrolly = String.format(javascriptText, getFileURLString(separator, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/jquery.scrolly.min.js"));
        final String scrollex = String.format(javascriptText, getFileURLString(separator, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/jquery.scrollex.min.js"));
        final String browser = String.format(javascriptText, getFileURLString(separator, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/browser.min.js"));
        final String breakpoints = String.format(javascriptText, getFileURLString(separator, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/breakpoints.min.js"));
        final String appJS = String.format(javascriptText, getFileURLString(separator, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/app.js"));
        final String boostrapjs = String.format(javascriptText, getFileURLString(separator, Generator.getBaseDirectory(), "ext/bootstrap/js/bootstrap.js"));
        final String cookiejs = String.format("<script src=\"\\%s\" ></script>", getFileURLString(separator, Generator.getBaseDirectory(), "ext/bootstrap/js/js.cookie.min.js"));

        final String scriptTag = "<script>" + NEWLINE
                + " // when a group is shown, save it as active" + NEWLINE
                + " $(\".collapse\").on('shown.bs.collapse', function(e) {" + NEWLINE
                + "     e.stopPropagation(); " + NEWLINE
                + "     var active = $(this).attr('id');" + NEWLINE
                + "     Cookies.set(active, \"true\");" + NEWLINE
                + "     $(\"#\" + active).addClass('show');" + NEWLINE
                + " });" + NEWLINE
                + " // when a group is hidden, save it as inactive" + NEWLINE
                + " $(\".collapse\").on('hidden.bs.collapse', function(e) {" + NEWLINE
                + "     e.stopPropagation(); " + NEWLINE
                + "     var active = $(this).attr('id');" + NEWLINE
                + "     Cookies.set(active, \"false\");" + NEWLINE
                + "     $(\"#\" + active).removeClass('show');" + NEWLINE
                + "     $(\"#\" + active).collapse(\"hide\");" + NEWLINE
                + " });" + NEWLINE
                + " " + NEWLINE
                + " $(document).ready(function() {" + NEWLINE
                + "      var allCookies = Cookies.get();" + NEWLINE
                + "      for (var cookie in allCookies) { " + NEWLINE
                + "          if (cookie != null) {" + NEWLINE
                + "              //remove default collapse settings" + NEWLINE
                + "              $(\"#\" + cookie).removeClass('show');" + NEWLINE
                + "              //show the group if the value is true " + NEWLINE
                + "              var cookieValue = Cookies.get(cookie);" + NEWLINE
                + "              if (cookieValue == (\"true\")) {" + NEWLINE
                + "                 $(\"#\" + cookie).collapse(\"show\");" + NEWLINE
                + "              } else {" + NEWLINE
                + "                 $(\"#\" + cookie).collapse(\"hide\");" + NEWLINE
                + "                 $(\"#\" + cookie + \" .collapse\").removeClass('show');" + NEWLINE
                + "              }" + NEWLINE
                + "          }" + NEWLINE
                + "      }" + NEWLINE
                + " });" + NEWLINE
                + NEWLINE
                + "</script>";

        // Add items to StringBuilder
        html.append(css);
        html.append(NEWLINE);
        html.append(noScript);
        html.append(NEWLINE);
        html.append(cssBootstrap);
        html.append(NEWLINE);
        html.append(jquery);
        html.append(NEWLINE);
        html.append(cookiejs);
        html.append(NEWLINE);
        html.append(dropotron);
        html.append(NEWLINE);
        html.append(scrolly);
        html.append(NEWLINE);
        html.append(scrollex);
        html.append(NEWLINE);
        html.append(browser);
        html.append(NEWLINE);
        html.append(breakpoints);
        html.append(NEWLINE);
        html.append(appJS);
        html.append(NEWLINE);
        html.append(boostrapjs);
        html.append(NEWLINE);
        html.append(startRowDiv);
        html.append(NEWLINE);
        html.append(startColDiv);
        html.append(NEWLINE);
        final String tocString =  new String(tocInput.readAllBytes(), StandardCharsets.UTF_8);
        final Parser parser = Parser.builder().build();
        final HtmlRenderer renderer = HtmlRenderer.builder().build();
        final Node tocDocument = parser.parse(tocString);
        final String tocHtml = renderer.render(tocDocument);
        html.append(tocHtml);
        html.append(NEWLINE);
        html.append(endDiv);
        html.append(NEWLINE);
        html.append(startInnerColDiv);
        html.append(NEWLINE);        
        final String rawInput = new String(pageInput.readAllBytes(), StandardCharsets.UTF_8);
        final Node pageDocument = parser.parse(rawInput);
        final String pageHtml = renderer.render(pageDocument);
        html.append(pageHtml);
        html.append(NEWLINE);
        html.append(endDiv);
        html.append(NEWLINE);
        html.append(endDiv);
        html.append(NEWLINE);
        html.append(endDiv);
        html.append(NEWLINE);
        html.append(scriptTag);

        String htmlString = html.toString();
        final int headTagIndex = htmlString.indexOf("<head ");
        int insertPos = 0;
        if (headTagIndex > -1) {
            insertPos = htmlString.substring(headTagIndex).indexOf(">") + headTagIndex + 1;                
        }
        final int metaIndex = htmlString.indexOf("<meta ");
        if (metaIndex == -1) {
            // add a meta tag after the head tag if it exists
            htmlString = htmlString.substring(0, insertPos) + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">" + htmlString.substring(insertPos);
        } else {
            // check the meta tag
            final int metaTagEnd = htmlString.substring(metaIndex).indexOf(">");
            String metaString = htmlString.substring(metaIndex, metaIndex + metaTagEnd + 1);
            final int contentIndex = metaString.indexOf("content=\"");
            if (contentIndex > -1) {
                final int endContentPos = metaString.substring(contentIndex+9).indexOf("\"");
                String contentString = metaString.substring(contentIndex + 9, contentIndex + 9 + endContentPos);
                final int charsetIndex = contentString.indexOf("charset=");
                if (charsetIndex > -1) {
                    // find end of charset info and overwrite it
                    int charsetEndIndex = contentString.substring(charsetIndex).indexOf(";");
                    // a value of -1 means id was the last entry of the content attribute
                    if (charsetEndIndex == -1) {
                        charsetEndIndex = contentString.length();
                    }
                    // replace the charset value with utf-8
                    contentString = contentString.substring(0, charsetIndex + 8) + "utf-8" + contentString.substring(charsetEndIndex);                    
                } else {
                    // no charset specified in the content attribute, add it to the content string
                    contentString = contentString + "; charset=utf-8";
                }
                // insert new content string into metastring
                metaString = metaString.substring(0, contentIndex + 9) + contentString + metaString.substring(contentIndex + 9 + endContentPos);
            } else {
                // no content attribute
                // check for charset attribute
                final int charsetAttrIndex = metaString.indexOf("charset=\"");
                if (charsetAttrIndex > -1) {
                    int charsetEndIndex = metaString.substring(charsetAttrIndex + 9).indexOf("\"");
                    if (charsetEndIndex > -1) {
                        // put utf-8 in the charset string
                        metaString = metaString.substring(0, charsetAttrIndex + 9) + "utf-8" + metaString.substring(charsetAttrIndex + 9 + charsetEndIndex);
                    }
                } else {
                    // add a content attribute to the metaString
                    metaString = metaString.substring(0, metaString.lastIndexOf("\"") + 1) + " content=\"text/html; charset=utf-8\">";
                }
            }
            // insert new metastring in htmlString
            final String nonMetaHtml = htmlString.substring(0, metaIndex) + htmlString.substring(metaIndex + metaTagEnd + 1);
            htmlString = nonMetaHtml.substring(0, insertPos) + metaString + nonMetaHtml.substring(insertPos);
        }
        return htmlString;
    }

    /**
     * Display the help page for the following HelpCtx
     *
     * @param helpCtx
     * @return
     */
    @Override
    public boolean display(final HelpCtx helpCtx) {
        final String sep = File.separator;

        final Preferences prefs = NbPreferences.forModule(HelpPreferenceKeys.class);
        final boolean isOnline = prefs.getBoolean(HelpPreferenceKeys.HELP_KEY, HelpPreferenceKeys.ONLINE_HELP);

        final String helpId = helpCtx.getHelpID();
        LOGGER.log(Level.INFO, "display help for: {0}", helpId);

        final String helpDefaultPath = sep + "ext" + sep + "docs" + sep + "CoreFunctionality" + sep + "src" + sep + "au" + sep + "gov"
                + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "functionality" + sep + "about-constellation.md";

        final String helpAddress = HelpMapper.getHelpAddress(helpId);
        // use the requested help file, or the About Constellation page if one is not given
        String helpLink = StringUtils.isNotEmpty(helpAddress) ? helpAddress.substring(2) : helpDefaultPath;

        try {
            final String url;
            if (isOnline) {
                if (helpLink.contains("constellation-")) {
                    helpLink = helpLink.substring(helpLink.indexOf("modules") + 7);
                }
                url = OFFICIAL_CONSTELLATION_WEBSITE + helpLink.replace(".md", ".html");
            } else {
                final File file = new File(Generator.getBaseDirectory() + sep + helpLink);
                final URL fileUrl = file.toURI().toURL();
                final int currentPort = HelpWebServer.start();
                url = String.format("http://localhost:%d/%s", currentPort, fileUrl);
            }

            final URI uri = new URI(url.replace("\\", "/"));

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                browse(uri);
                return true;
            }

            LOGGER.log(Level.WARNING, "Help Documentation was unable to launch because Desktop "
                    + "Browsing was not supported. Tried to navigate to: {0}", uri);

        } catch (final MalformedURLException | URISyntaxException ex) {
            LOGGER.log(Level.WARNING, ex, () -> "Help Documentation URL/URI was invalid - Tried to display help for: " + helpId);
        }
        return false;
    }

    /**
     * Browse to the supplied URI using the desktops browser.
     *
     * @param uri the URI to navigate to
     * @return true if successful, false otherwise
     */
    public static Future<?> browse(final URI uri) {
        LOGGER.log(Level.INFO, "Loading help uri {0}", uri);

        return pluginExecutor.submit(() -> {
            Thread.currentThread().setName("Browse Help");
            try {
                Desktop.getDesktop().browse(uri);
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, String.format("Failed to load the help URI %s", uri), ex);
            }
        });
    }
}

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
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
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
    private static final Pattern SPLIT_REGEX = Pattern.compile("</a>");
    private static final String SEP = File.separator;
    
    // Run in a different thread, not the JavaFX thread
    private static final ExecutorService pluginExecutor = Executors.newCachedThreadPool();

    public static void copy(final String filePath, final OutputStream out) throws IOException {
        final InputStream pageInput = getInputStream(filePath);
        final InputStream tocInput = getInputStream(Generator.getBaseDirectory() + SEP + Generator.getTOCDirectory());

        // avoid parsing utility files or images into html
        if (filePath.contains(".css") || filePath.contains(".js") || filePath.contains(".png") || filePath.contains(".jpg")) {
            out.write(pageInput.readAllBytes());
            return;
        }

        out.write(generateHTMLOutput(tocInput, pageInput).getBytes());
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
     * @param tocInput
     * @param pageInput
     * @return
     * @throws IOException
     */
    protected static String generateHTMLOutput(final InputStream tocInput, final InputStream pageInput) throws IOException {
        final StringBuilder html = new StringBuilder();
        
        final InputStream htmlTemplate = getInputStream(Generator.getBaseDirectory() + SEP + "ext" + SEP + "bootstrap" + SEP + "assets" + SEP + "HelpTemplate.html");
        final String templateHtml =  new String(htmlTemplate.readAllBytes(), StandardCharsets.UTF_8);
        html.append(templateHtml);
        
        // Create the css and js links and scripts
        final String stylesheetLink = "<link href=\"\\%s\" rel='stylesheet'></link>";
        final String javascriptText = "<script type=\"text/javascript\" src=\"\\%s\" ></script>";
        
        final String css = String.format(stylesheetLink, getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/assets/css/app.css"));
        final String noScript = String.format(stylesheetLink, getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/assets/css/noscript.css"));
        final String cssBootstrap = String.format(stylesheetLink, getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/css/bootstrap.css"));
        final String searchcss = String.format(stylesheetLink, getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/assets/css/search.css"));
        final String jquery = String.format("<script src=\"\\%s\" ></script>", getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/jquery.min.js"));
        final String dropotron = String.format(javascriptText, getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/jquery.dropotron.min.js"));
        final String scrolly = String.format(javascriptText, getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/jquery.scrolly.min.js"));
        final String scrollex = String.format(javascriptText, getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/jquery.scrollex.min.js"));
        final String browser = String.format(javascriptText, getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/browser.min.js"));
        final String breakpoints = String.format(javascriptText, getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/breakpoints.min.js"));
        final String appJS = String.format(javascriptText, getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/assets/js/app.js"));
        final String bootstrapjs = String.format(javascriptText, getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/js/bootstrap.js"));
        final String cookiejs = String.format("<script src=\"\\%s\" ></script>", getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/js/js.cookie.min.js"));
        final String searchjs = String.format(javascriptText, getFileURLString(SEP, Generator.getBaseDirectory(), "ext/bootstrap/js/index.min.js"));
        
        final StringBuilder scripts = new StringBuilder();      
        scripts.append(css).append(NEWLINE);
        scripts.append(noScript).append(NEWLINE);
        scripts.append(cssBootstrap).append(NEWLINE);
        scripts.append(searchcss).append(NEWLINE);
        scripts.append(jquery).append(NEWLINE);
        scripts.append(dropotron).append(NEWLINE);
        scripts.append(scrolly).append(NEWLINE);
        scripts.append(scrollex).append(NEWLINE);
        scripts.append(browser).append(NEWLINE);
        scripts.append(breakpoints).append(NEWLINE);
        scripts.append(appJS).append(NEWLINE);
        scripts.append(bootstrapjs).append(NEWLINE);
        scripts.append(cookiejs).append(NEWLINE);
        scripts.append(searchjs).append(NEWLINE);
        
        // Add in the links and scripts to the top of the file
        final int scriptIndex = html.indexOf("SCRIPTS");
        html.replace(scriptIndex, scriptIndex + 7, scripts.toString());
        
        
        // Add in the TOC
        final String tocString =  new String(tocInput.readAllBytes(), StandardCharsets.UTF_8);
        final Parser parser = Parser.builder().build();
        final HtmlRenderer renderer = HtmlRenderer.builder().build();
        final Node tocDocument = parser.parse(tocString);
        final String tocHtml = renderer.render(tocDocument);
        final int tocIndex = html.indexOf("TABLE_OF_CONTENTS");
        html.replace(tocIndex, tocIndex + 17, tocHtml);
        
        // Add in the help page 
        final String rawInput = new String(pageInput.readAllBytes(), StandardCharsets.UTF_8);
        final Node pageDocument = parser.parse(rawInput);
        final String pageHtml = renderer.render(pageDocument);
        final int pageIndex = html.indexOf("MAIN_PAGE");
        html.replace(pageIndex, pageIndex + 9, pageHtml);
        
        // Add in the list of documents to search 
        final List<String> documents = createSearchDocument(tocHtml);
        final StringBuilder documentString = new StringBuilder();
        documentString.append(documents.getFirst());
        for (int i = 1; i < documents.size(); i++) {
            documentString.append(",");
            documentString.append(documents.get(i));
        }
        final int documentsIndex = html.indexOf("HELP_PAGES");
        html.replace(documentsIndex, documentsIndex + 10, documentString.toString());

        return html.toString();
    }

    /**
     * Display the help page for the following HelpCtx
     *
     * @param helpCtx
     * @return
     */
    @Override
    public boolean display(final HelpCtx helpCtx) {
        final Preferences prefs = NbPreferences.forModule(HelpPreferenceKeys.class);
        final boolean isOnline = prefs.getBoolean(HelpPreferenceKeys.HELP_KEY, HelpPreferenceKeys.ONLINE_HELP);

        final String helpId = helpCtx.getHelpID();
        LOGGER.log(Level.INFO, "display help for: {0}", helpId);

        final String helpDefaultPath = SEP + "ext" + SEP + "docs" + SEP + "CoreFunctionality" + SEP + "src" + SEP + "au" + SEP + "gov"
                + SEP + "asd" + SEP + "tac" + SEP + "constellation" + SEP + "functionality" + SEP + "about-constellation.md";

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
                final File file = new File(Generator.getBaseDirectory() + SEP + helpLink);
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
    
    public static List<String> createSearchDocument(final String toc) {
        // Creates json file of documents to search 
        // Each entry has an id, title, category and link 
        final List<String> documents = new ArrayList<>();

        final String[] elements = SPLIT_REGEX.split(toc);
        int index = 0;
        String category = "";
        String pageName = "";
        String link = "";
        
        for (final String element : elements) {
            if (element.contains("data-target")) {
                final int dataIndex = element.indexOf("data-target");
                final String categoryString = element.substring(dataIndex);
                final int beginningIndex = categoryString.indexOf(">");
                final int endIndex = categoryString.indexOf("<");
                category = categoryString.substring(beginningIndex + 1, endIndex);
            }
            if (element.contains("a href")) {
                final int hrefIndex = element.indexOf("a href");
                final String linkString = element.substring(hrefIndex + 7);
                final int quoteIndex = linkString.indexOf("\"");
                final int endIndex = linkString.indexOf(">");
                link = linkString.substring(quoteIndex + 1, endIndex - 1);
                // Changing this to a replace instead of replaceAll stops the search from working as the links require double \ 
                link = link.replaceAll("\\\\", "\\\\\\\\");
                pageName = linkString.substring(endIndex + 1);
                
                final String page = String.format("""
                                    {
                                        "id": %s,
                                        "title": "%s",
                                        "category": "%s",
                                        "link": "%s"
                                    }
                                    """, index, pageName, category, link);
                documents.add(page);
                index++;
                
            }
        }
        
        // To update the online help search file change the boolean to true
        // Must also run adaptors when updating online help so those results aren't removed from the search file
        // Reset back to false after updating the search file 
        final boolean updateOnlineHelp = false;
        
        if (updateOnlineHelp) {
            // Create a json file for the online help search
            final String path = Generator.getOnlineHelpTOCDirectory(Generator.getBaseDirectory()) + "search.json";
            try (final FileWriter search = new FileWriter(path)) {
                final List<String> documentsHtml = documents;
                final StringBuilder documentString = new StringBuilder();
                documentString.append("[");
                final String firstEntry = documentsHtml.getFirst().replace(".md\"", ".html\"");
                documentString.append(firstEntry);
                for (int i = 1; i < documentsHtml.size(); i++) {
                    documentString.append(",");
                    final String newEntry = documentsHtml.get(i).replace(".md\"", ".html\"");
                    documentString.append(newEntry);
                }
                documentString.append("]");
                search.write(documentString.toString());

            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }

        return documents;
    }
}

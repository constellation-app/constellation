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
package au.gov.asd.tac.constellation.projectupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Rewrite the project.xml file with the latest dependencies.
 * <p>
 * This project will be automatically built during the
 * update-dependencies-clean-build ant target.
 *
 * @author sirius
 * @author arcturus
 */
public class ProjectUpdater extends Task {

    private static final Logger LOGGER = Logger.getLogger(ProjectUpdater.class.getName());
    private static final String NOT_A_VALID_PROJECTXML_FILE_MESSAGE = "Not a valid project.xml file";
    private static final String PACKAGE_TAG = "package";
    private static final String PADDING_LEVEL1 = "\n        ";
    private static final String PADDING_LEVEL2 = "\n            ";
    private static final String PADDING_LEVEL3 = "\n                ";

    private File projectDirectory = null;

    public void setProjectdirectory(final File projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public void execute() throws BuildException {
        try {
            logMessage("Updating wrapped jars for " + projectDirectory);

            final File projectFile = new File(projectDirectory, "nbproject/project.xml");
            final File oldProjectFile = new File(projectDirectory, "nbproject/project_old.xml");
            final File jarDirectory = new File(projectDirectory, "release/modules/ext");
            final File publicPackagesFile = new File(projectDirectory, "/src/public.xml");

            // Create a document to work on
            final Document document = readXMLFile(projectFile);

            // Find the data element and ensure there is only one
            final NodeList dataNodes = document.getElementsByTagName("data");
            if (dataNodes.getLength() != 1) {
                throw new IllegalStateException(NOT_A_VALID_PROJECTXML_FILE_MESSAGE);
            }
            final Node dataNode = dataNodes.item(0);

            final List<String> packages = new ArrayList<>();
            final Set<String> publicPackages = new TreeSet<>();
            Node publicPackagesNode = null;
            if (publicPackagesFile.exists()) {
                final NodeList publicPackagesNodes = document.getElementsByTagName("public-packages");
                if (publicPackagesNodes.getLength() != 1) {
                    throw new IllegalStateException(NOT_A_VALID_PROJECTXML_FILE_MESSAGE);
                }
                publicPackagesNode = publicPackagesNodes.item(0);

                final NodeList children = publicPackagesNode.getChildNodes();
                while (children.getLength() > 0) {
                    final Node child = children.item(0);
                    if (child instanceof Element childElement) {
                        if (childElement.getTagName().equals(PACKAGE_TAG)) {
                            publicPackages.add(childElement.getTextContent());
                        }
                    }
                    publicPackagesNode.removeChild(child);
                }

                final Document publicXMLFile = readXMLFile(publicPackagesFile);
                final NodeList packageNodes = publicXMLFile.getElementsByTagName(PACKAGE_TAG);
                for (int i = 0; i < packageNodes.getLength(); i++) {
                    packages.add(packageNodes.item(i).getTextContent());
                }
            }

            // Remove all the class-path-extension elements
            // Ensure that each is a child of the data element
            // Remore all white space around the elements
            final NodeList classPathExtensionNodes = document.getElementsByTagName("class-path-extension");
            while (classPathExtensionNodes.getLength() > 0) {
                final Node classPathExtensionNode = classPathExtensionNodes.item(0);
                if (classPathExtensionNode.getParentNode() == dataNode) {
                    Node nextNode = classPathExtensionNode.getNextSibling();
                    while (nextNode instanceof Text textNode) {
                        nextNode = nextNode.getNextSibling();
                        dataNode.removeChild(textNode);
                    }
                    Node prevNode = classPathExtensionNode.getPreviousSibling();
                    while (prevNode instanceof Text textNode) {
                        prevNode = prevNode.getPreviousSibling();
                        dataNode.removeChild(textNode);
                    }
                    dataNode.removeChild(classPathExtensionNode);
                } else {
                    throw new IllegalStateException(NOT_A_VALID_PROJECTXML_FILE_MESSAGE);
                }
            }

            // Restore each jar file to the project.xml
            // If the jar directory does not exist then assume no wrapped jars
            // Ensure each file is a JAR file
            // Add white space to make it look the same as the Netbeans version
            if (jarDirectory.exists()) {
                final File[] jarFiles = jarDirectory.listFiles();

                // Sort them so that the project.xml is generated consistently and avoids merge conflicts
                Arrays.sort(jarFiles, (final File o1, final File o2) -> {
                    final String p1 = o1.getAbsolutePath();
                    final String p2 = o2.getAbsolutePath();
                    return p1.compareTo(p2);
                });

                // Add the class path to project.xml
                for (final File jarFile : jarFiles) {
                    if (jarFile.getName().endsWith(".jar")) {
                        logMessage("\tIncluding jar file: " + jarFile.getName());
                        addClassPathExtension(document, jarFile, dataNode);

                        if (publicPackagesFile.exists()) {
                            extractMatchingPackages(jarFile, packages, publicPackages);
                        }
                    } else {
//                        throw new IllegalStateException("Not a JAR file: " + jarFile.getAbsolutePath());
                        logMessage("\tWARNING: Not a JAR file: " + jarFile.getAbsolutePath());
                    }
                }

                dataNode.appendChild(document.createTextNode(PADDING_LEVEL1));
            }

            if (publicPackagesFile.exists()) {
                for (final String publicPackage : publicPackages) {
                    if (!publicPackage.startsWith("META-INF")) {
                        final Element publicPackageElement = document.createElement(PACKAGE_TAG);
                        publicPackageElement.setTextContent(publicPackage);
                        publicPackagesNode.appendChild(document.createTextNode(PADDING_LEVEL3));
                        publicPackagesNode.appendChild(publicPackageElement);
                    }
                }
            }
            if (publicPackagesNode != null) {
                publicPackagesNode.appendChild(document.createTextNode(PADDING_LEVEL2));
            }

            // Delete the existing old file and replace it with a copy of the current project.xml
            final boolean oldProjectFileDeleted = oldProjectFile.delete();
            if (!oldProjectFileDeleted) {
                logMessage("ERROR: The old project.xml file could not be deleted.");
            }
            final boolean projectFileRenamed = projectFile.renameTo(oldProjectFile);
            if (!projectFileRenamed) {
                logMessage("ERROR: project.xml could not be renamed.");
            }

            // Save the edited document to project.xml
            saveXMLFile(document, projectFile);
        } catch (final IOException | IllegalStateException | ParserConfigurationException | TransformerException | DOMException ex) {
            logMessage("Exception during update: " + ex.getClass() + " " + ex.getMessage() + " " + ex.getStackTrace()[0]);
        }
    }

    private static void addClassPathExtension(final Document document, final File jarFile, final Node parentNode) {
        final Element classPathExtensionElement = document.createElement("class-path-extension");
        classPathExtensionElement.appendChild(document.createTextNode(PADDING_LEVEL3));

        final Element runtimeRelativePathElement = document.createElement("runtime-relative-path");
        runtimeRelativePathElement.setTextContent("ext/" + jarFile.getName());
        classPathExtensionElement.appendChild(runtimeRelativePathElement);
        classPathExtensionElement.appendChild(document.createTextNode(PADDING_LEVEL3));

        final Element binaryOriginElement = document.createElement("binary-origin");
        binaryOriginElement.setTextContent("release/modules/ext/" + jarFile.getName());
        classPathExtensionElement.appendChild(binaryOriginElement);
        classPathExtensionElement.appendChild(document.createTextNode(PADDING_LEVEL2));

        parentNode.appendChild(document.createTextNode(PADDING_LEVEL2));
        parentNode.appendChild(classPathExtensionElement);
    }

    private static Document readXMLFile(final File xmlFile) throws ParserConfigurationException, TransformerException, IOException {
        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        final DocumentBuilder builder = builderFactory.newDocumentBuilder();

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        final Transformer transformer = transformerFactory.newTransformer();

        // Create a document to work on
        final Document document = builder.newDocument();

        // Read in the existing project.xml into the document
        try (FileInputStream in = new FileInputStream(xmlFile)) {
            final Source loadSource = new StreamSource(in);
            final Result loadResult = new DOMResult(document);
            transformer.transform(loadSource, loadResult);
        }

        return document;
    }

    private static void saveXMLFile(final Document document, final File xmlFile) throws IOException, TransformerException {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        final Transformer transformer = transformerFactory.newTransformer();

        try (FileOutputStream out = new FileOutputStream(xmlFile)) {
            final Source saveSource = new DOMSource(document);
            final Result saveResult = new StreamResult(out);
            transformer.transform(saveSource, saveResult);
        }
    }

    private void extractMatchingPackages(final File jarFile, final List<String> expressions, final Set<String> publicPackages) throws IOException {
        try (final ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile))) {
            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                final String path = entry.getName();

                // security check
                if (path.contains("..") || path.startsWith(File.separator)) {
                    throw new IOException("Entry is trying to leave the parent dirctory: " + path);
                }

                final int lastDivider = path.lastIndexOf('/');
                if (lastDivider >= 0) {
                    final String folder = path.substring(0, lastDivider);
                    final String name = folder.replace('/', '.');
                    if (path.endsWith(".class")) {
                        publicPackages.remove(name);
                        for (final String expression : expressions) {
                            if (expression.endsWith("*")) {
                                if (name.startsWith(expression.substring(0, expression.length() - 1))) {
                                    publicPackages.add(name);
                                    break;
                                }
                            } else if (name.equals(expression)) {
                                publicPackages.add(name);
                                break;
                            }
                        }
                    }
                }

                entry = zip.getNextEntry();
            }
        }
    }

    private void logMessage(final String message) {
        if (getProject() != null) {
            getProject().log(message);
        } else {
            LOGGER.info(message);
        }
    }

}

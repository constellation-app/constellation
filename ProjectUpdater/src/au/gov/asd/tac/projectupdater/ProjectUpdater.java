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
package au.gov.asd.tac.projectupdater;

// IMPORTANT! You need to compile this class against ant.jar.
// The easiest way to do this is to add ${ant.core.lib} to your project's classpath.
// For example, for a plain Java project with no other dependencies, set in project.properties:
// javac.classpath=${ant.core.lib}
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author sirius
 */
public class ProjectUpdater extends Task {

    private File projectDirectory = null;

    public void setProjectdirectory(File projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public void execute() throws BuildException {
        try {

            logMessage("Updating wrapped jars for " + projectDirectory);

            File projectFile = new File(projectDirectory, "nbproject/project.xml");
            File oldProjectFile = new File(projectDirectory, "nbproject/project_old.xml");
            File jarDirectory = new File(projectDirectory, "release/modules/ext");
            File publicPackagesFile = new File(projectDirectory, "/src/public.xml");

            // Create a document to work on
            Document document = readXMLFile(projectFile);

            // Find the data element and ensure there is only one
            NodeList dataNodes = document.getElementsByTagName("data");
            if (dataNodes.getLength() != 1) {
                throw new IllegalStateException("Not a valid project.xml file");
            }
            Node dataNode = dataNodes.item(0);

            List<String> packages = new ArrayList<>();
            Set<String> publicPackages = new TreeSet<>();
            Node publicPackagesNode = null;
            if (publicPackagesFile.exists()) {
                NodeList publicPackagesNodes = document.getElementsByTagName("public-packages");
                if (publicPackagesNodes.getLength() != 1) {
                    throw new IllegalStateException("Not a valid project.xml file");
                }
                publicPackagesNode = publicPackagesNodes.item(0);

                NodeList children = publicPackagesNode.getChildNodes();
                while (children.getLength() > 0) {
                    Node child = children.item(0);
                    if (child instanceof Element) {
                        Element childElement = (Element) child;
                        if (childElement.getTagName().equals("package")) {
                            publicPackages.add(childElement.getTextContent());
                        }
                    }
                    publicPackagesNode.removeChild(child);
                }

                Document publicXMLFile = readXMLFile(publicPackagesFile);
                NodeList packageNodes = publicXMLFile.getElementsByTagName("package");
                for (int i = 0; i < packageNodes.getLength(); i++) {
                    packages.add(packageNodes.item(i).getTextContent());
                }
            }

            // Remove all the class-path-extension elements
            // Ensure that each is a child of the data element
            // Remore all white space around the elements
            NodeList classPathExtensionNodes = document.getElementsByTagName("class-path-extension");
            while (classPathExtensionNodes.getLength() > 0) {
                Node classPathExtensionNode = classPathExtensionNodes.item(0);
                if (classPathExtensionNode.getParentNode() == dataNode) {
                    Node nextNode = classPathExtensionNode.getNextSibling();
                    while (nextNode instanceof Text) {
                        Node textNode = nextNode;
                        nextNode = nextNode.getNextSibling();
                        dataNode.removeChild(textNode);
                    }
                    Node prevNode = classPathExtensionNode.getPreviousSibling();
                    while (prevNode instanceof Text) {
                        Node textNode = prevNode;
                        prevNode = prevNode.getPreviousSibling();
                        dataNode.removeChild(textNode);
                    }
                    dataNode.removeChild(classPathExtensionNode);
                } else {
                    throw new IllegalStateException("Not a valid project.xml file");
                }
            }

            // Restore each jar file to the project.xml
            // If the jar directory does not exist then assume no wrapped jars
            // Ensure each file is a JAR file
            // Add white space to make it look the same as the Netbeans version
            if (jarDirectory.exists()) {
                for (File jarFile : jarDirectory.listFiles()) {
                    if (jarFile.getName().endsWith(".jar")) {

                        logMessage("\tIncluding jar file: " + jarFile.getName());

                        addClassPathExtension(document, jarFile, dataNode);

                        if (publicPackagesFile.exists()) {
                            extractMatchingPackages(jarFile, packages, publicPackages);
                        }

                    } else {
                        throw new IllegalStateException("Not a JAR file: " + jarFile.getAbsolutePath());
                    }
                }
                dataNode.appendChild(document.createTextNode("\n        "));
            }

            if (publicPackagesFile.exists()) {
                for (String publicPackage : publicPackages) {
                    Element publicPackageElement = document.createElement("package");
                    publicPackageElement.setTextContent(publicPackage);
                    publicPackagesNode.appendChild(document.createTextNode("\n                "));
                    publicPackagesNode.appendChild(publicPackageElement);
                }
            }
            if (publicPackagesNode != null) {
                publicPackagesNode.appendChild(document.createTextNode("\n            "));
            }

            // Delete the existing old file and replace it with a copy of the current project.xml
            oldProjectFile.delete();
            projectFile.renameTo(oldProjectFile);

            // Save the edited document to project.xml
            saveXMLFile(document, projectFile);

        } catch (Exception ex) {
            logMessage("Exception during update: " + ex.getClass() + " " + ex.getMessage() + " " + ex.getStackTrace()[0]);
        }
    }

    private static void addClassPathExtension(Document document, File jarFile, Node parentNode) {

        Element classPathExtensionElement = document.createElement("class-path-extension");

        classPathExtensionElement.appendChild(document.createTextNode("\n                "));

        Element runtimeRelativePathElement = document.createElement("runtime-relative-path");
        runtimeRelativePathElement.setTextContent("ext/" + jarFile.getName());
        classPathExtensionElement.appendChild(runtimeRelativePathElement);

        classPathExtensionElement.appendChild(document.createTextNode("\n                "));

        Element binaryOriginElement = document.createElement("binary-origin");
        binaryOriginElement.setTextContent("release/modules/ext/" + jarFile.getName());
        classPathExtensionElement.appendChild(binaryOriginElement);

        classPathExtensionElement.appendChild(document.createTextNode("\n            "));

        parentNode.appendChild(document.createTextNode("\n            "));
        parentNode.appendChild(classPathExtensionElement);
    }

    private static Document readXMLFile(File xmlFile) throws Exception {

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        // Create a document to work on
        Document document = builder.newDocument();

        try ( // Read in the existing project.xml into the document
                FileInputStream in = new FileInputStream(xmlFile)) {
            Source loadSource = new StreamSource(in);
            Result loadResult = new DOMResult(document);
            transformer.transform(loadSource, loadResult);
        }

        return document;
    }

    private static void saveXMLFile(Document document, File xmlFile) throws Exception {

        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        try (FileOutputStream out = new FileOutputStream(xmlFile)) {
            Source saveSource = new DOMSource(document);
            Result saveResult = new StreamResult(out);
            transformer.transform(saveSource, saveResult);
        }
    }

    private void extractMatchingPackages(File jarFile, List<String> expressions, Set<String> publicPackages) throws Exception {
        ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile));
        ZipEntry entry = zip.getNextEntry();
        while (entry != null) {
            String path = entry.getName();
            int lastDivider = path.lastIndexOf('/');
            if (lastDivider >= 0) {
                String folder = path.substring(0, lastDivider);
                String name = folder.replace('/', '.');
                if (path.endsWith(".class")) {
                    publicPackages.remove(name);
                    for (String expression : expressions) {
                        if (expression.endsWith("*")) {
                            if (name.startsWith(expression.substring(0, expression.length() - 1))) {
                                publicPackages.add(name);
                                break;
                            }
                        } else {
                            if (name.equals(expression)) {
                                publicPackages.add(name);
                                break;
                            }
                        }
                    }
                }
            }

            entry = zip.getNextEntry();
        }
    }

    private void logMessage(String message) {
        if (getProject() != null) {
            getProject().log(message);
        } else {
            System.out.println(message);
        }
    }

    public static void main(String[] args) {
        ProjectUpdater p = new ProjectUpdater();
        p.setProjectdirectory(new File("d:/home/code/CONSTELLATION/Wrapper"));
        p.execute();
    }
}

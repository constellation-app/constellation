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
package au.gov.asd.tac.constellation.preferences;

import java.awt.GraphicsEnvironment;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class ApplicationOptionsPanelNGTest {

    public ApplicationOptionsPanelNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of setUserDirectory and getUserDirectory methods, of class ApplicationOptionsPanel.
     */
    @Test
    public void testSetUserDirectory() {
        System.out.println("setGetUserDirectory");
        String expResult = "RESULT";
        ApplicationOptionsPanel instance = new ApplicationOptionsPanel();
        instance.setUserDirectory(expResult);
        assertEquals(instance.getUserDirectory(), expResult);
    }

    /**
     * Test of setAutosaveEnabled and getAutosaveEnabled methods, of class ApplicationOptionsPanel.
     */
    @Test
    public void testSetGetAutosaveEnabled() {
        System.out.println("setGetAutosaveEnabled");
        boolean autosaveEnabled = false;
        ApplicationOptionsPanel instance = new ApplicationOptionsPanel();
        instance.setAutosaveEnabled(autosaveEnabled);
        assertEquals(instance.isAutosaveEnabled(), autosaveEnabled);
    }

    /**
     * Test of setAutosaveFrequency and getAutosaveFrequency methods, of class ApplicationOptionsPanel.
     */
    @Test
    public void testSetGetAutosaveFrequency() {
        System.out.println("setGetAutosaveFrequency");
        int autosaveFrequency = 0;
        ApplicationOptionsPanel instance = new ApplicationOptionsPanel();
        instance.setAutosaveFrequency(autosaveFrequency);
        assertEquals(instance.getAutosaveFrequency(), autosaveFrequency);

    }

    /**
     * Test of setWelcomeOnStartup and getWelcomeOnStartup methods, of class ApplicationOptionsPanel.
     */
    @Test
    public void testSetGetWelcomeOnStartup() {
        System.out.println("setGetWelcomeOnStartup");
        boolean welcomeOnStartup = false;
        ApplicationOptionsPanel instance = new ApplicationOptionsPanel();
        instance.setWelcomeOnStartup(welcomeOnStartup);
        assertEquals(instance.isWelcomeOnStartupSelected(), welcomeOnStartup);
    }

    /**
     * Test of setWhatsNewOnStartup and getWhatsNewOnStartup methods, of class ApplicationOptionsPanel.
     */
    @Test
    public void testSetGetWhatsNewOnStartup() {
        System.out.println("setGetWhatsNewOnStartup");
        boolean whatsNewOnStartup = false;
        ApplicationOptionsPanel instance = new ApplicationOptionsPanel();
        instance.setWhatsNewOnStartup(whatsNewOnStartup);
        assertEquals(instance.isWhatsNewOnStartupSelected(), whatsNewOnStartup);
    }

    /**
     * Test of setWebserverPort and getWebserverPort methods, of class ApplicationOptionsPanel.
     */
    @Test
    public void testSetGetWebserverPort() {
        System.out.println("setGetWebserverPort");
        int webserverPort = 0;
        ApplicationOptionsPanel instance = new ApplicationOptionsPanel();
        instance.setWebserverPort(webserverPort);
        assertEquals(instance.getWebserverPort(), webserverPort);
    }

    /**
     * Test of setNotebookDirectory and getNotebookDirectory methods, of class ApplicationOptionsPanel.
     */
    @Test
    public void testSetGetNotebookDirectory() {
        System.out.println("setGetNotebookDirectory");
        String notebookDirectory = "";
        ApplicationOptionsPanel instance = new ApplicationOptionsPanel();
        instance.setNotebookDirectory(notebookDirectory);
        assertEquals(instance.getNotebookDirectory(), notebookDirectory);
    }

    /**
     * Test of setRestDirectory and getRestDirectory methods, of class ApplicationOptionsPanel.
     */
    @Test
    public void testSetGetRestDirectory() {
        System.out.println("setGetRestDirectory");
        String restDirectory = "";
        ApplicationOptionsPanel instance = new ApplicationOptionsPanel();
        instance.setRestDirectory(restDirectory);
        assertEquals(instance.getRestDirectory(), restDirectory);
    }

    /**
     * Test of setDownloadPythonClient and getDownloadPythonClient methods, of class ApplicationOptionsPanel.
     */
    @Test
    public void testSetGetDownloadPythonClient() {
        System.out.println("setGetDownloadPythonClient");
        boolean downloadPythonClient = false;
        ApplicationOptionsPanel instance = new ApplicationOptionsPanel();
        instance.setDownloadPythonClient(downloadPythonClient);
        assertEquals(instance.isDownloadPythonClientSelected(), downloadPythonClient);
    }

    /**
     * Test of setFontSize and getFontSize methods, of class ApplicationOptionsPanel.
     */
    @Test
    public void testSetGetFontSize() {
        System.out.println("setGetFontSize");
        String fontSize = "12";
        ApplicationOptionsPanel instance = new ApplicationOptionsPanel();
        instance.setFontSize(fontSize);
        assertEquals(instance.getFontSize(), fontSize);
    }

    /**
     * Test of getFontList method, of class ApplicationOptionsPanel.
     */
    @Test
    public void testGetFontList() {
        System.out.println("getFontList");
        ApplicationOptionsPanel instance = new ApplicationOptionsPanel();
        assertEquals(instance.getFontList(), GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    }

    /**
     * Test of setColorModeSelection and getColorModeSelection methods, of class ApplicationOptionsPanel.
     */
    @Test
    public void testSetGetColorModeSelection() {
        System.out.println("setGetColorModeSelection");
        String currentColorMode = "None";
        ApplicationOptionsPanel instance = new ApplicationOptionsPanel();
        instance.setColorModeSelection(currentColorMode);
        assertEquals(instance.getColorModeSelection(), currentColorMode);
    }

}

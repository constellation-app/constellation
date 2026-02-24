/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.text;

import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import org.testfx.api.FxToolkit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class SpellCheckerNGTest {

    private static final Logger LOGGER = Logger.getLogger(SpellCheckerNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of checkSpelling method, of class SpellChecker.
     */
    @Test
    public void testCheckSpelling() {
        System.out.println("checkSpelling");
        final SpellCheckingTextArea textArea = new SpellCheckingTextArea(true);
        final SpellChecker instance = new SpellChecker(textArea);
        instance.checkSpelling();
    }

    /**
     * Test of popUpSuggestionsListAction method, of class SpellChecker.
     */
    @Test
    public void testPopUpSuggestionsListAction() {
        System.out.println("popUpSuggestionsListAction");
        final MouseEvent event = null;
        final SpellCheckingTextArea textArea = new SpellCheckingTextArea(true);
        final SpellChecker instance = new SpellChecker(textArea);
        Platform.runLater(() -> instance.popUpSuggestionsListAction(event));
    }

//    /**
//     * Test of canCheckSpelling method, of class SpellChecker.
//     */
//    @Test
//    public void testCanCheckSpelling() {
//        System.out.println("canCheckSpelling");
//        String newText = "";
//        SpellChecker instance = null;
//        boolean expResult = false;
//        boolean result = instance.canCheckSpelling(newText);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    /**
//     * Test of turnOffSpellChecking method, of class SpellChecker.
//     */
//    @Test
//    public void testTurnOffSpellChecking() {
//        System.out.println("turnOffSpellChecking");
//        boolean turnOffSpellChecking = false;
//        SpellChecker instance = null;
//        instance.turnOffSpellChecking(turnOffSpellChecking);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    /**
//     * Test of addWordsToIgnore method, of class SpellChecker.
//     */
//    @Test
//    public void testAddWordsToIgnore() {
//        System.out.println("addWordsToIgnore");
//        SpellChecker instance = null;
//        instance.addWordsToIgnore();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}

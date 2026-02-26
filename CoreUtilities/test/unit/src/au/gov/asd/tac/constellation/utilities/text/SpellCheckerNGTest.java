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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
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
public class SpellCheckerNGTest {

    private static final Logger LOGGER = Logger.getLogger(SpellCheckerNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }

        SpellChecker.LANGTOOL_LOAD.get();
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
//    @Test
//    public void testCheckSpelling() throws Exception {
//        System.out.println("checkSpelling");
//        final String text = "This is some text that has a spelling error! jkl";
//        final SpellCheckingTextArea textArea = new SpellCheckingTextArea(true);
//        textArea.setText(text);
//
//        final Class<org.languagetool.JLanguageTool> mockJLanguageTool = mock(org.languagetool.JLanguageTool.class);
//        final Method mockCheckMethod = mock(Method.class);
//        final Class mockMatch = mock(Class.class);
//        final List<Object> mockListOfMatches = new ArrayList<>();
//        mockListOfMatches.add(mockMatch);
//
//        when(mockJLanguageTool.getMethod("check", String.class)).thenReturn(mockCheckMethod);
//        when(mockCheckMethod.invoke(any(Object.class), anyString())).thenReturn(mockListOfMatches);
//
//        try (final MockedStatic<LanguagetoolClassLoader> languagetoolClassLoader = Mockito.mockStatic(LanguagetoolClassLoader.class)) {
//            languagetoolClassLoader.when(LanguagetoolClassLoader::getJLanguagetool).thenReturn(mockJLanguageTool);
//            assertEquals(LanguagetoolClassLoader.getJLanguagetool(), mockJLanguageTool);
//
//            assertEquals(mockJLanguageTool.getMethod("check", String.class), mockCheckMethod);
//            assertEquals(mockCheckMethod.invoke(null, ""), mockListOfMatches);
//
//            final SpellChecker instance = new SpellChecker(textArea);
//            instance.checkSpelling();
//            System.out.println(instance.getMatches());
//        }
//    }

    /**
     * Test of checkSpelling method, of class SpellChecker.
     */
    @Test
    public void testCheckSpellingBlank() {
        System.out.println("checkSpelling blank");
        final String text = "";
        final SpellCheckingTextArea textArea = new SpellCheckingTextArea(true);
        textArea.setText(text);

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

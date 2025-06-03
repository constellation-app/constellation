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
package au.gov.asd.tac.constellation.views.notes.utilities;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author altair1673
 */
public class MarkdownTreeNGTest {

    private static final Logger LOGGER = Logger.getLogger(MarkdownTreeNGTest.class.getName());
    
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
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
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

    public String getTypeString(final MarkdownNode.Type type) {
        return switch (type) {
            case ROOT -> "ROOT";
            case HEADING -> "HEADING";
            case PARAGRAPH -> "PARAGRAPH";
            case BOLD -> "BOLD";
            case ITALIC -> "ITALIC";
            case NORMAL -> "NORMAL";
            case STRIKETHROUGH -> "STRIKETHROUGH";
            case ORDERED_LIST -> "ORDERED LIST";
            case LIST_END -> "LIST END";
            case LIST_ITEM -> "LIST ITEM";
            case LINE_BREAK -> "LINE BREAK";
            default -> "Type doesn't exist";
        };
    }

    /**
     * Test of parse method, of class MarkdownTree.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        MarkdownTree instance = new MarkdownTree("~~Strikethrough text~~");
        instance.parse();

        MarkdownNode root = instance.getRoot();
        assertTrue(getTypeString(root.getChildren().get(0).getType()).equals("STRIKETHROUGH"));

        instance = new MarkdownTree("**Bold text**");
        instance.parse();

        root = instance.getRoot();
        assertTrue(getTypeString(root.getChildren().get(0).getType()).equals("BOLD"));

        instance = new MarkdownTree("# Heading");
        instance.parse();

        root = instance.getRoot();
        assertTrue(getTypeString(root.getChildren().get(0).getType()).equals("HEADING"));

        instance = new MarkdownTree("__Bold text__");
        instance.parse();

        root = instance.getRoot();
        assertTrue(getTypeString(root.getChildren().get(0).getType()).equals("BOLD"));

        instance = new MarkdownTree("_Italic text_");
        instance.parse();

        root = instance.getRoot();
        assertTrue(getTypeString(root.getChildren().get(0).getType()).equals("ITALIC"));

        instance = new MarkdownTree("*Italic text*");
        instance.parse();

        root = instance.getRoot();
        assertTrue(getTypeString(root.getChildren().get(0).getType()).equals("ITALIC"));

        instance = new MarkdownTree("_Italic text_**Bold Text**~~Strikethrough Text~~");
        instance.parse();

        root = instance.getRoot();
        //assertEquals(root.getChildren().size(), 3);
        assertTrue(getTypeString(root.getChildren().get(0).getType()).equals("ITALIC"));
        assertTrue(getTypeString(root.getChildren().get(1).getType()).equals("BOLD"));
        assertTrue(getTypeString(root.getChildren().get(2).getType()).equals("STRIKETHROUGH"));

        instance = new MarkdownTree("*~~__Text with multiple effects__~~*");
        instance.parse();

        root = instance.getRoot();
        assertTrue(getTypeString(root.getChildren().get(0).getType()).equals("ITALIC"));
        assertTrue(getTypeString(root.getChildren().get(0).getChildren().get(0).getType()).equals("STRIKETHROUGH"));
        assertTrue(getTypeString(root.getChildren().get(0).getChildren().get(0).getChildren().get(0).getType()).equals("BOLD"));

    }

    /**
     * Test of getTextNodes method, of class MarkdownTree.
     */
    @Test
    public void testGetTextNodes() {
        System.out.println("getTextNodes");
        MarkdownTree instance = new MarkdownTree("~~Strikethrough text~~**Bold Text**_Italic Text_");
        instance.parse();

        List<TextHelper> result = instance.getTextNodes();

        result.forEach(text -> System.out.println(text.getText().getText()));
        
        assertTrue(result.get(1).isBold());
        assertTrue(result.get(0).getText().isStrikethrough());
        assertTrue(result.get(2).isItalic());
    }

    /**
     * Test of getRenderedText method, of class MarkdownTree.
     */
    @Test
    public void testGetRenderedText() {
        System.out.println("getRenderedText");
        MarkdownTree instance = new MarkdownTree("Some sample text");
        instance.parse();

        TextFlow result = instance.getRenderedText();
        assertTrue(result.getChildren().get(0) instanceof Text);
    }
}

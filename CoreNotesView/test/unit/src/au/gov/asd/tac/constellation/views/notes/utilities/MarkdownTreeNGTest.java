/*
 * Copyright 2010-2022 Australian Signals Directorate
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
import static org.testng.Assert.assertEquals;
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

    public MarkdownTreeNGTest() {
    }

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
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    public String getTypeString(final MarkdownNode.Type type) {
        switch (type) {
            case ROOT:
                return "ROOT";
            case HEADING:
                return "HEADING";
            case PARAGRAPH:
                return "PARAGRAPH";
            case BOLD:
                return "BOLD";
            case ITALIC:
                return "ITALIC";
            case NORMAL:
                return "NORMAL";
            case STRIKETHROUGH:
                return "STRIKETHROUGH";
            case ORDERED_LIST:
                return "ORDERED LIST";
            case LIST_END:
                return "LIST END";
            case LIST_ITEM:
                return "LIST ITEM";
            case LINE_BREAK:
                return "LINE BREAK";
            default:
                return "Type doesn't exist";
        }
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
        assertEquals(getTypeString(root.getChildren().get(0).getType()).equals("STRIKETHROUGH"), true);

        instance = new MarkdownTree("**Bold text**");
        instance.parse();

        root = instance.getRoot();
        assertEquals(getTypeString(root.getChildren().get(0).getType()).equals("BOLD"), true);

        instance = new MarkdownTree("# Heading");
        instance.parse();

        root = instance.getRoot();
        assertEquals(getTypeString(root.getChildren().get(0).getType()).equals("HEADING"), true);

        instance = new MarkdownTree("__Bold text__");
        instance.parse();

        root = instance.getRoot();
        assertEquals(getTypeString(root.getChildren().get(0).getType()).equals("BOLD"), true);

        instance = new MarkdownTree("_Italic text_");
        instance.parse();

        root = instance.getRoot();
        assertEquals(getTypeString(root.getChildren().get(0).getType()).equals("ITALIC"), true);

        instance = new MarkdownTree("*Italic text*");
        instance.parse();

        root = instance.getRoot();
        assertEquals(getTypeString(root.getChildren().get(0).getType()).equals("ITALIC"), true);

        instance = new MarkdownTree("_Italic text_**Bold Text**~~Strikethrough Text~~");
        instance.parse();

        root = instance.getRoot();
        //assertEquals(root.getChildren().size(), 3);
        assertEquals(getTypeString(root.getChildren().get(0).getType()).equals("ITALIC"), true);
        assertEquals(getTypeString(root.getChildren().get(1).getType()).equals("BOLD"), true);
        assertEquals(getTypeString(root.getChildren().get(2).getType()).equals("STRIKETHROUGH"), true);

        instance = new MarkdownTree("*~~__Text with multiple effects__~~*");
        instance.parse();

        root = instance.getRoot();
        assertEquals(getTypeString(root.getChildren().get(0).getType()).equals("ITALIC"), true);
        assertEquals(getTypeString(root.getChildren().get(0).getChildren().get(0).getType()).equals("STRIKETHROUGH"), true);
        assertEquals(getTypeString(root.getChildren().get(0).getChildren().get(0).getChildren().get(0).getType()).equals("BOLD"), true);

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

        //assertEquals(result.size(), 3);
        assertEquals(result.get(1).isBold(), true);
        assertEquals(result.get(0).getText().isStrikethrough(), true);
        assertEquals(result.get(2).isItalic(), true);
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
        assertEquals(result.getChildren().get(0) instanceof Text, true);
    }

}

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
package au.gov.asd.tac.constellation.help.utilities.toc;

import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class TreeNodeNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
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
     * Test of addChild method, of class TreeNode.
     */
    @Test
    public void testAddChild() {
        System.out.println("addChild");

        final String data = "data";
        final String childData = "childData";
        final TreeNode<String> instance = new TreeNode<>(data);
        final TreeNode<String> childInstance = new TreeNode<>(childData);

        instance.addChild(childInstance);

        assertEquals(instance.getChildren().size(), 1);
        final TreeNode<String> result = instance.getChildren().get(0);
        assertEquals(result.getData(), childData);

        assertEquals(childInstance.getParent(), instance);
    }

    /**
     * Test of addChildren method, of class TreeNode.
     */
    @Test
    public void testAddChildren() {
        System.out.println("addChildren");

        final String data = "data";
        final TreeNode<String> instance = new TreeNode<>(data);
        final TreeNode<String> childInstance1 = new TreeNode<>(null);
        final TreeNode<String> childInstance2 = new TreeNode<>(null);
        final TreeNode<String> childInstance3 = new TreeNode<>(null);

        final List<TreeNode<String>> children = new ArrayList<>();
        children.add(childInstance1);
        children.add(childInstance2);
        children.add(childInstance3);

        instance.addChildren(children);

        assertEquals(instance.getChildren().size(), 3);
        assertEquals(instance.getChildren().get(0), childInstance1);
        assertEquals(instance.getChildren().get(1), childInstance2);
        assertEquals(instance.getChildren().get(2), childInstance3);

        assertEquals(childInstance1.getParent(), instance);
        assertEquals(childInstance2.getParent(), instance);
        assertEquals(childInstance3.getParent(), instance);
    }

    /**
     * Test of printTree method, of class TreeNode.
     */
    @Test
    public void testPrintTree() {
        System.out.println("printTree");

        final TreeNode<Integer> root = getNestedTree();
        final String stringTree = getStringTree();

        final StringBuilder builder = new StringBuilder();

        final String appender = " ";
        assertEquals(stringTree, TreeNode.printTree(root, appender, builder).toString());
    }

    /**
     * Test of search method, of class TreeNode.
     */
    @Test
    public void testSearch() {
        System.out.println("search");

        final TreeNode<TOCItem> root = getNestedTOCTree();
        final TOCItem itemToFind = new TOCItem("0", "zero");
        assertEquals(root, TreeNode.search(itemToFind, root));

        final TOCItem itemToFind2 = new TOCItem("22", "twotwo");
        final TreeNode<TOCItem> expectedItem2 = new TreeNode<>(itemToFind2);
        assertEquals(expectedItem2.getData(), TreeNode.search(itemToFind2, root).getData());

        final TOCItem itemToFind3 = new TOCItem("14", "onefour");
        final TreeNode<TOCItem> expectedItem3 = new TreeNode<>(itemToFind3);
        assertEquals(expectedItem3, TreeNode.search(itemToFind3, root));

        final TOCItem itemToFind4 = new TOCItem("20", "twozero");
        final TreeNode<TOCItem> expectedItem4 = new TreeNode<>(itemToFind4);
        assertEquals(expectedItem4, TreeNode.search(itemToFind4, root));

    }

    private TreeNode<Integer> getNestedTree() {
        final TreeNode<Integer> root = new TreeNode<>(0);

        final  TreeNode<Integer> one0 = new TreeNode<>(10);
        final TreeNode<Integer> two0 = new TreeNode<>(20);
        one0.addChild(two0);

        final TreeNode<Integer> one1 = new TreeNode<>(11);
        final TreeNode<Integer> two1 = new TreeNode<>(21);
        final TreeNode<Integer> two2 = new TreeNode<>(22);
        one1.addChild(two1);
        one1.addChild(two2);

        final TreeNode<Integer> three0 = new TreeNode<>(30);
        two2.addChild(three0);

        final TreeNode<Integer> one2 = new TreeNode<>(12);
        final TreeNode<Integer> two3 = new TreeNode<>(23);
        one2.addChild(two3);

        final TreeNode<Integer> three1 = new TreeNode<>(31);
        three1.addChild(new TreeNode<>(41));
        two3.addChild(three1);

        root.addChild(one0);
        root.addChild(one1);
        root.addChild(one2);
        root.addChild(new TreeNode<>(13));
        root.addChild(new TreeNode<>(14));

        return root;
    }

    private String getStringTree() {
        return " 0 10 20 11 21 22 30 12 23 31 41 13 14";
    }

    private TreeNode<TOCItem> getNestedTOCTree() {
        final TOCItem rootItem = new TOCItem("0", "zero");
        final TreeNode<TOCItem> root = new TreeNode<>(rootItem);

        final TOCItem one0Item = new TOCItem("10", "onezero");
        final TOCItem two0Item = new TOCItem("20", "twozero");
        final TreeNode<TOCItem> one0 = new TreeNode<>(one0Item);
        final TreeNode<TOCItem> two0 = new TreeNode<>(two0Item);
        one0.addChild(two0);

        final TOCItem one1Item = new TOCItem("11", "oneone");
        final TOCItem two1Item = new TOCItem("21", "twoone");
        final TOCItem two2Item = new TOCItem("22", "twotwo");
        final TreeNode<TOCItem> one1 = new TreeNode<>(one1Item);
        final TreeNode<TOCItem> two1 = new TreeNode<>(two1Item);
        final TreeNode<TOCItem> two2 = new TreeNode<>(two2Item);
        one1.addChild(two1);
        one1.addChild(two2);

        root.addChild(one0);
        root.addChild(one1);

        final TOCItem one3Item = new TOCItem("13", "onethree");
        final TOCItem one4Item = new TOCItem("14", "onefour");
        root.addChild(new TreeNode<>(one3Item));
        root.addChild(new TreeNode<>(one4Item));

        return root;
    }
}

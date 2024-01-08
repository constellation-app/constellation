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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import java.util.Arrays;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author mimosa
 */
public class DataAccessViewCategoryPanelControllerNGTest {

    public DataAccessViewCategoryPanelControllerNGTest() {
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
     * Test of update method, of class DataAccessViewCategoryPanelController.
     * This method enables to refresh the lists when the panel is opened
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        final DataAccessViewCategoryPanelController dataAccessViewCategoryPanelontroller = mock(DataAccessViewCategoryPanelController.class);
        final DataAccessViewCategoryPanel dataAccessViewCategoryPanel = new DataAccessViewCategoryPanel(dataAccessViewCategoryPanelontroller);

        final DataAccessViewCategoryPanel dataAccessViewCategoryPanelSpy = spy(dataAccessViewCategoryPanel);

        final DefaultListModel listModelV = new DefaultListModel();
        final DefaultListModel listModelH = new DefaultListModel();

        final JList<String> list1 = new JList<String>();
        listModelV.addElement("Import");
        listModelV.addElement("Utility");

        list1.setModel(listModelV);

        final JList<String> list2 = new JList<String>();
        listModelH.addElement("Clean");
        listModelH.addElement("Developer");

        list2.setModel(listModelH);

        doReturn(listModelV).when(dataAccessViewCategoryPanelSpy).getlistModelLeft();
        dataAccessViewCategoryPanelSpy.setVisibleCategory("[Import, Utility]");

        verify(dataAccessViewCategoryPanelSpy, times(1)).setVisibleCategory(anyString());
        assertEquals(dataAccessViewCategoryPanelSpy.getlistModelLeft(), listModelV);

        doReturn(listModelH).when(dataAccessViewCategoryPanelSpy).getlistModelRight();
        dataAccessViewCategoryPanelSpy.setHiddenCategory("[Clean, Developer]");

        verify(dataAccessViewCategoryPanelSpy, times(1)).setHiddenCategory(anyString());
        assertEquals(dataAccessViewCategoryPanelSpy.getlistModelRight(), listModelH);

    }

    /**
     * Test of applyChanges method, of class
     * DataAccessViewCategoryPanelController. This method enables to write the
     * contents of the lists to the file
     */
    @Test
    public void testApplyChanges() {
        System.out.println("applyChanges");
        final DataAccessViewCategoryPanelController dataAccessViewCategoryPanelontroller = mock(DataAccessViewCategoryPanelController.class);
        final DataAccessViewCategoryPanel dataAccessViewCategoryPanel = new DataAccessViewCategoryPanel(dataAccessViewCategoryPanelontroller);

        final DataAccessViewCategoryPanel dataAccessViewCategoryPanelSpy = spy(dataAccessViewCategoryPanel);


        /* Test visible types*/
        final JList<String> list1 = new JList();
        final DefaultListModel listModel1 = new DefaultListModel();
        listModel1.addElement("Import");
        listModel1.addElement("Utility");

        list1.setModel(listModel1);

        when(dataAccessViewCategoryPanelSpy.getVisibleCategory())
                .thenReturn(Arrays.asList(list1.getModel().getElementAt(0), list1.getModel().getElementAt(1)));

        List<String> expResult = Arrays.asList(new String[]{"Import", "Utility"});
        List<String> result = dataAccessViewCategoryPanelSpy.getVisibleCategory();
        verify(dataAccessViewCategoryPanelSpy, times(1)).getVisibleCategory();
        assertEquals(result, expResult);

        /* Test hidden types*/
        final JList<String> list2 = new JList();
        final DefaultListModel listModel2 = new DefaultListModel();
        listModel2.addElement("Clean");
        listModel2.addElement("Developer");

        list2.setModel(listModel2);

        when(dataAccessViewCategoryPanelSpy.getHiddenCategory())
                .thenReturn(Arrays.asList(list2.getModel().getElementAt(0), list2.getModel().getElementAt(1)));

        List<String> expResult2 = Arrays.asList(new String[]{"Clean", "Developer"});
        List<String> result2 = dataAccessViewCategoryPanelSpy.getHiddenCategory();
        verify(dataAccessViewCategoryPanelSpy, times(1)).getHiddenCategory();
        assertEquals(result2, expResult2);
        
        dataAccessViewCategoryPanel.setVisibleCategory("[Import, Utility]");
        dataAccessViewCategoryPanel.setHiddenCategory("[Developer, Clean]");
        dataAccessViewCategoryPanel.restoreDefaults();

        assertEquals(dataAccessViewCategoryPanel.getVisibleCategory().toString(), "[Import, Clean, Utility, Developer]");
        assertEquals(dataAccessViewCategoryPanel.getHiddenCategory().toString(), "[]");
    }

    /**
     * Test of isValid method, of class DataAccessViewCategoryPanelController.
     */
    @Test
    public void testIsValid() {
        System.out.println("isValid");
        final DataAccessViewCategoryPanelController dataAccessViewCategoryPanelController = mock(DataAccessViewCategoryPanelController.class);
        final DataAccessViewCategoryPanel dataAccessViewCategoryPanel = new DataAccessViewCategoryPanel(dataAccessViewCategoryPanelController);

        final DataAccessViewCategoryPanel dataAccessViewCategoryPanelSpy = spy(dataAccessViewCategoryPanel);
        final DataAccessViewCategoryPanelController dataAccessViewCategoryPanelControllerSpy = spy(dataAccessViewCategoryPanelController);

        List<String> list1 = Arrays.asList("Import");
        List<String> list2 = Arrays.asList("Utility", "Clean", "Developer");

        when(dataAccessViewCategoryPanelSpy.getVisibleCategory()).thenReturn(list1);
        when(dataAccessViewCategoryPanelSpy.getHiddenCategory()).thenReturn(list2);
         when(dataAccessViewCategoryPanelControllerSpy.getPanel()).thenReturn(dataAccessViewCategoryPanelSpy);

        boolean expResult = true;
        boolean result = dataAccessViewCategoryPanelControllerSpy.isValid();
        assertEquals(result, expResult);
        verify(dataAccessViewCategoryPanelSpy, times(1)).getVisibleCategory();
        verify(dataAccessViewCategoryPanelSpy, times(1)).getHiddenCategory();
        verify(dataAccessViewCategoryPanelControllerSpy, times(1)).isValid();
    }

    /**
     * Test of isChanged method, of class DataAccessViewCategoryPanelController.
     */
    @Test
    public void testIsChanged() {
        System.out.println("isChanged");
        final DataAccessViewCategoryPanelController dataAccessViewCategoryPanelController = mock(DataAccessViewCategoryPanelController.class);
        final DataAccessViewCategoryPanel dataAccessViewCategoryPanel = new DataAccessViewCategoryPanel(dataAccessViewCategoryPanelController);

        final DataAccessViewCategoryPanel dataAccessViewCategoryPanelSpy = spy(dataAccessViewCategoryPanel);
        final DataAccessViewCategoryPanelController dataAccessViewCategoryPanelControllerSpy = spy(dataAccessViewCategoryPanelController);
        List<String> list1 = Arrays.asList("Clean", "Developer");

        when(dataAccessViewCategoryPanelControllerSpy.getPanel()).thenReturn(dataAccessViewCategoryPanelSpy);
        when(dataAccessViewCategoryPanelSpy.getHiddenCategory()).thenReturn(list1);

        boolean expResult = false;
        boolean result = dataAccessViewCategoryPanelControllerSpy.isChanged();
        assertEquals(result, expResult);
        verify(dataAccessViewCategoryPanelControllerSpy, times(1)).isChanged();
    }

}

/*
* Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.svg.resources;

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test {@link SVHTemplateConstants}
 * 
 * @author capricornunicorn123
 */
public class SVGTemplateConstantsNGTest {
    
    public SVGTemplateConstantsNGTest() {
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
     * Test of getFileName method, of class SVGTemplateConstants.
     */
    @Test
    public void testGetFileName() {
        assertEquals(SVGTemplateConstants.ARROW_HEAD_LINK.getFileName(), "LinkArrowHead.svg");
        assertEquals(SVGTemplateConstants.ARROW_HEAD_TRANSACTION.getFileName(), "TransactionArrowHead.svg");
        assertEquals(SVGTemplateConstants.ARROW_HEAD_TRANSACTION_LOOP.getFileName(), "TransactionArrowHeadLoop.svg");
        assertEquals(SVGTemplateConstants.CONNECTION_LOOP.getFileName(), "ConnectionLoop.svg");
        assertEquals(SVGTemplateConstants.CONNECTION_LINEAR.getFileName(), "ConnectionLinear.svg");
        assertEquals(SVGTemplateConstants.IMAGE.getFileName(), "Image.svg");
        assertEquals(SVGTemplateConstants.LABEL.getFileName(), "Label.svg");
        assertEquals(SVGTemplateConstants.LAYOUT.getFileName(), "Layout.svg");
        assertEquals(SVGTemplateConstants.LINK.getFileName(), "Link.svg");
        assertEquals(SVGTemplateConstants.NODE.getFileName(), "Node.svg");
    }
    
}

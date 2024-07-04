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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import au.gov.asd.tac.constellation.views.wordcloud.content.ContentVectorClusteringServices.RankTokenCalculator;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for ContentVectorClusteringServices
 * 
 * @author Delphinus8821
 */
public class ContentVectorClusteringServicesNGTest {
    
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
     * Test of createKMeansClusteringService method, of class ContentVectorClusteringServices.
     */
    @Test
    public void testCreateKMeansClusteringService() {
        System.out.println("createKMeansClusteringService");
        final DefaultTokenHandler handler = new DefaultTokenHandler();
        handler.registerToken("test", 1);
        final ClusterDocumentsParameters clusterDocumentsParams = ClusterDocumentsParameters.getDefaultParameters();
        final int numberOfElements = 4;
        ContentVectorClusteringServices instance = ContentVectorClusteringServices.createKMeansClusteringService(handler, clusterDocumentsParams, numberOfElements);
        ContentVectorClusteringServices.VectorWeightingCalculator expResult = instance.new CountAppearancesCalculator(numberOfElements, clusterDocumentsParams.getThresholdPercentage(), true, clusterDocumentsParams.isBinarySpace(), clusterDocumentsParams.isSignificantAboveThreshold(), clusterDocumentsParams.getWeightingExponent());
        assertEquals(instance.getWeightingCalculator().getClass(), expResult.getClass());
        
        clusterDocumentsParams.setThresholdMethod(ContentAnalysisOptions.TokenThresholdMethod.RANK);
        expResult = mock(RankTokenCalculator.class);
        instance = ContentVectorClusteringServices.createKMeansClusteringService(handler, clusterDocumentsParams, numberOfElements);
        assertEquals(instance.getWeightingCalculator().getClass(), expResult.getClass());
    }

}

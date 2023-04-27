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
package au.gov.asd.tac.constellation.views.errorreport;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author OrionsGuardian
 */
public class ErrorReportSessionDataNGTest {
    
    
    @Test
    public void runSessionDataTest() {
        final MockedStatic<ErrorReportDialogManager> erdmStatic = Mockito.mockStatic(ErrorReportDialogManager.class);
        final ErrorReportDialogManager erdm = mock(ErrorReportDialogManager.class);
        erdmStatic.when(ErrorReportDialogManager::getInstance).thenReturn(erdm);
        
        ErrorReportSessionData session = ErrorReportSessionData.getInstance();

        ErrorReportEntry testEntry = new ErrorReportEntry(Level.SEVERE, "heading1", "summary1", "message1", ErrorReportSessionData.getNextEntryId());
        ErrorReportEntry testEntry2 = new ErrorReportEntry(Level.SEVERE, "heading2", "summary2", "message2", ErrorReportSessionData.getNextEntryId());
        ErrorReportEntry testEntry3 = testEntry2.copy();
        testEntry3.setEntryId(ErrorReportSessionData.getNextEntryId());
        session.storeSessionError(testEntry);
        session.storeSessionError(testEntry2);
        session.storeSessionError(testEntry3);
        
        final List<String> filters = new ArrayList<>();
        filters.add(Level.SEVERE.getName());
        List<ErrorReportEntry> storedList = session.refreshDisplayedErrors(filters);
        
        // should contain 2 entries, with the second one having 2 occurences
        assertEquals(storedList.size(), 2);        
        final ErrorReportEntry storedData = session.findDisplayedEntryWithId(testEntry2.getEntryId());
        assertEquals(storedData.getOccurrences(), 2);
        
        session.requestScreenUpdate(true);
        assertTrue(ErrorReportSessionData.screenUpdateRequested);
        
        session.removeEntry(testEntry.getEntryId());
        storedList = session.refreshDisplayedErrors(filters);
        assertEquals(storedList.size(), 1);         
    }
    
}

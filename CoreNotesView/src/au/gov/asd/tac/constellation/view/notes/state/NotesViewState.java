/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.view.notes.state;

import java.util.List;
import java.util.ArrayList;
import org.geotools.xml.xsi.XSISimpleTypes.DateTime;

/**
 *
 * @author sol695510
 */
public class NotesViewState {
    
    
    // list of entries
    private final List<NotesViewEntry>NotesViewEntries;
    
    public NotesViewState() {
        this.NotesViewEntries = new ArrayList();
    }
 
    public class NotesViewEntry {
        
//        private final Boolean isNoteAuto;
//        private final DateTime dateTimeOfEntry;
//        private final String noteContent;
        
        public NotesViewEntry() {
            
        }
        //boolean
        //timestamp
        //string note
    
    }
    
}

/*
 * Copyright 2010-2020 Australian Signals Directorate
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

package au.gov.asd.tac.constellation.graph.file.open;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * Get the recent files for the welcome page 
 * 
 * @author Delphinus8821
 */

public class RecentFilesWelcomePage { 
    
    static final List<RecentFiles.HistoryItem> files = RecentFiles.getRecentFiles();
    static final List<String> fileNames = new ArrayList<>();
    
    
    private RecentFilesWelcomePage() {
    }
    
    /**
     * Gets the names of the files that were recently saved
     * 
     * @return list of recent file names
     */
    public static List<String> getFileNames(){
        RecentFiles.init(); 
        for (int i = 0; i < files.size(); i++){
            final FileObject fo = RecentFiles.convertPath2File(files.get(i).getPath());
            if (fo != null && !fileNames.contains(files.get(i).getFileName())){
                fileNames.add(files.get(i).getFileName());  
            }   
        } 
        return fileNames;
    }
    
    /**
     * Opens the file that matches the name of the parameter
     * 
     * @param fileName 
     */
    public static void openGraph(final String fileName){
        int index = -1;
        for (int i = 0; i < files.size(); i++){
            if(fileName.equals(files.get(i).getFileName())){
                index = i;
            }
        }  
        if (index != -1){
            final String path = files.get(index).getPath();
        
            final File f = new File(path);
            final File nf = FileUtil.normalizeFile(f);
            OpenFile.open(FileUtil.toFileObject(nf), -1);
        } 
    }  
}

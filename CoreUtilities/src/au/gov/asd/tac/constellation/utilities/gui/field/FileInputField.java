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
package au.gov.asd.tac.constellation.utilities.gui.field;

import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.LayoutConstants;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.TextType;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang3.StringUtils;
import org.openide.filesystems.FileChooserBuilder;

/**
 * A {@link ConstellationinputField} for managing {@link File} selection. 
 * 
 * @author capricornunicorn123
 */
public final class FileInputField extends ConstellationInputField<List<File>> {
    
    private ExtensionFilter extensionFilter = null;
    private boolean acceptAll = true;
    
    private static final Logger LOGGER = Logger.getLogger(FileInputField.class.getName());
        
    public FileInputField(){
        throw new UnsupportedOperationException();
    }
    
    public FileInputField(FileInputKind fileInputKind){
        this(fileInputKind, null);
    }
    
    public FileInputField(FileInputKind fileInputKind, TextType textTypeOverride){
        this(fileInputKind, textTypeOverride, 1);
    }
    
    public FileInputField(FileInputKind fileInputKind, TextType textTypeOverride, int suggestedHeight) {
        super(LayoutConstants.INPUT_POPUP, 
                
                textTypeOverride != null 
                        ? textTypeOverride 
                        : switch(fileInputKind){
                            case OPEN_MULTIPLE, OPEN_MULTIPLE_OBSCURED -> TextType.MULTILINE;
                            default -> TextType.SINGLELINE;
                        }
        );
        
        if (suggestedHeight > 1){
            setWrapText(false);
            setPrefRowCount(suggestedHeight);
        }
        
        this.setRightLabel(fileInputKind.toString());
        this.registerRightButtonEvent(event -> {
            final CompletableFuture dialogFuture;
            final List<File> files = new ArrayList<>();
            switch (fileInputKind) {
                case OPEN, OPEN_OBSCURED -> dialogFuture = FileChooser.openOpenDialog(getFileChooser("Open")).thenAccept(optionalFile -> optionalFile.ifPresent(openFile -> {
                    if (openFile != null) {
                        files.add(openFile);
                    }
                }));
                case OPEN_MULTIPLE, OPEN_MULTIPLE_OBSCURED -> dialogFuture = FileChooser.openMultiDialog(getFileChooser("Open File(s)")).thenAccept(optionalFile -> optionalFile.ifPresent(openFiles -> {
                    if (openFiles != null) {
                        files.addAll(openFiles);
                    }
                }));
                case SAVE, SAVE_OBSCURED -> dialogFuture = FileChooser.openSaveDialog(getFileChooser("Save")).thenAccept(optionalFile -> optionalFile.ifPresent(saveFile -> {
                    if (saveFile != null) {
                        //Save files may have been typed by the user and an extension may not have been specified.
                        final String fnam = saveFile.getAbsolutePath();
                        final String expectedExtension = extensionFilter.getExtensions().get(0);
                        if (!fnam.toLowerCase().endsWith(expectedExtension)) {
                            saveFile = new File(fnam + expectedExtension);
                        }
                        files.add(saveFile);
                    }
                }));
                default -> {
                    dialogFuture = null;
                    LOGGER.log(Level.FINE, "ignoring file selection type {0}.", fileInputKind);
                }
            }
            
            // As the dialog windows are completed on another thread 
            // the execution of this method must wait until the thread has finnished executing.
            if (dialogFuture != null){
                try {
                    dialogFuture.get();
                } catch (final InterruptedException ex){
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                } catch (final ExecutionException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                }
            }
            if (!files.isEmpty()) { 
                this.setValue(files);
            }
        });
    }


    /**
     * Sets wether this FileInputField will have an "All Files" filter.
     * @param acceptAll 
     */
    public void setAcceptAll(final boolean acceptAll){
        this.acceptAll = acceptAll;
    }
    
    /**
     * Creates a FileChooser for the Parameter
     * If an extension filter has not been specified, all file types will be accepted by default.
     * @param parameter
     * @param title
     * @return 
     */
    private FileChooserBuilder getFileChooser(final String title) {
        
        FileChooserBuilder fileChooserBuilder = new FileChooserBuilder(title)
                .setTitle(title)
                .setAcceptAllFileFilterUsed(extensionFilter == null ? true : acceptAll)
                .setFilesOnly(true);

        if (extensionFilter != null) {
            for (final String extension : extensionFilter.getExtensions()){
                // Add a file filter for all registered exportable file types.
                fileChooserBuilder = fileChooserBuilder.addFileFilter(new FileFilter(){
                    @Override
                    public boolean accept(final File file) {
                        final String name = file.getName();
                        return (file.isFile() && StringUtils.endsWithIgnoreCase(name, extension)) || file.isDirectory();
                    }
                    @Override
                    public String getDescription() {
                        return extensionFilter.getDescription();
                    }
                });
            }
        }
        return fileChooserBuilder;
    }

    /**
     * Sets the text value of this input field based on a list of provided files.
     * @param files 
     */
    @Override
    public void setValue(List<File> files) {
        StringBuilder sb = new StringBuilder();
        files.stream().forEach(file -> {
            sb.append(file.getAbsolutePath());
            if (files.indexOf(file) + 1 != files.size()) {
                sb.append("\n");
            }
        });
        this.setText(sb.toString());     
    }    
    
    private List<File> getFiles() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void setFileFilter(ExtensionFilter extensionFilter) {
        this.extensionFilter = extensionFilter;
    }
    
    @Override
    public ContextMenu getDropDown() {
        throw new UnsupportedOperationException("FileInputField does not provide a ContextMenu");
    }
    
    @Override
    public boolean isValid(){
        return true;
    }

    @Override
    public List<File> getValue() {
        return new ArrayList<>();
    }



    /**
     * Describes the method of file selection for a parameter of this type.
     */
    public enum FileInputKind {

        /**
         * Allows selection of multiple files. Displays "Open" on the button.
         */
        OPEN_MULTIPLE("Open"),
        /**
         * Allows selection of multiple files. Displays "..." on the button.
         */
        OPEN_MULTIPLE_OBSCURED("..."),
        /**
         * Allows selection of a single file only. Displays "Open" on the button.
         */
        OPEN("Open"),
        /**
         * Allows selection of a single file only. Displays "..." on the button.
         */
        OPEN_OBSCURED("..."),
        /**
         * Allows selection of a file, or entry of a non-existing but valid file
         * path. Displays "Save" on the button.
         */
        SAVE("Save"),
                /**
         * Allows selection of a file, or entry of a non-existing but valid file
         * path. Displays "..." on the button.
         */
        SAVE_OBSCURED("..."),;

        
        private final String text;
        
        private FileInputKind(final String text){
            this.text = text;
        }
        
        @Override
        public String toString(){
            return text;
        }
    }
    
}

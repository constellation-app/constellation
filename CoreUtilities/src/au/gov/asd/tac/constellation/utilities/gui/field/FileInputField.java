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

import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.FileInputKind;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.TextType;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.Button;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ButtonRight;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang3.StringUtils;
import org.openide.filesystems.FileChooserBuilder;

/**
 * A {@link ConstellationinputField} for managing {@link File} selection. 
 * 
 * @author capricornunicorn123
 */
public final class FileInputField extends ConstellationInputField<List<File>> implements ButtonRight{
    
    private ExtensionFilter extensionFilter = null;
    private boolean acceptAll = true;
    private final FileInputKind fileInputKind;
    
    private static final Logger LOGGER = Logger.getLogger(FileInputField.class.getName());
        
    public FileInputField(){
        throw new UnsupportedOperationException();
    }
    
    public FileInputField(final FileInputKind fileInputKind){
        this(fileInputKind, null);
    }
    
    public FileInputField(final FileInputKind fileInputKind, final TextType textTypeOverride){
        this(fileInputKind, textTypeOverride, 1);
    }
    
    public FileInputField(final FileInputKind fileInputKind, final TextType textTypeOverride, final int suggestedHeight) {
        super(textTypeOverride != null 
                        ? textTypeOverride 
                        : switch(fileInputKind){
                            case OPEN_MULTIPLE, OPEN_MULTIPLE_OBSCURED -> TextType.MULTILINE;
                            default -> TextType.SINGLELINE;
                        }
        );
        
        this.fileInputKind = fileInputKind;
        if (suggestedHeight > 1){
//            setWrapText(false);
            //setPrefRowCount(suggestedHeight);
        }
        initialiseDepedantComponents();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Local Private Methods">   
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
    
    private List<File> getFiles() {
        return new ArrayList<>();
    }

    public void setFileFilter(final ExtensionFilter extensionFilter) {
        this.extensionFilter = extensionFilter;
    }
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="Value Modification & Validation Implementation"> 
    @Override
    public List<File> getValue() {
        return getFiles();
    }    
    
    /**
     * Sets the text value of this input field based on a list of provided files.
     * @param files 
     */
    @Override
    public void setValue(final List<File> files) {
        final StringBuilder sb = new StringBuilder();
        files.stream().forEach(file -> {
            sb.append(file.getAbsolutePath());
            if (files.indexOf(file) + 1 != files.size()) {
                sb.append("\n");
            }
        });
        this.setText(sb.toString());     
    }    
    
    @Override
    public boolean isValid(){
        return true;
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="ContextMenuContributor Implementation"> 
    @Override
    public List<MenuItem> getLocalMenuItems() {
        final MenuItem format = new MenuItem("Select File");
        format.setOnAction(value -> executeRightButtonAction());
        return Arrays.asList(format);
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Button Event Implementation">    
    @Override
    public Button getRightButton() {
        return new Button(new Label(fileInputKind.toString()), Button.ButtonType.POPUP) {
            @Override
            public EventHandler<? super MouseEvent> action() {
                return event -> executeRightButtonAction();
            }
        };
    }
    
    @Override
    public void executeRightButtonAction() {
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
    }
    // </editor-fold>     

    // <editor-fold defaultstate="collapsed" desc="Drop Down Implementation">   
    @Override
    public ContextMenu getDropDown() {
        throw new UnsupportedOperationException("FileInputField does not provide a ContextMenu");
    }
    // </editor-fold> 
}

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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.FileInputField.FileInputKind;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javafx.stage.FileChooser.ExtensionFilter;
import org.openide.util.lookup.ServiceProvider;

/**
 * A FileParameterType controls a PluginParameter that holds a file path.
 *
 * @author sirius
 */
/**
 * The FileParameterType defines {@link PluginParameter} objects that hold one
 * or more {@link File} values.
 *
 * @author sirius
 */
@ServiceProvider(service = PluginParameterType.class)
public class FileParameterType extends PluginParameterType<FileParameterValue> {

    /**
     * A String ID with which to distinguish parameters that have this type.
     */
    public static final String ID = "file";

    /**
     * Constructs a new instance of this type.
     * <p>
     * Note: This constructor should not be called directly; it is public for
     * the purposes of lookup (which may be removed for types in the future). To
     * buildId parameters from the type, the static method
     * {@link #build buildId()} should be used, or the singleton
     * {@link #INSTANCE INSTANCE}.
     */
    public FileParameterType() {
        super(ID);
    }

    /**
     * The singleton instance of the type that should be used to construct all
     * parameters that have this type.
     */
    public static final FileParameterType INSTANCE = new FileParameterType();

    /**
     * Construct a new {@link PluginParameter} of this type.
     *
     * @param id The String id of the parameter to construct.
     * @return A {@link PluginParameter} of FileParameterType.
     */
    public static PluginParameter<FileParameterValue> build(final String id) {
        return new PluginParameter<>(new FileParameterValue(), INSTANCE, id);
    }

    /**
     * Construct a new {@link PluginParameter} of this type with initial value
     * represented by the given {@link FileParameterValue}.
     *
     * @param id The String id of the parameter to construct.
     * @param pv A {@link FileParameterValue} describing the initial value of
     * the parameter being constructed.
     * @return A {@link PluginParameter} of FileParameterType.
     */
    public static PluginParameter<FileParameterValue> build(final String id, final FileParameterValue pv) {
        return new PluginParameter<>(pv, INSTANCE, id);
    }

    /**
     * Set the kind of file selection for the given parameter.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @param kind A {@link FileParameterKind} constant to set for the given
     * parameter.
     */
    public static void setKind(final PluginParameter<FileParameterValue> parameter, final FileInputKind kind) {
        parameter.getParameterValue().setKind(kind);
    }

    /**
     * Get the kind of file selection for the given parameter.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @return The {@link FileParameterKind} for the given parameter.
     */
    public static FileInputKind getKind(final PluginParameter<FileParameterValue> parameter) {
        return parameter.getParameterValue().getKind();
    }

    /**
     * Set the file selection filter for the given parameter. This indicates
     * that only files with certain extensions should be selectable when
     * performing file selection for this parameter.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @param fileFilter An {@link ExtensionFilter} object that describes how
     * files should be filtered by extension.
     */
    public static void setFileFilters(final PluginParameter<FileParameterValue> parameter, final ExtensionFilter fileFilter) {
        parameter.getParameterValue().setFilter(fileFilter);
    }

    /**
     * Get the file selection filter for the given parameter. This indicates
     * that only files with certain extensions should be selectable when
     * performing file selection for this parameter.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @return The {@link ExtensionFilter} object that describes how files
     * should be filtered by extension for the given parameter.
     */
    public static ExtensionFilter getFileFilters(final PluginParameter<FileParameterValue> parameter) {
        return parameter.getParameterValue().getFilter();
    }
    
    /**
     * Determine if the "All Files" selection filter should be enabled for the given parameter. 
     * This will be active by default when the user is presented with a file selection window. 
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @return boolean that indicates is the accept all file selection filter is enabled.
     */
    public static boolean isAcceptAllFileFilterUsed(final PluginParameter<FileParameterValue> parameter) {
        return parameter.getParameterValue().isAcceptAllFileFilterUsed();
    }
    
    /**
     * Enable the "All Files" selection filter for the given parameter. 
     * This will be active by default when the user is presented with a file selection window. 
     *
     * @param parameter A {@link PluginParameter} of this type.
     */
    public static void enableAcceptAllFileFilter(final PluginParameter<FileParameterValue> parameter) {
        parameter.getParameterValue().enableAcceptAllFileFilter();
    }
    
    @Override
    public String validateString(final PluginParameter<FileParameterValue> param, final String stringValue) {
        final FileParameterValue v = param.getParameterValue();
        return v.validateString(stringValue);
    }

    /**
     * Describes the method of file selection for a parameter of this type.
     */
    public enum FileParameterKind {

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
        
        private FileParameterKind(final String text){
            this.text = text;
        }
        
        @Override
        public String toString(){
            return text;
        }
    }

    /**
     * An implementation of {@link ParameterValue} corresponding to this type.
     * It holds one or more {@link File} values, as well as a
     * {@link FileParameterKind} and an {@link ExtensionFilter} which describe
     * file selection.
     */
    public static class FileParameterValue extends ParameterValue {

        private final List<String> files;
        private FileInputKind kind;
        private ExtensionFilter filter;
        private boolean acceptAllFileFilterUsed;
        

        /**
         * Constructs a new FileParameterValue
         */
        public FileParameterValue() {
            files = new ArrayList<>();
            kind = FileInputKind.OPEN_MULTIPLE; // backward-compatible default.
            filter = null;
            acceptAllFileFilterUsed = false;
        }

        /**
         * Constructs a new FileParameterValue holding files corresponding to
         * the specified list of string paths.
         *
         * @param files A list of strings representing the file paths that this
         * parameter value should hold.
         */
        public FileParameterValue(final List<String> files) {
            this.files = new ArrayList<>();
            this.files.addAll(files);
            kind = FileInputKind.OPEN_MULTIPLE; // backward-compatible default.
            filter = null;
            acceptAllFileFilterUsed = false;
        }

        /**
         * Constructs a new FileParameterValue with the same values as the
         * specified FileParameterValue.
         *
         * @param fpv The {@link FileParameterValue} to copy.
         */
        public FileParameterValue(final FileParameterValue fpv) {
            files = new ArrayList<>(fpv.files);
            kind = fpv.kind;
            filter = fpv.filter;
            acceptAllFileFilterUsed = fpv.acceptAllFileFilterUsed;
        }

        /**
         * Set the current value
         *
         * @param newFiles The list of {@link File} objects for this parameter
         * value to hold.
         * @return True if the new value was different to the current value,
         * false otherwise.
         */
        public boolean set(final List<File> newFiles) {
            // TODO: determine a better if condition
            // this one is comparing List<String> with List<File>
            if (!Objects.equals(files, newFiles)) {
                final List<File> nf = newFiles != null ? newFiles : Collections.emptyList();
                files.clear();
                nf.forEach(f -> files.add(f.getAbsolutePath()));
                return true;
            }

            return false;
        }

        /**
         * Get the current value from this parameter value.
         *
         * @return The list of {@link File} objects that this parameter value is
         * holding.
         */
        public List<File> get() {
            final List<File> fileObjects = new ArrayList<>();
            files.forEach(f -> fileObjects.add(new File(f.trim())));
            return Collections.unmodifiableList(fileObjects);
        }

        /**
         * Get the {@link FileParameterKind}.
         *
         * @return A {@link FileParameterKind} constant indicating the kind of
         * file selection.
         */
        public FileInputKind getKind() {
            return kind;
        }

        /**
         * Set the {@link FileParameterKind}.
         *
         * @param kind The {@link FileParameterKind} constant indicating the
         * kind of file selection.
         */
        public void setKind(final FileInputKind kind) {
            this.kind = kind;
        }

        /**
         * Get the {@link ExtensionFilter}.
         *
         * @return An {@link ExtensionFilter} indicating the filtering of file
         * selection.
         */
        public ExtensionFilter getFilter() {
            return filter;
        }

        /**
         * Set the {@link ExtensionFilter}.
         *
         * @param filter The {@link ExtensionFilter} indicating the filtering of
         * file selection.
         */
        public void setFilter(final ExtensionFilter filter) {
            this.filter = filter;
        }
        
        /**
         * Determine if the "All Files" selection filter is used.
         *
         * @return boolean indicating if the accept all file filter is used.
         */
        public boolean isAcceptAllFileFilterUsed() {
            return acceptAllFileFilterUsed;
        }
        
        /**
         * Ensure the "All Files" selection filter is used.
         */
        public void enableAcceptAllFileFilter() {
            acceptAllFileFilterUsed = true;
        }

        @Override
        public String validateString(final String s) {
            final String[] fileStrings = s.split("\n");
            final boolean filesValid = Arrays.stream(fileStrings).allMatch(fileString ->{
                final File validationFile = new File(fileString);
                return (validationFile.isDirectory() || (validationFile.getParentFile() != null && validationFile.getParentFile().exists()));
            });
            
            return s.isBlank() || filesValid ? null : "The specified file path does not contain valid directories";
        }

        @Override
        public boolean setStringValue(final String s) {
            final String[] names = s.split(SeparatorConstants.SEMICOLON);
            final List<String> newFiles = new ArrayList<>();
            for (final String name : names) {
                if (!name.isEmpty()) {
                    newFiles.add(name);
                }
            }

            if (!Objects.equals(files, newFiles)) {
                files.clear();
                files.addAll(newFiles);
                return true;
            }
            return false;
        }

        @Override
        public Object getObjectValue() {
            final List<File> fileObjects = new ArrayList<>();
            files.forEach(f -> fileObjects.add(new File(f)));
            return fileObjects;
        }

        @Override
        public boolean setObjectValue(final Object o) {
            @SuppressWarnings("unchecked") //o will be list of files which extends object type
            final List<File> listFileO = (List<File>) o;
            return set(listFileO);
        }

        @Override
        protected FileParameterValue createCopy() {
            return new FileParameterValue(this);
        }

        @Override
        public int hashCode() {
            return files.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final FileParameterValue other = (FileParameterValue) obj;
            return files.equals(other.files);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            files.stream().forEach(file -> {
                if (sb.length() > 0) {
                    sb.append(SeparatorConstants.SEMICOLON);
                }

                sb.append(file);
            });

            return sb.toString();
        }
    }
}

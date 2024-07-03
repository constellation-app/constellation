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
package au.gov.asd.tac.constellation.utilities.file;

/**
 *
 * @author aldebaran30701
 */
public class FileExtensionConstants {

    private FileExtensionConstants() {
        throw new IllegalStateException("Utility class");
    }

    // Constellation file formats
    public static final String STAR = ".star";
    public static final String NEBULA = ".nebula";
    public static final String STAR_AUTOSAVE = ".star_auto";

    // Infomap extensions
    public static final String TREE = ".tree";
    public static final String CLUSTER = ".clu";
    public static final String RANK = ".rank";
    public static final String FLOW = ".flow";

    // Value file extensions
    public static final String TAB_SEPARATED_VALUE = ".tsv";
    public static final String COMMA_SEPARATED_VALUE = ".csv";
    public static final String XLSX = ".xlsx";
    public static final String XLS = ".xls";
    public static final String XML = ".xml";
    
    // Font extensions
    public static final String OPEN_TYPE_FONT = ".otf";
    public static final String TRUE_TYPE_FONT = ".ttf";

    // Zip extensions
    public static final String GZIP = ".gz";

    // Common file extensions
    public static final String TEXT = ".txt";
    public static final String JAVA = ".java";
    public static final String JAR = ".jar";
    public static final String CLASS = ".class";
    public static final String JAVASCRIPT = ".js";
    public static final String JSON = ".json";
    public static final String MARKDOWN = ".md";
    public static final String HTML = ".html";
    public static final String PYTHON = ".py";
    public static final String CASCADING_STYLE_SHEET = ".css";
    public static final String BINARY = ".bin";
    public static final String BACKUP = ".bak";
    
    //Geographical file extensions
    public static final String GEO_PACKAGE = ".gpkg";
    public static final String KML = ".kml";
    public static final String SHAPE = ".shp";
    
    //Graph file extensions
    public static final String GML = ".gml";
    public static final String PAJEK = ".net";
    public static final String GRAPHML = ".graphml";
        
    // Image extensions
    public static final String JPG = ".jpg";
    public static final String PNG = ".png";
    public static final String GIF = ".gif";
    public static final String SVG = ".svg"; 
    
}

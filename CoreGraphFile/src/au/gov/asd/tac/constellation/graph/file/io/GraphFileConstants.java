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
package au.gov.asd.tac.constellation.graph.file.io;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;

/**
 * Constants used for the Graph file format.
 *
 * @author algol
 */
public final class GraphFileConstants {

    /**
     * No constructor.
     */
    private GraphFileConstants() {
        throw new IllegalStateException("Constants Class");
    }
    /**
     * The file extensions for ZipEntry files.
     */
    public static final String FILE_EXTENSION = FileExtensionConstants.TEXT;
    /**
     * The field separator in CSV files.
     */
    public static final char SEPARATOR = '\t';
    /**
     * The separator in field descriptions.
     */
    public static final char FQ_SEPARATOR = '|';
    /**
     * The vertex id in the file.
     */
    public static final String VX_ID = "vx_id_";

    /**
     * The reference to a transaction's id in the file.
     */
    public static final String TX_ID = "tx_id_";

    /**
     * The reference to a transaction's source vertex in the file.
     */
    public static final String SRC = "vx_src_";
    /**
     * The reference to a transaction's destination vertex in the file.
     */
    public static final String DST = "vx_dst_";

    /**
     * Field indicating whether the transaction is directed or not.
     */
    public static final String DIR = "tx_dir_";
}

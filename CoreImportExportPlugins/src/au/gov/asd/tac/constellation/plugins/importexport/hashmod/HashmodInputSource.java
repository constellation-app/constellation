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
package au.gov.asd.tac.constellation.plugins.importexport.hashmod;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * In HashmodInputSource provides an abstract source for data, whether or not it
 * is a real file or an InputStream.
 *
 * @author CrucisGamma
 */
public class HashmodInputSource {

    private final File file;
    private final InputStream inputStream;

    public HashmodInputSource(File file) {
        this.file = file;
        this.inputStream = null;
    }

    public HashmodInputSource(InputStream inputStream) {
        this.file = null;
        this.inputStream = inputStream;
    }

    public File getFile() {
        return file;
    }

    public InputStream getInputStream() throws IOException {
        if (inputStream != null) {
            return inputStream;
        }

        if (file != null && file.exists()) {
            return new FileInputStream(file);
        }

        return null;
    }
}

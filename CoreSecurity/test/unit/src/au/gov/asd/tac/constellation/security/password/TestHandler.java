/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.security.password;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Logger Handler to be used with testing password obfuscator and deobfuscator
 *
 * @author antares
 */
public class TestHandler extends Handler {
    
    private final List<LogRecord> records = new LinkedList<>();
    
    protected LogRecord getLastLog() {
        return records.getLast();
    }
    
    protected List<LogRecord> getLogs() {
        return Collections.unmodifiableList(records);
    }

    @Override
    public void publish(final LogRecord logRecord) {
        records.add(logRecord);
    }

    @Override
    public void flush() {
        records.clear();
    }

    @Override
    public void close() throws SecurityException {
        records.clear();
    }
}

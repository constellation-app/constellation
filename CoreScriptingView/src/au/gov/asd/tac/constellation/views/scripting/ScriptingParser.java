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
package au.gov.asd.tac.constellation.views.scripting;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;

/**
 * A custom script parser for use in the Scripting View.
 *
 * @author algol
 */
public class ScriptingParser extends AbstractParser {

    private String msg;
    private int line;
    private int offset;
    private int length;

    public ScriptingParser() {
        msg = null;
        line = -1;
    }

    @Override
    public ParseResult parse(final RSyntaxDocument doc, final String style) {
        final DefaultParseResult result = new DefaultParseResult(this);
        final int lineCount = doc.getDefaultRootElement().getElementCount();
        final long t0 = System.currentTimeMillis();
        final long time = System.currentTimeMillis() - t0;
        result.setParsedLines(0, lineCount - 1);
        result.setParseTime(time);

        if (msg != null) {
            final ParserNotice pn = new DefaultParserNotice(this, msg, line, offset, length);
            result.addNotice(pn);
        }

        return result;
    }

    public void setError(final String msg, final int line) {
        setError(msg, line, -1, -1);
    }

    public void setError(final String msg, final int line, final int offset, final int length) {
        this.msg = msg;
        this.line = line;
        this.offset = offset;
        this.length = length;
    }

    public void clearError() {
        msg = null;
    }
}

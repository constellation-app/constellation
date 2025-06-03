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
package au.gov.asd.tac.constellation.plugins.importexport.model;

/**
 *
 * @author sirius
 */
public class CellValue implements Comparable<CellValue> {

    private final String originalText;

    @SuppressWarnings("rawtypes") //raw type needed as comparable value can be string or integer
    private final Comparable comparable;
    private String text;
    private String message;
    private boolean error;
    private boolean included = true;

    public CellValue(final String text) {
        this.comparable = this.text = this.originalText = text;
    }

    public CellValue(final int value) {
        this.text = this.originalText = String.valueOf(value);
        this.comparable = value;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return error;
    }

    public void setMessage(final String message, final boolean error) {
        this.message = message;
        this.error = error;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setIncluded(final boolean included) {
        this.included = included;
    }

    public boolean isIncluded() {
        return included;
    }

    @Override
    public int compareTo(CellValue o) {
        if (comparable == null) {
            return o.comparable == null ? 0 : -1;
        } else if (o.comparable == null) {
            return 1;
        } else {
            return comparable.compareTo(o.comparable);
        }
    }
}

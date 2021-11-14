/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.help.utilities.toc;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * Java in memory representation of a Table of contents item
 *
 * @author aldebaran30701
 */
public class TOCItem {

    private final String target;
    private final String text;

    public TOCItem(final String text, final String target) {
        this.text = text;
        this.target = target;
    }

    protected String getTarget() {
        return target;
    }

    protected String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text + " -> " + target;
    }

    /**
     * Check whether the values for text and target are both the same
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(final Object obj) {
        return obj != null && obj instanceof TOCItem
                && StringUtils.equals(text, ((TOCItem) obj).getText())
                && StringUtils.equals(target, ((TOCItem) obj).getTarget());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.target);
        hash = 97 * hash + Objects.hashCode(this.text);
        return hash;
    }

}

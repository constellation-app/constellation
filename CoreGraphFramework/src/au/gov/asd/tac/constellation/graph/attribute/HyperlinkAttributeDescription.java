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
package au.gov.asd.tac.constellation.graph.attribute;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values are URIs.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeDescription.class)
public class HyperlinkAttributeDescription extends AbstractObjectAttributeDescription<URI> {

    public static final String ATTRIBUTE_NAME = "hyperlink";
    public static final Class<URI> NATIVE_CLASS = URI.class;
    public static final URI DEFAULT_VALUE = null;

    public HyperlinkAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    @SuppressWarnings("unchecked") // Casts are manually checked
    protected URI convertFromObject(final Object object) {
        try {
            return super.convertFromObject(object);
        } catch (final IllegalArgumentException ex) {
            if (object instanceof URL url) {
                try {
                    return url.toURI();
                } catch (final URISyntaxException ex2) {
                    throw new IllegalArgumentException(String.format(
                            "Error converting Object '%s' to hyperlink", object.getClass()));
                }
            } else {
                throw ex;
            }
        }
    }

    @Override
    protected URI convertFromString(final String string) {
        if (StringUtils.isBlank(string)) {
            return getDefault();
        } else {
            try {
                return new URI(string);
            } catch (final URISyntaxException ex) {
                throw new IllegalArgumentException(String.format(
                        "Error converting String '%s' to hyperlink", string));
            }
        }
    }
}

/*
 * Copyright 2010-2019 Australian Signals Directorate
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
import org.openide.util.lookup.ServiceProvider;

/**
 * Hyperlink attribute.
 *
 * @author sirius
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
    @SuppressWarnings("unchecked") //Casts are manually checked
    protected URI convertFromObject(final Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof URI) {
            return (URI) object;
        } else if (object instanceof URL) {
            try {
                return ((URL) object).toURI();
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException("Error converting object to hyperlink: " + object);
            }
        } else if (object instanceof String) {
            return convertFromString((String) object);
        } else {
            throw new IllegalArgumentException("Error converting object to hyperlink: " + object);
        }
    }

    @Override
    protected URI convertFromString(String string) {
        if (string == null) {
            return null;
        } else {
            try {
                return new URI(string);
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException("Error converting object to hyperlink: " + string);
            }
        }
    }

    @Override
    public boolean canBeImported() {
        return false;
    }
}

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
package au.gov.asd.tac.constellation.importexport.geospatial;

import au.gov.asd.tac.constellation.pluginframework.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.utilities.geospatial.Shape;
import java.util.Objects;
import org.opengis.referencing.FactoryException;
import org.openide.util.Exceptions;

/**
 * Spatial Reference Parameter Value
 *
 * @author cygnus_x-1
 */
public class SpatialReferenceParameterValue extends ParameterValue {

    private Shape.SpatialReference spatialReference;

    public SpatialReferenceParameterValue() {
        this.spatialReference = null;
    }

    public SpatialReferenceParameterValue(final Shape.SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
    }

    @Override
    public String validateString(final String s) {
        try {
            Shape.SpatialReference.valueOf(s);
        } catch (IllegalArgumentException ex) {
            return String.format("%s is not a valid element type", s);
        }
        return null;
    }

    @Override
    public boolean setStringValue(final String s) {
        final Shape.SpatialReference stringSpatialReference = Shape.SpatialReference.valueOf(s);
        final boolean equal = Objects.equals(stringSpatialReference, spatialReference);
        if (!equal) {
            spatialReference = stringSpatialReference;
        }
        return equal;
    }

    @Override
    public Object getObjectValue() {
        return spatialReference;
    }

    @Override
    public boolean setObjectValue(final Object o) {
        if (o instanceof Shape.SpatialReference) {
            final Shape.SpatialReference reference = (Shape.SpatialReference) o;
            final boolean equal = Objects.equals(reference, spatialReference);
            if (!equal) {
                spatialReference = reference;
            }
            return equal;
        }
        return false;
    }

    @Override
    protected ParameterValue createCopy() {
        return new SpatialReferenceParameterValue(spatialReference);
    }

    @Override
    public String toString() {
        String stringSRS = "No Value";
        if (spatialReference == null) {
            return stringSRS;
        } else {
            try {
                stringSRS = spatialReference.getSrs();
            } catch (final FactoryException ex) {
                Exceptions.printStackTrace(ex);
            }
            return stringSRS;
        }
    }
}

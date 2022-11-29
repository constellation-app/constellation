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
package au.gov.asd.tac.constellation.views.mapview.exporters;

/**
 * An interface for an export plugin which should be available to the Map View.
 *
 * @author cygnus_x-1
 */
public interface MapExporter {

    public abstract String getDisplayName();

    public abstract String getPluginReference();

    public static class MapExporterWrapper {

        private final MapExporter exporter;

        public MapExporterWrapper(final MapExporter exporter) {
            this.exporter = exporter;
        }

        public final MapExporter getExporter() {
            return exporter;
        }

        @Override
        public final String toString() {
            return exporter.getDisplayName();
        }
    }
}

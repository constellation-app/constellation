/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.analyticview.utilities;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities class to hold caches of the initial state of each graph 
 * before the size and color buttons are activated in the analytic view.
 * 
 * @author Delphinus8821
 */
public class AnalyticTranslatorUtilities {
    
    private static final Map<String, Map<Integer, Float>> vertexSizeCache = new HashMap<>();
    private static final Map<String, Map<Integer, Float>> transactionSizeCache = new HashMap<>();
    private static final Map<String, Map<Integer, ConstellationColor>> vertexColorCache = new HashMap<>();
    private static final Map<String, Map<Integer, ConstellationColor>> transactionColorCache = new HashMap<>();
    private static final Map<String, Map<Integer, Float>> vertexHideCache = new HashMap<>();
    private static final Map<String, Map<Integer, Float>> transactionHideCache = new HashMap<>();
    
    private AnalyticTranslatorUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, Map<Integer, Float>> getVertexSizeCache() {
        return vertexSizeCache;
    }

    public static Map<String, Map<Integer, Float>> getTransactionSizeCache() {
        return transactionSizeCache;
    }

    public static Map<String, Map<Integer, ConstellationColor>> getVertexColorCache() {
        return vertexColorCache;
    }

    public static Map<String, Map<Integer, ConstellationColor>> getTransactionColorCache() {
        return transactionColorCache;
    }
    
    public static Map<String, Map<Integer, Float>> getVertexHideCache() {
        return vertexHideCache;
    }
    
    public static Map<String, Map<Integer, Float>> getTransactionHideCache() {
        return transactionHideCache;
    }
    
    public static void addToVertexSizeCache(final String currentGraphKey, final Map<Integer, Float> vertexSizes) {
        vertexSizeCache.put(currentGraphKey, vertexSizes);
    }
    
    public static void addToTransactionSizeCache(final String currentGraphKey, final Map<Integer, Float> transactionSizes) {
        transactionSizeCache.put(currentGraphKey, transactionSizes);
    }
    
    public static void addToVertexColorCache(final String currentGraphKey, final Map<Integer, ConstellationColor> vertexColors) {
        vertexColorCache.put(currentGraphKey, vertexColors);
    }
    
    public static void addToTransactionColorCache(final String currentGraphKey, final Map<Integer, ConstellationColor> transactionColors) {
        transactionColorCache.put(currentGraphKey, transactionColors);
    }
    
    public static void addToVertexHideCache(final String currentGraphKey, final Map<Integer, Float> vertexHideValues) {
        vertexHideCache.put(currentGraphKey, vertexHideValues);
    }
    
    public static void addToTransactionHideCache(final String currentGraphKey, final Map<Integer, Float> transactionHideValues) {
        transactionHideCache.put(currentGraphKey, transactionHideValues);
    }
    
}

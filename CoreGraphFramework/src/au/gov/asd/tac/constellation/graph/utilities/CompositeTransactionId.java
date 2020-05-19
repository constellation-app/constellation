/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.utilities;

/**
 *
 * @author twilight_sparkle
 */
public class CompositeTransactionId {
    
    private static final String COMPOSITE = "composite:";

    private String originalSourceNode;
    private String originalDestinationNode;
    private boolean sourceContracted;
    private boolean destContracted;
    private final String suffix;

    public CompositeTransactionId(final String originalSourceNode, final String originalDestinationNode, final String suffix, final boolean sourceContracted, final boolean destContracted) {
        this.originalSourceNode = originalSourceNode;
        this.originalDestinationNode = originalDestinationNode;
        this.sourceContracted = sourceContracted;
        this.destContracted = destContracted;
        this.suffix = suffix;
    }

    public String getOriginalSourceNode() {
        return originalSourceNode;
    }

    public void setOriginalSourceNode(String originalSourceNode) {
        this.originalSourceNode = originalSourceNode;
    }

    public String getOriginalDestinationNode() {
        return originalDestinationNode;
    }

    public void setOriginalDestinationNode(String originalDestinationNode) {
        this.originalDestinationNode = originalDestinationNode;
    }

    public boolean isSourceContracted() {
        return sourceContracted;
    }

    public void setSourceContracted(boolean sourceContracted) {
        this.sourceContracted = sourceContracted;
    }

    public boolean isDestContracted() {
        return destContracted;
    }

    public void setDestContracted(boolean destContracted) {
        this.destContracted = destContracted;
    }
    
    public static CompositeTransactionId fromString(final String id) {
        if (id == null || !id.startsWith(COMPOSITE)) {
            return new CompositeTransactionId(null, null, id, false, false);
        } else {
            final boolean sourceContracted = Boolean.valueOf(id.substring(id.indexOf(':') + 1, id.indexOf(':', id.indexOf(':') + 1)));
            final boolean destContracted = Boolean.valueOf(id.substring(id.indexOf(':', id.indexOf(':') + 1) + 1, id.indexOf('[')));
            final String source = id.indexOf('[') + 1 == id.indexOf("->") ? null : id.substring(id.indexOf('[') + 1, id.indexOf("->"));
            final String dest = id.indexOf("->") + 2 == id.indexOf("]_") ? null : id.substring(id.indexOf("->") + 2, id.indexOf("]_"));
            final String suffix = id.substring(id.indexOf("]_") + 2);
            return new CompositeTransactionId(source, dest, suffix, sourceContracted, destContracted);
        }
    }

    @Override
    public String toString() {
        if (originalSourceNode == null && originalDestinationNode == null) {
            return suffix;
        } else if (originalSourceNode == null) {
            return COMPOSITE + sourceContracted + ":" + destContracted + "[->" + originalDestinationNode + "]_" + suffix;
        } else if (originalDestinationNode == null) {
            return COMPOSITE + sourceContracted + ":" + destContracted + "[" + originalSourceNode + "->]_" + suffix;
        } else {
            return COMPOSITE + sourceContracted + ":" + destContracted + "[" + originalSourceNode + "->" + originalDestinationNode + "]_" + suffix;
        }
    }
}

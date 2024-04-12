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
package au.gov.asd.tac.constellation.graph.schema.analytic.concept;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.HyperlinkAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * Content Concept
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = SchemaConcept.class)
public class ContentConcept extends SchemaConcept {

    @Override
    public String getName() {
        return "Content";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(AnalyticConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }

    public static class VertexAttribute {

        public static final SchemaAttribute APPLICATION = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Application")
                .setDescription("The program or application which had the content")
                .build();
        public static final SchemaAttribute AUTHOR = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Author").
                setDescription("The author of the content")
                .build();
        public static final SchemaAttribute CHAR_COUNT = new SchemaAttribute.Builder(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "Content.CharacterCount")
                .setDescription("The character count excluding spaces")
                .build();
        public static final SchemaAttribute COMPANY = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Company")
                .setDescription("The company authoring the content")
                .build();
        public static final SchemaAttribute CONTENT = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content")
                .setDescription("Any textual content applying to the node")
                .build();
        public static final SchemaAttribute CONTENT_TRANSLATED = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Translated")
                .setDescription("Content which has been translated to English")
                .build();
        public static final SchemaAttribute DESCRIPTION = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Description")
                .setDescription("The description of the content")
                .build();
        public static final SchemaAttribute URL = new SchemaAttribute.Builder(GraphElementType.VERTEX, HyperlinkAttributeDescription.ATTRIBUTE_NAME, "Content.URL")
                .setDescription("The URL link to the content")
                .build();
        public static final SchemaAttribute DOCUMENT_PAGE_COUNT = new SchemaAttribute.Builder(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "Content.PageCount")
                .setDescription("The page count of the document")
                .build();
        public static final SchemaAttribute DOCUMENT_TEMPLATE = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Template")
                .setDescription("The template used to create the document")
                .build();
        public static final SchemaAttribute FILE_NAME = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.FileName")
                .setDescription("The name of the file")
                .build();
        public static final SchemaAttribute FILE_EXTENSION = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.FileExtension")
                .setDescription("The type of the file e.g .jpeg, .html")
                .build();
        public static final SchemaAttribute FILE_TYPE = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.FileType")
                .setDescription("The type of the file e.g JPEG, HTML")
                .build();
        public static final SchemaAttribute LAST_AUTHOR = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.LastAuthor")
                .setDescription("The last author of the content")
                .build();
        public static final SchemaAttribute LINE_COUNT = new SchemaAttribute.Builder(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "Content.LineCount")
                .setDescription("The line count excluding spaces")
                .build();
        public static final SchemaAttribute PERMISSION = new SchemaAttribute.Builder(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "Content.Permission")
                .setDescription("The permission level for the content")
                .build();
        public static final SchemaAttribute SIZE = new SchemaAttribute.Builder(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "Content.Size")
                .setDescription("The size of the file")
                .build();
        public static final SchemaAttribute SUBJECT = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Subject")
                .setDescription("The subject of the content")
                .build();
        public static final SchemaAttribute TITLE = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Title")
                .setDescription("The title of the content")
                .build();
        public static final SchemaAttribute VENDOR = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Vendor")
                .setDescription("The vendor of the application")
                .build();
        public static final SchemaAttribute VERSION = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Version")
                .setDescription("The version number of the content")
                .build();
    }

    public static class TransactionAttribute {

        public static final SchemaAttribute APPLICATION = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Application")
                .setDescription("The intended application for the content e.g Microsoft Office Word if the content is a Word document")
                .build();
        public static final SchemaAttribute AUTHOR = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Author").
                setDescription("The author of the content")
                .build();
        public static final SchemaAttribute CHAR_COUNT = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, IntegerAttributeDescription.ATTRIBUTE_NAME, "Content.CharacterCount")
                .setDescription("The character count excluding spaces")
                .build();
        public static final SchemaAttribute COMPANY = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Company")
                .setDescription("The company authoring the content")
                .build();
        public static final SchemaAttribute CONTENT = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content")
                .setDescription("Any textual content applying to the transaction")
                .build();
        public static final SchemaAttribute CONTENT_TRANSLATED = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Translated")
                .setDescription("Content which has been translated to English")
                .build();
        public static final SchemaAttribute DESCRIPTION = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Description")
                .setDescription("The description of the content")
                .build();
        public static final SchemaAttribute URL = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, HyperlinkAttributeDescription.ATTRIBUTE_NAME, "Content.URL")
                .setDescription("The URL link to the content")
                .build();
        public static final SchemaAttribute DOCUMENT_PAGE_COUNT = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, IntegerAttributeDescription.ATTRIBUTE_NAME, "Content.PageCount")
                .setDescription("The page count of the document")
                .build();
        public static final SchemaAttribute DOCUMENT_TEMPLATE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Template")
                .setDescription("The template used to create the document")
                .build();
        public static final SchemaAttribute SUBJECT = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Subject")
                .setDescription("The subject of the content stored on this transaction")
                .build();
        public static final SchemaAttribute TITLE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Title")
                .setDescription("The title of the content")
                .build();
        public static final SchemaAttribute FILE_NAME = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.FileName")
                .setDescription("The name of the file")
                .build();
        public static final SchemaAttribute FILE_EXTENSION = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.FileExtension")
                .setDescription("The type of the file e.g .jpeg, .html")
                .build();
        public static final SchemaAttribute FILE_TYPE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.FileType")
                .setDescription("The type of the file e.g JPEG, HTML")
                .build();
        public static final SchemaAttribute LAST_AUTHOR = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.LastAuthor")
                .setDescription("The last author of the content")
                .build();
        public static final SchemaAttribute LINE_COUNT = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, IntegerAttributeDescription.ATTRIBUTE_NAME, "Content.LineCount")
                .setDescription("The line count excluding spaces")
                .build();
        public static final SchemaAttribute PERMISSION = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, IntegerAttributeDescription.ATTRIBUTE_NAME, "Content.Permission")
                .setDescription("The permission level for the content")
                .build();
        public static final SchemaAttribute SIZE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, IntegerAttributeDescription.ATTRIBUTE_NAME, "Content.Size")
                .setDescription("The size of the file")
                .build();
        public static final SchemaAttribute VENDOR = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Vendor")
                .setDescription("The vendor of the application")
                .build();
        public static final SchemaAttribute VERSION = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Content.Version")
                .setDescription("The version number of the content")
                .build();
    }

    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> schemaAttributes = new ArrayList<>();
        schemaAttributes.add(VertexAttribute.APPLICATION);
        schemaAttributes.add(VertexAttribute.AUTHOR);
        schemaAttributes.add(VertexAttribute.CHAR_COUNT);
        schemaAttributes.add(VertexAttribute.COMPANY);
        schemaAttributes.add(VertexAttribute.CONTENT);
        schemaAttributes.add(VertexAttribute.CONTENT_TRANSLATED);
        schemaAttributes.add(VertexAttribute.DESCRIPTION);
        schemaAttributes.add(VertexAttribute.DOCUMENT_PAGE_COUNT);
        schemaAttributes.add(VertexAttribute.DOCUMENT_TEMPLATE);
        schemaAttributes.add(VertexAttribute.FILE_EXTENSION);
        schemaAttributes.add(VertexAttribute.FILE_NAME);
        schemaAttributes.add(VertexAttribute.FILE_TYPE);
        schemaAttributes.add(VertexAttribute.LAST_AUTHOR);
        schemaAttributes.add(VertexAttribute.LINE_COUNT);
        schemaAttributes.add(VertexAttribute.PERMISSION);
        schemaAttributes.add(VertexAttribute.SIZE);
        schemaAttributes.add(VertexAttribute.SUBJECT);
        schemaAttributes.add(VertexAttribute.TITLE);
        schemaAttributes.add(VertexAttribute.URL);
        schemaAttributes.add(VertexAttribute.VENDOR);
        schemaAttributes.add(VertexAttribute.VERSION);
        schemaAttributes.add(TransactionAttribute.APPLICATION);
        schemaAttributes.add(TransactionAttribute.AUTHOR);
        schemaAttributes.add(TransactionAttribute.CHAR_COUNT);
        schemaAttributes.add(TransactionAttribute.COMPANY);
        schemaAttributes.add(TransactionAttribute.CONTENT);
        schemaAttributes.add(TransactionAttribute.CONTENT_TRANSLATED);
        schemaAttributes.add(TransactionAttribute.DESCRIPTION);
        schemaAttributes.add(TransactionAttribute.DOCUMENT_PAGE_COUNT);
        schemaAttributes.add(TransactionAttribute.DOCUMENT_TEMPLATE);
        schemaAttributes.add(TransactionAttribute.FILE_EXTENSION);
        schemaAttributes.add(TransactionAttribute.FILE_NAME);
        schemaAttributes.add(TransactionAttribute.FILE_TYPE);
        schemaAttributes.add(TransactionAttribute.LAST_AUTHOR);
        schemaAttributes.add(TransactionAttribute.LINE_COUNT);
        schemaAttributes.add(TransactionAttribute.PERMISSION);
        schemaAttributes.add(TransactionAttribute.SIZE);
        schemaAttributes.add(TransactionAttribute.SUBJECT);
        schemaAttributes.add(TransactionAttribute.TITLE);
        schemaAttributes.add(TransactionAttribute.URL);
        schemaAttributes.add(TransactionAttribute.VENDOR);
        schemaAttributes.add(TransactionAttribute.VERSION);
        return Collections.unmodifiableCollection(schemaAttributes);
    }
}

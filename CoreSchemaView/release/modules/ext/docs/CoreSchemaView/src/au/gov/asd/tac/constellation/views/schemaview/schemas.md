# Schemas

Schemas are used to help Constellation interpret the data that is stored on the graph. Each
graph type in Constellation has a 'schema' associated with it, allowing it to understand
certain types of data and giving it the opportunity to improve data quality and visualisation
based on this knowledge. 

When you create a new graph, you can specify the schema that the graph will use:
-   Visual Graph
-   Analytic Graph

## Visual Graph
The Visual Graph knows how to deal with visualisation, meaning colours, icons,
labels and sizing will all work and can be edited, but the graph won't do anything
smart with these properties on your behalf. 

Adding a node to a Visual Graph will create something visible in your graph with a default colour and a label.

## Analytic Graph
The Analytic Graph is all about general graph analysis, meaning it knows about
basic node and transaction types, and provides attributes which can be used for
calculating properties of your graph such as clusters and centralities. 

Adding a node to an Analytic Graph will create a visual element where its colour and icon
are determined by its type.
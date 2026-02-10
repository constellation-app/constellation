# Extract Types from Text

## Identify schema type values within text and add them to your graph

<br />
<img src="../ext/docs/CoreDataAccessView/resources/ExtractTypesFromText.png" alt="Extract Types From Text" />
<br />

Extract from Text cycles through the schema node types known by
Constellation. For each schema type that has an associated regular
expression, all matches for that regular expression in the text are
added to the graph.

## Parameters

-   *Text* - The text to extract the schema types from

New nodes are added with the following attributes:

## Source Node

-   Seed - True


## Example
Entering the following text:
<br />
<br />
test@email.com
<br />
https://www.google.com/
<br />
<br />
Creates two nodes. One will be of type "Email" and will have "test@email.com" as it's identifier, and the other will be of type "URL" and will have "https://www.google.com/" as it's identifier.
<br />
<br />
<img src="../ext/docs/CoreDataAccessView/resources/ExtractTypesFromTextExample.png" alt="Extract Types From Text Example" />
<br />
<br />
<img src="../ext/docs/CoreDataAccessView/resources/ExtractTypesFromTextExampleResult.png" alt="Extract Types From Text Example Result" />
<br />
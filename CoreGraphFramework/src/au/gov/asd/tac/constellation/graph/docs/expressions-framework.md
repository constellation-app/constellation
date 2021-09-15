# Expressions Framework

The expressions framework is a structure which can be used to query or
assign the Vertex and Transaction attributes within a graph. The
framework will work on all attributes which do not include a restricted
character within its "AttributeName". An expression can return zero or
more matches.

## Query Language

The query language is designed to be robust with respect to features
such as nesting and type casting. To allow this, plain text is used to
gain the flexibility necessary. Users will be responsible for forming
queries which adhere to the below rules.

## Forming a Query Expression

A valid query for a vertex or transaction takes the form of
\[AttributeName\] \[Operator\] \[value\].

-   The "AttributeName" used will not include any restricted characters,
    and will be case sensitive.
-   The "AttributeName" will be represented as plain, unquoted text.
-   The "Operator" must be separated by atleast one whitespace
    character.
-   The "Operator" must be valid and from the list specified below.
-   The "Value" must be quoted using single('value') or double
    quotes("value").

  

How to form an expression to find a vertex with the "Label" attribute
matching "Vertex #1\<Unknown>".

-   "AttributeName" is Label
-   "Operator" is == or equals
-   "Value" is "Vertex #1\<Unknown>" or 'Vertex #1\<Unknown>'

  

Combining these into the pattern \[AttributeName\] \[Operator\]
\[Value\] yields the following suitable query strings.

-   Label == "Vertex #1\<Unknown>"
-   Label == 'Vertex #1\<Unknown>'
-   Label equals "Vertex #1\<Unknown>"
-   Label equals 'Vertex #1\<Unknown>'

## Operators

Operators can be used to query, edit, assign and compare values. Below
is the current list of supported operators.

  

<table class="table table-striped">
<thead>
<tr class="header">
<th><strong>Operator</strong></th>
<th><strong>Query Representation</strong></th>
<th><strong>Word Operator</strong></th>
<th><strong>Precedence</strong></th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Not</td>
<td>!</td>
<td>not</td>
<td>2</td>
</tr>
<tr class="even">
<td>Multiply</td>
<td>*</td>
<td>multiply</td>
<td>3</td>
</tr>
<tr class="odd">
<td>Divide</td>
<td>/</td>
<td>divide</td>
<td>3</td>
</tr>
<tr class="even">
<td>Modulo</td>
<td>%</td>
<td>mod</td>
<td>3</td>
</tr>
<tr class="odd">
<td>Add</td>
<td>+</td>
<td>add</td>
<td>4</td>
</tr>
<tr class="even">
<td>Subtract</td>
<td>-</td>
<td>subtract</td>
<td>4</td>
</tr>
<tr class="odd">
<td>Contains</td>
<td>N/A</td>
<td>contains</td>
<td>4</td>
</tr>
<tr class="even">
<td>Starts With</td>
<td>N/A</td>
<td>startswith</td>
<td>4</td>
</tr>
<tr class="odd">
<td>Ends With</td>
<td>N/A</td>
<td>endswith</td>
<td>4</td>
</tr>
<tr class="even">
<td>Greater Than</td>
<td>&gt;</td>
<td>gt</td>
<td>6</td>
</tr>
<tr class="odd">
<td>Less Than</td>
<td>&lt;</td>
<td>lt</td>
<td>6</td>
</tr>
<tr class="even">
<td>Greater Than Or Equals</td>
<td>&gt;=</td>
<td>gteq</td>
<td>6</td>
</tr>
<tr class="odd">
<td>Less Than Or Equals</td>
<td>&lt;=</td>
<td>lteq</td>
<td>6</td>
</tr>
<tr class="even">
<td>Equals</td>
<td>==</td>
<td>equals</td>
<td>7</td>
</tr>
<tr class="odd">
<td>Not Equals</td>
<td>!=</td>
<td>notequals</td>
<td>7</td>
</tr>
<tr class="even">
<td>Bitwise And</td>
<td>&amp;</td>
<td>N/A</td>
<td>8</td>
</tr>
<tr class="odd">
<td>Exclusive Or</td>
<td>^</td>
<td>xor</td>
<td>9</td>
</tr>
<tr class="even">
<td>Bitwise Or</td>
<td>|</td>
<td>N/A</td>
<td>10</td>
</tr>
<tr class="odd">
<td>And</td>
<td>&amp;&amp;</td>
<td>and</td>
<td>11</td>
</tr>
<tr class="even">
<td>Or</td>
<td>||</td>
<td>or</td>
<td>12</td>
</tr>
<tr class="odd">
<td>Assign</td>
<td>=</td>
<td>assign</td>
<td>14</td>
</tr>
</tbody>
</table>

## Nested Queries

The expression framework allows for nested queries. This makes querying
complex conditions possible. Nested queries are created by surrounding
internal queries with parenthesis (inner query).  
See below for an example.

  

(Label == 'Vertex #0\<Unknown>' || Label == 'Vertex #1\<Unknown>' ) &&
Type == 'Machine Identifier'

Will show a Vertex which is either Vertex #0\<Unknown> or Vertex
#1\<Unknown> and has a "Type" of 'Machine Identifier'.

## Restricted Characters

Restricted characters are characters written into the query string which
will not be parsed correctly due to the constraints of the query
language.

-   Single Quote - '''
-   Double Quote - '"'
-   Parenthesis - '(' and ')' (*when not used as a nesting delimiter*)

## Advanced Usage

### Precalculation Tools

The framework allows complex actions such as arithmetic operation on
both sides of the Operator. The framework will attempt to correctly
calculate any well parsed expression.  
Care must be taken for things like lexicographic ordering with different
variable types.  
Addition as shown below works similar to appending for String types.

(Label + 'extratext') == 'Vertex #1\<Unknown>extratext'

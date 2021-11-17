# Extract Words from Text

## Extract Words from a specified transaction attribute

This plugin will extract words from content (that is, words found in the
specified content attribute of all transactions) and represent them as
nodes on the graph. The source node will be linked to the word by a
transaction of type Referenced.

## Parameters

-   *Content Attribute* - Specify which transaction attribute contains
    the words to be extracted
-   *Words to Extract* - Newline-delimited list of words to be extracted
    from the content attribute. If left blank, all words will be
    extracted
-   *Use Regular Expressions* - Treat the Words to Extract as a list of
    regular expressions (not applicable if Words to Extract is empty)
-   *Whole Words Only* - Match the Words to Extract on whole word only
    (not applicable if Words to Extract is empty)
-   *Minimum Word Length* - Only extract words of length equal to or
    greater this number of characters (not applicable if Words to
    Extract is populated)
-   *Remove Special Characters* - Remove special characters from
    extracted words (not applicable if Words to Extract is populated)
-   *Lower Case* - Converts content and Words to Extract (if supplied)
    to lowercase.
-   *Extract Schema Types* - Cycles through the schema node types known
    by Constellation. For each schema type that has an associated
    regular expression, all matches for that regular expression found in
    the content are added to the graph.
-   *Transactions* - Choose whether to link nodes to words found in
    either incoming or outgoing transactions
-   *Selected Transactions Only* - Extract words from selected
    transactions only.
-   *Regular Expression Only* - Treat all Words to Extract as regular
    expressions

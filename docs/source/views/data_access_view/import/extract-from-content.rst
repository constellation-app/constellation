Extract Words from Content Attribute
------------------------------------

This plugin will extract words from content (that is, words found in the specified content attribute of all transactions) and represent them as nodes on the graph. The source node will be linked to the word by a transaction of type Referenced.

Parameters
``````````

*Content Attribute:* Specify which transaction attribute contains the content to be extracted.

*Words to Extract:* Enter a newline-delimited list of words, and only these will be extracted from content. Otherwise, all words will be extracted.

*Extract Regex:* Treats the Words to Extract parameters as a list of regular expressions. This parameter is disabled if Words to Extract is empty.

*Whole Word Only:* Matches the Words to Extract on whole word only. This parameter is disabled if Words to Extract is empty.

*Minimum Word Length:* Only extract words of length equal to or greater this number of characters. This parameter is disabled if Words to Extract is populated.

*Remove Special Characters:* Remove special characters from extracted words. This parameter is disabled if Words to Extract is populated.

*Case Insensitive:* Converts content and Words to Extract (if supplied) to lowercase.

*Extract Schema Types:* Cycles through the schema node types known by CONSTELLATION. For each schema type that has an associated regular expression, all matches for that regular expression are found in the content and added to the graph.

*Outgoing Transactions:* When checked, the plugin will link nodes to words found in outgoing transactions.

*Incoming Transactions:* When checked, the plugin will link nodes to words found in incoming transactions.

*Selected Only:* When selected, the plugin will only extract words from selected transactions only.


.. help-id: au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ExtractFromContentPlugin

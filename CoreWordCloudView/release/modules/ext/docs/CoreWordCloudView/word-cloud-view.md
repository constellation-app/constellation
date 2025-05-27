# Word Cloud View

<table class="table table-striped">
<colgroup>
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
</colgroup>
<thead>
<tr class="header">
<th>Constellation Action</th>
<th>Keyboard Shortcut</th>
<th>User Action</th>
<th style="text-align: center;">Menu Icon</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Open Word Cloud View</td>
<td>Ctrl + Shift + W</td>
<td>Views -&gt; Word Cloud View</td>
<td style="text-align: center;"><img src="../ext/docs/CoreWordCloudView/resources/word_cloud.png" alt="Word Cloud View Icon" /></td>
</tr>
</tbody>
</table>

## Introduction

The Word Cloud View is used to create word clouds based on words or phrases found 
within attributes in nodes and transactions on a graph. The length of the phrase,
how many words the phrase can span and the minimum number of times the phrase or 
word needs to appear on a graph for it to be shown in the word cloud can be 
modified. A text file is also able to be added to do a background frequency
comparison. 

<div style="text-align: center">

<img src="../ext/docs/CoreWordCloudView/resources/WordCloudView.png" alt="Word Cloud View" />

</div>
<br />

## Generate Word Cloud

-   *Element Type* - The type of element to gather data from when generating the 
word cloud. Options include:
    -   Nodes
    -   Transactions
-   *Attribute* - The attribute of the chosen element type to generate the word cloud from.
For example, using element type "Node" with the attribute "Geo.Country" will
generate a word cloud containing the names of countrys stored in the graph's nodes.
-   *Phrase Length* - The number of words in each "phrase" on the word cloud.
For example, continuing with the "Geo.Country" example above,
a phrase length of one will seperate each word in a country's name.
"New Zealand" would be seperated into two phrases, "New" and "Zealand".
Setting the phrase length to two would result in "New Zealand" staying as one phrase,
and filter out countries with only one word in their name.
-   *Phrase Span* - The number of words a phrase can span.
-   *Threshold* - The minimum number of graph elements the phrase must occur in 
the be present in the word cloud (inclusive).
-   *Background File* - A text file can be given containing phrases to filter the 
word cloud by. If a phrase is present on the graph but not in this text file, 
it will not appear on the word cloud. See below for additional filtering information.
-   *Background Filter* - Choose how filtering occurs with the given text file in 
the "background File" section. Options include:
    -   Contain any word in phrase
    -   Contain all words in phrase
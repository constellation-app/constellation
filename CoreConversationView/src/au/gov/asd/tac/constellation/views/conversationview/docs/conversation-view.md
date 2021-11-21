# Conversation View

<table class="table table-striped">
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
<td>Open Conversation View</td>
<td>Ctrl + Shift + C</td>
<td>Views -&gt; Conversation View</td>
<td style="text-align: center;"><img src="../constellation/CoreConversationView/src/au/gov/asd/tac/constellation/views/conversationview/docs/resources/conversation_view.png" alt="Conversation View Icon" /></td>
</tr>
</tbody>
</table>

## Introduction

The Conversation View displays the text of the "Content" attribute of
transactions selected between two nodes in a format resembling a
messaging application. Like a messaging application, the content is
ordered chronologically by the "DateTime" attribute. Only the content
for selected transactions with datetime values are displayed. If
transactions between more than two sets of unique nodes are selected
then no conversation text will be displayed.

<div style="text-align: center">

<img src="../constellation/CoreConversationView/src/au/gov/asd/tac/constellation/views/conversationview/docs/resources/ConversationView.png" alt="Conversation
View" />

</div>

The content is displayed in chat "bubbles", similar to those used by
some smart phones and instant messaging clients. The text and time
stamps can be selected and copied. To copy selected text, press Ctrl-C
or right click and use the context menu. Constellation will "beep" if
you use Ctrl-C but copy the text anyway. When text is selected in a
bubble, any text already selected in other bubbles is not automatically
deselected however only text in the current bubble is copied.

## Translations

If translated content is present in the graph (the "Content.Translated"
transaction attribute), you can choose to view the original content, the
translation, or both using the buttons at the top of the window. If you
are viewing translations only and a particular transaction does not have
a translation, then the original content will be displayed in italics.

You can enable or disable the hover translation capability available for
certain languages using the check box at the top of the Conversation
View. Disabling this will make it easier to select and copy text from
the window.

## Adding Content attributes

The "Content" Attribute needs to be present in order to be able to use
the Conversation View. If it is not already there, you can click on the
"Add Content Attributes" button found at the top of the Conversation
View. This will add all of the content attributes to your graph
(including "Content" and "Content.Translated").

## Searching Conversations

Using the search bar at the top of the view, you are able to search the
conversations (and translations) currently presented. Simply type in a
search term and the view will highlight all the matches found as well as
present a count of how many matches there are. It will only search the
visible parts of a bubble (i.e. the parts toggled on using the buttons
at the top of the window). If you change what parts of the bubble are
shown, the search will update with the new results. You can jump to each match 
by pressing the Previous and Next buttons.

# Conversation View

<table data-border="1">
<caption>Conversation View Actions</caption>
<thead>
<tr class="header">
<th scope="col"><strong>Constellation Action</strong></th>
<th scope="col"><strong>Keyboard Shortcut</strong></th>
<th scope="col"><strong>User Action</strong></th>
<th style="text-align: center;" scope="col"><strong>Menu Icon</strong></th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Open Conversation View</td>
<td>Ctrl + Shift + C</td>
<td>Views -&gt; Conversation View</td>
<td style="text-align: center;"><img src="../resources/conversation_view.png" alt="Conversation View Icon" /></td>
</tr>
</tbody>
</table>

Conversation View Actions

## Introduction

The Conversation View displays the text of the "Content" attribute of
transactions selected between two nodes in a format resembling a
messaging application. Like a messaging application, the content is
ordered chronologically by the "DateTime" attribute. Only the content
for selected transactions with datetime values are displayed. If
transactions between more than two sets of unique nodes are selected
then no conversation text will be displayed.

<div style="text-align: center">

![Conversation View](resources/ConversationView.png)

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

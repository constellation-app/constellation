## Decorators

Decorators are small icons that are displayed in the corners of a node's
main icon. They can provide extra information about a node such as
showing the flag of a country, or marking a node as good or bad. When
they are enabled, decorators are displayed on all nodes in the graph.
Decorators use the same icons as the node, so any icon displayed by a
node can be used as a decorator.

To add a decorator to a node, use <span class="mono">Edit →
Decorators</span>. The Decorators dialog allows you to select a
decorator for each of the four corners of the main icon.

CONSTELLATION includes icons for the flags of many countries. To use a
flag as a decorator, there must be an attribute that has the name of a
country (which matches the alias of the flag icon) as its value. If
there is an attribute that contains values such as <span
class="mono">Australia</span>, <span class="mono">Canada</span>, or
<span class="mono">Japan</span>, then that attribute is suitable.

Select the appropriate attribute in one of the corners, and select OK.
The node icons will now display the country flag in the corner that you
selected. If the attribute contains a value for which no icon alias
exists, no decorator will be displayed.

To mark an icon as good or bad, use the icons <span
class="mono">Misc.True</span> and <span class="mono">Misc.False</span>.
Use an attribute that contains one of the values <span
class="mono">True</span> or <span class="mono">False</span>, and an
appropriate icon will be displayed. The attribute could be a string
containing the text "<span class="mono">True</span>" or "<span
class="mono">False</span>", or a boolean attribute.

# Decorators

Decorators are small icons that are displayed in the corners of a node's
main icon. They can provide extra information about a node such as
showing the flag of a country, or marking a node as good or bad. When
they are enabled, decorators are displayed on all nodes in the graph.
Decorators use the same icons as the node, so any icon displayed by a
node can be used as a decorator.

To add a decorator to a node, open the Attribute Editor and edit the
"decorators" graph attribute. The resulting dialog allows you to select
a node attribute to decorate with its value for each of the four corners
of the main icon.

NOTE: A decorator will only be displayed on a node if there exists an
icon whose name or alias matches the attribute value. If no such icon
exists, no decorator will be displayed.

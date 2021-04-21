## Icons

Nodes are displayed using a combination of several icons in a 64x64
square. Icons need not be square: they may use transparent pixels to be
displayed with different shapes.

<table>
<caption>A description of the different attributes that make up a node icon</caption>
<thead>
<tr class="header">
<th scope="col">Attribute</th>
<th scope="col">Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>icon</td>
<td>This is the main icon, for example, a flag or a person. It should be 48x48.</td>
</tr>
<tr class="even">
<td>background_icon</td>
<td>This is the background icon, typically a square or circle. It should be 56x56. This icon is combined with the color, so background icons are usually shades of white.</td>
</tr>
<tr class="odd">
<td>color</td>
<td>The background icon is combined with this color.</td>
</tr>
<tr class="even">
<td>selected</td>
<td>This icon is used by CONSTELLATION to indicate that a node is selected. This cannot be changed.</td>
</tr>
</tbody>
</table>

A description of the different attributes that make up a node icon

The sizes specified above are indicative; for instance, an icon of a
tree may be higher than 48 pixels and narrower than 48 pixels. However,
in general the icon should be smaller than the background icon, which in
turn should be smaller than the selected icon.

## Custom Icons

CONSTELLATION has its own built-in set of icons. You can augment the
icons with your own personal icons; however, you cannot override a
built-in icon with your own custom icon. Custom icons can be <span
class="mono">PNG</span> or <span class="mono">JPG</span> format.

In your <span class="mono">.CONSTELLATION</span> directory is a a
subdirectory <span class="mono">Icons</span>. When CONSTELLATION starts,
it looks in this directory for custom icons.

Icon names must be categorised. For example, <span
class="mono">Person.DoctorWho.png</span> is in the <span
class="mono">Person</span> category, whereas <span
class="mono">Vehicle.DoctorWho.Tardis.png</span> is in category <span
class="mono">Vehicle</span>, subcategory <span
class="mono">DoctorWho</span>. The icon selection dialog uses the
categories to organise the icons, so an icon called <span
class="mono">MyIcon.png</span> will not be used.

Icons can be any size, however any icon greater than 48 pixels in width
or height will be automatically reduced to no more than 48 pixels.
Because of this restriction, custom 56x56 background icons cannot be
used.

## Icon Aliases

Icons are specified by name in the <span class="mono">icon</span>
attribute. For instance, to display the custom Doctor Who icon above,
use <span class="mono">Person.DoctorWho</span> as the <span
class="mono">icon</span> attribute value.

Icons can also be referred to by their alias, which is just the last
part of the name. This means that the <span class="mono">icon</span>
attribute value <span class="mono">DoctorWho</span> will also display
the <span class="mono">Person.DoctorWho</span> icon.

If there is more than one icon with the same alias (e.g. <span
class="mono">Person.DoctorWho</span> and <span
class="mono">TVSeries.DoctorWho</span>, an arbitrary decision will be
made as to which one to use.

## Characters

Included in the built-in icons are the ASCII characters 32 (space) to
126 (tilde) in the <span class="mono">Char</span> category. These icons
are white on a transparent background, so when they are used as values
of the <span class="mono">background\_icon</span> attribute, their color
can be changed using the <span class="mono">color</span> attribute.

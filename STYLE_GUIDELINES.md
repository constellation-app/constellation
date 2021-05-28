## Coding Style Guidelines

When the constellation project grows and has more contributors, it’s no
longer possible to just tell contributors to “follow the code formatting
in the code.” Ostensibly, that never worked anyway.

Here is an attempt to describe how the source code should be formatted.
It’s a collection of good practises, but will get better over time as it
gets more organised.

***Note that this style guide is for reference purposes. If you apply
NetBean’s default formatting layout then you comply with all coding
style guidelines.***

## Indentation

Always spaces, two of them. Never tabs.

## Spacing

-   Always a space after `if`. Use `if (condition)` not `if(condition)`.
    Example if/else block:

<!-- -->

        if (condition) {
          // yes like this
        } else {
          // something besides
        }

-   No single line `if` blocks

<!-- -->

    if (this) something
    else condition condition condition

instead use

    if (this) {
      // something
    } else {
      // condition condition condition
    }

-   No spaces inside parens.

<!-- -->

    if( condition )  // BAD
    if ( condition ){  // BADDER
    if (condition) {  // good

-   Use a space after the paren and before the curly brace:

<!-- -->

    if (condition) {

not

    if (condition){

-   For an empty block add a `//` comment to be made inside:

<!-- -->

    if (emptyBlock) {
      // a useful comment here about why this case is skipped
    }

-   Use spaces before/after commas:

-   `someFunction(apple,bear,cat); //bad`

-   `someFunction(apple, bear, cat);  // correct`

-   Use spaces before/after use of +

-   `String s = "this is an " + "excessive use " + "of quotes to demonstrate concatenation.";`

-   `String s = "this is the "+adjective+" wrong way "+exclamation+" and is tough to read";`

-   Spaces before/after operators in nearly all cases:

    -   `if (a == b)` not `if (a==b)`

    -   `for (int i = 0; i < 10; i++) {`

-   …with an exception for cases where it helps clarify order of
    operations:

    -   `int a = 13 + b*12 + c*7 + d` is ok to keep the \* adjacent for
        easier reading

## Brace yourself

Always use braces:

    for (int i = 0; i < 10; i++) {
      println(i);
    }

never this:

    for (int i = 0; i < 10; i++)
      println(i);

Not using braces is too prone to causing subtle errors when merging code
from multiple people.

The rare exception is the occasional ‘continue’ statement at the top of
a for-loop, or sometimes a single line `if (somethingOrOther) return;`
if it just doesn’t make sense to have a lot of extras.

Starting brace goes on same line, end brace goes on its own. An else
statement should look like `} else {`

## Blank lines

Use two blank lines between function blocks.

One blank line after the package declaration.

Two blank lines between the imports and the class definition.

No blank line after the function definition or before the closing brace.

## Fiddly things

-   Use `String[] lines` instead of `String lines[]`

-   Use `if (condition == null)` instead of `if (null == condition)`. It
    reads better to most people. Putting `null` on the left is good
    practice in C, C++, and perhaps other languages, but the error that
    it avoids is impossible in Java anyway. And if you want to be really
    pedantic, the compiler may even produce less efficient code with
    `if (null ==` because `if_acmpne` is used, which requires two
    variables, instead of `ifnonnull`.

-   Only use the `?` operator if it saves multiple lines of code. One
    probably use is a short function that returns a result immediately,
    i.e. `return (something == null) ? 0 : Integer.parseInt(something)`

-   Place || && etc on the end, not the start, of the line. This keeps
    variables lined up with one another. (This is different from Oracle
    style)

-   `static` goes first (before `public` et al) (This is different from
    Oracle style)

## Functions

When adding parameters to a function, add to the end. Don’t change
order, even if it feels more intuitive for that variation. Only increase
the number, don’t have alternate forms with the same basic number of
arguments.

## Comments

-   Always one space after the `//` in single line comments

-   Two spaces before `//` at the end of a line (that has code as well)

-   Try to use `//` comments inside functions, to make it easier to
    remove a whole block via `/* */`

-   When making a fix related to an issue on Github, include the URL to
    the issue. Don’t just put \#3257 or something like that, use the
    URL. We can spare the extra characters: the extra letters won’t make
    the PDE run slower, but will make it easier for someone returning to
    the code to look up the issue.

## New lines

-   Keep code under 80 columns. Break up statements if you must.

    -   There are some exceptions (the `PreferencesFrame` class, for
        instance) where breaking things up is even uglier, so the 80
        column limit is occasionally ignored.

-   Avoid the chaining madness that has afflicted Java programming of
    late, where dots are placed at the end (or even beginning) of
    multi-line indented poetry.

## Layout

Please try to avoid the following style:

       if (parent ==null) return fromAngle((float)(Math.random()*Math.PI*2),target);
       else               return fromAngle(parent.random(PConstants.TWO_PI),target);

(Point being, don’t get cute with adding extra space to indentation for
symmetry that you like personally. This is really subjective territory,
and should just be avoided.)

Please don’t stack up declarations and definitions:

    public static final int STATUS_EMPTY = 100, STATUS_COMPILER_ERR = 200, STATUS_WARNING = 300, STATUS_INFO = 400, STATUS_ERR = 500;

Because someday, we’re gonna remove STATUS\_INFO and that’ll make it
tougher to see the change. Just write them out like the following way:

    static final int STATUS_EMPTY = 100;
    static final int STATUS_COMPILER_ERR = 200;
    ...etc

If importing many items inside a single package, consider using \*
instead of listing out a dozen individual classes.

Please do not expand the imports in source that you’re editing.

Where possible, make sure text files checked into the repo are text
files, and are marked as such so they can use the correct encoding for
the platform. Please use this convention for newer files (or help us fix
the others).

## Philosophical

-   **Inner classes** don’t reach in and reference inner classes from
    classes not in the same file.

-   **Ordering if blocks** Where possible make the default case the
    first part of the ‘if’ block. this can’t be done slavishly because
    it can make things awkward, but it’s generally a useful idea.

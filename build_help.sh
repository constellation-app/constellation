#!/bin/bash

# remove any existing markdown files
find */src/ -name \*.md |
    xargs rm

# convert html to markdown
find */src/ -name *.html |
    while read x
    do
        ~/AppData/Local/Pandoc/pandoc -f html -t commonmark $x > "$x".md
    done

find */src/ -name *.html.md |
    while read x
    do
        mv "$x" "${x%.html.md}".md
    done

# remove the old html files
find */src/ -name *.html | while read x; do rm "$x"; done

# add files to git
git add *.html
git add *.png
git add \*.md

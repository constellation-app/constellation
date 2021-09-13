#!/bin/sh

# convert markdown to html & add headers 
find */src/ -path '*/docs/*' -name "*.md" | 
    while read x
    do 
   	 pandoc --template="onlinePandocTemplate.txt" -f commonmark -t html $x > "$x".html
    done

# we do need to rename them to .html instead of .md.html for the next line to work 

# move all html files to the docs folder 
find */src/ -path '*/docs/*' -name "*html" | xargs cp --parents -t ./documentation

# move all of the png & jpegs to the docs folder structure 
find */src/ -path '*/docs/*' -name "*.png" | xargs cp --parents -t ./documentation
find */src/ -path '*/docs/*' -name "*.jpg" | xargs cp --parents -t ./documentation

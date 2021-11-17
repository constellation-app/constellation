#!/bin/sh

# convert markdown to html & add headers 
#find */src/ -path '*/docs/*' -name "*.md" | 
#    while read x
#    do 
#   	 pandoc --template="onlinePandocTemplate.txt" -f commonmark -t html $x > "$x".html
#    done

#echo "renaming all .md.html to .html" 
#find */src -path '*/docs/*' -name "*.md.html" | 
#    while read x 
#    do 
 #       mv "$x" "${x%.md.html}".html 
 #   done 


# move all html files to the docs folder 
find */src -path '*/docs/*' -name "*md" | xargs cp --parents -t HelpDocumentation

# move all of the png & jpegs to the docs folder structure 
find */src -path '*/docs/*' -name "*.png" | xargs cp --parents -t HelpDocumentation
find */src -path '*/docs/*' -name "*.jpg" | xargs cp --parents -t HelpDocumentation

#!/bin/bash

# convert toc from markdown to html 
echo "find toc.md and convert with pandoc"
pandoc -f commonmark -t html toc.md > "toc".html

echo "rename toc.md.html to toc.html"
mv "$toc.md.html" "${toc.md.html}".html


echo "search and replace start"

while read a; do
    echo ${a//md/html}
done < toc.html > toc.html.t
mv toc.html{.t,}

echo "search and replace end"


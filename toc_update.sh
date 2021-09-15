#!/bin/bash

# convert toc from markdown to html
echo "find toc.md and convert with pandoc"
pandoc -f commonmark -t html ./constellation/toc.md > "toc".html

echo "search and replace start"
while read a; do
    echo ${a//md/html}
done < ./constellation/toc.html > ./constellation/toc.html.t
mv ./constellation/toc.html.t ./constellation/toc.html
mv ./constellation/toc.html ./pages/docs/help/toc.html
echo "search and replace end"

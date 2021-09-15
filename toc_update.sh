#!/bin/bash

# convert toc from markdown to html
echo "find toc.md and convert with pandoc"
pandoc -f commonmark -t html ./constellation/toc.md > ./constellation/toc.html

echo "search and replace start"
sed -i -e 's/.md/.html/g' ./constellation/toc.html
mv ./constellation/toc.html ./pages/docs/help/toc.html
echo "search and replace end"

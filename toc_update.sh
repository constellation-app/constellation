#!/bin/bash

# convert toc from markdown to html
echo "converting toc.md with pandoc..."
pandoc -f commonmark -t html ./constellation/toc.md > ./constellation/toc.html
echo "done converting."

echo "replace .md links with .html..."
sed -i -e 's/.md/.html/g' ./constellation/toc.html
mv ./constellation/toc.html ./pages/docs/help/toc.html
echo "done replacing."

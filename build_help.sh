find */src/ -name *.html | while read x; do ~/applications/pandoc-2.13/bin/pandoc -f html -t commonmark $x > "$x".md; done
find */src/ -name *.html.md | while read x; do mv "$x" "${x%.html.md}".md; done
find */src/ -name *.html | while read x; do rm "$x"; done
git add *.html
git add *.png
git add \*.md

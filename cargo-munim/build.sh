#!/bin/sh
# Builds a standalone index.html (double-click to open, works offline) from src/app.html
cd "$(dirname "$0")"
{ printf '<!doctype html>\n<html lang="hi">\n<head>\n<meta charset="utf-8">\n<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">\n<meta name="theme-color" content="#0a6b78">\n';
  sed -n '1,/<\/style>/p' src/app.html
  printf '</head>\n<body>\n'
  sed '1,/<\/style>/d' src/app.html
  printf '</body>\n</html>\n'; } > index.html
echo "built index.html"

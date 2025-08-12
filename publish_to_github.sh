#!/bin/sh
read -p "Repository URL: " URL
git init
git remote add origin "$URL"
git add .
git commit -m "Initial commit"
git push -u origin main

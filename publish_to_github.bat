@echo off
set /p URL=Repository URL: 
git init
git remote add origin %URL%
git add .
git commit -m "Initial commit"
git push -u origin main

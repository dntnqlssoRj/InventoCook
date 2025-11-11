#!/bin/bash
cd "/Users/kimjunghoon/Library/Mobile Documents/com~apple~CloudDocs/java/Demo1"

# 컴파일
echo "컴파일 중..."
javac -cp "lib/*:." src/GanttChartMaker.java -d out/production/Demo1

if [ $? -eq 0 ]; then
    echo "컴파일 성공!"
    echo "프로그램 실행 중..."
    java -cp "out/production/Demo1:lib/*" GanttChartMaker
else
    echo "컴파일 실패!"
    exit 1
fi









<<<<<<< HEAD
=======

>>>>>>> 3bbc11b (Initial commit)

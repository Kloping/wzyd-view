#!/bin/bash

if [ -z "${1}" ]; then
    echo "[bash] No Parameters,Default Skip Compilation."
elif [ "${1}" = "false" ]; then
    echo "[bash] Skip Compilation."
else
    echo "[bash] Start Clean And Copy-dependencies Compilation."
    echo "[bash] Delay 3s after rm -r libs"
    echo "[bash] 3s"
    sleep 1s
    echo "[bash] 2s"
    sleep 1s
    echo "[bash] 1s"
    sleep 1s
    rm -r libs
    mvn clean dependency:copy-dependencies -DoutputDirectory=libs compile
fi

java -Dfile.encoding=UTF-8 -XX:+UseG1GC -classpath "./target/classes:./libs/*" io.github.gdpl2112.WzryDpApplication

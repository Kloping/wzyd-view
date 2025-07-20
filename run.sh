if [ ${1} == "false" ]; then
  echo "Skip compilation"
else
  rm -r libs
  mvn clean dependency:copy-dependencies -DoutputDirectory=libs compile
fi

java -Dfile.encoding=UTF-8 -classpath "./target/classes:./libs/*" io.github.gdpl2112.WzryDpApplication

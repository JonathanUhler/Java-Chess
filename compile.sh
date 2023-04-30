#!/bin/bash


mkdir -p obj
rm -rf obj/*

javac -Xlint:unchecked -Xlint:deprecation -cp 'src/lib/*' -d obj/ $(find src/main -name '*.java')

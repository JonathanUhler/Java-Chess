cd src/main
javadoc engine/**/*.java tests/*.java server/*.java client/*.java client/**/*.java \
		-d ../../documentation/javadoc \
		-cp ../lib/*.jar
cd ..

cd src/main
javadoc engine/**/*.java util/*.java tests/*.java server/*.java client/*.java \
		-d ../../documentation/javadoc \
		-cp ../lib/*.jar
cd ..

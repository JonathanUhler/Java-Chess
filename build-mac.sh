jpackage \
  --name JavaChess \
  --input ./src \
  --dest ./release \
  --verbose \
  --icon ./src/reference/icon.icns \
  --main-jar Chess.jar \
  --main-class main.Chess \
  --mac-package-name "Java Chess"
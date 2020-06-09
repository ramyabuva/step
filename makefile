CLANG_FORMAT=node_modules/clang-format/bin/linux_x64/clang-format --style=Google
CSS_VALIDATOR=node_modules/css-validator/bin/css-validator
ESLINT=node_modules/eslint/bin/eslint.js
HTML_VALIDATE=node_modules/html-validate/bin/html-validate.js
PRETTIER=node_modules/prettier/bin-prettier.js

node_modules:
	npm install clang-format prettier css-validator html-validate eslint eslint-config-google

pretty: node_modules
	$(PRETTIER) --write portfolio/src/main/webapp/*.html portfolio/src/main/webapp/css/*.css
	find portfolio/src/main/java -iname *.java | xargs $(CLANG_FORMAT) -i
	find portfolio/src/main/webapp -iname *.js | xargs $(CLANG_FORMAT) -i

validate: node_modules
	$(HTML_VALIDATE) portfolio/src/main/webapp/*.html
	$(CSS_VALIDATOR) portfolio/src/main/webapp/css/*.css
	$(ESLINT) portfolio/src/main/webapp/js/*.js

package:
	mvn package
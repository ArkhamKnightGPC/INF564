
JAVAC := javac -cp lib/java-cup-11a.jar:bin -d bin
PGM := java -cp lib/java-cup-11a.jar:bin mini_turtle.Main

all: src/mini_turtle/Lexer.java src/mini_turtle/parser.java
	$(JAVAC) src/mini_turtle/*.java
	$(PGM) test.logo

.PHONY: test tests

test:
	$(PGM) test.logo

tests:
	for f in tests/*.logo; do $(PGM) $$f; done

# cup and jflex

JAVACUP := java -jar ../../lib/java-cup-11a.jar

src/mini_turtle/parser.java src/mini_turtle/sym.java: src/mini_turtle/Parser.cup
	cd src/mini_turtle/ && $(JAVACUP) -package mini_turtle Parser.cup

%.java: %.flex
	rm -f $@
	jflex $<

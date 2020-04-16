
.PHONY: default
default: compile start ;

JJTREE=jjtree
JAVACC=javacc
JJDOC=jjdoc
JAVAC=javac
GRAMMAR=java1_7
OUTPUT=bin/javaparser

build:
	mkdir -p $(OUTPUT)
	$(JJTREE) $(GRAMMAR).jjt
	$(JAVACC) $(GRAMMAR).jj
	$(JJDOC) $(GRAMMAR).jj
	cp -rf src/javaparser/*.java .
	$(JAVAC) *.java
	mv $(GRAMMAR).jj $(OUTPUT)
	mv *.class $(OUTPUT)
	rm *.java

clean:
	rm -rf bin

mrproper: clean
	rm -rf *~ $(GRAMMAR).html

compile: clean
	mkdir bin
	$(JAVAC) -d bin src/javaparser/*.java

clear:
	rm -rf results

run: clear
	java -cp bin javaparser.JavaParser1_7 @target

test: clear
	java -cp bin javaparser.JavaParser1_7 test/Test.java

wordcount: clear
	java -cp bin javaparser.JavaParser1_7 test/WordCount.java

dot:
	-find results -type f -name "*.dot" | xargs dot -Tpng -O

ast:
	java -cp bin analyst.helper.ASTWriter $(FROM) $(TO) $(DEPTH)
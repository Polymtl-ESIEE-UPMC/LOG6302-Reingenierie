JJTREE=jjtree
JAVACC=javacc
JJDOC=jjdoc
JAVAC=javac
GRAMMAR=java1_7
OUTPUT=bin/javaparser

.PHONY: compile clean mrproper

compile:
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

clear:
	rm -rf results
	rm -rf uml

mrproper: clean
	rm -rf *~ $(GRAMMAR).html

run: clear
	java -cp bin javaparser.JavaParser1_7 @log

start: run
	find results/dot -type f -name "*.dot" | xargs dot -Tpng -O
	mkdir -p results/uml && mv results/dot/*.png results/uml

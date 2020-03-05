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

mrproper: clean
	rm -rf *~ $(GRAMMAR).html

run:
	java -cp bin javaparser.JavaParser1_7 @log 

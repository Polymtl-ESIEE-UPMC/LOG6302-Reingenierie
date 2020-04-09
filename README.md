# polymtl-LOG6302-Reingenierie

`make compile` pour compiler

`make run` pour lancer l'analyse des fichiers de ./apache/

`make test` pour analyser que le fichier ./apache/Test.java, ce qui est un exemple simple

`make dot` pour generer les fichiers PNG de .dot

Le resultat (dot et PNG) est dans le folder results

Il y a des flags pour activer ou desactiver le fonctionnement UML ou CFG. Simplement changer la valeur boolean au debut de fichier ./src/javaparser/Zeus.java

`make ast FROM=$node1 TO=$node2 DEPTH=$number` est un helper pour visualiser le AST entre node1 et node1, detaille jusqu'au le sous niveau $number, il n'a pas d'impact sur le main program

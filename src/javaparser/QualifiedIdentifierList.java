/* Generated By:JJTree: Do not edit this line. QualifiedIdentifierList.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package javaparser;

public
class QualifiedIdentifierList extends SimpleNode {
  public QualifiedIdentifierList(int id) {
    super(id);
  }

  public QualifiedIdentifierList(JavaParser1_7 p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(JavaParser1_7Visitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=59c3e78bd613bea5dbdfadea925a7a18 (do not edit this line) */

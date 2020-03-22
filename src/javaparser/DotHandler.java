package javaparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DotHandler {

  private static DotHandler dot_handler_instance = new DotHandler();
  private FileOutputStream output_stream_uml_dot_file;
  private int indent = 0;
  private boolean new_line = true;

  public DotHandler() {
    try {
      File uml_dot_file = new File("./results/dot/uml.dot");
      uml_dot_file.getParentFile().mkdirs();
      uml_dot_file.createNewFile(); // if file already exists will do nothing
      this.output_stream_uml_dot_file = new FileOutputStream(uml_dot_file, false);

      this.begin("digraph G {");
      this.newLine();

      this.writeln("fontname = \"Bitstream Vera Sans\"");
      this.writeln("fontsize = 8");
      this.newLine();

      this.begin("node [");
      this.writeln("fontname = \"Bitstream Vera Sans\"");
      this.writeln("fontsize = 8");
      this.writeln("shape = \"record\"");
      this.end("]");

      this.begin("edge [");
      this.writeln("fontname = \"Bitstream Vera Sans\"");
      this.writeln("fontsize = 8");
      this.end("]");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static DotHandler getInstance() {
    return dot_handler_instance;
  }

  public DotHandler writeln(String str) {
    write(str);
    write("\n");
    this.new_line = true;
    return this;
  }

  public DotHandler write(String str) {
    try {
      if (this.new_line) {
        this.output_stream_uml_dot_file.write((CustomString.makeCustomString(str).indent(indent).finish()).getBytes());
        this.new_line = false;
      } else {
        this.output_stream_uml_dot_file.write(str.getBytes());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return this;
  }

  public DotHandler begin(String str) {
    this.writeln(str);
    this.indent++;
    return this;
  }

  public DotHandler end(String str) {
    this.indent--;
    this.writeln(str);
    this.newLine();
    return this;
  }

  public DotHandler newLine() {
    this.writeln("");
    return this;
  }

  public void finish() {
    try {
      this.end("}");
      this.output_stream_uml_dot_file.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
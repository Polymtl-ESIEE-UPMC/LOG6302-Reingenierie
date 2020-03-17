package javaparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DotHandler {

  private static DotHandler dot_handler_instance = new DotHandler();
  private FileOutputStream output_stream_uml_dot_file;
  private int indent = 0;

  public DotHandler() {
    try {
      File uml_dot_file = new File("./results/dot/uml.dot");
      uml_dot_file.getParentFile().mkdirs();
      uml_dot_file.createNewFile(); // if file already exists will do nothing
      this.output_stream_uml_dot_file = new FileOutputStream(uml_dot_file, false);

      this.begin("digraph G {");
      this.spaceLine();

      this.write("fontname = \"Bitstream Vera Sans\"");
      this.write("fontsize = 8");
      this.spaceLine();

      this.begin("node [");
      this.write("fontname = \"Bitstream Vera Sans\"");
      this.write("fontsize = 8");
      this.write("shape = \"record\"");
      this.end("]");

      this.begin("edge [");
      this.write("fontname = \"Bitstream Vera Sans\"");
      this.write("fontsize = 8");
      this.end("]");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static DotHandler getInstance() {
    return dot_handler_instance;
  }

  public DotHandler write(String str) {
    try {
      this.output_stream_uml_dot_file
          .write((CustomString.makeCustomString(str).indent(indent).finish() + "\n").getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return this;
  }

  public DotHandler begin(String str) {
    this.write(str);
    this.indent++;
    return this;
  }

  public DotHandler end(String str) {
    this.indent--;
    this.write(str);
    this.spaceLine();
    return this;
  }

  public DotHandler spaceLine() {
    this.write("");
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
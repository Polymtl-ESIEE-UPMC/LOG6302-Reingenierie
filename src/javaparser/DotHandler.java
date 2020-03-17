package javaparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DotHandler {

  private static DotHandler dot_handler_instance = new DotHandler();
  private FileOutputStream output_stream_uml_dot_file;

  public DotHandler() {
    try {
      File uml_dot_file = new File("./uml.dot");
      uml_dot_file.createNewFile(); // if file already exists will do nothing
      this.output_stream_uml_dot_file = new FileOutputStream(uml_dot_file, true);

      this.write("digraph G { \n");

      this.write(CustomString.makeCustomString("fontname = \"Bitstream Vera Sans\"").indent().finish() + "\n");
      this.write(CustomString.makeCustomString("fontsize = 8").indent().finish() + "\n");

      this.write(CustomString.makeCustomString("node [").indent().finish() + "\n");
      this.write(CustomString.makeCustomString("fontname = \"Bitstream Vera Sans\"").indent(2).finish() + "\n");
      this.write(CustomString.makeCustomString("fontsize = 8").indent(2).finish() + "\n");
      this.write(CustomString.makeCustomString("shape = \"record\"").indent(2).finish() + "\n");
      this.write(CustomString.makeCustomString("]").indent().finish() + "\n");

      this.write(CustomString.makeCustomString("edge [").indent().finish() + "\n");
      this.write(CustomString.makeCustomString("fontname = \"Bitstream Vera Sans\"").indent(2).finish() + "\n");
      this.write(CustomString.makeCustomString("fontsize = 8").indent(2).finish() + "\n");
      this.write(CustomString.makeCustomString("]").indent().finish() + "\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static DotHandler getInstance() {
    return dot_handler_instance;
  }

  public DotHandler write(String str) {
    try {
      this.output_stream_uml_dot_file.write(str.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return this;
  }

  public void finish() {
    try {
      this.write("}");
      this.output_stream_uml_dot_file.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
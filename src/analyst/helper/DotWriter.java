package analyst.helper;

import java.io.FileOutputStream;
import java.io.IOException;

public abstract class DotWriter {

  protected FileOutputStream output_stream_uml_dot_file;
  protected int indent = 0;
  protected boolean new_line = true;

  protected void writeLabel(String id, String label) {
    begin(id + " [");
    writeln("label = \"" + label + "\"");
    end("]");
  }

  protected void writeHeader() {
    begin("digraph G {");
    newLine();

    writeln("fontname = \"Bitstream Vera Sans\"");
    writeln("fontsize = 8");
    newLine();

    begin("node [");
    writeln("fontname = \"Bitstream Vera Sans\"");
    writeln("fontsize = 8");
    writeln("shape = \"record\"");
    end("]");

    begin("edge [");
    writeln("fontname = \"Bitstream Vera Sans\"");
    writeln("fontsize = 8");
    end("]");
  }

  protected void begin(final String str) {
    writeln(str);
    this.indent++;
  }

  protected void end(final String str) {
    this.indent--;
    writeln(str);
    newLine();
  }

  protected void writeln(final String str) {
    write(str);
    write("\n");
    this.new_line = true;
  }

  protected void write(final String str) {
    try {
      if (this.new_line) {
        this.output_stream_uml_dot_file.write((openStringEditor(str).indent(this.indent).close()).getBytes());
        this.new_line = false;
      } else {
        this.output_stream_uml_dot_file.write(str.getBytes());
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  protected void newLine() {
    writeln("");
  }

  protected StringEditor openStringEditor(String str) {
    return new StringEditor(str);
  }

  private class StringEditor {
    private String str;

    private StringEditor(final String str) {
      this.str = str;
    }

    private StringEditor indent(final int n) {
      if (n < 0)
        throw new Error("Expect positive value in indent function of class CustomString, instead having " + n);
      for (int i = 0; i < n; i++) {
        this.str = "  " + this.str;
      }
      return this;
    }

    private String close() {
      return this.str;
    }
  }

}
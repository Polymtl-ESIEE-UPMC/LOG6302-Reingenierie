package analyst.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DotSlicedWriter extends DotTreeWriter {
  private final String path;
  private final String variable;

  public DotSlicedWriter(final String path, final String variable) {
    this.path = path;
    this.variable = variable;
  }

  public void writeSliced(FlowProvider provide) {
    createDotFile();
    super.writeHeader();
    super.writeFlows(provide);
    super.end("}");
    try {
      super.output_stream_uml_dot_file.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void createDotFile() {
    try {
      final File uml_dot_file = new File(this.path + this.variable + ".dot");
      uml_dot_file.getParentFile().mkdirs();
      uml_dot_file.createNewFile(); // if file already exists will do nothing
      super.output_stream_uml_dot_file = new FileOutputStream(uml_dot_file, false);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
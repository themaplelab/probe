package probe;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class GXL2Text {

	public static void main(String[] args) {
		try {
			CallGraph graph = new GXLReader().readCallGraph(new FileInputStream(args[0]));
			new TextWriter().write(graph,
					new GZIPOutputStream(new FileOutputStream(args[0].replace(".gxl", ".txt.gzip"))));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

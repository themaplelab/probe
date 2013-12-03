package probe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/** Reads a call graph from a text file. */
public class TextReader {
	/** Read a call graph from a text file. */
	public CallGraph readCallGraph(InputStream file) throws IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(file));
		CallGraph ret = new CallGraph();

		while (true) {
			String line = in.readLine();
			if (line == null)
				break;

			if (line.equals(Util.ClassTag)) {
				String id = in.readLine();
				String pkg = in.readLine();
				String name = in.readLine();

				ProbeClass cls = ObjectManager.v().getClass(pkg, name);
				nodeToClass.put(id, cls);
			} else if (line.equals(Util.MethodTag)) {
				String id = in.readLine();
				String name = in.readLine();
				String signature = in.readLine();
				String cls = in.readLine();

				ProbeMethod m = ObjectManager.v().getMethod(nodeToClass.get(cls), name, signature);
				nodeToMethod.put(id, m);
			} else if (line.equals(Util.EntrypointTag)) {
				String id = in.readLine();

				ret.entryPoints().add(nodeToMethod.get(id));
			} else if (line.equals(Util.EdgeTag)) {
				String src = in.readLine();
				String dst = in.readLine();
				String weight = in.readLine();

				ret.edges().add(new CallEdge(nodeToMethod.get(src), nodeToMethod.get(dst), Double.parseDouble(weight)));
			} else {
				throw new RuntimeException("Unexpected line: " + line);
			}
		}

		return ret;
	}

	/* End of public methods. */

	private Map<String, ProbeClass> nodeToClass = new HashMap<String, ProbeClass>();
	private Map<String, ProbeMethod> nodeToMethod = new HashMap<String, ProbeMethod>();
}

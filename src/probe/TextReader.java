package probe;
import java.util.*;
import java.io.*;

/** Reads a call graph from a text file. */
public class TextReader {
    /** Read a call graph from a text file. */
    public CallGraph readCallGraph( InputStream file ) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(file));
        CallGraph ret = new CallGraph();

        while(true) {
            String line = in.readLine();
            if(line == null) break;
            if(line.equals("CLASS")) {
                String id = in.readLine();
                String pkg = in.readLine();
                String name = in.readLine();
                ProbeClass cls = ObjectManager.v().getClass(pkg, name);
                nodeToClass.put(id, cls);
            } else if(line.equals("METHOD")) {
                String id = in.readLine();
                String name = in.readLine();
                String signature = in.readLine();
                String cls = in.readLine();
                ProbeMethod m = ObjectManager.v().getMethod(
                        (ProbeClass) nodeToClass.get(cls),
                        name,
                        signature
                );
                nodeToMethod.put(id, m);
            } else if(line.equals("ENTRYPOINT")) {
                String id = in.readLine();
                ret.entryPoints().add( (ProbeMethod) nodeToMethod.get(id) );
            } else if(line.equals("CALLEDGE")) {
                String src = in.readLine();
                String dst = in.readLine();
                String context = in.readLine();
                ret.edges().add(new CallEdge(
                        (ProbeMethod) nodeToMethod.get(src),
                        (ProbeMethod) nodeToMethod.get(dst)
                ));
            } else {
                throw new RuntimeException("Unexpected line: "+line);
            }
        }

        return ret;
    }

    /* End of public methods. */

    private Map nodeToClass = new HashMap();
    private Map nodeToMethod = new HashMap();
}

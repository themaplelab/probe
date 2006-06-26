package probe.soot;
import soot.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import soot.jimple.*;
import soot.util.queue.*;
import soot.jimple.paddle.*;
import probe.ObjectManager;
import probe.ProbeMethod;
import probe.ProbeClass;

public class CallGraph extends SceneTransformer {
    static String filename = "CallGraph.gxl.gz";
    public static void main(String[] args) 
    {
	PackManager.v().getPack("wjtp").add(
	    new Transform("wjtp.probecg", new CallGraph()));


        LinkedList al = new LinkedList();
        for( int i = 0; i < args.length; i++ ) {
            if( args[i].equals("-dd") ) {
                filename = args[++i];
                continue;
            }
            al.add(args[i]);
        }
        args = new String[al.size()];
        for( int i = 0; i < args.length; i++ ) {
            args[i] = (String) al.removeFirst();
        }
	soot.Main.main(args);
    }

    protected void internalTransform(String phaseName, Map options)
    {
        System.out.println("running wjtp.probecg");
        probe.CallGraph probecg = new probe.CallGraph();
        soot.jimple.toolkits.callgraph.CallGraph cg = Scene.v().getCallGraph();
        Set sources = new HashSet();
        Set targets = new HashSet();

        Iterator it = cg.listener();
        while( it.hasNext() ) {
            soot.jimple.toolkits.callgraph.Edge e =
                (soot.jimple.toolkits.callgraph.Edge) it.next();
            sources.add( e.src() );
            targets.add( e.tgt() );
            probecg.edges().add( new probe.Edge(
                        probeMethod(e.src()), probeMethod(e.tgt())));
            if( e.kind() == Kind.NEWINSTANCE ) {
                ProbeMethod newInstance = ObjectManager.v().getMethod(
                        ObjectManager.v().getClass("java.lang.Class"),
                        "newInstance0",
                        ""
                );
                probecg.edges().add(new probe.Edge(newInstance, probeMethod(e.tgt())));
            } else if( e.kind() == Kind.PRIVILEGED ) {
                ProbeMethod doPriv = probeMethod(e.srcStmt().getInvokeExpr().getMethod());
                probecg.edges().add(new probe.Edge(doPriv, probeMethod(e.tgt())));
            }
        }

        for( Iterator mIt = Scene.v().getEntryPoints().iterator(); mIt.hasNext(); ) {

            final SootMethod m = (SootMethod) mIt.next();
            probecg.entryPoints().add(probeMethod(m));
        }

        try {
            new probe.GXLWriter().write(probecg, new GZIPOutputStream( new FileOutputStream(new File(
                            filename))));
        } catch (IOException ioe) {
            System.out.println("Error while writing to file: " + ioe);
        }
    }
    private ProbeMethod probeMethod(SootMethod m) {
        SootClass cl = m.getDeclaringClass();
        ProbeClass cls = ObjectManager.v().getClass(cl.getPackageName(),
                cl.getShortName());
        ProbeMethod gm = ObjectManager.v().getMethod(cls,
                m.getName(), m.getBytecodeParms());
        return gm;
    }
}



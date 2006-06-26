package probe.soot;
import soot.*;
import soot.tagkit.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import soot.jimple.*;
import soot.util.queue.*;
import soot.jimple.toolkits.callgraph.*;
import probe.ObjectManager;
import probe.ProbeMethod;
import probe.ProbeStmt;
import probe.ProbeClass;
import probe.GXLWriter;

public class Polymorphic extends SceneTransformer {
    static String filename = "Polymorphic.gxl.gz";
    public static void main(String[] args) 
    {
	PackManager.v().getPack("wjtp").add(
	    new Transform("wjtp.probepoly", new Polymorphic()));


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
        System.out.println("running wjtp.polymorphic");
        probe.Polymorphic poly = new probe.Polymorphic();
        soot.jimple.toolkits.callgraph.CallGraph cg = Scene.v().getCallGraph();

        Iterator it = cg.listener();
        while( it.hasNext() ) {
            Edge e = (Edge) it.next();

            if( e.kind() != Kind.VIRTUAL 
            &&  e.kind() != Kind.INTERFACE ) continue;

            Stmt stmt = e.srcStmt();
            SootMethod target = e.tgt();
            SootMethod oldTarget = (SootMethod) targets.get(stmt);
            if( oldTarget == null ) {
                targets.put(stmt, target);
            } else if( !oldTarget.equals(target) ) {
                poly.stmts().add( probeStmt(stmt, e.src()));
            }
        }

        try {
            new GXLWriter().write(poly, new GZIPOutputStream( new FileOutputStream(new File(
                            filename))));
        } catch (IOException ioe) {
            System.out.println("Error while writing to file: " + ioe);
        }
    }
    private ProbeStmt probeStmt( Stmt s, SootMethod m) {
        BytecodeOffsetTag t = (BytecodeOffsetTag) s.getTag("BytecodeOffsetTag");
        if( t == null ) throw new RuntimeException( s+" in "+m+" doesn't have an offset tag" );
        return ObjectManager.v().getStmt(probeMethod(m), t.getBytecodeOffset());
    }
    private ProbeMethod probeMethod(SootMethod m) {
        SootClass cl = m.getDeclaringClass();
        ProbeClass cls = ObjectManager.v().getClass(cl.getPackageName(),
                cl.getShortName());
        ProbeMethod gm = ObjectManager.v().getMethod(cls,
                m.getName(), m.getBytecodeParms());
        return gm;
    }
    private Map targets = new HashMap();
    private Set poly = new HashSet();
}



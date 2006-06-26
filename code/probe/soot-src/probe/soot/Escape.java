package probe.soot;
import soot.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import soot.jimple.*;
import soot.util.queue.*;
import soot.jimple.paddle.*;
import soot.jimple.toolkits.callgraph.*;
import soot.tagkit.*;
import probe.ObjectManager;
import probe.ProbeMethod;
import probe.ProbeClass;
import probe.ProbeStmt;

public class Escape extends SceneTransformer {
    static String filename = "Escape.gxl.gz";
    public static void main(String[] args) 
    {
	PackManager.v().getPack("wjtp").add(
	    new Transform("wjtp.probeescape", new Escape()));

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
        System.out.println("running wjtp.probeescape");
        final Map nodeToHeapObj = new HashMap();

        probe.Escape esc = new probe.Escape();
        BDDEscapeAnalysis ea = new BDDEscapeAnalysis();
        ea.analyze();

        NodeManager nm = PaddleScene.v().nodeManager();
        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {
            final SootClass cl = (SootClass) clIt.next();
            for( Iterator mIt = cl.getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                MethodNodeFactory mnf = new MethodNodeFactory(
                        m, Results.v().nodeFactory());
                if( !m.hasActiveBody() ) continue;
                for( Iterator sIt = m.getActiveBody().getUnits().iterator(); sIt.hasNext(); ) {
                    final Stmt s = (Stmt) sIt.next();
                    if(s instanceof AssignStmt) {
                        AssignStmt as = (AssignStmt) s;
                        Value rhs = as.getRightOp();
                        if( rhs instanceof AnyNewExpr ) {
                            ProbeStmt ps = probeStmt(m, s);
                            nodeToHeapObj.put(mnf.getNode(rhs), ps);
                            if(Results.v().reachableMethods().contains(m)) {
                                esc.anyAlloc().add(ps);
                            }
                        }
                    }
                }
            }
        }
        for( Iterator nodeIt = ea.escapesThread(); nodeIt.hasNext(); ) {
            final Node node = (Node) nodeIt.next();
            ProbeStmt ps = (ProbeStmt) nodeToHeapObj.get(node);
            if( ps == null ) continue;
            esc.escapesThread().add(ps);
        }
        for( Iterator nodeIt = ea.escapesMethod(); nodeIt.hasNext(); ) {
            final Node node = (Node) nodeIt.next();
            ProbeStmt ps = (ProbeStmt) nodeToHeapObj.get(node);
            if( ps == null ) continue;
            esc.escapesMethod().add(ps);
        }

        try {
            new probe.GXLWriter().write(esc, new GZIPOutputStream( new FileOutputStream(new File(
                            filename))));
        } catch (IOException ioe) {
            System.out.println("Error while writing to file: " + ioe);
        }
    }
    private ProbeStmt probeStmt(SootMethod m, Stmt s) {
        if( !s.hasTag("BytecodeOffsetTag") ) 
            throw new RuntimeException( "The statement "+s+
                    " in method "+m+" has no BytecodeOffsetTag." );
        BytecodeOffsetTag tag = (BytecodeOffsetTag) 
            s.getTag("BytecodeOffsetTag");
        return ObjectManager.v().getStmt(probeMethod(m), tag.getBytecodeOffset());
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


package probe.soot;
import soot.*;
import soot.tagkit.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import soot.jimple.*;
import soot.util.queue.*;
import soot.jimple.paddle.*;
import probe.ObjectManager;
import probe.ProbeField;
import probe.ProbeStmt;
import probe.ProbeMethod;
import probe.ProbeClass;
import probe.GXLWriter;

public class SideEffect extends SceneTransformer {
    static String filename = "SideEffect.gxl.gz";
    public static void main(String[] args) 
    {
	PackManager.v().getPack("wjtp").add(
	    new Transform("wjtp.se", new SideEffect()));

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
        System.out.println( "running phase wjtp.sideeffect" );
        probe.SideEffect se = new probe.SideEffect();
        SideEffectAnalysis sea = new SideEffectAnalysis();
        sea.analyze();

        System.out.println("building up side-effect gxl");
        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {
            final SootClass cl = (SootClass) clIt.next();
            System.out.println("processing class "+cl);
            for( Iterator mIt = cl.getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                if( !m.hasActiveBody() ) continue;
                for( Iterator sIt = m.getActiveBody().getUnits().iterator(); sIt.hasNext(); ) {
                    final Stmt s = (Stmt) sIt.next();
                    ArrayList fieldSet = new ArrayList();
                    for( Iterator pfIt = sea.readSet(m, s); pfIt.hasNext(); ) {
                        final PaddleField pf = (PaddleField) pfIt.next();
                        ProbeField probeField = probeField(pf);
                        if( probeField != null ) {
                            fieldSet.add(probeField);
                        }
                    }
                    if( !fieldSet.isEmpty() ) {
                        se.reads().put(
                                probeStmt(m, s),
                                ObjectManager.v().getFieldSet(fieldSet)
                                );
                    }
                    fieldSet = new ArrayList();
                    for( Iterator pfIt = sea.writeSet(m, s); pfIt.hasNext(); ) {
                        final PaddleField pf = (PaddleField) pfIt.next();
                        ProbeField probeField = probeField(pf);
                        if( probeField != null ) {
                            fieldSet.add(probeField);
                        }
                    }
                    if( !fieldSet.isEmpty() ) {
                        se.writes().put(
                                probeStmt(m, s),
                                ObjectManager.v().getFieldSet(fieldSet)
                                );
                    }
                }
            }
        }
        System.out.println("done building up side-effect gxl; writing");

        try {
            new GXLWriter().write(se, new GZIPOutputStream( new FileOutputStream(new File(
                            filename))));
        } catch (IOException ioe) {
            System.out.println("Error while writing to file: " + ioe);
        }
        System.out.println("done writing side-effect gxl");
    }
    private ProbeField probeField(PaddleField pf) {
        if( !(pf instanceof SootField) ) {
            return null;
        }
        SootField sf = (SootField) pf;
        return ObjectManager.v().getField(probeClass(sf.getDeclaringClass()),
                    sf.getName());
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
        ProbeClass cls = probeClass(m.getDeclaringClass());
        ProbeMethod gm = ObjectManager.v().getMethod(cls,
                m.getName(), m.getBytecodeParms());
        return gm;
    }
    private ProbeClass probeClass(SootClass cl) {
        return ObjectManager.v().getClass(cl.getPackageName(),
                cl.getShortName());
    }
}


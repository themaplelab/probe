package probe.soot;
import soot.*;
import soot.tagkit.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import soot.jimple.*;
import soot.util.queue.*;
import soot.jimple.paddle.*;
import soot.jimple.toolkits.pointer.*;
import soot.toolkits.graph.*;
import probe.ObjectManager;
import probe.ProbeStmt;
import probe.ProbeMethod;
import probe.ProbeClass;
import probe.GXLWriter;

public class FailCastIntra extends SceneTransformer {
    static String filename = "FailCastIntra.gxl.gz";
    public static void main(String[] args) 
    {
	PackManager.v().getPack("wjtp").add(
	    new Transform("wjtp.fci", new FailCast()));

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
        System.out.println("running wjtp.failcastintra");
        probe.FailCast fc = new probe.FailCast();
        FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {
            final SootClass cl = (SootClass) clIt.next();
            for( Iterator mIt = cl.getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                if( !m.hasActiveBody() ) continue;
                Body b = m.getActiveBody();
                CastCheckEliminator cce = new CastCheckEliminator(
                        new BriefUnitGraph(b));

                for( Iterator sIt = b.getUnits().iterator(); sIt.hasNext(); ) {

                    final Stmt s = (Stmt) sIt.next();
                    if( !s.hasTag("CastCheckTag") ) continue;
                    CastCheckTag tag = (CastCheckTag) s.getTag("CastCheckTag");
                    if(s.hasTag("BytecodeOffsetTag")) fc.anyCast().add(probeStmt(m, s));
                    if( !tag.canEliminateCheck() ) {
                        if(s.hasTag("BytecodeOffsetTag")) fc.stmts().add(probeStmt(m, s));
                    }
                }
            }
        }

        try {
            new GXLWriter().write(fc, new GZIPOutputStream( new FileOutputStream(new File(
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


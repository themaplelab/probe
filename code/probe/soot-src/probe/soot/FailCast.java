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
import probe.ProbeStmt;
import probe.ProbeMethod;
import probe.ProbeClass;
import probe.GXLWriter;

public class FailCast extends SceneTransformer {
    static String filename = "FailCast.gxl.gz";
    public static void main(String[] args) 
    {
	PackManager.v().getPack("wjtp").add(
	    new Transform("wjtp.fc", new FailCast()));

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
        System.out.println("running wjtp.failcast");
        probe.FailCast fc = new probe.FailCast();
        FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
        PointsToAnalysis pa = Scene.v().getPointsToAnalysis();
        int totalCasts = 0;
        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {
            final SootClass cl = (SootClass) clIt.next();
            for( Iterator mIt = cl.getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                if( !m.hasActiveBody() ) continue;
stmt:
                for( Iterator sIt = m.getActiveBody().getUnits().iterator(); sIt.hasNext(); ) {
                    final Stmt s = (Stmt) sIt.next();
                    if( !(s instanceof AssignStmt) ) continue;
                    AssignStmt as = (AssignStmt) s;
                    Value rhs = as.getRightOp();
                    if( !(rhs instanceof CastExpr) ) continue;
                    CastExpr ce = (CastExpr) rhs;
                    if(s.hasTag("BytecodeOffsetTag")) fc.anyCast().add(probeStmt(m, s));
                    totalCasts++;
                    Value opv = ce.getOp();
                    if(!(opv instanceof Local)) continue;
                    Local op = (Local) opv;
                    if(! (op.getType() instanceof RefLikeType) ) {
                        if(s.hasTag("BytecodeOffsetTag")) fc.stmts().add(probeStmt(m, s));
                        continue;
                    }
                    PointsToSet pt = pa.reachingObjects(op);
                    for( Iterator tIt = pt.possibleTypes().iterator(); tIt.hasNext(); ) {
                        final Type t = (Type) tIt.next();
                        Type child;
                        if( t instanceof AnySubType ) { 
                            child = ((AnySubType) t).getBase();
                        } else {
                            child = t;
                        }
                        if( !fh.canStoreType(t, ce.getCastType()) ) {
                            if(s.hasTag("BytecodeOffsetTag")) fc.stmts().add(probeStmt(m, s));
                            //System.out.println( "added "+probeStmt(m,s)+" because ptset of "+op+" has possible type "+t);
                            continue stmt;
                        }
                    }
                }
            }
        }
        System.out.println("Total casts: "+totalCasts);

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


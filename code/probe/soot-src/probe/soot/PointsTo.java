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
import probe.ProbeParameter;
import probe.Pointer;
import probe.HeapObject;
import probe.External;

public class PointsTo extends SceneTransformer {
    static String filename = "PointsTo.gxl.gz";
    public static void main(String[] args) 
    {
	PackManager.v().getPack("wjtp").add(
	    new Transform("wjtp.probept", new PointsTo()));


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
        System.out.println("running wjtp.probept");
        Map nodeToPointer = new HashMap();
        final Map nodeToHeapObj = new HashMap();

        probe.PointsTo probept = new probe.PointsTo();
        NodeManager nm = PaddleScene.v().nodeManager();
        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {
            final SootClass cl = (SootClass) clIt.next();
            for( Iterator mIt = cl.getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                MethodNodeFactory mnf = new MethodNodeFactory(m, Results.v().nodeFactory());
                for( int i = 0; i < m.getParameterCount(); i++ ) {
                    if(!(m.getParameterTypes().get(i) instanceof RefLikeType))
                        continue;
                    nodeToPointer.put( mnf.caseParm(i), probeParm( m, i ) );
                }
                if(!m.isStatic()) {
                    nodeToPointer.put( mnf.caseThis(), probeParm(m, -1) );
                }
                if(m.getReturnType() instanceof RefLikeType) {
                    nodeToPointer.put( mnf.caseRet(), probeParm(m, -2) );
                }
                if( !m.hasActiveBody() ) continue;
                for( Iterator sIt = m.getActiveBody().getUnits().iterator(); sIt.hasNext(); ) {
                    final Stmt s = (Stmt) sIt.next();
                    if(s instanceof AssignStmt) {
                        AssignStmt as = (AssignStmt) s;
                        Value rhs = as.getRightOp();
                        if( rhs instanceof AnyNewExpr ) {
                            nodeToHeapObj.put(mnf.getNode(rhs),
                                    probeStmt(m, s));
                        }
                    }
                    for( Iterator vbIt = s.getUseAndDefBoxes().iterator(); vbIt.hasNext(); ) {
                        final ValueBox vb = (ValueBox) vbIt.next();
                        Value v = vb.getValue();
                        Local l = null;
                        if( v instanceof InstanceFieldRef ) {
                            l = (Local) ((InstanceFieldRef)v).getBase();
                        } else if( v instanceof ArrayRef ) {
                            l = (Local) ((ArrayRef)v).getBase();
                        } else if(v instanceof InstanceInvokeExpr) {
                            l = (Local) ((InstanceInvokeExpr)v).getBase();
                        }
                        if( l == null ) continue;
                        nodeToPointer.put(mnf.getNode(l), probeStmt(m, s));
                    }
                }
            }
        }
        AbsP2Sets p2sets = Results.v().p2sets();
        for( Iterator vnIt = nodeToPointer.keySet().iterator(); vnIt.hasNext(); ) {
            final VarNode vn = (VarNode) vnIt.next();
            final HashSet hs = new HashSet();
            for( Iterator cvnIt = vn.contexts(); cvnIt.hasNext(); ) {
                final ContextVarNode cvn = (ContextVarNode) cvnIt.next();
                p2sets.get(cvn).forall( new P2SetVisitor() {
                public final void visit( ContextAllocNode n ) {
                    ContextAllocNode can = (ContextAllocNode) n;
                    AllocNode an = can.obj();
                    HeapObject ho = (HeapObject) nodeToHeapObj.get(an);
                    if( ho == null ) ho = External.v();
                    hs.add(ho);
                }});
            }
            Pointer ptr = (Pointer) nodeToPointer.get(vn);
            probept.pointsTo().put( ptr, ObjectManager.v().getPtSet(hs) );
        }

        try {
            new probe.GXLWriter().write(probept, new GZIPOutputStream( new FileOutputStream(new File(
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
    private ProbeParameter probeParm(SootMethod m, int i) {
        return ObjectManager.v().getParameter(probeMethod(m), i);
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



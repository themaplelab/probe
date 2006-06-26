package probe.soot;
import soot.*;
import soot.tagkit.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import soot.jimple.*;
import soot.util.queue.*;
import soot.jimple.paddle.*;
import soot.jimple.toolkits.callgraph.*;
import probe.ObjectManager;
import probe.ProbeMethod;
import probe.ProbeClass;
import probe.GXLWriter;

public class CIRecursive extends SceneTransformer {
    static String filename = "CIRecursive.gxl.gz";
    public static void main(String[] args) 
    {
	PackManager.v().getPack("wjtp").add(
	    new Transform("wjtp.probecirec", new CIRecursive()));


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
        System.out.println( "running phase wjtp.cirecursive" );
        probe.Recursive rec = new probe.Recursive();
        BDDRecursiveAnalysis ra = new BDDRecursiveAnalysis();
        ra.setContextInsensitive();
        ra.analyze();

        for( Iterator mIt = ra.recursiveMethods(); mIt.hasNext(); ) {

            final SootMethod m = (SootMethod) mIt.next();
            rec.methods().add(probeMethod(m));
        }

        try {
            new GXLWriter().write(rec, new GZIPOutputStream( new FileOutputStream(new File(
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



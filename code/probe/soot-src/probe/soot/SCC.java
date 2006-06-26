package probe.soot;
import soot.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import soot.jimple.*;
import soot.util.queue.*;
import soot.jimple.paddle.*;
import soot.toolkits.graph.*;
import probe.ObjectManager;
import probe.ProbeMethod;
import probe.ProbeClass;

public class SCC extends SceneTransformer {
    /*
    static String filename = "SCC.gxl.gz";
    public static void main(String[] args) 
    {
	PackManager.v().getPack("wjtp").add(
	    new Transform("wjtp.scc", new SCC()));


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

    */
    protected void internalTransform(String phaseName, Map options)
    {
        /*
        System.out.println("running wjtp.scc");
        probe.CallGraph probecg = new probe.CallGraph();
        StronglyConnectedComponents scc = ZhuContext.savedScc;
        int totalNodes = 0;

        for( Iterator componentIt = scc.getComponents().iterator(); componentIt.hasNext(); ) {

            final List component = (List) componentIt.next();
            totalNodes += component.size();
        }
        System.out.println("Total nodes: "+totalNodes);

        for( Iterator componentIt = scc.getComponents().iterator(); componentIt.hasNext(); ) {

            final List component = (List) componentIt.next();
            if(component.size() < 1000) continue;
            System.out.println("Component of "+component.size()+" nodes ("+
                    (100*component.size()+totalNodes/2)/totalNodes+"%)");
            for( Iterator mIt = component.iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                probecg.entryPoints().add(probeMethod(m));
            }
        }

        try {
            new probe.GXLWriter().write(probecg, new GZIPOutputStream( new FileOutputStream(new File(
                            filename))));
        } catch (IOException ioe) {
            System.out.println("Error while writing to file: " + ioe);
        }
        */
    }
    /*
    private ProbeMethod probeMethod(SootMethod m) {
        SootClass cl = m.getDeclaringClass();
        ProbeClass cls = ObjectManager.v().getClass(cl.getPackageName(),
                cl.getShortName());
        ProbeMethod gm = ObjectManager.v().getMethod(cls,
                m.getName(), m.getBytecodeParms());
        return gm;
    }
    */
}



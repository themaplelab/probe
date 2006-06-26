package probe;
import java.util.*;
import java.io.*;
import java.util.zip.*;

/** Calculates and reports the differences between two call graphs. */
public class CallGraphBFS {
    public static void usage() {
        System.out.println( "Usage: java probe.CallGraphBFS graph.gxl package class methodname methodsig [package2 class2 methodname2 methodsig2]" );
        System.exit(1);
    }
    public static final void main( String[] args ) {
        boolean doneOptions = false;
        String filename = null;
        String pkg = null;
        String cls = null;
        String method = null;
        String sig = null;
        String pkg2 = null;
        String cls2 = null;
        String method2 = null;
        String sig2 = null;
        LinkedList ignore = new LinkedList();
        for( int i = 0; i < args.length; i++ ) {
            if( !doneOptions && args[i].equals("--") ) doneOptions = true;
            else if( filename == null ) filename = args[i];
            else if( pkg == null ) pkg = args[i];
            else if( cls == null ) cls = args[i];
            else if( method == null ) method = args[i];
            else if( sig == null ) sig = args[i];
            else if( pkg2 == null ) pkg2 = args[i];
            else if( cls2 == null ) cls2 = args[i];
            else if( method2 == null ) method2 = args[i];
            else if( sig2 == null ) sig2 = args[i];
            else ignore.add(args[i]);
        }
        if( pkg2 == null ) pkg2 = pkg;
        if( cls2 == null ) cls2 = cls;
        if( method2 == null ) method2 = method;
        if( sig2 == null ) sig2 = sig;
        if( sig2 == null ) usage();

        CallGraph a;
        try {
            try {
                a = new GXLReader().readCallGraph(new FileInputStream(filename));
            } catch( RuntimeException e ) {
                a = new GXLReader().readCallGraph(new GZIPInputStream(new FileInputStream(filename)));
            }
        } catch( IOException e ) {
            throw new RuntimeException( "caught IOException "+e );
        }

        Set reachables = a.findReachables();

        ProbeClass pc = ObjectManager.v().getClass(pkg, cls);
        ProbeMethod pm = ObjectManager.v().getMethod(pc, method, sig);
        if( !reachables.contains(pm) ) {
            System.out.println( ""+pm+" not reachable in call graph" );
        }
        ProbeClass pc2 = ObjectManager.v().getClass(pkg2, cls2);
        ProbeMethod pm2 = ObjectManager.v().getMethod(pc2, method2, sig2);
        if( !reachables.contains(pm2) ) {
            System.out.println( ""+pm2+" not reachable in call graph" );
        }
        Set ignoreMethods = new HashSet();
        while(!ignore.isEmpty()) {
            String ipkg = (String) ignore.removeFirst();
            String icls = (String) ignore.removeFirst();
            String imethod = (String) ignore.removeFirst();
            String isig = (String) ignore.removeFirst();
            ProbeClass ipc = ObjectManager.v().getClass(ipkg, icls);
            ProbeMethod ipm = ObjectManager.v().getMethod(ipc, imethod, isig);
            if( !reachables.contains(ipm) ) {
                System.out.println( ""+ipm+" not reachable in call graph" );
            }
            ignoreMethods.add(ipm);
        }
        LinkedList queue = new LinkedList();
        HashMap prev = new HashMap();
        queue.addLast(pm);
        while(!queue.isEmpty()) {
            ProbeMethod m = (ProbeMethod) queue.removeFirst();
            for( Iterator eIt = a.edges().iterator(); eIt.hasNext(); ) {
                final Edge e = (Edge) eIt.next();
                if( e.src() != m ) continue;
                ProbeMethod dst = e.dst();
                if(ignoreMethods.contains(dst)) continue;
                if(prev.containsKey(dst)) continue;
                prev.put(dst, m);
                queue.addLast(dst);
                if(dst == pm2) {
                    dumpPath( prev, pm2, pm );
                }
            }
        }
    }

    public static void dumpPath( Map prev, ProbeMethod cur, ProbeMethod root ) {
        ProbeMethod prevMethod = (ProbeMethod) prev.get(cur);
        if( prevMethod == root ) {
            System.out.println(prevMethod);
            System.out.println(cur);
            return;
        }
        dumpPath(prev, prevMethod, root);
        System.out.println(cur);
    }
}


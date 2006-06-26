package probe;
import java.util.*;
import java.io.*;

/** Outputs statistics about the number of potentially recursive methods. */
public class RecursiveInfo {
    public static String dashLib = null;
    public static boolean dashV = false;
    public static String filename = null;
    public static void usage() {
        System.out.println( "Usage: java probe.RecursiveInfo [options] recursive.gxl" );
        System.out.println( "  -v : print list of potentially recursive methods" );
        System.out.println( "  -lib file : ignore methods in packages listed in file" );
        System.exit(1);
    }
    public static final void main( String[] args ) {
        boolean doneOptions = false;
        for( int i = 0; i < args.length; i++ ) {
            if( !doneOptions && args[i].equals("-lib") ) dashLib = args[++i];
            else if( !doneOptions && args[i].equals("-v") ) dashV = true;
            else if( filename == null ) filename = args[i];
            else usage();
        }
        if( filename == null ) usage();

        Collection libs = Util.readLib(dashLib);

        Recursive a;
        try {
            a = new GXLReader().readRecursive(new FileInputStream(filename));
        } catch( IOException e ) {
            throw new RuntimeException( "caught IOException "+e );
        }

        Collection methods = Util.filterLibs(libs, a.methods());
        System.out.println( "Potentially recursive methods : "+methods.size() );

        if(dashV) {
            for( Iterator mIt = methods.iterator(); mIt.hasNext(); ) {
                final ProbeMethod m = (ProbeMethod) mIt.next();
                System.out.println(m);
            }
        }
    }

}


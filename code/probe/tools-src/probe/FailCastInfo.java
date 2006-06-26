package probe;
import java.util.*;
import java.io.*;
import java.util.zip.*;

/** Outputs statistics about the number of cast statements that may fail.
 * */
public class FailCastInfo {
    public static boolean dashA = false;
    public static boolean dashV = false;
    public static String dashLib = null;
    public static String filename = null;
    public static void usage() {
        System.out.println( "Usage: java probe.FailCastInfo [options] failcast.gxl" );
        System.out.println( "  -a : print list of all cast statements" );
        System.out.println( "  -v : print list of potentially failing cast statements" );
        System.out.println( "  -lib file : ignore methods in packages listed in file" );
        System.exit(1);
    }
    public static final void main( String[] args ) {
        boolean doneOptions = false;
        for( int i = 0; i < args.length; i++ ) {
            if( !doneOptions && args[i].equals("-v") ) dashV = true;
            else if( !doneOptions && args[i].equals("-a") ) dashA = true;
            else if( !doneOptions && args[i].equals("-lib") ) dashLib = args[++i];
            else if( !doneOptions && args[i].equals("--") ) doneOptions = true;
            else if( filename == null ) filename = args[i];
            else usage();
        }
        if( filename == null ) usage();

        Collection libs = Util.readLib(dashLib);

        FailCast a;
        try {
            try {
                a = new GXLReader().readFailCast(new FileInputStream(filename));
            } catch( RuntimeException e ) {
                a = new GXLReader().readFailCast(new GZIPInputStream(new FileInputStream(filename)));
            }
        } catch( IOException e ) {
            throw new RuntimeException( "caught IOException "+e );
        }

        Collection stmts = Util.filterLibs(libs, a.stmts());
        System.out.println( "Potentially failing casts : "+stmts.size() );

        if(dashV) {
            for( Iterator sIt = stmts.iterator(); sIt.hasNext(); ) {
                final ProbeStmt s = (ProbeStmt) sIt.next();
                System.out.println(s);
            }
        }

        Collection executes = Util.filterLibs(libs, a.executes());
        System.out.println( "Total casts : "+executes.size() );

        if(dashA) {
            for( Iterator sIt = executes.iterator(); sIt.hasNext(); ) {
                final ProbeStmt s = (ProbeStmt) sIt.next();
                System.out.println(s);
            }
        }
    }

}


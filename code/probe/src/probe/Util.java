package probe;
import java.util.*;
import java.io.*;

/** Utility methods. */
public class Util {
    public static Collection readLib(String filename) {
        if(filename == null) return null;
        Collection ret = new HashSet();
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(filename));
            while(in.available() != 0) {
                ret.add(in.readLine());
            }
            in.close();
        } catch( Exception e ) {
            throw new RuntimeException(e);
        }
        return ret;
    }

    public static Collection filterLibs(Collection libs, Collection items) {
        if(libs == null ) return items;
        Collection ret = new HashSet();
        for( Iterator itemIt = items.iterator(); itemIt.hasNext(); ) {
            final Object item = (Object) itemIt.next();
            Object newItem = item;
            if( newItem instanceof Edge ) {
                newItem = ((Edge) newItem).src();
            }
            if( newItem instanceof ProbeStmt ) {
                newItem = ((ProbeStmt) newItem).method();
            }
            if( newItem instanceof ProbeMethod ) {
                newItem = ((ProbeMethod) newItem).cls();
            }
            if( newItem instanceof ProbeClass ) {
                if( !libs.contains(((ProbeClass)newItem).pkg()) ) ret.add(item);
            } else throw new RuntimeException("unrecognized item "+item+" of type "+item.getClass());
        }
        return ret;
    }
}


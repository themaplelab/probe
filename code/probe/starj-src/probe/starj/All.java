package probe.starj;
import starj.*;
import starj.toolkits.printers.*;
import starj.dependencies.*;
import starj.events.*;
import starj.util.*;
import starj.spec.*;
import starj.toolkits.services.*;
import java.util.*;
import probe.ProbeMethod;
import probe.ProbeClass;
import probe.ObjectManager;
import java.io.*;


public class All {
    public static final void main( String[] args ) {
        Pack printers = (Pack) Scene.v().getRootPack().getByName("toolkits.printers");
        printers.add(new CallGraph());
        printers.add(new ExecutesMany());
        printers.add(new Polymorphic());
        printers.add(new Recursive());
        printers.add(new FailCast());
        printers.add(new SideEffect());
        Main.main(args);
    }
}

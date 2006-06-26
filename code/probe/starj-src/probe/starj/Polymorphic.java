package probe.starj;
import starj.*;
import starj.coffer.*;
import starj.toolkits.printers.*;
import starj.dependencies.*;
import starj.events.*;
import starj.util.*;
import starj.spec.*;
import starj.toolkits.services.*;
import java.util.*;
import probe.ProbeStmt;
import probe.ProbeMethod;
import probe.ProbeClass;
import probe.ObjectManager;
import probe.GXLWriter;
import java.io.*;


public class Polymorphic extends AbstractPrinter {
    public static final void main( String[] args ) {
        Pack printers = (Pack) Scene.v().getRootPack().getByName("toolkits.printers");
        printers.add(new Polymorphic());
        Main.main(args);
    }

    public Polymorphic() {
        super("polymorphic", "Outputs a GXL set of polymorphic invokes");
    }

    public OperationSet operationDependencies() {
        OperationSet depSet = new OperationSet();
        depSet.add(InstructionResolver.v());
        return depSet;
    }
    public EventDependencySet eventDependencies() {
        EventDependencySet dep_set = new EventDependencySet();
        FieldMask method_mask = new TotalMask(
                Constants.FIELD_RECORDED
                | Constants.FIELD_METHOD_ID);

        dep_set.add(new EventDependency(
                Event.METHOD_ENTRY2,
                method_mask,
                true,
                new EventDependency(
                    Event.METHOD_ENTRY,
                    method_mask,
                    true)));

        return dep_set;
    }

    public void apply( EventBox box ) {
        Event event = box.getEvent();
        
        AbstractMethodEntryEvent e = (AbstractMethodEntryEvent) event;
        InstructionContext ic = e.getCallSiteContext();
        if (ic == null) {
            return;
        }
        
        InvokeInstruction call_site = (InvokeInstruction) ic.getInstruction();
        if (call_site == null || !call_site.isDynamic()) {
            return;
        }

        MethodEntity target = IDResolver.v().getMethodEntity(e.getMethodID());
        if (target == null) {
            return;
        }

        int offset = call_site.getOffset();
        ProbeStmt stmt = findStmt(offset, ic.getMethod());
        MethodEntity oldTarget = (MethodEntity) targets.get(stmt);
        if( oldTarget == null ) {
            targets.put(stmt, target);
        } else if( !oldTarget.equals(target) ) {
            polymorphic.add(stmt);
        }
    }
    public void done() {
        probe.Polymorphic poly = new probe.Polymorphic();
        poly.stmts().addAll( polymorphic );
        try {
            new GXLWriter().write(poly, out);
        } catch( IOException e ) {
            throw new RuntimeException( "Got IOException writing GXL: "+e );
        }
        super.done();
    }

    private ProbeStmt findStmt( int offset, MethodEntity me ) {
        return ObjectManager.v().getStmt(findMethod(me), offset);
    }
    private ProbeMethod findMethod( MethodEntity me ) {
        String sig = me.getMethodSignature();
        int i = sig.indexOf(')');
        sig = sig.substring(1, i);
        String name = me.getMethodName();
        ClassEntity ce = me.getClassEntity();
        String className = ce.getClassName();
        ProbeClass cl = ObjectManager.v().getClass(className);
        ProbeMethod m = ObjectManager.v().getMethod(cl, name, sig);
        return m;
    }

    private HashMap targets = new HashMap();
    private HashSet polymorphic = new HashSet();
}

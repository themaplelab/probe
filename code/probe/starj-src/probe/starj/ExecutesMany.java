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


public class ExecutesMany extends AbstractPrinter {
    public static final void main( String[] args ) {
        Pack printers = (Pack) Scene.v().getRootPack().getByName("toolkits.printers");
        printers.add(new ExecutesMany());
        Main.main(args);
    }

    public ExecutesMany() {
        super("executesmany", "Outputs a GXL set of allocations that execute more than once");
    }

    public OperationSet operationDependencies() {
        OperationSet depSet = new OperationSet();
        depSet.add(InstructionResolver.v());
        return depSet;
    }
    public EventDependencySet eventDependencies() {
        EventDependencySet dep_set = new EventDependencySet();
        dep_set.add(new EventDependency(
                Event.INSTRUCTION_START,
                new TotalMask(Constants.FIELD_RECORDED
                             |Constants.FIELD_ENV_ID
                             |Constants.FIELD_OFFSET),
                true));

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
        
        switch (event.getID()) {
            case Event.INSTRUCTION_START:
                {
                    InstructionStartEvent e = (InstructionStartEvent) event;
                    Instruction i = e.getInstruction();
                    if( i == null ) {
                        throw new RuntimeException("Got null instruction!");
                    }
                    int opcode = i.getOpcode();
                    if( opcode == Code.NEW 
                     || opcode == Code.NEWARRAY 
                     || opcode == Code.ANEWARRAY 
                     || opcode == Code.MULTIANEWARRAY ) {
                        InstructionContext ic = InstructionResolver.v().
                            getCurrentContext(e.getEnvID());
                        MethodEntity currentMethod = ic.getMethod();
                        int offset = e.getOffset();
                        ProbeStmt stmt = findStmt(offset, currentMethod);
                        if( !executesonce.add(stmt) ) executesmany.add(stmt);
                    }
                }
                break;
            case Event.METHOD_ENTRY:
            case Event.METHOD_ENTRY2:
                break;
            default:
                throw new RuntimeException("weird event");
        }        
    }
    public void done() {
        probe.ExecutesMany eo = new probe.ExecutesMany();
        eo.stmts().addAll( executesmany );
        try {
            new GXLWriter().write(eo, out);
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

    private HashSet executesonce = new HashSet();
    private HashSet executesmany = new HashSet();
}

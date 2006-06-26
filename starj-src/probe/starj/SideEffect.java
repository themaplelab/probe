package probe.starj;
import starj.*;
import starj.toolkits.printers.*;
import starj.dependencies.*;
import starj.events.*;
import starj.util.*;
import starj.spec.*;
import starj.toolkits.services.*;
import starj.coffer.*;
import java.util.*;
import probe.ProbeStmt;
import probe.ProbeField;
import probe.ProbeFieldSet;
import probe.ProbeMethod;
import probe.ProbeClass;
import probe.ObjectManager;
import probe.GXLWriter;
import java.io.*;


public class SideEffect extends AbstractPrinter {
    public static final void main( String[] args ) {
        Pack printers = (Pack) Scene.v().getRootPack().getByName("toolkits.printers");
        printers.add(new SideEffect());
        Main.main(args);
    }

    public SideEffect() {
        super("sideeffect", "Outputs a GXL side-effect set");
    }

    public EventDependencySet eventDependencies() {
        EventDependencySet depSet = new EventDependencySet();
        FieldMask ei = 
            new TotalMask( Constants.FIELD_ENV_ID|Constants.FIELD_METHOD_ID );
        depSet.add(new EventDependency(Event.METHOD_ENTRY2,
                    ei,
                    true,
                    new EventDependency(Event.METHOD_ENTRY,
                        ei,
                        true)));
        depSet.add(new EventDependency(Event.METHOD_EXIT,
                    ei,
                    true));

        depSet.add(new EventDependency(
                Event.INSTRUCTION_START,
                new TotalMask(Constants.FIELD_RECORDED
                             |Constants.FIELD_ENV_ID
                             |Constants.FIELD_OFFSET
                             |Constants.FIELD_METHOD_ID),
                true));

        return depSet;
    }

    public OperationSet operationDependencies() {
        OperationSet depSet = new OperationSet();
        depSet.add(IDResolver.v());
        return depSet;
    }

    public void apply( EventBox box ) {
        Event event = box.getEvent();
        
        switch (event.getID()) {
            case Event.METHOD_ENTRY:
            case Event.METHOD_ENTRY2:
                {
                    AbstractMethodEntryEvent e = (AbstractMethodEntryEvent) event;
                    MethodEntity me = IDResolver.v().getMethodEntity(e.getMethodID());

                    if( me == null ) break;

                    me2id.put( me, e.getMethodID() );

                    int thread = event.getEnvID();

                    LinkedList stack = (LinkedList) stacks.get( thread );
                    if( stack == null ) {
                        stack = new LinkedList();
                        stacks.put( thread, stack );
                    }
                    stack.addLast( me );

                    LinkedList instStack = (LinkedList) instStacks.get( thread );
                    if( instStack == null ) {
                        instStack = new LinkedList();
                        instStacks.put( thread, instStack );
                    }
                    instStack.addLast( (ProbeStmt) envToInst.get(e.getEnvID()) );
                }
                break;
            case Event.METHOD_EXIT:
                {
                    MethodExitEvent e = (MethodExitEvent) event;

                    MethodEntity me = IDResolver.v().getMethodEntity(e.getMethodID());

                    if( me == null ) break;

                    me2id.put( me, e.getMethodID() );

                    int thread = e.getEnvID();

                    LinkedList stack = (LinkedList) stacks.get( thread );
                    if( stack == null ) {
                        stack = new LinkedList();
                        stacks.put( thread, stack );
                    }

                    if( stack.isEmpty() ) {
                        System.out.println( "Stack is empty when exiting method "+me );
                        break;
                    }
                    MethodEntity top = (MethodEntity) stack.getLast();
                    if( top.equals(me) ) {
                        stack.removeLast();
                    } else {
                        // Sometimes JVMPI is broken and the stack gets 
                        // messed up. If the current method is nowhere
                        // on the stack, just ignore it. It it is on
                        // the stack, just chop off the stack at the current
                        // method.
                        System.out.println( "Exiting method "+me+" but stack is "+stack );
                        int i = stack.lastIndexOf(me);
                        if(i>=0) {
                            while(stack.size()>i) {
                                stack.removeLast();
                            }
                        }
                    }

                    LinkedList instStack = (LinkedList) instStacks.get( thread );
                    instStack.removeLast();

                }
                break;
                case Event.INSTRUCTION_START:
                {
                    InstructionStartEvent e = (InstructionStartEvent) event;
                    Instruction i = e.getInstruction();
                    int thread = event.getEnvID();
                    int opcode = i.getOpcode();
                    envToInst.put(thread, stmt(e));

                    if( opcode == Code.GETFIELD 
                     || opcode == Code.GETSTATIC ) {
                        ProbeStmt stmt = stmt(e);
                        ProbeField field = getField((FieldInstruction) i);
                        addRead( stmt, field );
                        LinkedList instStack = (LinkedList) instStacks.get( thread );
                        for( Iterator sIt = instStack.iterator(); sIt.hasNext(); ) {
                            final ProbeStmt s = (ProbeStmt) sIt.next();
                            addRead( stmt, field );
                        }
                    } else if( opcode == Code.PUTFIELD
                            || opcode == Code.PUTSTATIC ) {
                        ProbeStmt stmt = stmt(e);
                        ProbeField field = getField((FieldInstruction) i);
                        addWrite( stmt, field );
                        LinkedList instStack = (LinkedList) instStacks.get( thread );
                        for( Iterator sIt = instStack.iterator(); sIt.hasNext(); ) {
                            final ProbeStmt s = (ProbeStmt) sIt.next();
                            addWrite( stmt, field );
                        }
                    }
                }
                break;
            default:
                throw new RuntimeException("weird event");
        }        
    }
    private ProbeField getField( FieldInstruction i ) {
        FieldrefConstant frc = (FieldrefConstant) i.getConstant();
        ConstantPool cp = i.getConstantPool();
        NameAndTypeConstant fieldDesc = (NameAndTypeConstant)
            cp.get(frc.getNameAndTypeIndex()); 
        Utf8Constant fieldName = (Utf8Constant) 
            cp.get(fieldDesc.getNameIndex());
        ClassConstant classDesc = (ClassConstant) 
            cp.get(frc.getClassIndex()); 
        Utf8Constant className = (Utf8Constant)
            cp.get(classDesc.getNameIndex());
        ProbeClass cl = ObjectManager.v().getClass(className.getValue());
        ProbeField fld = ObjectManager.v().getField(cl, fieldName.getValue());
        return fld;
    }
    private void addRead( ProbeStmt stmt, ProbeField field ) {
        add(rec.reads(), stmt, field);
    }
    private void addWrite( ProbeStmt stmt, ProbeField field ) {
        add(rec.writes(), stmt, field);
    }
    private void add(Map map, ProbeStmt stmt, ProbeField field) {
        ProbeFieldSet pfs = (ProbeFieldSet) map.get(stmt);
        Collection fields;
        if( pfs == null ) {
            fields = new HashSet();
        } else {
            fields = pfs.fields();
            if(fields.contains(field)) return;
            fields = new HashSet(fields);
            fields.add(field);
        }
        map.put(stmt, ObjectManager.v().getFieldSet(fields));
    }
    private ProbeStmt stmt( Event e ) {
        InstructionContext ic = InstructionResolver.v().
            getCurrentContext(e.getEnvID());
        MethodEntity currentMethod = ic.getMethod();
        int offset = ic.getInstruction().getOffset();
        ProbeStmt stmt = ObjectManager.v().getStmt(findMethod(currentMethod),
                offset);
        return stmt;
    }
    probe.SideEffect rec = new probe.SideEffect();
    public void done() {
        try {
            new GXLWriter().write(rec, out);
        } catch( IOException e ) {
            throw new RuntimeException( "Got IOException writing GXL: "+e );
        }
        super.done();
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

    private IntHashMap stacks = new IntHashMap();
    private IntHashMap instStacks = new IntHashMap();
    private ObjectToIntHashMap me2id = new ObjectToIntHashMap();
    private IntHashMap envToInst = new IntHashMap();
}

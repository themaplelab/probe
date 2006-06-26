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
import probe.GXLWriter;
import java.io.*;


public class Recursive extends AbstractPrinter {
    public static final void main( String[] args ) {
        Pack printers = (Pack) Scene.v().getRootPack().getByName("toolkits.printers");
        printers.add(new Recursive());
        Main.main(args);
    }

    public Recursive() {
        super("recursive", "Outputs a GXL set of recursive methods");
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
                    MethodEvent e = (MethodEvent) event;
                    MethodEntity me = IDResolver.v().getMethodEntity(e.getMethodID());

                    if( me == null ) break;

                    me2id.put( me, e.getMethodID() );

                    int thread = event.getEnvID();

                    LinkedList stack = (LinkedList) stacks.get( thread );
                    if( stack == null ) {
                        stack = new LinkedList();
                        stacks.put( thread, stack );
                    }

                    for( Iterator smeIt = stack.iterator(); smeIt.hasNext(); ) {

                        final MethodEntity sme = (MethodEntity) smeIt.next();
                        if( me.equals( sme ) ) {
                            recursive.add(me);
                        }
                    }

                    stack.addLast( me );
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
                    MethodEntity top = (MethodEntity) stack.removeLast();
                    if( !top.equals( me ) ) {
                        System.out.println( "Stack is "+stack );
                        throw new RuntimeException( "Exiting method "+me+
                                " but top of stack is "+top );
                    }
                }
                break;
            default:
                throw new RuntimeException("weird event");
        }        
    }
    public void done() {
        probe.Recursive rec = new probe.Recursive();
        for( Iterator meIt = recursive.iterator(); meIt.hasNext(); ) {
            final MethodEntity me = (MethodEntity) meIt.next();
            rec.methods().add( findMethod(me) );
        }
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
    private HashSet recursive = new HashSet();
    private ObjectToIntHashMap me2id = new ObjectToIntHashMap();
}

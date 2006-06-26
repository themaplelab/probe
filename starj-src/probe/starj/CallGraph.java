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


public class CallGraph extends AbstractPrinter {
    public static final void main( String[] args ) {
        Pack printers = (Pack) Scene.v().getRootPack().getByName("toolkits.printers");
        printers.add(new CallGraph());
        Main.main(args);
    }

    public CallGraph() {
        super("callgraph", "Outputs a dynamic call graph");
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

                    if( !stack.isEmpty() ) {
                        MethodEntity top = (MethodEntity) stack.getLast();
                        HashSet targets =
                            (HashSet) edges.get( top );
                        if( targets == null ) {
                            targets = new HashSet();
                            edges.put( top, targets );
                        }
                        targets.add( me );
                    }

                    if( stack.isEmpty() ) entryPoints.add(me);

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
                }
                break;
            default:
                throw new RuntimeException("weird event");
        }        
    }
    public void done() {
        probe.CallGraph cg = new probe.CallGraph();
        Object[] keys = me2id.keySet();
        for( int i = 0; i < keys.length; i++ ) {
            final MethodEntity src = (MethodEntity) keys[i];
            HashSet targets = (HashSet) edges.get(src);
            if( targets == null ) continue;
            ProbeMethod probeSrc = findMethod(src);
            for( Iterator tgtIt = targets.iterator(); tgtIt.hasNext(); ) {
                final MethodEntity tgt = (MethodEntity) tgtIt.next();
                cg.edges().add( new probe.CallEdge( probeSrc, findMethod(tgt) ) );
            }
        }
        for( Iterator meIt = entryPoints.iterator(); meIt.hasNext(); ) {
            final MethodEntity me = (MethodEntity) meIt.next();
            cg.entryPoints().add( findMethod(me) );
        }
        try {
            new GXLWriter().write(cg, out);
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
    private HashMap edges = new HashMap();
    private ObjectToIntHashMap me2id = new ObjectToIntHashMap();
    private HashSet entryPoints = new HashSet();
}

package probe;
import java.util.*;

/** A representation of the set of cast instructions that fail.
 */
public class FailCast {
    /** @return The (mutable) set of ProbeStmt's representing cast
     * instructions that fail.
     */
    public Set/*ProbeStmt*/ stmts() {
        return stmts;
    }

    /** @return The (mutable) set of ProbeStmt's representing any cast
     * in the program.
     */
    public Set/*ProbeStmt*/ anyCast() {
        return anyCast;
    }

    /* End of public methods. */

    private Set stmts = new HashSet();
    private Set anyCast = new HashSet();
}


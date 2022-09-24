package gitlet;

import java.io.File;
import java.io.Serializable;

/** An interface describing dumpable objects.
 *  All other gitlet objects like commit, tree, and blob are inherited
 *  from this interface. This object maintain the persistence of itself
 *  using methods @code{store()}, @code{load(id)} etc.
 *  A visualization of the Dumpables' hierarchy is as follows:
 *  *********************** Hierarchy ***************************
 *  Dumpable (getID(), log(), store(), load(Dumpable), dump())
 * |
 * +- commit (getTree(), getMsg(), getParent(), getRelative())
 * |
 * +- blob (getFile(), getContent())
 * |
 * +- tree (getMapping(), empty(), isEmpty())
 *    |
 *    + BlobTree (add(blob), merge(BlobTree))
 *    | |
 *    | +- Stage (unstage(File), addDeleted(File),
 *    |           findModified(CWD), findDeleted(CWD))
 *    |
 *    + CommitTree (add(commit))
 *
 *  @author P. N. Hilfinger, Y. Y. Y
 */
interface Dumpable extends Serializable {
    /** Return the SHA-1 value of this dumpable. */
    String getID();
    /** Compose a log of this dumpable object. */
    String log();
    /** Store a dumpable object. */
    void store(File storePath);
    /** Load a dumpable object and replace with it. */
    Dumpable load(File f);
    /** Print useful information about this object on System.out. */
    void dump();
}

package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/** Represent a gitlet tree interface.
 *  A TREE object is connected strictly with commits.
 *  The TreeNode subclass of this tree object can be
 *  both Blob and Commit
 *
 *  @author Y. Y. Y
 */
public interface tree extends Serializable, Dumpable {
    /** Return the Mapping of this tree. */
    HashMap<String, String> getMapping();
    /** Empty this tree. */
    void empty();
    /** Check if there is any element in this tree. */
    boolean isEmpty();
}

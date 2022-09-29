package gitlet;

import java.io.Serializable;
import java.util.TreeMap;

/** Represent a gitlet tree interface.
 *
 *  @author Y. Y. Y
 */
public interface Tree extends Serializable, Dumpable {
    /**
     * Return the Mapping of this tree.
     */
    TreeMap<String, String> getMapping();
    /** Empty this tree. */
    void empty();
    /** Check if there is any element in this tree. */
    boolean isEmpty();
}

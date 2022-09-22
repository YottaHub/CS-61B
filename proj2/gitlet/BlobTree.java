package gitlet;

import com.sun.source.tree.Tree;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/** Represent a gitlet blob tree object.
 *  All tree nodes of a blob tree are blobs only.
 *  It is used to representing staging area or tracked files
 *  of a commit object.
 *
 *  @author Y. Y. Y
 */
public class BlobTree implements tree {
    /** Mapping blobs in <Key filename, Value SHA-1 value> pairs. */
    private HashMap<String, String> Mapping = new HashMap<>();
    /** SHA-1 value for this blob tree. */
    private String ID;

    /** New a blob tree object
     *
     * @param blobs files included in this tree
     */
    public BlobTree(blob... blobs) {
        this.Mapping = new HashMap<>();
        for (blob b: blobs)
            this.Mapping.put(b.getFile(), b.getID());
        this.ID = Utils.sha1(Utils.serialize(this));
    }

    /** New an empty blob tree. */
    public BlobTree() {}

    /** A method to connect two blob trees, e.g. merge the
     * staging area and blob tree of a parent commit.
     * Note: old files in tracked will be replaced by new
     * files in this commit tree.
     *
     * @param stage the staging area
     */
    public void merge(Stage stage) {
        HashMap<String, String> merged = this.Mapping;
        merged.putAll(stage.getMapping());
        this.Mapping = merged;
        this.ID = Utils.sha1(Utils.serialize(this));
    }

    /** Empty this blob tree. */
    public void empty() {
        this.Mapping = new HashMap<>();
        this.ID = "";
    }

    /** Check if there is any element in this tree. */
    public boolean isEmpty() {
        return this.Mapping == null || this.Mapping.isEmpty();
    }

    /** Check if a dumpable object exists in this tree by its ID. */
    public boolean isContained(String filename) {
        return this.Mapping.containsKey(filename);
    }

    /** Return the SHA-1 value of this commit tree. */
    public String getID() {
        return this.ID;
    }

    public HashMap<String, String> getMapping() {
        return this.Mapping;
    }

    /** Return the SHA-1 value of the target file. */
    public String getBlobID(String filename) {
        return this.Mapping.get(filename);
    }

    /** Compose a verbose and tree-structure version of log on this tree
     *
     *  @return a string represent the content and structure on this tree
     */
    public String log() {
        // header of a tree object
        String log = "tree ";
        // add ID
        log += this.ID + "\n";
        if (this.Mapping != null)
            // add one line log for all dumpables in this tree
            for (Map.Entry<String, String> p: this.Mapping.entrySet())
                log += "*\t" + p.getValue() + "\t" + p.getKey() + "\n";
        return log;
    }

    /** Store this tree and generate its SHA-1 value. */
    public void store(File storePath){
        Utils.writeObject(Utils.join(storePath, this.ID), this);
    }

    /** Load a tree object by its ID and return it for assignment.
     *
     *  @param f a stored BlobTree object
     *  @return the stored BlobTree object
     */
    public Dumpable load(File f) {
        return Utils.readObject(f, BlobTree.class);
    }

    /** Print useful information about this object on System.out. */
    public void dump() {
        System.out.println(this.log());
    }
}

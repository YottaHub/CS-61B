package gitlet;

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
public class BlobTree implements Tree {
    /** Mapping blobs in <Key filename, Value SHA-1 value> pairs. */
    private HashMap<String, String> mapping = new HashMap<>();
    /** SHA-1 value for this blob tree. */
    private String Id;

    /** New a blob tree object
     *
     * @param blobs files included in this tree
     */
    public BlobTree(Blob... blobs) {
        this.mapping = new HashMap<>();
        for (Blob b: blobs) {
            this.mapping.put(b.getFile(), b.getID());
        }
        this.Id = Utils.sha1(Utils.serialize(this));
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
        HashMap<String, String> merged = this.mapping;
        merged.putAll(stage.getMapping());
        this.mapping = merged;
        this.Id = Utils.sha1(Utils.serialize(this));
    }

    /** Empty this blob tree. */
    public void empty() {
        this.mapping = new HashMap<>();
        this.Id = "";
    }

    /** Check if there is any element in this tree. */
    public boolean isEmpty() {
        return this.mapping == null || this.mapping.isEmpty();
    }

    /** Check if a dumpable object exists in this tree by its ID. */
    public boolean isContained(String filename) {
        return this.mapping.containsKey(filename);
    }

    /** Return the SHA-1 value of this commit tree. */
    public String getID() {
        return this.Id;
    }

    public HashMap<String, String> getMapping() {
        return this.mapping;
    }

    /** Return the SHA-1 value of the target file. */
    public String getBlobID(String filename) {
        return this.mapping.get(filename);
    }

    /** Compose a verbose and tree-structure version of log on this tree
     *
     *  @return a string represent the content and structure on this tree
     */
    public String log() {
        // header of a tree object
        String log = "tree ";
        // add ID
        log += this.Id + "\n";
        if (this.mapping != null) {
            // add one line log for all dumpables in this tree
            for (Map.Entry<String, String> p : this.mapping.entrySet()) {
                log += "*\t" + p.getValue() + "\t" + p.getKey() + "\n";
            }
        }
        return log;
    }

    /** Store this tree and generate its SHA-1 value. */
    public void store(File storePath){
        Utils.writeObject(Utils.join(storePath, this.Id), this);
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

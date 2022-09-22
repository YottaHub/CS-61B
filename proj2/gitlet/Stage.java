package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Stage extends BlobTree {
    /** Mapping blobs in <Key filename, Value SHA-1 value> pairs. */
    private HashMap<String, String> Mapping = new HashMap<>();
    /** Record the deleted file before next commit in stage. */
    private Map<String, String> deleted = new HashMap<>();
    /** SHA-1 value for this blob tree. */
    private String ID;

    /** New an empty stage. */
    public Stage() {}

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

    /** Add a blob to this tree and replace if overlapped. * Buggy
     *
     * @param b the added blob
     */
    public void add(blob b) {
        this.Mapping.put(b.getFile(), b.getID());
    }

    /** Unstage a file in the staging area. */
    public void unstage(String target) {
        String ID = this.Mapping.get(target);
        this.Mapping.remove(target);
        this.deleted.put(target, ID);
    }

    /** Add deleted file to the staging area. */
    public void addDeletion(String target, String ID) {
        this.deleted.put(target, ID);
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
        this.ID = Utils.sha1(Utils.serialize(this));
        Utils.writeObject(Utils.join(storePath, this.ID), this);
    }

    /** Load a tree object by its ID and return it for assignment.
     *
     *  @param f a stored BlobTree object
     *  @return the stored BlobTree object
     */
    public Dumpable load(File f) {
        return Utils.readObject(f, Stage.class);
    }

    /** Print useful information about this object on System.out. */
    public void dump() {
        System.out.println(this.log());
    }
}

package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Stage extends BlobTree {
    /** Mapping blobs in <Key filename, Value SHA-1 value> pairs. */
    private HashMap<String, String> mapping = new HashMap<>();
    /** Record the deleted file before next commit in stage. */
    private Map<String, String> deleted = new HashMap<>();
    /** SHA-1 value for this blob tree. */
    private String id;

    /** New an empty stage. */
    public Stage() { }

    /** Return the SHA-1 value of this commit tree. */
    public String getID() {
        return this.id;
    }

    public HashMap<String, String> getMapping() {
        return this.mapping;
    }

    public Map<String, String> getDeleted() {
        return this.deleted;
    }

    /** Return the SHA-1 value of the target file. */
    public String getBlobID(String filename) {
        return this.mapping.get(filename);
    }


    /** Empty this blob tree. */
    public void empty() {
        this.mapping = new HashMap<>();
        this.deleted = new HashMap<>();
        this.id = "";
    }

    /** Check if there is any element in this tree. */
    public boolean isEmpty() {
        return this.mapping.isEmpty() && this.deleted.isEmpty();
    }

    /** Check if a dumpable object exists in this tree by its ID. */
    public boolean isContained(String filename) {
        return this.mapping.containsKey(filename);
    }

    /** Check if a dumpable object exists in the deleted by its ID. */
    public boolean isDeleted(String filename) {
        return this.deleted.containsKey(filename);
    }

    /** Add a blob to this tree and replace if overlapped.
     *  If the blob has been deleted and not modified, remove it
     *  from the deleted.
     *
     * @param b the added blob
     */
    public void add(Blob b) {
        if (!this.deleted.remove(b.getFile(), b.getID())) {
            this.mapping.put(b.getFile(), b.getID());
        }
    }

    /** Unstage a file in the staging area. */
    public String unstage(String target) {
        String blobId = this.mapping.get(target);
        this.mapping.remove(target);
        return blobId;
    }

    /** Add deleted file to the staging area. */
    public void addDeletion(String target, String id) {
        this.deleted.put(target, id);
    }

    /** Check if the staging area is changed. */
    public boolean isChanged() {
        return (!this.mapping.isEmpty() || !this.deleted.isEmpty());
    }

    /** Compose a verbose and tree-structure version of log on this tree
     *
     *  @return a string represent the content and structure on this tree
     */
    public String log() {
        // header of a tree object
        String log = "tree ";
        // add ID
        log += this.id + "\n";
        log += "staged:\n";
        if (this.mapping != null) {
            // add one line log for all dumpables in this tree
            for (Map.Entry<String, String> p : this.mapping.entrySet()) {
                log += "*\t" + p.getValue() + "\t" + p.getKey() + "\n";
            }
        }
        log += "deleted:\n";
        if (this.deleted != null) {
            // add one line log for all dumpables in this tree
            for (Map.Entry<String, String> p : this.deleted.entrySet()) {
                log += "*\t" + p.getValue() + "\t" + p.getKey() + "\n";
            }
        }
        return log;
    }

    /** Store this tree and generate its SHA-1 value. */
    public void store(File storePath) {
        this.id = Utils.sha1(Utils.serialize(this));
        Utils.writeObject(Utils.join(storePath, this.id), this);
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

package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/** Represent a gitlet commit tree object.
 *  All tree nodes of a commit tree are commits only.
 *  It is used to representing a branch or commit history.
 *
 *  @author Y. Y. Y
 */
public class CommitTree implements Tree {
    /** Record <Key Commit ID, Value commit message> pairs. */
    private HashMap<String, String> mapping = new HashMap<>();
    /** The latest commit of this branch. */
    private String head;
    /** SHA-1 value of the commit tree. */
    private String id;

    /** New a commit tree object. * Buggy
     *
     * @param commits commit history of a branch
     */
    public CommitTree(Commit... commits) {
        for (Commit c: commits) {
            this.head = c.getID();
            this.mapping.put(c.getID(), c.getMsg());
        }
        this.id = Utils.sha1(Utils.serialize(this));
    }

    /** New an empty commit tree. */
    public CommitTree() { }

    /** Add a commit to this tree and replace if overlapped.
     *
     * @param c the added commit
     */
    public void add(Commit c) {
        this.mapping.put(c.getID(), c.getMsg());
        this.head = c.getID();
        this.id = Utils.sha1(Utils.serialize(this));
    }

    /** Return the ID of the latest commit in this tree. */
    public String getLast() {
        return this.head;
    }

    /** Return the commits map of this tree. */
    public HashMap<String, String> getMapping() {
        return this.mapping;
    }

    /** Empty this commit tree. */
    public void empty() {
        this.mapping = new HashMap<>();
        this.id = "";
        this.head = "";
    }

    /** Check if the mapping of this tree is empty. */
    public boolean isEmpty() {
        return this.mapping.isEmpty();
    }

    /** Return the SHA-1 value of this commit tree. */
    public String getID() {
        return this.id;
    }

    /** Find all commits in this tree with same commit message. */
    public String findMsg(String msg) {
        String ids = "";
        for (Map.Entry<String, String> p : this.mapping.entrySet()) {
            if (p.getValue().equals(msg)) {
                ids += p.getKey() + "\n";
            }
        }
        return ids;
    }

    /** Compose a verbose and tree-structure version of log on this tree
     *
     *  @return a string represent the content and structure on this tree
     */
    public String log() {
        // header of a tree object
        String log = "tree ";
        // add ID
        log += this.getID() + "\n";
        if (this.mapping != null) {
            // add one line log for all dumpables in this tree
            for (Map.Entry<String, String> p : this.mapping.entrySet()) {
                log += "*\t" + p.getValue() + "\t" + p.getKey() + "\n";
            }
        }
        return log;
    }

    /** Store this tree and generate its SHA-1 value. */
    public void store(File storePath) {
        Utils.writeObject(Utils.join(storePath, this.id), this);
    }

    /** Load a tree object by its ID and return it for assignment.
     *
     *  @param f a stored CommitTree object
     *  @return the stored CommitTree object
     */
    public Dumpable load(File f) {
        return Utils.readObject(f, CommitTree.class);
    }

    /** Print useful information about this object on System.out. */
    public void dump() {
        System.out.println(this.log());
    }
}

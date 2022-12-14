package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Represents a gitlet commit object.
 *
 *  @author Y. Y. Y
 */
public class Commit implements Serializable, Dumpable {
    /** The message of this Commit. */
    private String message;
    /** The creation time of this Commit. */
    private Date timeStamp;
    /** Store parents of this Commit. */
    // Note: Simplified for two parents at most in this project
    private String[] parents = new String[2];
    /** The SHA-1 value of this Commit. */
    private String id;
    /** The ID of the tracked blob tree. */
    private String tree;

    /**
     * New a commit object
     *
     * @param d    creation time
     * @param msg  user victimized massage
     * @param p    SHA-1 value of the parent commit
     * @param t    SHA-1 value of the tracked blob tree
     */
    public Commit(Date d, String msg, String p, String t) {
        this.timeStamp = d;
        this.message = msg;
        this.parents[0] = p;
        this.tree = t;
        this.id = Utils.sha1(Utils.serialize(this));
    }

    /**
     * New a commit object
     *
     * @param d    creation time
     * @param msg  user victimized massage
     * @param p    SHA-1 value of the parent commit
     * @param r    Id of the relative commit or null
     * @param t    SHA-1 value of the tracked blob tree
     */
    public Commit(Date d, String msg, String p, String r, String t) {
        this.timeStamp = d;
        this.message = msg;
        this.parents[0] = p;
        this.parents[1] = r;
        this.tree = t;
        this.id = Utils.sha1(Utils.serialize(this));
    }

    /** New an empty commit. */
    public Commit() { }

    /**
     * Add this commit to another branch.
     */
    public void setRelative(String relative) {
        if (this.parents[1] == null) {
            this.parents[1] = relative;
        }
    }

    /** Return the related tree. */
    public String getTree() {
        return this.tree;
    }

    /** Return the SHA-1 value of this commit. */
    public String getID() {
        return this.id;
    }

    /** Return the message of this commit. */
    public String getMsg() {
        return this.message;
    }

    /** Return the SHA-1 value of the first parent. */
    public String getParent() {
        return this.parents[0];
    }

    /** Return the time stamp of this commit. */
    public Date getTimeStamp() {
        return this.timeStamp;
    }

    /** Return the SHA-1 value of the second parent. */
    public String getRelative() {
        return this.parents[1];
    }

    /** Compose a verbose version of log on this commit
     *
     * @return a string contains useful info about this commit
     */
    public String log() {
        // header of a commit object
        String log = "===\ncommit ";
        // add ID
        log += this.id + "\n";
        // add merge info
        if (parents[1] != null) {
            log += "Merge: " + this.parents[0].substring(0, 7)
                    + "\t" + this.parents[1].substring(0, 7) + "\n";
        }
        // add time stamp
        // format: "Date: \w\w\w \w\w\w \d+ \d\d:\d\d:\d\d \d\d\d\d [-+]\d\d\d\d"
        String pattern = "E MMM d HH:mm:ss yyyy Z";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        log += "Date: " + dateFormat.format(this.timeStamp) + "\n";
        // add commit message
        log += this.message + "\n\n";
        return log;
    }

    /** Compose a verbose version of log on this commit
     *
     * @return a string contains useful info about this commit
     */
    public String debugLog() {
        // header of a commit object
        String log = "===\ncommit ";
        // add ID
        log += this.id + "\n";
        log += "tree " + this.getTree() + "\n";
        // add merge info
        if (parents[1] != null) {
            log += "Merge: " + this.parents[0].substring(0, 7)
                    + "\t" + this.parents[1].substring(0, 7) + "\n";
        }
        // add time stamp
        // format: "Date: \w\w\w \w\w\w \d+ \d\d:\d\d:\d\d \d\d\d\d [-+]\d\d\d\d"
        String pattern = "E MMM d HH:mm:ss yyyy Z";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        log += "Date: " + dateFormat.format(this.timeStamp) + "\n";
        // add commit message
        log += this.message + "\n\n";
        return log;
    }

    /** Store this commit and generate its SHA-1 value. */
    public void store(File storePath) {
        // all dumpable objects must store in "$REPO_DIR/objects/../..."
        Utils.writeObject(Utils.join(storePath, this.id), this);
    }

    /** Load a Commit object by its ID and return it for assignment.
     *
     *  @param f a stored Commit object
     *  @return the stored Commit object
     */
    public Dumpable load(File f) {
        return Utils.readObject(f, Commit.class);
    }

    /** Print useful information about this object on System.out. */
    public void dump() {
        System.out.println(this.debugLog());
    }
}

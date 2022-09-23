package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/** Represent a gitlet blob object.
 *  BLOB records the history of ONE file in current gitlet
 *  working directory by the file name.
 */
public class Blob implements Serializable, Dumpable {
    /** Tracked file of this blob. */
    private final String file;
    /** SHA-1 value of this blob. */
    private final String Id;
    /** The file content of this blob. */
    private final byte[] content;
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    // commits that link to this blob
    // private Set<String> links;

    /** New a blob object
     *
     * @param filename a tracked file
     */
    public Blob(String filename) {
        this.file = filename;
        this.content = Utils.readContents(Utils.join(CWD, filename));
        this.Id = Utils.sha1(Utils.serialize(this));
    }

    /** Return the SHA-1 value of this blob. */
    public String getID() {
        return this.Id;
    }

    /** Return the name of related file. */
    public String getFile() {
        return this.file;
    }

    /** Return the content of this blob as string. */
    public String getContent() {
        return new String(this.content, StandardCharsets.UTF_8);
    }

    /** Compose a very verbose log of this blob
     *
     * @return a string contains useful info about this blob
     */
    public String log() {
        // header of a blob object
        String log = "blob ";
        // add ID
        log += this.Id + "\n";
        // add content
        log += "------\n" + this.getContent();
        return log;
    }

    /** Store this blob and generate its SHA-1 value. */
    public void store(File storePath) {
        Utils.writeObject(Utils.join(storePath, this.Id), this);
    }

    /** Load a blob object by its ID and return it for assignment.
     *
     *  @param f a stored Commit object
     *  @return the stored Commit object
     */
    public Dumpable load(File f) {
        return Utils.readObject(f, Blob.class);
    }

    /** Print useful information about this object on System.out. */
    public void dump() {
        System.out.println(this.log());
    }
}

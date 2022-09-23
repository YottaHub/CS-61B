package gitlet;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *
 *  @author Y. Y. Y
 */
public class Repository {
    /*  Visualization of a gitlet repository
        .gitlet (repository)
        |
        +- objects (directory)
        |  |
        |  + 8b0d5 (commit, message = "init commit", tree = "")
        |  |
        |  + bc04f (commit, message = "add v1.txt", tree = "5840f")
        |  |
        |  + 9ee02 (blob, content = "gitlet version 1")
        |  |
        |  + 5840f (BlobTree, set = {"v1.txt: 9ee02"})
        |
        +- HEAD (file, contents = "refs/master")
        |
        +- logs (directory)
        |  |
        |  + master (plain text, content = [8b0d5, bc04f])
        |
        +- refs (directory)
        |  |
        |  + master (CommitTree, set = {8b0d5: "init commit"; bc04f: "add v1.txt"})
        |
        +- index (Stage, added = {}, deleted = {})
     */

    /* DIRECTORIES */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The refs directory. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    /** The OBJECT directory where stores all objects' content */
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");

    /* FILES */

    /** The master branch. */
    public static File MASTER = join(REFS_DIR, "master");
    /** Collect global commits. */
    public static File GLOBAL = join(REFS_DIR, "global");
    /** The file stores the directory of current branch */
    public static File HEAD = join(GITLET_DIR, "HEAD");
    /** The staging area. */
    public static File STAGE = join(GITLET_DIR, "index");


    /* ***************************************************************
       ********************    Main Methods    ***********************
       *************************************************************** */


    /** The first step of creating a local gitlet working directory.
     *  Initialize a gitlet repository with fixed massage "init
     *  commit" and a default time stamp.
     */
    public static void init() {
        // Set up persistence
        buildRepo();

        // New the first commit
        Commit c = new Commit(new Date(0), "initial commit", "", "");
        // Store the commit
        c.store(OBJECT_DIR);
        // Headed to master branch by default
        Utils.writeContents(HEAD, "refs/master");
        // Branch is a CommitTree object that stores a commit history
        // Stores the latest commit in the master branch by default
        update(c);
        // Rich the global commit zone
        CommitTree global = Utils.readObject(GLOBAL, CommitTree.class);
        global.add(c);
        Utils.writeObject(GLOBAL, global);
    }

    /** Update the current branch after a commit. */
    private static void update(Commit c) {
        // combine .gitlet and the branch refs in HEAD
        File path = join(GITLET_DIR, readContentsAsString(HEAD));
        // load it as parent commit, using some tricky way
        CommitTree branch = readObject(path, CommitTree.class);
        branch.add(c);
        writeObject(path, branch);
    }

    /** Adds an input file to the staging area, Staging an already-staged
     *  file overwrites the previous entry in the staging area with the
     *  new contents.
     *
     *  @param filename the file adds to the staging area
     */
    public static void add(String filename){
        // Convert input file to blob object
        Blob b = new Blob(checkFile(filename, "File does not exist."));
        // Store the blob
        b.store(OBJECT_DIR);
        Stage stage = Utils.readObject(STAGE, Stage.class);
        // Adding a tracked and identical file has no effect
        // Put the blob into the stage
        stage.add(b);
        // Save the stage
        // Note: Differ from the @code{store}
        Utils.writeObject(STAGE, stage);
    }

    /** Saves a snapshot of tracked files in the current commit and staging area.
     *  By default, each commit’s snapshot of files will be exactly the same as
     *  its parent commit’s snapshot of files; it will keep versions of files
     *  exactly as they are, and not update them. A commit will only update the
     *  contents of files it is tracking that have been staged for addition at
     *  the time of commit, in which case the commit will now include the version
     *  of the file that was staged instead of the version it got from its parent.
     *
     *  @param msg commit message
     */
    public static void commit(String msg) {
        // Every commit must have a non-blank message
        if (msg.equals("")) {
            exitWithPrint("Please enter a commit message.");
        }
        // Fetch the parent commit
        Commit parent = (Commit) fetchHead();
        // Convert index to tree
        // If no files have been staged, abort
        Stage stage = Utils.readObject(STAGE, Stage.class);
        if (!stage.isChanged()) {
            exitWithPrint("No changes added to the commit.");
        }
        // Fetch the blob tree of the parent commit if not empty
        BlobTree tracked = new BlobTree();
        if (parent.getTree() != null && !parent.getTree().equals("")) {
            File trackedDir = join(OBJECT_DIR, parent.getTree());
            tracked.load(trackedDir);
        }
        // Union the staging area and tracked files from parent commit
        tracked.merge(stage);
        // Store the merged blob tree as a completed gitlet object with an ID
        tracked.store(OBJECT_DIR);
        // Make a commit
        Commit c = new Commit(new Date(), msg, parent.getID(), tracked.getID());
        // Store the commit
        c.store(OBJECT_DIR);
        // Update the current branch
        update(c);
        // The staging area is cleared after a commit
        stage.empty();
        writeObject(STAGE, stage);
        // Rich the global commit zone
        CommitTree global = readObject(GLOBAL, CommitTree.class);
        global.add(c);
        Utils.writeObject(GLOBAL, global);
    }

    /** Unstage the file if it is currently staged for addition. If the file
     *  is tracked in the current commit, stage it for removal and remove the
     *  file from the working directory if the user has not already done so
     *  (do *not* remove it unless it is tracked in the current commit).
     *
     * @param filename file to remove
     */
    public static void remove(String filename) {
        // Error message of this method
        String msg = "No reason to remove the file.";
        // Check if the file exist
        checkFile(filename, msg);
        // Check if the file is staged
        Stage stage = Utils.readObject(STAGE, Stage.class);
        if (stage.isContained(filename)) {
            // If this file is found in staging area, unstage it
            String id = stage.unstage(filename);
            // Save the adapted staging area
            writeObject(STAGE, stage);
            // Delete the unstaged file
            join(OBJECT_DIR, id).delete();
        } else {
            // Fetch the current tracked blob tree
            Commit current = (Commit) fetchHead();
            BlobTree tracked = (BlobTree) fetch(current.getTree());
            if (tracked != null && tracked.isContained(filename)) {
                // If the file is in the current commit, delete it from the working directory
                stage.addDeletion(filename, tracked.getBlobID(filename));
                restrictedDelete(join(CWD, filename));
            } else {
                // File not found
                exitWithPrint(msg);
            }
        }
        Utils.writeObject(STAGE, stage);
    }

    /** Print out logs from the Head Commit. */
    public static void log() {
         Commit head = (Commit) fetchHead();
         System.out.print(verboseLog(head));
    }

    /** Print out logs for all commits. */
    public static void globalLog() {
        CommitTree global = (CommitTree) new CommitTree().load(GLOBAL);
        HashMap<String, String> mapping = global.getMapping();
        StringBuilder ids = new StringBuilder();
        for (Map.Entry<String, String> p : mapping.entrySet()) {
            Commit c = (Commit) fetch(p.getKey());
            ids.append(c.log());
        }
        System.out.print(ids);
    }

    /** Takes the version of the file as it exists in the head commit
     *  and puts it in the working directory, overwriting the version
     *  of the file that’s already there if there is one. The new
     *  version of the file is not staged.
     *
     * @param filename the file to check out
     */
    public static void checkout(String filename) {
        Commit head = (Commit) fetchHead();
        BlobTree tree = (BlobTree) fetch(head.getTree());
        if (tree != null && tree.isContained(filename)) {
            Blob b = (Blob) fetch(tree.getBlobID(filename));
            assert b != null;
            writeContents(join(CWD, filename), b.getContent());
        } else {
            exitWithPrint("File does not exist in that commit.");
        }
    }

    /** Takes the version of the file as it exists in the commit with the
     *  given id, and puts it in the working directory, overwriting the
     *  version of the file that’s already there if there is one.
     *  The new version of the file is not staged.
     *
     *  @param commitID the commit to check
     *  @param filename the file to check out
     */
    public static void checkout(String commitID, String filename) {
        // for short-uid cases
        if (commitID.length() < 40) {
            List<String> fileList = Utils.plainFilenamesIn(OBJECT_DIR);
            int cnt = 0;
            for (String f : fileList) {
                if (f.startsWith(commitID)) {
                    commitID = f;
                    cnt += 1;
                }
            }
            if (cnt > 1) {
                // cannot narrow to one specific commit
                exitWithPrint("No commit with that id exists.");
            }
        }
        Commit checked = (Commit) fetch(commitID);
        if (checked == null) {
            exitWithPrint("No commit with that id exists.");
        }
        BlobTree tree = (BlobTree) fetch(checked.getTree());
        if (tree != null && tree.isContained(filename)) {
            Blob b = (Blob) fetch(tree.getBlobID(filename));
            assert b != null;
            writeContents(join(CWD, filename), b.getContent());
        } else {
            exitWithPrint("File does not exist in that commit.");
        }
    }

    /** Prints out the ids of all commits that have the given
     *  commit message, one per line.
     *
     *  @param msg commit message to find
     */
    public static void find(String msg) {
        CommitTree global = Utils.readObject(GLOBAL, CommitTree.class);
        String ids = global.findMsg(msg);
        if (ids.equals("")) {
            exitWithPrint("Found no commit with that message.");
        } else {
            System.out.print(ids);
        }
    }


    /* ***************************************************************
     *******************    Internal Methods    **********************
     *************************************************************** */


    /** Set up persistence for a gitlet repository. */
    private static void buildRepo() {
        // Initialize a new gitlet working directory
        if (!GITLET_DIR.mkdir()) {
            // Note: Never re-initialize an existing repository
            exitWithPrint("A Gitlet version-control system already " +
                    "exists in the current directory.");
        }
        // Create refs directory
        REFS_DIR.mkdir();
        // Create object directory
        OBJECT_DIR.mkdir();
        // Create HEAD
        writeObject(HEAD, "refs/master");
        // Create the master branch
        writeObject(MASTER, new CommitTree());
        // Create the staging area
        writeObject(STAGE, new Stage());
        // Create global commit collecting zone
        writeObject(GLOBAL, new CommitTree());
    }

    /** Check if the filename points at an existed file,
     *  return null if not existed
     *
     * @param filename the file to check
     * @param msg error message
     * @return the name of the found file or null
     */
    private static String checkFile(String filename, String msg) {
        File filePath = Utils.join(CWD, filename);
        // If the file does not exist, abort
        if (!filePath.exists()) {
            exitWithPrint(msg);
        }
        else {
            return filename;
        }
        return null;
    }

    /** Compose a very verbose log info of all commit info starting from
     *  the HEAD commit to init commit.
     *
     *  @return collected and ordered commit info
     */
    private static String verboseLog(Commit head) {
        StringBuilder log = new StringBuilder();
        while (head != null) {
            log.append(head.log());
            head = (Commit) fetch(head.getParent());
        }
        return log.toString();
    }

    /** Fetch the commit at HEAD. */
    private static Dumpable fetchHead() {
        File branch = join(GITLET_DIR, readContentsAsString(HEAD));
        File parentDir = join(OBJECT_DIR, readObject(branch, CommitTree.class).getLast());
        return new Commit().load(parentDir);
    }

    /** Fetch a dumpable object.
     *
     * @param filename sha-1 value of the object to fetch
     */
    private static Dumpable fetch(String filename) {
        if (filename.equals(""))  return null;
        File path = join(OBJECT_DIR, filename);
        if (!path.exists()) {
            return null;
        }
        return readObject(join(OBJECT_DIR, filename), Dumpable.class);
    }

    /** Check if .gitlet directory exist in current working directory */
    public static boolean checkEnv() {
        return GITLET_DIR.exists();
    }

    /**
     * Print MESSAGE and exits with error code 0
     *
     * @param msg message to print
     */
    public static void exitWithPrint(String msg) {
        if (msg != null && !msg.equals("")) {
            System.out.println(msg);
        }
        System.exit(0);
    }
}

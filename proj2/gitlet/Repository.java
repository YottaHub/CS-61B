package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *
 *  @author Y. Y. Y
 */
public class Repository implements Serializable {
    /*  Visualization of a gitlet repository
        .gitlet (top folder)
        |
        +- REPO (Repository)
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
        +- refs (directory)
        |  |
        |  + master (CommitTree, set = {8b0d5: "init commit"; bc04f: "add v1.txt"})
        |  |
        |  + remotes (folder)
        |       |
        |       + origin(file, contents = location of remote directory)
        |
        +- index (Stage)
        |
        +- global (CommitTree)
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

    /** The repository object. */
    private static final File REPO = join(GITLET_DIR, "REPO");
    /** The master branch. */
    private static final File MASTER = join(REFS_DIR, "master");
    /** Collect global commits. */
    private static final File GLOBAL = join(GITLET_DIR, "global");
    /** The file stores the directory of current branch */
    private static final File HEAD = join(GITLET_DIR, "HEAD");
    /** The staging area. */
    private static final File STAGE = join(GITLET_DIR, "index");

    /* REMOTES */
    /** The remote directory. */
    private static final File REMOTE_DIR = join(REFS_DIR, "remotes");
    /** The remote Repository. */
    private static Repository ORIGIN;



    /* ***************************************************************
       ********************    Main Methods    ***********************
       *************************************************************** */

    /* Basic Functions */

    /** The first step of creating a local gitlet working directory.
     *  Initialize a gitlet repository with fixed massage "init
     *  commit" and a default time stamp.
     */
    public static void init() {
        // Set up persistence for a repository
        Repository repository = new Repository();
        repository.buildRepo();
        // New the first commit
        Commit c = new Commit(new Date(0), "initial commit", "", "");
        // Headed to master branch by default
        Utils.writeContents(HEAD, "refs/master");
        // Branch is a CommitTree object that stores a commit history
        // Stores the latest commit in the master branch by default
        repository.update(c);
    }

    /** Update current branch and global branch after a commit.
     *
     * @param c the added commit
     */
    public void update(Commit c) {
        // Store the commit
        save(c);
        // add to current branch
        File path = join(GITLET_DIR, readContentsAsString(HEAD));
        CommitTree branch = readObject(path, CommitTree.class);
        branch.add(c);
        writeObject(path, branch);
        // Rich the global commit zone
        CommitTree global = readObject(GLOBAL, CommitTree.class);
        global.add(c);
        writeObject(GLOBAL, global);
    }

    public void save(Dumpable obj) {
        obj.store(OBJECT_DIR);
    }

    /** Adds an input file to the staging area, Staging an already-staged
     *  file overwrites the previous entry in the staging area with the
     *  new contents.
     *
     *  @param filename the file adds to the staging area
     */
    public void add(String filename) {
        // Convert input file to blob object
        Blob b = new Blob(checkFile(filename, "File does not exist."));
        // Store the blob
        save(b);
        Stage stage = readObject(STAGE, Stage.class);
        // Put the blob into the stage
        BlobTree workingTree = fetchTrackedTree(fetchHead());
        if (workingTree.isContained(filename)
                && workingTree.getBlobID(filename).equals(b.getID())
                && !stage.isDeleted(filename)) {
            // Adding a tracked and identical file has no effect
            return;
        } else {
            stage.add(b);
        }
        // Save staging area into index
        writeObject(STAGE, stage);
    }

    /** Saves a snapshot of tracked files in the current commit and staging area.
     *  By default, each commit’s snapshot of files will be exactly the same as
     *  its parent commit’s snapshot of files; it will keep versions of files
     *  exactly as they are, and not update them. A commit will only update the
     *  contents of files it is tracking that have been staged for addition at
     *  the time of commit, in which case the commit will now include the version
     *  of the file that was staged instead of the version it got from its parent.
     *
     * @param msg commit message
     * @param relative null or name of another branch
     */
    public Commit commit(String msg, String relative) {
        // Every commit must have a non-blank message
        if (msg.equals("")) {
            exitWithPrint("Please enter a commit message.");
        }
        // Fetch the parent commit
        Commit parent = fetchHead();
        // If no files have been staged, abort
        Stage stage = readObject(STAGE, Stage.class);
        if (relative == null && !stage.isChanged()) {
            exitWithPrint("No changes added to the commit.");
        }
        // Fetch the blob tree of the parent commit if not empty
        BlobTree tracked = new BlobTree();
        if (parent.getTree() != null && !parent.getTree().equals("")) {
            tracked = (BlobTree) fetchBlobTree(parent.getTree());
        }
        // Union the staging area and tracked files from parent commit
        tracked.merge(stage);
        // Union the tracking files of two commits
        if (relative != null) {
            Commit r = (Commit) fetch(relative);
            tracked.merge((BlobTree) fetch(r.getTree()));
        }
        // Store the merged blob tree as a completed gitlet object with an ID
        save(tracked);
        // Make a commit
        Commit c = new Commit(new Date(), msg, parent.getID(), relative, tracked.getID());
        // Update current branch and global branch and store c
        update(c);
        // The staging area is cleared after a commit
        stage.empty();
        writeObject(STAGE, stage);
        return c;
    }

    /** Unstage the file if it is currently staged for addition. If the file
     *  is tracked in the current commit, stage it for removal and remove the
     *  file from the working directory if the user has not already done so
     *  (do *not* remove it unless it is tracked in the current commit).
     *
     * @param filename file to remove
     */
    public void remove(String filename) {
        // Error message of this method
        String msg = "No reason to remove the file.";
        // Check if the file is staged
        Stage stage = readObject(STAGE, Stage.class);
        if (stage.isContained(filename)) {
            // If this file is found in staging area, unstage it
            String id = stage.unstage(filename);
            // Delete the unstaged file
            join(OBJECT_DIR, id).delete();
        } else {
            // Fetch the current tracked blob tree
            Commit current = fetchHead();
            BlobTree tracked = fetchBlobTree(current.getTree());
            if (tracked.isContained(filename)) {
                // If the file is in the current commit, delete it from the working directory
                stage.addDeletion(filename, tracked.getBlobID(filename));
                restrictedDelete(join(CWD, filename));
            } else {
                // remove an untracked file
                exitWithPrint(msg);
            }
        }
        // save the staging area
        writeObject(STAGE, stage);
    }

    /* Log & Status */

    /** Print out logs from current HEAD to the starting commit of current branch. */
    public void log() {
        Commit head = fetchHead();
        System.out.print(verboseLog(head));
    }

    /** Compose a very verbose log info of all commit info starting from
     *  the HEAD commit to init commit.
     *
     *  @param head the starting commit
     *  @return collected and ordered commit info
     */
    private String verboseLog(Commit head) {
        StringBuilder log = new StringBuilder();
        while (head != null) {
            log.append(head.log());
            head = (Commit) fetch(head.getParent());
        }
        return log.toString();
    }

    /** Print out logs for all commits. */
    public void globalLog() {
        // not in good style
        CommitTree global = (CommitTree) new CommitTree().load(GLOBAL);
        TreeMap<String, String> mapping = global.getMapping();
        StringBuilder ids = new StringBuilder();
        for (Map.Entry<String, String> p : mapping.entrySet()) {
            Commit c = fetchCommit(p.getKey());
            ids.append(c.log());
        }
        System.out.print(ids);
    }

    /** Prints out the ids of all commits that have the given
     *  commit message, one per line.
     *
     *  @param msg commit message to find
     */
    public void find(String msg) {
        CommitTree global = readObject(GLOBAL, CommitTree.class);
        String ids = global.findMsg(msg);
        if (ids.equals("")) {
            exitWithPrint("Found no commit with that message.");
        } else {
            System.out.print(ids);
        }
    }

    /** Displays what branches currently exist, and marks the current
     * branch with a `*`. Also displays what files have been staged for
     * addition or removal.
     */
    public void status() {
        String coverUp = "=== Branches ===\n";
        String branchPart = "";
        List<String> branchList = plainFilenamesIn(REFS_DIR);
        // Compose branch part by inspecting current branch
        for (String branch: branchList) {
            if (isHead(branch)) {
                // move current branch to the head and star
                branchPart = "*%s%n".formatted(branch) + branchPart;
            } else {
                branchPart += "%s%n".formatted(branch);
            }
        }
        // Compose staged part by inspecting the mapping in stage
        coverUp += branchPart + "\n=== Staged Files ===\n";
        Stage stage = readObject(STAGE, Stage.class);
        for (Map.Entry<String, String> p: stage.getMapping().entrySet()) {
            coverUp += "%s%n".formatted(p.getKey());
        }
        // Compose removed part by inspecting the deleted in stage
        coverUp += "\n=== Removed Files ===\n";
        for (Map.Entry<String, String> p: stage.getDeleted().entrySet()) {
            coverUp += "%s%n".formatted(p.getKey());
        }
        // Compose modified part by comparing the sha-1 values
        coverUp += "\n=== Modifications Not Staged For Commit ===\n";
        // All working files in current working directory
        List<String> workingFileList = Utils.plainFilenamesIn(CWD);
        BlobTree workingTree = fetchTrackedTree(fetchHead());
        // Iterating all files in the blob tree
        for (Map.Entry<String, String> p: workingTree.getMapping().entrySet()) {
            // If a file disappears and not staged
            if (!p.getValue().equals("deleted") && !workingFileList.contains(p.getKey())
                    && !stage.getDeleted().containsKey(p.getKey())) {
                coverUp += "%s (deleted)%n".formatted(p.getKey());
            }
        }
        for (String file: workingFileList) {
            Blob temp = new Blob(file);
            // A modified file must be modified and its former version should be committed or staged
            if (workingTree.isContained(file) && (workingTree.getBlobID(temp.getFile()) == null
                    || !workingTree.getBlobID(temp.getFile()).equals(temp.getID()))) {
                coverUp += "%s (modified)%n".formatted(file);
            }
        }
        // Compose untracked part
        coverUp += "\n=== Untracked Files ===\n";
        for (String file: workingFileList) {
            // If a working file is untracked in the current branch
            // and would be overwritten by the checkout
            if (!workingTree.isContained(file)) {
                coverUp += "%s%n".formatted(file);
            }
        }
        System.out.println(coverUp);
    }

    /* Checkout */

    /** Takes the version of the file as it exists in the head commit
     *  and puts it in the working directory, overwriting the version
     *  of the file that’s already there if there is one. The new
     *  version of the file is not staged.
     *
     *  @param filename name of target file
     */
    public void checkout(String filename) {
        Commit head = fetchHead();
        BlobTree tree = fetchBlobTree(head.getTree());
        if (tree.isContained(filename)) {
            Blob b = fetchBlob(tree.getBlobID(filename));
            // save this blob object
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
     *  @param commitID uid or short uid of a commit
     *  @param filename name of a file in this commit
     */
    public void checkout(String commitID, String filename) {
        Commit checked = fetchCommit(commitID);
        BlobTree tree = fetchBlobTree(checked.getTree());
        if (tree.isContained(filename)) {
            Blob b = (Blob) fetch(tree.getBlobID(filename));
            // write the fetched blob into CWD
            writeContents(join(CWD, filename), b.getContent());
        } else {
            exitWithPrint("File does not exist in that commit.");
        }
    }

    /** Takes all files in the commit at the head of the given branch,
     *  and puts them in the working directory, overwriting the versions
     *  of the files that are already there if they exist.
     *
     *  @param branchName the branch to be the head
     */
    public void checkoutBranch(String branchName) {
        // checkout current branch
        if (isHead(branchName)) {
            exitWithPrint("No need to checkout the current branch.");
        }
        File branchPath = join(REFS_DIR, branchName);
        if (!branchPath.exists()) {
            exitWithPrint("No such branch exists.");
        }
        // check out the branch head commit
        String head = readObject(branchPath, CommitTree.class).getLast();
        checkoutCommit(head, false);
        // Move HEAD to the new branch
        writeContents(HEAD, "refs/%s".formatted(branchName));
    }

    /** Check out a commit.
     *
     * @param commitId uid of a commit
     * @param changeHead need to reset or not
     */
    private void checkoutCommit(String commitId, boolean changeHead) {
        Commit c = fetchCommit(commitId);
        // check if any file in the working directory is untracked
        checkUntracked();
        // All working files in current working directory
        List<String> workingFileList = Utils.plainFilenamesIn(CWD);
        // Any files that are tracked in the current branch but are not
        // present in the checked-out branch are deleted.
        for (String file: workingFileList) {
            new File(file).delete();
        }
        // Write all files in the target commit into CWD
        BlobTree tree = fetchBlobTree(c.getTree());
        for (Map.Entry<String, String> p: tree.getMapping().entrySet()) {
            String filename = p.getKey();
            String address = p.getValue();
            // write if file is still tracked in this commit
            if (address != null && !address.equals("deleted")) {
                writeContents(join(CWD, filename), fetchBlob(address).getBytes());
            }

        }
        // Clear the staging area
        Stage stage = readObject(STAGE, Stage.class);
        stage.empty();
        writeObject(STAGE, stage);
        // Need to change the current branch head to the checked commit
        if (changeHead) {
            CommitTree branch = fetchCurrentBranch();
            branch.setLast(c);
            writeObject(join(GITLET_DIR, readContentsAsString(HEAD)), branch);
        }
    }

    /** Checks out all the files tracked by the given commit. Removes tracked
     *  files that are not present in that commit. Also moves the current
     *  branch’s head to that commit node.
     *
     * @param commitId the commit resetting to
     */
    public void reset(String commitId) {
        checkoutCommit(commitId, true);
    }

    /* Branch */

    /** Creates a new branch with the given name, and points it at the
     *  current head commit.
     *
     * @param branchName name of the new branch
     */
    public void branch(String branchName) {
        File branchPath = join(REFS_DIR, branchName);
        if (branchPath.exists()) {
            // If a branch with the given name already exists, exits
            exitWithPrint("A branch with that name already exists.");
        } else {
            // Create a new commit tree starts at the current commit
            Commit head = fetchHead();
            CommitTree branch = new CommitTree(head);
            // Save the new branch
            saveBranch(branch, branchName);
        }
    }

    /** Deletes the branch with the given name. */
    public void removeBranch(String branchName) {
        // Check if it is current working branch
        if (isHead(branchName)) {
            exitWithPrint("Cannot remove the current branch.");
        }
        // Check if a branch named branchName exists
        File branchPath = join(REFS_DIR, branchName);
        if (!branchPath.exists()) {
            exitWithPrint("A branch with that name does not exist.");
        }
        // Delete this branch only, don't do anything else
        branchPath.delete();
    }

    public void saveBranch(CommitTree branch, String name) {
        writeObject(join(REFS_DIR, name), branch);
    }

    /* Merge */

    /*  Main situations:
     *  Spilt Point | HEAD | Branch | Merged | case
     *  origin | origin | Modified | Stage | A
     *  origin | Modified | origin | Hold | B
     *  origin | Modified | same M | Hold | C
     *  None | New | None | Hold | D
     *  None | None | New | Checkout & Stage | E
     *  origin | origin | absent | remove | F
     *  origin | My M | Ur M | conflict | G
     */
    /** Merges files from the given branch into the current branch.
     *
     * @param branchName name of another branch
     */
    public void merge(String branchName, Repository other) {
        if (other == null) {
            other = this;
        }
        // Check if there are uncommitted changes
        Stage stage = readObject(STAGE, Stage.class);
        if (stage.isChanged()) {
            exitWithPrint("You have uncommitted changes.");
        }
        // Check if the given branch is the current branch
        if (isHead(branchName)) {
            exitWithPrint("Cannot merge a branch with itself.");
        }
        // Check if a branch with the given name does not exist
        File branchPath = join(REFS_DIR, branchName);
        if (!branchPath.exists()) {
            exitWithPrint("A branch with that name does not exist.");
        }
        checkUntracked();
        boolean isConflicted = mergeHelper(branchName, other);
        // Compose merge message
        String currentBranchName = readContentsAsString(HEAD).split("/")[1];
        String msg = "Merged %s into %s.".formatted(branchName, currentBranchName);
        // A special commit after merging
        Commit mHead = (Commit) fetch(readObject(join(REFS_DIR, branchName),
                CommitTree.class).getLast());
        commit(msg, mHead.getID());
        if (isConflicted) {
            exitWithPrint("Encountered a merge conflict.");
        }
    }

    private boolean mergeHelper(String branchName, Repository other) {
        // Fetch head commit of the given branch
        Commit mHead = (Commit) fetch(readObject(join(REFS_DIR, branchName),
                CommitTree.class).getLast());
        // Fetch current head commit
        Commit cHead = fetchHead();
        Commit ancestor = findLatestAncestor(cHead, mHead, this, other);
        // If the split point is the current branch
        if (ancestor.getID().equals(cHead.getID())) {
            // checkout the given branch
            checkoutCommit(mHead.getID(), true);
            exitWithPrint("Current branch fast-forwarded.");
        }
        // If the split point is the same commit as the given branch
        if (ancestor.getID().equals(mHead.getID())) {
            exitWithPrint("Given branch is an ancestor of the current branch.");
        }
        BlobTree divergedTree = other.fetchBlobTree(mHead.getTree());
        BlobTree currentTree = this.fetchBlobTree(cHead.getTree());
        BlobTree ancestorTree = this.fetchBlobTree(ancestor.getTree());
        boolean isConflicted = false;
        for (Map.Entry<String, String> p: divergedTree.getMapping().entrySet()) {
            String name = p.getKey();
            String mAddress = p.getValue();
            String cAddress = currentTree.getBlobID(name);
            String aAddress = ancestorTree.getBlobID(name);
            if (mAddress.equals("deleted") && cAddress != null && cAddress.equals(aAddress)) {
                // case F: origin | origin | absent |
                // file is same in the split point and current branch, but
                // deleted in the given branch
                remove(name);
            } else if (aAddress == null && cAddress != null && cAddress.equals("deleted")
                    && !mAddress.equals("deleted")) {
                // A very special case that current branch have added this file and deleted
                break;
            } else if (!mAddress.equals("deleted") && !mAddress.equals(cAddress)
                    && ((cAddress == null && aAddress == null)
                    || (cAddress != null  && cAddress.equals(aAddress)))) {
                // case A: origin | origin | Modified |
                // file is same in split point and current branch but
                // modified (not deleted) in the given branch
                // case E: None | None | New
                // file is added in the given branch
                checkout(mHead.getID(), name);
                add(name);
            } else if (!mAddress.equals(cAddress) && !mAddress.equals(aAddress)
                    && ((cAddress == null && cAddress != aAddress)
                    || cAddress != null && !cAddress.equals(aAddress))) {
                // case G: origin | My M | Ur M |
                // file differs in three commits
                conflict(currentTree.getBlobID(name), mAddress, name);
                isConflicted = true;
            }
        }
        return isConflicted;
    }

    /** Find the lowest common ancestor of two commits.
     *
     * @param cHead head commit of current working branch
     * @param mHead head commit of another working branch
     * @param cRepository repository of current working branch
     * @param mRepository repository of another working branch
     * @return their lowest common ancestor
     */
    private static Commit findLatestAncestor(Commit cHead, Commit mHead,
                                       Repository cRepository, Repository mRepository) {
        Commit mBack = mHead;
        Commit cBack = cHead;
        while (!cBack.getID().equals(mBack.getID())) {
            // Move the older commit to its parent commit
            if (cBack.getTimeStamp().compareTo(mBack.getTimeStamp()) > 0) {
                cBack = cRepository.fetchCommit(cBack.getParent());
            } else {
                mBack = mRepository.fetchCommit(mBack.getParent());
            }
        }
        return cBack;
    }

    /** Adapts the contents of conflicted files. */
    private void conflict(String a, String b, String name) {
        Blob current = (Blob) fetch(a);
        Blob given = (Blob) fetch(b);
        StringBuilder content = new StringBuilder("<<<<<<< HEAD\n");
        if (current != null) {
            // treat deleted file as empty
            content.append(current.getContent());
        }
        content.append("=======\n");
        if (given != null) {
            content.append(given.getContent());
        }
        content.append(">>>>>>>\n");
        writeContents(join(CWD, name), content.toString());
        add(name);
    }

    /* Remote */

    /** Saves the given login information under the given remote name.
     *
     * @param origin name of remote Repository
     * @param directory location of remote directory
     */
    public void addRemote(String origin, String directory) {
        // replace "/" with "\" on Windows system
        directory = directory.replaceAll("/", Matcher.quoteReplacement(File.separator));
        // store the name of remote directory in file named with given string
        File remote = join(REMOTE_DIR, origin);
        if (remote.exists()) {
            exitWithPrint("A remote with that name already exists.");
        }
        // write the location of remote directory in refs/remote/origin/origin
        writeObject(join(remote, origin), directory);
    }

    /** Remove information associated with the given remote name.
     *
     * @param target remote to delete
     */
    public void rmRemote(String target) {
        // disconnect with the target remote
        File remote = join(REMOTE_DIR, target);
        if (!remote.exists()) {
            exitWithPrint("A remote with that name does not exist.");
        }
        // remove the remote
        remote.delete();
    }

    /** Append the current branch’s commits to the end of the given branch at
     *  the given remote.
     *
     * @param origin target remote to push
     * @param main target branch in the remote
     */
    public void push(String origin, String main) {
        setUpRemoteEnv(origin);
        CommitTree remoteBranch = ORIGIN.fetchCommitTree(main);
        Commit remoteHead = ORIGIN.fetchCommit(remoteBranch.getLast());
        CommitTree currentBranch = this.fetchCurrentBranch();
        Commit currentHead = this.fetchCommit(currentBranch.getLast());
        Commit ancestor = findLatestAncestor(currentHead, remoteHead, this, ORIGIN);
        // head commit in remote must be a history commit in current working branch
        if (!ancestor.getID().equals(remoteHead.getID())) {
            exitWithPrint("Please pull down remote changes before pushing.");
        }
        Commit c = currentHead;
        // copy all objects from current head commit to remote head commit into $REMOTE$/objects
        while (!c.getID().equals(ancestor.getID())) {
            // store blob tree
            BlobTree tree = this.fetchBlobTree(c.getTree());
            ORIGIN.save(tree);
            // store blobs in tree
            for (Map.Entry<String, String> blob : tree.getMapping().entrySet()) {
                ORIGIN.save(fetchBlob(blob.getValue()));
            }
            ORIGIN.update(c);
            // fetch next commit
            c = fetchCommit(c.getParent());
        }
        remoteBranch.setLast(currentHead);
        // finally, update the branch
        ORIGIN.saveBranch(remoteBranch, main);
        // one more step, reset remote into same status as current head commit
        ORIGIN.reset(currentHead.getID());
    }

    private void setUpRemoteEnv(String origin) {
        File remoteDir = new File(readContentsAsString(join(REMOTE_DIR, origin)));
        if (!remoteDir.exists()) {
            exitWithPrint("Remote directory not found.");
        }
        ORIGIN = Repository.activate(remoteDir);
    }

    /** Brings down commits from the remote Gitlet repository into the local Gitlet repository.
     *
     * @param origin target remote to fetch
     * @param main target branch in the remote
     */
    public String fetchRemote(String origin, String main) {
        setUpRemoteEnv(origin);
        CommitTree remoteBranch = ORIGIN.fetchCommitTree(main);
        if (remoteBranch == null) {
            exitWithPrint("That remote does not have that branch.");
        }
        Commit remoteHead = ORIGIN.fetchCommit(remoteBranch.getLast());
        CommitTree currentBranch = this.fetchCurrentBranch();
        Commit currentHead = this.fetchCommit(currentBranch.getLast());
        Commit ancestor = findLatestAncestor(currentHead, remoteHead, this, ORIGIN);
        Commit c = remoteHead;
        // copy all new objects from remote into current branch
        while (!c.getID().equals(ancestor.getID())) {
            // store blob tree
            BlobTree tree = ORIGIN.fetchBlobTree(c.getTree());
            this.save(tree);
            // store blobs in tree
            for (Map.Entry<String, String> blob : tree.getMapping().entrySet()) {
                this.save(ORIGIN.fetchBlob(blob.getValue()));
            }
            this.update(c);
            // fetch next commit
            c = ORIGIN.fetchCommit(c.getParent());
        }
        saveBranch(remoteBranch, origin + main);
        return origin + main;
    }

    /** Fetches branch `[remote name]/[remote branch name]` as for the `fetch` command,
     *  and then merges that fetch into the current branch.
     *
     * @param origin target remote to pull
     * @param main target branch in the remote
     */
    public void pull(String origin, String main) {
        merge(fetchRemote(origin, main), this);
    }


    /* ***************************************************************
     *******************    Internal Methods    **********************
     *************************************************************** */


    /* Persistence */

    public static Repository activate(File repoDir) {
        return readObject(join(repoDir, "REPO"), Repository.class);
    }

    public Repository() { }

    /** Set up persistence for a gitlet repository. */
    private void buildRepo() {
        // Initialize a new gitlet working directory
        if (!GITLET_DIR.mkdir()) {
            // Note: Never re-initialize an existing repository
            exitWithPrint("A Gitlet version-control system already "
                    + "exists in the current directory.");
        }
        // Create refs directory
        REFS_DIR.mkdir();
        // Create remote folder
        REMOTE_DIR.mkdir();
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
        // Store this repository
        writeObject(REPO, this);
    }


    /** Check if the filename points at an existed file in CWD,
     *  return null if not existed.
     *  NOTES: Unnecessary
     *
     * @param filename the file to check
     * @param msg error message
     * @return the name of the found file or null
     */
    private String checkFile(String filename, String msg) {
        File filePath = join(CWD, filename);
        // If the file does not exist, abort
        if (!filePath.exists()) {
            exitWithPrint(msg);
        } else {
            return filename;
        }
        return null;
    }

    /** Check if this branch is identical to current branch.
     *
     * @return if the given branch is the current branch or not
     */
    private boolean isHead(String branchName) {
        String current = readContentsAsString(HEAD);
        return current.equals("refs/" + branchName);
    }

    /* Fetch */

    /** Complete a short-uid to a full-length one.
     *
     * @param shortId short-uid for an object
     * @return full-length uid or null
     */
    private String autoComplete(String shortId) {
        List<String> fileList = Utils.plainFilenamesIn(OBJECT_DIR);
        if (fileList == null) {
            return null;
        }
        int cnt = 0;
        if (fileList.contains(shortId)) {
            // if it is not short
            cnt = 1;
        } else {
            for (String full : fileList) {
                if (full.startsWith(shortId)) {
                    shortId = full;
                    cnt += 1;
                }
            }
        }
        if (cnt != 1) {
            // cannot narrow to one specific commit
            return null;
        }
            return shortId;
        }

    /** Fetch a dumpable object stored at OBJECT_DIR.
     *
     * @param filename sha-1 value of the object to fetch
     */
    private Dumpable fetch(String filename) {
        // illegal filename
        if (filename == null || filename.equals("") || filename.equals("deleted"))  {
            return null;
        }
        // filename not exist
        File path = join(OBJECT_DIR, filename);
        if (!path.exists()) {
            return null;
        }
        return readObject(join(OBJECT_DIR, filename), Dumpable.class);
    }

    /** Fetch the commit at HEAD.
     *
     * @return the latest commit in current branch
     */
    private Commit fetchHead() {
        File branch = join(GITLET_DIR, readContentsAsString(HEAD));
        File parentDir = join(OBJECT_DIR, readObject(branch, CommitTree.class).getLast());
        return (Commit) new Commit().load(parentDir);
    }

    /** Fetch the current branch.
     *
     * @return current branch
     */
    public CommitTree fetchCurrentBranch() {
        return (CommitTree) new CommitTree().load(join(GITLET_DIR, readContentsAsString(HEAD)));
    }

    /** Fetch the tracked tree of the target commit and merge into the staging area.
     *
     * @param head head commit of current branch
     * @return a blob tree contains all tracking files
     */
    private BlobTree fetchTrackedTree(Commit head) {
        // The blob tree of the target commit
        BlobTree workingTree = fetchBlobTree(head.getTree());
        // Merge the stage and the working tree of current commit
        workingTree.merge(readObject(STAGE, Stage.class));
        return workingTree;
    }

    /** Fetch a commit by its id, exits if no such commit.
     *
     * @param commitId uid of target commit
     */
    private Commit fetchCommit(String commitId) {
        // for short-uid cases
        Commit c = (Commit) fetch(autoComplete(commitId));
        if (c == null) {
            exitWithPrint("No commit with that id exists.");
        }
        return c;
    }

    private Blob fetchBlob(String id) {
        return (Blob) fetch(id);
    }

    private BlobTree fetchBlobTree(String id) {
        BlobTree tree = (BlobTree) fetch(id);
        if (tree == null) {
            tree = new BlobTree();
        }
        return tree;
    }

    /** Fetch a branch with given name.
     *
     * @param branch name of a branch
     * @return commit tree of this branch or null if no branch with this name
     */
    private CommitTree fetchCommitTree(String branch) {
        File branchDir = join(REFS_DIR, branch);
        if (!branchDir.exists()) {
            return null;
        } else {
            return readObject(branchDir, CommitTree.class);
        }
    }

    /* Others */

    /** Check if any working file is untracked. */
    private void checkUntracked() {
        List<String> workingFileList = Utils.plainFilenamesIn(CWD);
        BlobTree workingTree = fetchTrackedTree(fetchHead());
        for (String file : workingFileList) {
            if (!workingTree.isContained(file)) {
                exitWithPrint("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        }
    }

    /** Check if .gitlet directory exist in current working directory */
    public static boolean checkEnv() {
        return REPO.exists();
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

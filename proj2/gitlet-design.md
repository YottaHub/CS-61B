# Gitlet Design Document

## References

[Gitlet-Spec](https://sp21.datastructur.es/materials/proj/proj2/proj2)

[Git - Book (git-scm.com)](https://git-scm.com/book/en/v2)

[Version Control (Git) · the missing semester of your cs education (mit.edu)](https://missing.csail.mit.edu/2020/version-control/)

## Classes and Data Structures

```
.gitlet (top folder)
    |
    +- REPO (Repository)
    |
    +- objects (folder)
    |    |
    |    + 8b0d5 (commit, message = "init commit", tree = "")
    |    |
    |    + bc04f (commit, message = "add v1.txt", tree = "5840f")
    |    |
    |    + 9ee02 (blob, content = "gitlet version 1")
    |    |
    |    + 5840f (BlobTree, set = {"v1.txt: 9ee02"})
    |
    +- HEAD (file, contents = "refs/master")
    |
    +- refs (folder)
    |   |
    |   + master (CommitTree, map = {8b0d5: "init commit"; bc04f: "add v1.txt"})
    |   |
    |   + remotes (folder)
    |       |
    |       + origin(file, contents = location of remote directory)
    |
    +- index (Stage)
    |
    +- global (CommitTree, map = {8b0d5: "init commit"; bc04f: "add v1.txt"})
```

### Interface 1 Dumpable

#### Fields

1. `String getID()` Return the SHA-1 value of this dumpable
2. `String log()` Compose a log of this dumpable object
3. `void store(File storePath)` Store this dumpable object at given path
4. `Dumpable load(File f)` Load and assign a dumpable object
5. `void dump()` Print useful information about this object on System.out


### Interface 2 tree

#### Fields

1. `TreeMap<String, String> getMapping() ` Return the Mapping of this tree
2. `void empty()` Empty this tree
3. ` boolean isEmpty()` Check if there is any element in this tree

### Class Repository

```java
/* DIRECTORIES */

/** The current working directory. */
private final File CWD;
/** The .gitlet directory. */
private final File GITLET_DIR;
/** The refs directory. */
private final File REFS_DIR;
/** The OBJECT directory where stores all objects' content */
private final File OBJECT_DIR;

/* FILES */

/** The repository object. */
private final File REPO;
/** The master branch. */
private final File MASTER;
/** Collect global commits. */
private final File GLOBAL;
/** The file stores the directory of current branch */
private final File HEAD;
/** The staging area. */
private final File STAGE;

/* REMOTES */
/** The remote directory. */
private final File REMOTE_DIR;
/** The remote Repository. */
private Repository ORIGIN;
```

## Afterword

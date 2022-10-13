package gitlet;

import java.io.File;

import static gitlet.Repository.*;
import static gitlet.Utils.join;

/** Driver class for Gitlet - a subset of the Git version-control system.
 *
 *  @author Y. Y. Y
 */
public class Main {
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(System.getProperty("user.dir"), ".gitlet");

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        // If input args is empty, exit
        if (args.length == 0)
            exitWithPrint("Please enter a command.");

        String cmd = args[0];
        switch (cmd) {
            case "init" -> {
                // java gitlet.Main init
                validateNumArgs(args, 1);
                init();
            }
            case "add" -> {
                validateNumArgs(args, 2);
                activate().add(args[1]);
            }
            case "commit" -> {
                validateNumArgs(args, 2);
                activate().commit(args[1], null);
            }
            case "rm" -> {
                // java gitlet.Main rm [file name]
                validateNumArgs(args, 2);
                activate().remove(args[1]);
            }
            case "log" -> {
                // java gitlet.Main log
                validateNumArgs(args, 1);
                activate().log();
            }
            case "global-log" -> {
                // java gitlet.Main global-log
                validateNumArgs(args, 1);
                activate().globalLog();
            }
            case "checkout" -> {
                if (args.length == 3) {
                    // java gitlet.Main checkout -- [file name]
                    if (!args[1].equals("--"))
                        exitWithPrint("Incorrect operands.");
                    activate().checkout(args[2]);
                } else if (args.length == 4) {
                    // java gitlet.Main checkout [commit id] -- [file name]
                    if (!args[2].equals("--"))
                        exitWithPrint("Incorrect operands.");
                    activate().checkout(args[1], args[3]);
                } else if (args.length == 2) {
                    // java gitlet.Main checkout [branch name]
                    activate().checkoutBranch(args[1]);
                } else exitWithPrint("Incorrect operands.");
            }
            case "find" -> {
                // java gitlet.Main find [commit message]
                validateNumArgs(args, 2);
                activate().find(args[1]);
            }
            case "branch" -> {
                // java gitlet.Main branch [branch name]
                validateNumArgs(args, 2);
                activate().branch(args[1]);
            }
            case "rm-branch" -> {
                // java gitlet.Main rm-branch [branch name]
                validateNumArgs(args, 2);
                activate().removeBranch(args[1]);
            }
            case "status" -> {
                // java gitlet.Main status
                validateNumArgs(args, 1);
                activate().status();
            }
            case "reset" -> {
                // java gitlet.Main reset [commit id]
                validateNumArgs(args, 2);
                activate().reset(args[1]);
            }
            case "merge" -> {
                // java gitlet.Main merge [branch name]
                validateNumArgs(args, 2);
                activate().merge(args[1], null);
            }
            case "add-remote" -> {
                // java gitlet.Main add-remote [remote-name] [remote directory]/.gitlet
                validateNumArgs(args, 3);
                activate().addRemote(args[1], args[2]);
            }
            case "rm-remote" -> {
                // java gitlet.Main rm-remote [remote-name]
                validateNumArgs(args, 2);
                activate().rmRemote(args[1]);
            }
            case "push" -> {
                // java gitlet.Main push [remote name] [remote branch name]
                validateNumArgs(args, 3);
                activate().push(args[1], args[2]);
            }
            case "fetch" -> {
                // java gitlet.Main fetch [remote name] [remote branch name]
                validateNumArgs(args, 3);
                activate().fetchRemote(args[1], args[2]);
            }
            case "pull" -> {
                // java gitlet.Main pull [remote name] [remote branch name]
                validateNumArgs(args, 3);
                activate().pull(args[1], args[2]);
            }
            default -> exitWithPrint("No command with that name exists.");
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * exits the program with error message if they do not match.
     *
     * @param args Argument array from command line
     * @param n    Number of expected arguments
     */
    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n)
            exitWithPrint("Incorrect operands.");
    }

    /** Check the existence of an initialized Gitlet working
     *  directory before executing the command. Exit the
     *  program with error message if not.
     *
     * @return the working gitlet repository
     */
    public static Repository activate() {
        if (!Repository.checkEnv())
            exitWithPrint("Not in an initialized Gitlet directory.");
        return Repository.activate(GITLET_DIR);
    }
}

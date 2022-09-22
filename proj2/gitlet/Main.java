package gitlet;

import java.io.IOException;
import java.util.Objects;

import static gitlet.Repository.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *
 *  @author Y. Y. Y
 */
public class Main {
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
                validateNumArgs(args, 1);
                init();
            }
            case "add" -> {
                checkRequirement();
                validateNumArgs(args, 2);
                add(args[1]);
            }
            case "commit" -> {
                checkRequirement();
                validateNumArgs(args, 2);
                commit(args[1]);
            }
            case "rm" -> {
                checkRequirement();
                validateNumArgs(args, 2);
                remove(args[1]);
            }
            case "log" -> {
                checkRequirement();
                validateNumArgs(args, 1);
                log();
            }
            case "global-log" -> {
                checkRequirement();
                validateNumArgs(args, 1);
                globalLog();
            }
            case "checkout" -> {
                checkRequirement();
                if (args.length == 3) {
                    if (!args[1].equals("--"))
                        exitWithPrint("Incorrect operands.");
                    checkout(args[2]);
                } else if (args.length == 4) {
                    if (!args[2].equals("--"))
                        exitWithPrint("Incorrect operands.");
                    checkout(args[1], args[3]);
                } else exitWithPrint("Incorrect operands.");
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

    /**
     *  Check the existence of an initialized Gitlet working
     *  directory before executing the command. Exit the
     *  program with error message if not.
     */
    public static void checkRequirement() {
        if (!Repository.checkEnv())
            exitWithPrint("Not in an initialized Gitlet directory.");
    }
}

package ru.snek;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandList {
    private static String[] array = {"show", "clear", "save", "info", "help", "quit", "exit", "insert", "remove", "remove_greater_key", "test", "load", "import", "log"};
    private static ArrayList<String> commands = new ArrayList<>(Arrays.asList(array));

    public static boolean exists(String command) {
        return commands.contains(command);
    }

    public static boolean isOneWord(String command) {
        if(!exists(command)) return false;
        switch (command) {
            case "insert":
            case "remove":
            case "remove_greater_key":
            case "import":
                return false;
            default:
                return true;
        }
    }
}

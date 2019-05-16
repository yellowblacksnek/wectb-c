package ru.snek;

import static ru.snek.CommandList.exists;
import static ru.snek.CommandList.isOneWord;
import static ru.snek.Printer.println;

public class ConsoleInputHandler {
    private boolean multiline;
    private String bufferred;
    private String command;
    private boolean exit;

    public ConsoleInputHandler() {
        multiline = false;
        bufferred = "";
        command = null;
        exit = false;
    }

    public boolean process(String s) {
        String com = s;
        boolean wasMultilined = multiline;
        boolean correct = false;
        if (wasMultilined) {
            if (com.trim().equals("")) multiline = false;
            else multiline = isMultilined(bufferred + " " + com);
            correct = isCorrect(bufferred + " " + com);
        } else {
            if (com.trim().equals("")) return false;
            multiline = isMultilined(com);
            correct = isCorrect(com);
        }
        if (multiline) {
            if (bufferred.trim().equals("")) bufferred += com;
            else bufferred = bufferred + " " + com;
        }
        if (wasMultilined && !multiline) {
            com = bufferred + " " + com;
            bufferred = "";
        }
        if (!multiline) {
            if (!correct) println(wrongCommand(com));
            else {
                if (com.trim().equals("exit") || com.trim().equals("quit")) {
                    com = "quit";
                    exit = true;
                }
                command = com;
                return true;
            }
        }
        return false;
    }

    public String getCommand() {
        return command;
    }

    public boolean getExit() {
        return exit;
    }

    private boolean isMultilined(String s) {
        String temp = s.replaceAll("\\s+", " ");
        String[] splitted = temp.split(" ");
        switch (splitted[0]) {
            case "insert":
                if(splitted.length < 2) return false;
                if(!splitted[1].startsWith("\"")) return false;
                int x = temp.indexOf("\"",temp.indexOf("\"")+1);
                boolean keyDone = x > -1;
                if(keyDone) {
                    if(temp.length() > x + 1) {
                        if(temp.charAt(x+1)!=' ') return false;
                        if (temp.length() > x + 2) {
                            if (temp.charAt(x + 2) != '{') {
                                return false;
                            } else {
                                if (temp.indexOf("}", x + 3) > 0) {
                                    return false;
                                }
                            }
                        }
                    }
                }
                return true;
            case "remove":
            case "remove_greater_key":
                return (Utils.countInStr(s, '\"') % 2 != 0);
        }
        return false;
    }

    private boolean isCorrect(String s) {
        String[] splitted = s.split(" ");
        if(!exists(splitted[0])) return false;
        if(splitted.length == 1) {
            if(!isOneWord(s.trim())) return false;
        }
        else {
            switch (splitted[0]) {
                case "insert":
                    if(!splitted[1].startsWith("\"") ||
                            Utils.countInStr(s, '\"') % 2 != 0 ||
                            Utils.countInStr(s, '{') == 0 ||
                            Utils.countInStr(s, '}') == 0 ) return false;
                    String temp = s.replaceAll("\\s+", " ");
                    int x = temp.indexOf("\"",temp.indexOf("\"")+1);
                    boolean keyDone = x > -1;
                    if(keyDone) {
                        if(temp.length() > x+1) {
                            if(temp.charAt(x+1) != ' ') return false;
                            if (temp.length() > x + 2) {
                                if (temp.charAt(x + 2) != '{') return false;
                                if (temp.indexOf("}", temp.indexOf("{", x + 1) + 1) < 0) return false;
                            }
                        }
                    }
                    break;
                case "remove":
                case "remove_greater_key":
                    if(!splitted[1].startsWith("\"") ||
                            Utils.countInStr(s, '\"') % 2 != 0) return false;
                case "help":
                    if(!splitted[1].equals("insert")) return false;
                case "import":
                    break;
            }
        }
        return true;
    }

    private String wrongCommand(String s) {
        String[] splitted = s.split(" ");
        if(!exists(splitted[0])) return "Нет такой команды!";
        String message = "Неверный формат команды!\n";
        switch (splitted[0]) {
            case "insert" :
                message += "insert \"String key\" {element}";
                break;
            case "remove" :
                message += "remove \"String key\"";
                break;
            case "remove_greater_key" :
                message += "remove_greater_key \"String key\"";
                break;
            case "import":
                message += "import path";
                break;
            case "help":
                message += "help (insert)";
                break;
        }
        return message;
    }
}

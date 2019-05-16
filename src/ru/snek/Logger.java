package ru.snek;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Stack;
import static ru.snek.Printer.*;

public class Logger {
    public static final boolean showAll = false;
    public static final boolean showIOExceptions = false;
    public static final boolean showExceptions = true;

    private static class ExceptionInfo {
        private StackTraceElement[] stackTrace;
        private String message;

        public ExceptionInfo(StackTraceElement[] arr, String message) {
            stackTrace = arr;
            this.message = message;
        }

        public String toString() {
            String out = "";
            out += message + '\n';
            for(StackTraceElement el : stackTrace) out += el.toString() +'\n';
            out += "---------------";
            return out;
        }
    }

    private static Stack<ExceptionInfo> logs = new Stack<>();

    public static void addToLogs(StackTraceElement[] el, String message) {
        logs.push(new ExceptionInfo(el, message));
    }
    public static void printLogs() {
        if(logs.empty()) {
            errprintln("Пусто");
            return;
        }
        for(ExceptionInfo e : logs) errprintln(e);
        logs.clear();
    }

    public static void handleException(Exception exception) {
        try {
            addToLogs(exception.getStackTrace(), exception.getClass().getName());
            throw exception;
        } catch (SocketTimeoutException e) {
            errprintln("\r"+ "Время ожидания ответа истекло.");
        } catch (IOException e) {
            if(showAll || showIOExceptions) errprintln("\r"+ e.getMessage());
        } catch (Exception e) {
            if(showAll || showExceptions) errprintln("\r"+ e.getMessage());
        }
    }
}

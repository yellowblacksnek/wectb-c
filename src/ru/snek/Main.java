package ru.snek;

import java.io.File;
import java.io.IOException;

import static ru.snek.FileInteractor.*;
import static ru.snek.Printer.*;

public class Main {
    public static void main(String []args) throws IOException, ClassNotFoundException {
        if(args.length < 1) {
            errprintln("Необходимо указать адрес и порт. (Или только порт)");
            System.exit(1);
        }
        String addr = null;
        int port = -1;
        if(args.length == 1) {
            port = Integer.valueOf(args[0]);
        }
        if(args.length > 1) {
            addr = args[0];
            port = Integer.valueOf(args[1]);
        }
        if(port < 0 || port > 65535) {
            errprintln("Неправильный порт.");
            System.exit(1);
        }
        String config = null;
        try { config = getFileString(openFile("clientConfig")); }
        catch (Exception e) {
            errprintln(e.getMessage());
            System.exit(1);
        }
        config = config.replaceAll("\n", "").replaceAll("\\s+", " ");
        Connection.Type type = null;
        Connection.Realisation real = null;
        try {
            type = Connection.Type.valueOf(config.split(" ")[0]);
            real = Connection.Realisation.valueOf(config.split(" ")[1]);
        } catch (Exception e) {
            errprintln("Неверный формат конфиг-файла.\n" + e.getMessage());
            System.exit(1);
        }
        Client client = new Client(type, real);
        client.start(addr, port);
    }

    public static Connection.Type getType(File file) {
        return Connection.Type.valueOf(getFileString(file).split(" ")[0]);
    }

    public static Connection.Realisation getRealisation(File file) {
        return Connection.Realisation.valueOf(getFileString(file).split(" ")[1]);
    }
}
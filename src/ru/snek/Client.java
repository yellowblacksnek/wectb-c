package ru.snek;

import ru.snek.Collection.Malefactor;
import ru.snek.Collection.MapWrapperUtils;

import java.io.*;
import java.net.*;
import java.util.Map;

import static ru.snek.FileInteractor.*;
import static ru.snek.Logger.*;
import static ru.snek.Printer.*;
import static ru.snek.Utils.*;

public class Client {
    private Connection con;
    private Connection.Type type;
    private Connection.Realisation real;

    public Client(Connection.Type type, Connection.Realisation real)  {
        this.type = type;
        this.real = real;
    }

    public void start(String addr, int port) {
        InetAddress address = null;
        try {
            if(addr == null) address = InetAddress.getLocalHost();
            else address = InetAddress.getByName(addr);
        } catch (UnknownHostException e) {
            errprintln("Не удалось определить адрес.");
            System.exit(1);
        }
        loop(address, port);
    }

    private void loop(InetAddress address, int port) {
        boolean killClient = false;
        boolean[] welcomeShown = {false};
        while(!killClient) {
            connectLoop(address, port);
            showWelcome(welcomeShown);
            killClient = mainLoop();
            println("Соединение разорвано.");
        }
    }

    private void showWelcome(boolean[] shown) {
        if(shown[0]) return;
        println("Вэлкам. Можно ввести help, чтобы получить список команд.");
        shown[0] = true;
    }

    private void connectLoop(InetAddress address, int port){
        boolean connected = false;
        while(!connected) {
            try {
                print("Попытка установить соединение с сервером.\r");
                con = new Connection(type, real, address, port);
                connected = con.checkConnection();
            } catch (IOException e) {
                handleException(e);
            }
            if(!connected) {
                clearLine();
                println("Не удалось установить соединение с сервером.");
                println("Попробовать снова? Y/y - да.");
                String in = getConsoleInput();
                if (in.equals("log")) {
                    handleCommand("log");
                    in = getConsoleInput();
                }
                if(in.trim().equals("Y") || in.trim().equals("y")) continue;
                else System.exit(1);
            } else {
                clearLine();
                println("Соединение установлено.");
            }

        }
    }

    private boolean mainLoop() {
        try {
            boolean connected = true;
            ConsoleInputHandler handler = new ConsoleInputHandler();
            while (connected) {
                String input = getConsoleInput();
                if(handler.process(input)) {
                    String command = handler.getCommand();
                    if(command == null) continue;
                    connected = handleCommand(command);
                    if(handler.getExit()) return true;
                }
            }
            return  false;
        } finally {
            con.close();
        }
    }

    private boolean handleCommand(String c) {
        if (c.trim().equals("log")) {
            printLogs();
            return true;
        }
        String responseString;
        PleaseWait waiter = new PleaseWait();
        try {
            String[] splitted = c.split(" ");
            Message command;
            if (splitted.length == 1) command = new Message(splitted[0]);
            else command = new Message<>(splitted[0], c.substring(c.indexOf(' ') + 1));
            if (command.getCommand().equals("import")) command = handleImport((String) command.getData());
            if (command.getCommand().equals("insert")) command = handleInsert((String) command.getData());

            con.send(command, waiter);
            waiter.setStage(2);
            if(command.getCommand().equals("quit")) return false;
            Message response = con.receive(waiter);
            if (response == null) throw new Exception("В ответ пришло null.");

            responseString = processResponse(response);
        } catch(IOException e) {
            waiter.stop();
            handleException(e);
          return false;
        } catch (Exception e) {
            waiter.stop();
            handleException(e);
            return con.checkConnection();
        } finally {
            waiter.stop();
        }
        println(responseString);
        return true;
    }

    private Message handleInsert(String data) throws Exception {
        Malefactor mf;
        int sec = data.indexOf("\"", data.indexOf("\"")+1);
        String key = data.substring(1, sec);
        String json = data.substring(sec+1);
        String csv = MapWrapperUtils.parseJson(json);
        mf = MapWrapperUtils.elementFromString(csv);
        return new Message<>("insert", mf, key);
    }

    private Message handleImport(String filePath) throws Exception {
        File file = openFile(filePath);
        if (file.length() > (type == Connection.Type.UDP ? 65000 : 256 * 1024 * 1024))
            throw new Exception("Файл слишком большой. " + (type == Connection.Type.UDP ? ">64кБ" : ">256МБ"));
        String fileStr = getFileString(file);
        return new Message<>("import", fileStr);
    }

    private String processResponse(Message res) {
        if (res.getCommand().equals("show")) {
            Map<String, Malefactor> map = (Map) res.getData();
            StringBuilder message = new StringBuilder();
            if (map.size() == 0) return "Коллекция пуста.";
            else {
                map.entrySet().stream().map(i -> (i.getKey() + " : " + i.getValue()+ "\n")).forEach(message::append);
                if (!message.toString().equals("")) message.deleteCharAt(message.length() - 1);
                return message.toString();
            }
        } else return (String) res.getData();
    }
}

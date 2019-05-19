package ru.snek;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import static ru.snek.Utils.*;

public class Connection {
    enum Type {TCP,UDP}
    enum Realisation {STD,CHANNEL}
    private boolean tcp = false;
    private boolean std = false;

    private Socket socket;
    private SocketChannel channel;

    private DatagramSocket dSocket;
    private DatagramChannel dChannel;

    private int bufferSize = 2048;
    private final int maxBufferSize = 65507;
    private final int defaultTimeout = 20000;

    Connection(Type type, Realisation real, InetAddress addr, int port) throws IOException {
        if (type == Type.TCP) tcp = true;
        if (real == Realisation.STD) std = true;
        SocketAddress address = new InetSocketAddress(addr, port);
        if (tcp) {
            if (std) {
                socket = new Socket();
                socket.connect(address);
                socket.setSoTimeout(defaultTimeout);
            } else {
                channel = SocketChannel.open(address);
                channel.socket().setSoTimeout(defaultTimeout);
            }
        } else {
            if (std) {
                dSocket = new DatagramSocket();
                dSocket.connect(address);
            } else {
                dChannel = DatagramChannel.open();
                dChannel.connect(address);
            }
        }
    }

    public Message receive(PleaseWait waiter) throws IOException {
        boolean waiterOn = waiter != null;
        ByteBuffer buf = ByteBuffer.allocate(bufferSize);
        if (tcp) {
            int size = 0;
            int total = 0;
            do {
                int read = std ? socket.getInputStream()
                        .read(buf.array(), total, buf.array().length - total)
                        : channel.read(buf);
                if (read == -1) throw new EOFException();
                if (size == 0) {
                    if(std) socket.setSoTimeout(500);
                    else channel.socket().setSoTimeout(500);
                    if(waiterOn) waiter.setStage(3);
                    size = getSizeFromArr(buf.array());
                    if(size == 0) return errMsg("Пришли некорректные данные.");
                    byte[] cut = Arrays.copyOf(buf.array(), read);
                    byte[] extra = containsMore(cut);
                    buf = ByteBuffer.allocate(size);
                    if (extra != null) {
                        total += extra.length;
                        buf.put(extra);
                    }
                } else total += read;
                if(waiterOn) waiter.setPercentage(getPercentage(total, size));
            } while (total < size);
            if(std) socket.setSoTimeout(defaultTimeout);
            else channel.socket().setSoTimeout(defaultTimeout);
        } else {
            if (std) {
                dSocket.setSoTimeout(defaultTimeout);
                DatagramPacket i = new DatagramPacket(buf.array(), buf.array().length);
                dSocket.receive(i);
                dSocket.setSoTimeout(500);
            } else {
                dChannel.socket().setSoTimeout(defaultTimeout);
                dChannel.read(buf);
                dChannel.socket().setSoTimeout(500);

            }
            if (waiterOn) waiter.setStage(3);
            int size = getSizeFromArr(buf.array());
            if(size == 0) return errMsg("Пришли некорректные данные.");
            int amount = size <= maxBufferSize ? 1 : (size / maxBufferSize + 1);
            byte[] bigBuf = new byte[amount > 1 ? amount*maxBufferSize : size];
            buf = ByteBuffer.wrap(bigBuf);
            for (int j = 0; j < size;) {
                if (std) {
                    int len = size - j;
                    if(len > maxBufferSize) len = maxBufferSize;
                    DatagramPacket o = new DatagramPacket(bigBuf, j, len);
                    dSocket.receive(o);
                    j += o.getLength();
                } else {
                    j += dChannel.read(buf);
                }
                if (waiterOn) waiter.setPercentage(getPercentage(j, size));
            }
        }
        Message mes = (Message) objectFromByteArray(buf.array());
        return mes;
    }

    public void send(Message obj, PleaseWait waiter) throws Exception {
        boolean waiterOn = waiter != null;
        byte[] objAsArr;
        objAsArr = objectAsByteArray(obj);
        if (objAsArr == null) throw new Exception("Это странно");
        if (tcp) {
            if(waiterOn)waiter.setStage(1);
            byte[] sizeArr = getSizeArr(objAsArr);
            if (std) socket.getOutputStream().write(sizeArr);
            else channel.write(ByteBuffer.wrap(sizeArr));
            int portionSize = bufferSize;
            for (int i = 0; i < objAsArr.length; ) {
                int len = objAsArr.length - i < portionSize ? objAsArr.length - i : portionSize;
                if (std) socket.getOutputStream().write(objAsArr, i, len);
                else channel.write(ByteBuffer.wrap(objAsArr, i, len));
                i += portionSize;
                int percentage = getPercentage(i, objAsArr.length);
                if(waiterOn)waiter.setPercentage(percentage);
            }
        } else {
            if(waiterOn) waiter.setStage(4);
            ByteBuffer bb = ByteBuffer.wrap(objAsArr);
            if (std) {
                DatagramPacket o = new DatagramPacket(bb.array(), bb.array().length);
                dSocket.send(o);
            } else {
                dChannel.write(bb);
            }
        }
    }

    public boolean checkConnection() {
        try {
            if(tcp) {
                if (std) {
                    if (socket.isClosed()) return false;
                    socket.setSoTimeout(1000);
                } else {
                    if (!channel.isOpen()) return false;
                    channel.socket().setSoTimeout(1000);
                }
            } else {
                if (std) {
                    if (dSocket.isClosed()) return false;
                    dSocket.setSoTimeout(1000);
                } else {
                    if (!dChannel.isOpen()) return false;
                    dChannel.socket().setSoTimeout(1000);
                }
            }
            send(new Message("test"), null);
            Message response = receive(null);
            if(tcp) {
                if (std) socket.setSoTimeout(defaultTimeout);
                else channel.socket().setSoTimeout(defaultTimeout);
            } else {
                if (std) dSocket.setSoTimeout(defaultTimeout);
                else dChannel.socket().setSoTimeout(defaultTimeout);
            }
            if (response == null) return false;
            if (((String) response.getData()).equals("ok!")) return true;
            else return false;
        } catch (Exception e) {
            return false;
        }
    }

    private Message errMsg(String error) {
        return new Message<>("error", error);
    }

    public void close() {
        try {
            if(std) {
                if(tcp)socket.close();
                else dSocket.close();
            } else {
                if(tcp) channel.close();
                else dChannel.close();
            }
        } catch(IOException e) {  }
    }
}

package ru.liner.vr360client.tcp;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import ru.liner.vr360client.utils.Networks;
import ru.liner.vr360client.utils.Worker;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 01.05.2022, воскресенье
 **/
public abstract class HostRetriever extends Worker {
    private final InetAddress inetAddress;
    private final MulticastSocket multicastSocket;
    private final Thread multicastThread;
    private final String host;
    private int port;
    private final int bufferSize;


    public HostRetriever(String host, int port) throws IOException {
        this(host, port, 1024 * 8);
    }

    public HostRetriever(String host, int port, int bufferSize) throws IOException {
        this.host = host;
        this.port = port;
        this.bufferSize = bufferSize;
        this.multicastThread = new Thread(this);
        this.multicastSocket = new MulticastSocket(port);
        this.multicastSocket.setReuseAddress(true);
        this.inetAddress = InetAddress.getByName(host);
    }


    public boolean isRunning() {
        return multicastThread.isAlive() && isRunning;
    }

    public boolean isClosed() {
        return multicastSocket == null || multicastSocket.isClosed();
    }


    @Override
    public void execute() {
        try {
            port = multicastSocket.getLocalPort();
            multicastSocket.joinGroup(inetAddress);
            while (isRunning) {
                DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
                try {
                    multicastSocket.receive(packet);
                } catch (SocketException e) {
                    e.printStackTrace();
                    if (isClosed())
                        break;
                }
                byte[] data = new byte[packet.getLength() - packet.getOffset()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, data.length);
                String stringData = new String(data);
                if (Networks.isValidHost(stringData)) {
                    if (accept(stringData)) {
                        stop();
                        if (!isClosed()) {
                            multicastSocket.leaveGroup(InetAddress.getByName(host));
                        }
                    }
                }
            }
        } catch (Exception e) {
            stop();
        }
    }

    public abstract boolean accept(String host);

    @Override
    public long delay() {
        return 16;
    }
}

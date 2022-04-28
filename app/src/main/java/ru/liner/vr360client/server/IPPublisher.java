package ru.liner.vr360client.server;


import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import ru.liner.vr360client.tcp.UDPMulticast;
import ru.liner.vr360client.utils.Constant;
import ru.liner.vr360client.utils.Networks;
import ru.liner.vr360client.utils.Utils;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 26.04.2022, вторник
 **/
public class IPPublisher extends Thread {
    private final UDPMulticast udpMulticast;

    public IPPublisher() throws IOException {
        udpMulticast = new UDPMulticast(Constant.IP_REQUEST, Constant.MULTICAST_PORT);
    }

    @Override
    public synchronized void start() {
        super.start();
        udpMulticast.start();
    }

    @Override
    public void interrupt() {
        super.interrupt();
        udpMulticast.stop();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            udpMulticast.writeString(Objects.requireNonNull(Networks.getLocalIpAddress()));
            Utils.sleep(TimeUnit.SECONDS.toMillis(1));
        }
    }
}

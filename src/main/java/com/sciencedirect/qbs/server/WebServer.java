package com.sciencedirect.qbs.server;

import com.sun.net.httpserver.HttpServer;
import org.neo4j.driver.v1.Session;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer {

    public static final Integer port = 8080;


    public boolean startServer(final Session session) {

        final HttpServer server;

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException ioe) {
            return false;
        }

        System.out.println("server started at " + port);
        server.createContext("/", new RootHandler(session));
        server.setExecutor(null);
        server.start();

        return true;
    }
}

package mywebserver.server;

import mywebserver.manager.PluginServiceManager;
import mywebserver.sensor.TemperatureSensor;
import mywebserver.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;

public class WebServer {

    private static final Logger LOG = LogManager.getLogger();

    private ServerSocket serverSocket;

    public WebServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setReuseAddress(true);
    }

    public void start() {
        registerPlugins();
        handleSensors();
        handleClients();
    }

    public void stop() throws IOException {
        LOG.info("Server connection on port {} closed.", this.serverSocket.getLocalPort());
        this.serverSocket.close();
    }

    private void handleClients() {
        LOG.info("Waiting for client to connect ...");
        while (true) {
            try {
                ClientHandler clientHandler = new ClientHandler(this.serverSocket.accept());
                new Thread(clientHandler).start();
                LOG.info("A new client is connected: {}", this.serverSocket);
            } catch (IOException e) {
                LOG.error("Unable to process client request\n", e);
            }
        }
    }

    private void handleSensors() {
        TemperatureSensor temperatureSensor = new TemperatureSensor();
        new Thread(temperatureSensor).start();
    }

    private void registerPlugins() {
        try {
            PluginServiceManager.getInstance().loadServices(Constants.PLUGIN_SERVICE_PATH);
        } catch (IOException e) {
            LOG.error("Loading plugins failed, {}", e.getMessage());
        }
    }
}

package mywebserver.server;

import BIF.SWE1.interfaces.Plugin;
import BIF.SWE1.interfaces.PluginManager;
import BIF.SWE1.interfaces.Request;
import BIF.SWE1.interfaces.Response;
import mywebserver.manager.PluginServiceManager;
import mywebserver.request.WebRequest;
import mywebserver.response.WebResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private static final Logger LOG = LogManager.getLogger();
    private Socket clientSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private PluginManager pluginManager;

    ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.pluginManager = PluginServiceManager.getInstance().getPluginManager();
    }

    @Override
    public void run() {
        try {
            LOG.info("Client connected on port {}", this.clientSocket.getPort());
            this.inputStream = this.clientSocket.getInputStream();
            this.outputStream = this.clientSocket.getOutputStream();
            Response response = new WebResponse();
            Request request = new WebRequest(this.inputStream);

            float max = 0f;
            Plugin pluginToHandle = null;
            // check for every plugin probability if can handle
            for (Plugin plugin : pluginManager.getPlugins()) {
                float pluginProbability = plugin.canHandle(request);
                if (pluginProbability > max) {
                    max = pluginProbability;
                    pluginToHandle = plugin;
                }
            }
            if (pluginToHandle != null) {
                response = pluginToHandle.handle(request);
            }
            response.send(this.outputStream);
        } catch (Exception e) {
            LOG.error("Handling plugins failed, {}.", e.getMessage());
        } finally {
            closeStreams();
        }
    }

    private void closeStreams() {
        try {
            LOG.info("Client connection on {} port closed", this.clientSocket.getPort());
            this.outputStream.flush();
            this.outputStream.close();
            this.inputStream.close();
            this.clientSocket.close();
        } catch (IOException error) {
            LOG.error(error);
        }
    }
}

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class LoadBalancer {
    private static final int[] BACKEND_PORTS = {8081, 8082, 8083};
    private static int currentServerIndex = 0;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Load Balancer running on Port 8080");
        System.out.println("Target Backend: localhost:" + BACKEND_PORTS[currentServerIndex]);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Accepted connection from client.");
            
            // handle client in a new thread
            Thread t = new Thread(() -> handleClient(clientSocket));
            t.start();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            InputStream clientInput = clientSocket.getInputStream();
            byte[] requestBuffer = new byte[4096];
            
            int bytesFromBrowser = clientInput.read(requestBuffer);
            if (bytesFromBrowser > 0) {
                 System.out.println("Browser sent " + bytesFromBrowser + " bytes.");
            }


            //open connection to python
            Socket backendSocket = null;
            int attempts = 0;
            int maxRetries = 5;

            while (backendSocket == null && attempts < maxRetries) {
                int port = getNextBackendPort();
                attempts++;

                try {
                    backendSocket = new Socket("localhost", port);
                    System.out.println("Connected to backend port: " + port);
                } catch (IOException e) {
                    System.err.println("Attempt " + attempts + ": Backend port " + port + "is dead (retrying the next port)");
                }
            }

            if (backendSocket == null) {
                System.out.println("All the servers appear to be down. Sending 503");
                clientSocket.getOutputStream().write("HTTP/1.1 503 Service Unavailable\r\n\r\nNo Servers Available".getBytes());
                clientSocket.close();
                return;
            }
            // send a request
            PrintWriter toBackend = new PrintWriter(backendSocket.getOutputStream(), true);
            toBackend.println("GET / HTTP/1.1");
            toBackend.println("Host: localhost:8081");
            toBackend.println(""); 
            toBackend.flush();

            // read and write the backend response
            InputStream backendInput = backendSocket.getInputStream();
            OutputStream clientOutput = clientSocket.getOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;
            
            //keep reading from backend and writing to cleint
            while ((bytesRead = backendInput.read(buffer)) != -1) {
                clientOutput.write(buffer, 0, bytesRead);
            }
            
            // cleanup
            clientOutput.flush();
            backendSocket.close();
            clientSocket.close();
            System.out.println("Traffic bridged successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized int getNextBackendPort() {
        int port = BACKEND_PORTS[currentServerIndex];
        
        currentServerIndex++;
        
        // loop back to 0 after the end
        if (currentServerIndex >= BACKEND_PORTS.length) {
            currentServerIndex = 0;
        }
        
        System.out.println("Routing traffic to Backend Port: " + port);
        return port;
    }

}

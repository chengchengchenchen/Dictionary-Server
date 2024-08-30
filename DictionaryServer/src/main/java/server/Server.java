package server;

import client.ClientRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import enums.Request;
import enums.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Slf4j
public class Server {
    private static final int PORT = 8080;
    private static final String FILE_NAME = "dictionary.json";
    private Map<String, List<String>> dictionary;
    private final Gson gson = new Gson();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public Server() {
        dictionary = loadDictionary();
    }

    private Map<String, List<String>> loadDictionary() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, List<String>>>() {
                }.getType();
                return gson.fromJson(reader, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }

    private void saveDictionary() {
        try (Writer writer = new FileWriter(FILE_NAME)) {
            gson.toJson(dictionary, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log.info("Dictionary Server is running on port: {}", PORT);
            System.out.println("Dictionary Server is running on port " + PORT);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            saveDictionary();
            System.out.println("Dictionary saved and server stopped.");
        }
    }

    private void handleClient(Socket clientSocket) {
        System.out.println("1");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                try {
                    // Deserialize JSON request
                    ClientRequest request = gson.fromJson(inputLine, ClientRequest.class);
                    System.out.println(request.toString());
                    // Handle request and generate response
                    ServerResponse response = processRequest(request);
                    // Serialize response to JSON and send back to client
                    out.println(gson.toJson(response));

                } catch (JsonSyntaxException | IllegalArgumentException e) {
                    out.println(gson.toJson(new ServerResponse(Response.FORBIDDEN, "Invalid request format.")));
                }
            }
        } catch (IOException e) {
            System.out.println("客户端连接中断：" + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("客户端连接已关闭。");
            } catch (IOException e) {
                System.err.println("关闭客户端连接时出错：" + e.getMessage());
            }
        }
    }

    private ServerResponse processRequest(ClientRequest request) {
        return new ServerResponse(Response.SUCCESS, "a");
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
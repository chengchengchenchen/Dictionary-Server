import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static final int PORT = 8080;
    private static final String FILE_NAME = "dictionary.json";
    private Map<String, String> dictionary;
    private final Gson gson = new Gson();

    public Server() {
        dictionary = loadDictionary();
    }

    private Map<String, String> loadDictionary() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            System.out.println("enter");
            try (Reader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, String>>() {
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
            System.out.println("Dictionary Server is running on port " + PORT);

            /*while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    //handleClient(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
            dictionary.put("tes", "none sense");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            saveDictionary();
            System.out.println("Dictionary saved and server stopped.");
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
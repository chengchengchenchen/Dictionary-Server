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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Server {
    private static int PORT = 8080;
    private static String FILE_NAME = "dictionary.json";
    private final ConcurrentHashMap<String, List<String>> dictionary;
    private final Gson gson = new Gson();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public Server() {
        dictionary = loadDictionary();
    }

    private  ConcurrentHashMap<String, List<String>> loadDictionary() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            log.info("Loading dictionary from file: {}", FILE_NAME);
            try (Reader reader = new FileReader(file)) {
                Type type = new TypeToken<ConcurrentHashMap<String, List<String>>>() {}.getType();
                return gson.fromJson(reader, type);
            } catch (IOException e) {
                log.error("Failed to load dictionary from file: {}", FILE_NAME, e);
            }
        }
        log.warn("Dictionary file not found. Starting with an empty dictionary.");
        return new  ConcurrentHashMap<>();
    }

    private void saveDictionary() {
        try (Writer writer = new FileWriter(FILE_NAME)) {
            gson.toJson(dictionary, writer);
            log.info("Dictionary successfully saved to file: {}", FILE_NAME);
        } catch (IOException e) {
            log.error("Error saving dictionary to file: {}", FILE_NAME, e);
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log.info("Dictionary Server is running on port: {}", PORT);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    log.error("Error accepting client connection: {}", e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            log.error("Server socket error on port: {}: {}", PORT, e.getMessage(), e);
        }
    }

    private void handleClient(Socket clientSocket) {
        log.info("Client connected: {}", clientSocket.getRemoteSocketAddress());
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                try {
                    ClientRequest request = gson.fromJson(inputLine, ClientRequest.class);
                    log.info("Received request: {}", request);
                    ServerResponse response = processRequest(request);
                    out.println(gson.toJson(response));
                } catch (JsonSyntaxException | IllegalArgumentException e) {
                    log.warn("Invalid request format: {}", e.getMessage());
                    out.println(gson.toJson(new ServerResponse(Response.FORBIDDEN)));
                }
            }
        } catch (IOException e) {
            log.info("Client disconnected: {}", e.getMessage());
        } finally {
            try {
                clientSocket.close();
                log.info("Client socket closed: {}", clientSocket.getRemoteSocketAddress());
            } catch (IOException e) {
                log.error("Error closing client socket: {}", e.getMessage(), e);
            }
        }
    }

    private ServerResponse processRequest(ClientRequest clientRequest) {
        Request action = clientRequest.request;
        ServerResponse serverResponse;
        switch(action){
            case CREATE:
                serverResponse = CreateWord(clientRequest);
                break;

            case ADD:
                serverResponse = AddMeaning(clientRequest);
                break;

            case UPDATE:
                serverResponse = UpdateWord(clientRequest);
                break;

            case REMOVE:
                serverResponse = RemoveWord(clientRequest);
                break;

            case SEARCH:
                serverResponse = SearchWord(clientRequest);
                break;

            default:
                serverResponse = new ServerResponse(Response.NO_ACTION, "Unknown request type.");
                log.warn("Unknown request type received: {}", action);
                break;
        }
        return serverResponse;
    }

    private ServerResponse CreateWord(ClientRequest clientRequest){
        String word = clientRequest.word;
        List<String> meanings = clientRequest.meanings;
        ServerResponse serverResponse = new ServerResponse();

        // putIfAbsent
        if (dictionary.putIfAbsent(word, meanings) == null) {
            serverResponse.response = Response.CREATED;
            serverResponse.word = clientRequest.word;
            log.info("Added new word '{}' with meanings: {}", word, meanings);
        } else {
            serverResponse.response = Response.DUPLICATE;
            serverResponse.word = clientRequest.word;
            log.info("Word '{}' already exists in the dictionary.", word);
        }
        saveDictionary();  // save
        return serverResponse;
    }

    private ServerResponse AddMeaning(ClientRequest clientRequest){
        String word = clientRequest.word;
        String newMeaning = clientRequest.newMeaning;
        ServerResponse serverResponse = new ServerResponse();

        List<String> meanings = dictionary.get(word);
        if (meanings == null) {
            serverResponse.response = Response.NO_WORD;
            serverResponse.word = word;
            log.warn("Word '{}' not found in dictionary.", word);
            return serverResponse;
        }
        // synchronize list of meanings to ensure thread security
        synchronized (meanings) {
            if (meanings.contains(newMeaning)) {
                serverResponse.response = Response.DUPLICATE_MEANING;
                serverResponse.word = word;
                log.warn("New meaning '{}' for word '{}' duplicates.", newMeaning, word);
                return serverResponse;
            }

            // add
            meanings.add(newMeaning);
            dictionary.put(word, meanings);
            serverResponse.response = Response.ADDED;
            serverResponse.word = word;
            log.info("Added word '{}' meaning '{}'.", word, newMeaning);
        }

        saveDictionary();  // save
        return serverResponse;
    }

    private ServerResponse UpdateWord(ClientRequest clientRequest) {
        String word = clientRequest.word;
        String oldMeaning = clientRequest.oldMeaning;
        String newMeaning = clientRequest.newMeaning;
        ServerResponse serverResponse = new ServerResponse();

        List<String> meanings = dictionary.get(word);
        if (meanings == null) {
            serverResponse.response = Response.NO_WORD;
            serverResponse.word = word;
            log.warn("Word '{}' not found in dictionary.", word);
            return serverResponse;
        }

        // synchronize list of meanings to ensure thread security
        synchronized (meanings) {
            if (!meanings.contains(oldMeaning)) {
                serverResponse.response = Response.NO_MEANING;
                serverResponse.word = word;
                log.warn("Old meaning '{}' not found for word '{}'.", oldMeaning, word);
                return serverResponse;
            }

            if (meanings.contains(newMeaning)) {
                serverResponse.response = Response.DUPLICATE_MEANING;
                serverResponse.word = word;
                log.warn("New meaning '{}' for word '{}' duplicates.", newMeaning, word);
                return serverResponse;
            }

            // update
            meanings.remove(oldMeaning);
            meanings.add(newMeaning);
            dictionary.put(word, meanings);
            serverResponse.response = Response.UPDATED;
            serverResponse.word = word;
            log.info("Updated word '{}' from '{}' to '{}'.", word, oldMeaning, newMeaning);
        }

        saveDictionary();  // save
        return serverResponse;
    }

    private ServerResponse RemoveWord(ClientRequest clientRequest){
        String word = clientRequest.word;
        ServerResponse serverResponse = new ServerResponse();

        if (dictionary.remove(word) != null) {
            serverResponse.response = Response.REMOVED;
            serverResponse.word = clientRequest.word;
            log.info("Removed word '{}'", word);
        } else {
            serverResponse.response = Response.NO_WORD;
            serverResponse.word = clientRequest.word;
            log.warn("Word '{}' not found in the dictionary.", word);
        }

        saveDictionary();  // save
        return serverResponse;
    }

    private ServerResponse SearchWord(ClientRequest clientRequest){
        String word = clientRequest.word;
        ServerResponse serverResponse = new ServerResponse();

        List<String> meanings = dictionary.get(word);
        if (meanings != null) {
            serverResponse.response = Response.SUCCESS;
            serverResponse.word = word;
            serverResponse.meanings = meanings;
            log.info("Found word '{}': meanings: {}", word, meanings);
        } else {
            serverResponse.response = Response.NO_WORD;
            serverResponse.word = word;
            log.warn("Word '{}' not found in the dictionary.", word);
        }

        return serverResponse;
    }
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar DictionaryServer.jar <port> <dictionary-file>");
            return;
        }

        try {
            PORT = Integer.parseInt(args[0]);
            FILE_NAME = args[1];
            Server server = new Server();
            server.start();
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + args[0]);
        }
    }
}

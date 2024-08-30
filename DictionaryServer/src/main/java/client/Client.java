package client;

import client.ClientRequest;
import com.google.gson.Gson;
import enums.Request;
import server.ServerResponse;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private static final String SERVER_ADDRESS = "localhost"; // 服务器地址
    private static final int SERVER_PORT = 8080;              // 服务器端口
    private final Gson gson = new Gson();

    public void start() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to Dictionary Server");
            List<String> a = new ArrayList<>();
            a.add("a");
            a.add("b");
            ClientRequest request = new ClientRequest(Request.REMOVE, "test", a);

            // 将请求对象序列化为 JSON 并发送到服务器
            out.println(gson.toJson(request));

            // 从服务器接收响应
            String response = in.readLine();
            if (response != null) {
                ServerResponse serverResponse = gson.fromJson(response, ServerResponse.class);
                System.out.println("Server response: " + serverResponse);
            }
            while(true){}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}

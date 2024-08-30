import client.ClientRequest;
import enums.Request;
import enums.Response;
import server.ServerResponse;

import java.util.ArrayList;
import java.util.List;

public class test {
    public static void main(String[] args) {
        List<String> a = new ArrayList<>();
        a.add("a");
        a.add("b");
        ClientRequest clientRequest = new ClientRequest(Request.ADD, "apple", a);
        System.out.println(clientRequest.toString());

        ServerResponse serverResponse = new ServerResponse(Response.SUCCESS, "apple", a);
        System.out.println(serverResponse.toString());
    }
}

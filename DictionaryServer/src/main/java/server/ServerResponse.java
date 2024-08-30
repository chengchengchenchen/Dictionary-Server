package server;

import enums.Request;
import enums.Response;

import java.util.List;

public class ServerResponse {
    public Response response;
    public String word;
    public List<String> meanings;

    //SEARCH
    public ServerResponse(Response response, String word, List<String> meanings){
        this.response = response;
        this.word = word;
        this.meanings = meanings;
    }

    //ADD || UPDATE || REMOVE
    public ServerResponse(Response response, String word){
        this.response = response;
        this.word = word;
    }
    public ServerResponse(){}

    @Override
    public String toString() {
        return "server.ServerResponse{" +
                "reponse=" + response +
                ", word='" + word + '\'' +
                ", meanings='" + meanings + '\'' +
                '}';
    }
}

/*
Student Name: Jingcheng Qian
Student ID: 1640690
*/

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

    //CREATE || ADD || UPDATE || REMOVE
    public ServerResponse(Response response, String word){
        this.response = response;
        this.word = word;
    }

    public ServerResponse(Response response) {
        this.response = response;
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

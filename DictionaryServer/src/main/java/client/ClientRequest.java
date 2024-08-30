package client;

import enums.Request;

import java.util.List;

public class ClientRequest {
    public Request request;
    public String word;
    public List<String> meanings;
    public String oldMeaning;
    public String newMeaning;

    //UPDATE
    public ClientRequest(Request request, String word, String oldMeaning, String newMeaning){
        this.request = request;
        this.word = word;
        this.oldMeaning = oldMeaning;
        this.newMeaning = newMeaning;
    }

    //ADD
    public ClientRequest(Request request, String word, List<String> meanings){
        this.request = request;
        this.word = word;
        this.meanings = meanings;
    }

    //SEARCH || REMOVE
    public ClientRequest(Request request, String word){
        this.request = request;
        this.word = word;
    }

    public ClientRequest(){}

    @Override
    public String toString() {
        return "client.ClientRequest{" +
                "request=" + request +
                ", word='" + word + '\'' +
                ", meanings='" + meanings + '\'' +
                ", oldMeaning='" + oldMeaning + '\'' +
                ", newMeaning='" + newMeaning + '\'' +
                '}';
    }
}

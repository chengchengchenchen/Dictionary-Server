package client;

import com.google.gson.Gson;
import enums.Request;
import enums.Response;
import server.ServerResponse;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

public class ClientGUI {
    private JPanel rootPanel;
    private JFormattedTextField ipText;
    private JFormattedTextField portText;
    private JButton connectButton;
    private JLabel ipLable;
    private JLabel portLabel;
    private JPanel connectPanel;
    private JFormattedTextField wordText;
    private JFormattedTextField oldmeaningText;
    private JLabel wordLabel;
    private JFormattedTextField newmeaningText;
    private JButton disconnectButton;
    private JTextArea meaningsTextArea;
    private JList<String> meaningsList;
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JLabel oldmeaningLabel;
    private JLabel newmeaningLabel;
    private JLabel statusLabel;
    private JPanel inputPanel;
    private JPanel outputPanel;
    private JPanel actionPanel;
    private JButton searchButton;
    private JButton removeButton;
    private JButton addButton;
    private JButton updateButton;

    private Socket clientSocket; // Socket to monitor connection
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson();

    public ClientGUI() {
        connectButton.addActionListener(e -> connectToServer());
        disconnectButton.setEnabled(false);
        disconnectButton.addActionListener(e -> disconnectFromServer());

        searchButton.addActionListener(e -> searchWord());
        addButton.addActionListener(e -> addWord());
        updateButton.addActionListener(e -> updateWord());
        removeButton.addActionListener(e -> removeWord());
    }

    private void connectToServer() {
        String ip = ipText.getText();
        String portStr = portText.getText();

        try {
            int port = Integer.parseInt(portStr);
            clientSocket = new Socket(ip, port);
            clientSocket.setSoTimeout(5000);

            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            statusLabel.setText("Connected: " + ip + ":" + port);
            statusLabel.setForeground(Color.GREEN);

            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
        } catch (NumberFormatException e) {
            statusLabel.setText("Wrong port format, please input a number.");
            statusLabel.setForeground(Color.RED);
        } catch (IOException e) {
            statusLabel.setText("Connection failed: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void disconnectFromServer() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                statusLabel.setText("Disconnected");
                statusLabel.setForeground(Color.ORANGE);
            } else {
                statusLabel.setText("Unconnected to server");
                statusLabel.setForeground(Color.RED);
            }
        } catch (IOException e) {
            statusLabel.setText("Disconnection failed: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        } finally {
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
        }
    }

    private void searchWord() {
        // Clear old list
        listModel.clear();

        if (clientSocket == null || clientSocket.isClosed()) {
            statusLabel.setText("Not connected to server");
            statusLabel.setForeground(Color.RED);
            return;
        }

        String word = wordText.getText().trim();
        if (word.isEmpty()) {
            statusLabel.setText("Please enter a word to search.");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // Send search action to server
        ClientRequest request = new ClientRequest(Request.SEARCH, word);
        String jsonRequest = gson.toJson(request);
        out.println(jsonRequest);

        try {
            String responseJson = in.readLine();
            ServerResponse serverResponse = gson.fromJson(responseJson, ServerResponse.class);

            statusLabel.setText(serverResponse.response.getDescription());
            if (serverResponse.response == Response.SUCCESS) {

                statusLabel.setForeground(Color.GREEN);

                List<String> meanings = serverResponse.meanings;

                for (String meaning : meanings) {
                    listModel.addElement(meaning);
                }
                meaningsList.setModel(listModel);
            } else {
                statusLabel.setForeground(Color.RED);
            }
        } catch (SocketTimeoutException e) {
            statusLabel.setText("Timeout: No response from server.");
            statusLabel.setForeground(Color.RED);
        } catch (IOException e) {
            statusLabel.setText("Error during search: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void addWord() {
        if (clientSocket == null || clientSocket.isClosed()) {
            statusLabel.setText("Not connected to server");
            statusLabel.setForeground(Color.RED);
            return;
        }

        String word = wordText.getText().trim();
        String meanings = meaningsTextArea.getText().trim();

        if (word.isEmpty() || meanings.isEmpty()) {
            statusLabel.setText("Please enter both a word and at least a meaning.");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // Split meanings by newline
        List<String> meaningsList = Arrays.asList(meanings.split("\\n"));

        // Send add action to server
        ClientRequest request = new ClientRequest(Request.ADD, word, meaningsList);
        System.out.println(request);
        String jsonRequest = gson.toJson(request);
        out.println(jsonRequest);

        try {
            String responseJson = in.readLine();
            ServerResponse serverResponse = gson.fromJson(responseJson, ServerResponse.class);

            statusLabel.setText(serverResponse.response.getDescription());
            if (serverResponse.response == Response.ADDED) {
                statusLabel.setForeground(Color.GREEN);
            } else {
                statusLabel.setForeground(Color.RED);
            }
        } catch (SocketTimeoutException e) {
            statusLabel.setText("Timeout: No response from server.");
            statusLabel.setForeground(Color.RED);
        } catch (IOException e) {
            statusLabel.setText("Error during add: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void updateWord() {
        if (clientSocket == null || clientSocket.isClosed()) {
            statusLabel.setText("Not connected to server");
            statusLabel.setForeground(Color.RED);
            return;
        }

        String word = wordText.getText().trim();
        String oldMeaning = oldmeaningText.getText().trim();
        String newMeaning = newmeaningText.getText().trim();

        if (word.isEmpty() || oldMeaning.isEmpty() || newMeaning.isEmpty()) {
            statusLabel.setText("Please enter a word, old meaning, and new meaning.");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // Send update request to server
        ClientRequest request = new ClientRequest(Request.UPDATE, word, oldMeaning, newMeaning);
        String jsonRequest = gson.toJson(request);
        out.println(jsonRequest);

        try {
            String responseJson = in.readLine();
            ServerResponse serverResponse = gson.fromJson(responseJson, ServerResponse.class);

            statusLabel.setText(serverResponse.response.getDescription());
            if (serverResponse.response == Response.UPDATED) {
                statusLabel.setForeground(Color.GREEN);
            } else {
                statusLabel.setForeground(Color.RED);
            }
        } catch (SocketTimeoutException e) {
            statusLabel.setText("Timeout: No response from server.");
            statusLabel.setForeground(Color.RED);
        } catch (IOException e) {
            statusLabel.setText("Error during update: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void removeWord() {
        if (clientSocket == null || clientSocket.isClosed()) {
            statusLabel.setText("Not connected to server");
            statusLabel.setForeground(Color.RED);
            return;
        }

        String word = wordText.getText().trim();

        if (word.isEmpty()) {
            statusLabel.setText("Please enter a word to remove.");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // 发送删除请求到服务器
        ClientRequest request = new ClientRequest(Request.REMOVE, word);
        String jsonRequest = gson.toJson(request);
        out.println(jsonRequest);

        try {
            String responseJson = in.readLine();
            ServerResponse serverResponse = gson.fromJson(responseJson, ServerResponse.class);

            statusLabel.setText(serverResponse.response.getDescription());
            if (serverResponse.response == Response.REMOVED) {
                statusLabel.setForeground(Color.GREEN);
            } else {
                statusLabel.setForeground(Color.RED);
            }
        } catch (SocketTimeoutException e) {
            statusLabel.setText("Timeout: No response from server.");
            statusLabel.setForeground(Color.RED);
        } catch (IOException e) {
            statusLabel.setText("Error during remove: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ClientGUI");
        frame.setContentPane(new ClientGUI().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}

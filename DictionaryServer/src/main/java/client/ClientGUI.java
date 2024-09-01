package client;

import com.google.gson.Gson;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import enums.Request;
import enums.Response;
import server.ServerResponse;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

public class ClientGUI {
    private JPanel rootPanel;
    private JPanel connectPanel;
    private JPanel inputPanel;
    private JPanel outputPanel;
    private JPanel actionPanel;
    private JLabel ipLable;
    private JFormattedTextField ipText;
    private JLabel portLabel;
    private JFormattedTextField portText;
    private JButton connectButton;
    private JButton disconnectButton;
    private JLabel wordLabel;
    private JFormattedTextField wordText;
    private JLabel oldmeaningLabel;
    private JFormattedTextField oldmeaningText;
    private JLabel newmeaningLabel;
    private JFormattedTextField newmeaningText;
    private JTextArea meaningsTextArea;
    private JLabel statusLabel;
    private JList meaningsList;
    private JButton searchButton;
    private JButton removeButton;
    private JButton addButton;
    private JButton updateButton;
    private JButton createButton;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private Socket clientSocket; // Socket to monitor connection
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson();

    public ClientGUI() {
        // register every action to button
        connectButton.addActionListener(e -> connectToServer());
        disconnectButton.setEnabled(false);
        disconnectButton.addActionListener(e -> disconnectFromServer());

        searchButton.addActionListener(e -> searchWord());
        createButton.addActionListener(e -> createWord());
        addButton.addActionListener(e -> addMeaning());
        updateButton.addActionListener(e -> updateWord());
        removeButton.addActionListener(e -> removeWord());
    }

    private void connectToServer() {
        String ip = ipText.getText();
        String portStr = portText.getText();

        try {
            int port = Integer.parseInt(portStr);
            clientSocket = new Socket(ip, port);
            // avoid endless waiting
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

    private void createWord() {
        // Clear old list
        listModel.clear();

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
        ClientRequest request = new ClientRequest(Request.CREATE, word, meaningsList);
        String jsonRequest = gson.toJson(request);
        out.println(jsonRequest);

        try {
            String responseJson = in.readLine();
            ServerResponse serverResponse = gson.fromJson(responseJson, ServerResponse.class);

            statusLabel.setText(serverResponse.response.getDescription());
            if (serverResponse.response == Response.CREATED) {
                statusLabel.setForeground(Color.GREEN);
            } else {
                statusLabel.setForeground(Color.RED);
            }
        } catch (SocketTimeoutException e) {
            statusLabel.setText("Timeout: No response from server.");
            statusLabel.setForeground(Color.RED);
        } catch (IOException e) {
            statusLabel.setText("Error during create: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void addMeaning() {
        // Clear old list
        listModel.clear();

        if (clientSocket == null || clientSocket.isClosed()) {
            statusLabel.setText("Not connected to server");
            statusLabel.setForeground(Color.RED);
            return;
        }

        String word = wordText.getText().trim();
        String newMeaning = newmeaningText.getText().trim();

        if (word.isEmpty() || newMeaning.isEmpty()) {
            statusLabel.setText("Please enter both a word and a new meaning.");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // Send add action to server
        ClientRequest request = new ClientRequest(Request.ADD, word, newMeaning);
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
            statusLabel.setText("Error during create: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void updateWord() {
        // Clear old list
        listModel.clear();

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
        // Clear old list
        listModel.clear();

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

        // send delete to server
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.setEnabled(true);
        connectPanel = new JPanel();
        connectPanel.setLayout(new GridLayoutManager(1, 6, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(connectPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 1, false));
        ipLable = new JLabel();
        ipLable.setText("IP");
        connectPanel.add(ipLable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ipText = new JFormattedTextField();
        connectPanel.add(ipText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        portLabel = new JLabel();
        portLabel.setText("Port");
        connectPanel.add(portLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        portText = new JFormattedTextField();
        connectPanel.add(portText, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        connectButton = new JButton();
        connectButton.setText("Connect");
        connectPanel.add(connectButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        disconnectButton = new JButton();
        disconnectButton.setText("Disconnect");
        connectPanel.add(disconnectButton, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayoutManager(2, 6, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(inputPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        wordLabel = new JLabel();
        wordLabel.setText("word");
        inputPanel.add(wordLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        wordText = new JFormattedTextField();
        wordText.setText("");
        inputPanel.add(wordText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        oldmeaningLabel = new JLabel();
        oldmeaningLabel.setText("oldmeaning");
        inputPanel.add(oldmeaningLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        oldmeaningText = new JFormattedTextField();
        inputPanel.add(oldmeaningText, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        newmeaningLabel = new JLabel();
        newmeaningLabel.setText("newmeaning");
        inputPanel.add(newmeaningLabel, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        newmeaningText = new JFormattedTextField();
        inputPanel.add(newmeaningText, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        meaningsTextArea = new JTextArea();
        meaningsTextArea.setRows(10);
        meaningsTextArea.setText("");
        meaningsTextArea.setToolTipText("Add word meaning(s) here:");
        meaningsTextArea.setWrapStyleWord(false);
        inputPanel.add(meaningsTextArea, new GridConstraints(1, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 150), null, 0, false));
        outputPanel = new JPanel();
        outputPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(outputPanel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        meaningsList = new JList();
        outputPanel.add(meaningsList, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 150), null, 0, false));
        statusLabel = new JLabel();
        statusLabel.setText("Status:");
        outputPanel.add(statusLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        actionPanel = new JPanel();
        actionPanel.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(actionPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        searchButton = new JButton();
        searchButton.setText("Search");
        actionPanel.add(searchButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        removeButton = new JButton();
        removeButton.setText("Remove");
        actionPanel.add(removeButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        addButton = new JButton();
        addButton.setText("Add");
        actionPanel.add(addButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        updateButton = new JButton();
        updateButton.setText("Update");
        actionPanel.add(updateButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        createButton = new JButton();
        createButton.setText("Create");
        actionPanel.add(createButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}

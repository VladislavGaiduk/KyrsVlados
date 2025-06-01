package com.server.network;

import com.server.enums.Operation;
import com.server.exceptions.NoConnectionException;
import com.server.models.User;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ResourceBundle;

public class ServerClient {
    private static ServerClient instance;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    @Getter
    @Setter
    private static User currentUser;

    private ServerClient() throws NoConnectionException {
        connect(); // Если подключение провалится, выбросится исключение NoConnectionException
    }

    // Метод для функционирования паттерна синглтона
    public static synchronized ServerClient getInstance() throws NoConnectionException {
        if (instance == null) {
            try {
                instance = new ServerClient();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }


    private void connect() throws NoConnectionException {
        ResourceBundle bundle = ResourceBundle.getBundle("server");

        String serverAddress = bundle.getString("SERVER_IP");
        int serverPort = Integer.parseInt(bundle.getString("SERVER_PORT"));

        try {
            socket = new Socket(serverAddress, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to the server at " + serverAddress + ":" + serverPort);
        } catch (IOException e) {
            throw new NoConnectionException("Failed to connect to the server at " + serverAddress + ":" + serverPort);
        }
    }

    public void disconnect() {
        try {
            Request request = new Request(Operation.DISCONNECT, null);

            Response response = sendRequest(request);

            if(response.isSuccess()) {
                System.out.println("Disconnected successfully from the server.");
            }
        } catch (Exception e) {
            System.err.println("Failed to send disconnect request: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
                instance = null;
                System.out.println("Disconnected from the server.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Отправляет запрос и возвращает на него ответ
    public Response sendRequest(Request request) {
        try {
            System.out.println("Sending request: " + request.getOperation());
            out.writeObject(request);
            out.flush();

            return processResponse();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Response processResponse() {
        try {
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}

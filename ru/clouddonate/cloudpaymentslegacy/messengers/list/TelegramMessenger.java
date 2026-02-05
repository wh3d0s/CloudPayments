package ru.clouddonate.cloudpaymentslegacy.messengers.list;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import ru.clouddonate.cloudpaymentslegacy.messengers.api.ConnectException;
import ru.clouddonate.cloudpaymentslegacy.messengers.api.MessengerService;

public class TelegramMessenger implements MessengerService {
    private final String apiToken;
    private final List<String> ids;
    private final Gson gson;

    public TelegramMessenger(String apiToken, List<String> ids) {
        this.apiToken = apiToken;
        this.ids = new ArrayList<>(ids);
        this.gson = new Gson();
    }

    @Override
    public void connect() throws ConnectException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.telegram.org/bot" + this.apiToken + "/getMe").openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) {
                throw new ConnectException("Не удалось выполнить соединение. Code: " + connection.getResponseCode());
            }

            try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject response = gson.fromJson(reader, JsonObject.class);
                if (!response.has("ok") || !response.get("ok").getAsBoolean()) {
                    throw new ConnectException("Telegram API error: " + response);
                }
            }
        } catch (IOException e) {
            throw new ConnectException(e.getMessage());
        }
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void sendMessage(String message) {
        for (String id : this.ids) {
            try {
                JsonObject json = new JsonObject();
                json.addProperty("chat_id", id);
                json.addProperty("text", message);
                sendRequest(json.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendRequest(String jsonPayload) throws IOException {
        URL url = new URL("https://api.telegram.org/bot" + this.apiToken + "/sendMessage");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != 200) {
            System.err.println("Не удалось отправить сообщение.: " + connection.getResponseCode());
        }
        connection.disconnect();
    }

    public String getApiToken() {
        return this.apiToken;
    }

    public List<String> getIds() {
        return this.ids;
    }
}
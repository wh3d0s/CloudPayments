package ru.clouddonate.cloudpaymentslegacy.messengers.list;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import ru.clouddonate.cloudpaymentslegacy.messengers.api.ConnectException;
import ru.clouddonate.cloudpaymentslegacy.messengers.api.MessengerService;

public class TelegramMessenger implements MessengerService {
   private final String apiToken;
   private final List<String> ids = new ArrayList();

   public TelegramMessenger(String apiToken, List<String> ids) {
      this.apiToken = apiToken;
      this.ids.addAll(ids);
   }

   public void connect() throws ConnectException {
      try {
         URL url = new URL("https://api.telegram.org/bot" + this.apiToken + "/getMe");
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("GET");
         connection.setConnectTimeout(5000);
         connection.setReadTimeout(5000);
         int responseCode = connection.getResponseCode();
         if (responseCode != 200) {
            throw new ConnectException("Invalid bot token or unable to connect to Telegram API. Response code: " + responseCode);
         } else {
            StringBuilder response = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            Throwable var6 = null;

            try {
               String line;
               try {
                  while((line = br.readLine()) != null) {
                     response.append(line);
                  }
               } catch (Throwable var16) {
                  var6 = var16;
                  throw var16;
               }
            } finally {
               if (br != null) {
                  if (var6 != null) {
                     try {
                        br.close();
                     } catch (Throwable var15) {
                        var6.addSuppressed(var15);
                     }
                  } else {
                     br.close();
                  }
               }

            }

            if (!response.toString().contains("\"ok\":true")) {
               throw new ConnectException("Telegram API response indicates failure: " + response);
            } else {
               connection.disconnect();
            }
         }
      } catch (IOException var18) {
         throw new ConnectException("Failed to connect to Telegram API: " + var18.getMessage());
      }
   }

   public void disconnect() {
   }

   public void sendMessage(String message) {
      this.ids.forEach((id) -> {
         try {
            URL url = new URL("https://api.telegram.org/bot" + this.getApiToken() + "/sendMessage");
            HttpURLConnection connection = getHttpURLConnection(message, id, url);
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
               System.out.println("Failed to send message to Telegram. Response Code: " + responseCode);
            }

            connection.disconnect();
         } catch (Exception var6) {
            var6.printStackTrace();
         }

      });
   }

   private static HttpURLConnection getHttpURLConnection(String message, String id, URL url) throws IOException {
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);
      String payload = "{\"chat_id\":\"" + id + "\",\"text\":\"" + message + "\"}";
      OutputStream os = connection.getOutputStream();
      Throwable var6 = null;

      try {
         byte[] input = payload.getBytes(StandardCharsets.UTF_8);
         os.write(input, 0, input.length);
      } catch (Throwable var15) {
         var6 = var15;
         throw var15;
      } finally {
         if (os != null) {
            if (var6 != null) {
               try {
                  os.close();
               } catch (Throwable var14) {
                  var6.addSuppressed(var14);
               }
            } else {
               os.close();
            }
         }

      }

      return connection;
   }

   @Generated
   public String getApiToken() {
      return this.apiToken;
   }

   @Generated
   public List<String> getIds() {
      return this.ids;
   }
}

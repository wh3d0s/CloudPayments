package ru.clouddonate.cloudpaymentslegacy.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

public final class HttpReader {
   public static List<String> getLinesFromUrl(String stringUrl) {
      ArrayList lines = new ArrayList();

      try {
         URL url = new URL(stringUrl);
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setRequestMethod("GET");
         BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

         String line;
         while((line = reader.readLine()) != null) {
            lines.add(line);
         }

         reader.close();
         conn.disconnect();
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return lines;
   }

   @Generated
   private HttpReader() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}

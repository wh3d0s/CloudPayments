package ru.clouddonate.cloudpaymentslegacy.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileUtil {
   public static String readInputStream(InputStream inputStream) {
      StringBuilder stringBuilder = new StringBuilder();

      try {
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

         String line;
         while((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append('\n');
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return stringBuilder.toString();
   }

   public static List<String> readFile(String filePath) {
      ArrayList lines = new ArrayList();

      try {
         BufferedReader reader = new BufferedReader(new FileReader(filePath));
         Throwable var3 = null;

         try {
            String line;
            try {
               while((line = reader.readLine()) != null) {
                  lines.add(line);
               }
            } catch (Throwable var13) {
               var3 = var13;
               throw var13;
            }
         } finally {
            if (reader != null) {
               if (var3 != null) {
                  try {
                     reader.close();
                  } catch (Throwable var12) {
                     var3.addSuppressed(var12);
                  }
               } else {
                  reader.close();
               }
            }

         }
      } catch (IOException var15) {
         System.err.println("Ошибка при чтении файла: " + var15.getMessage());
      }

      return lines;
   }

   public static void writeFile(String filePath, List<String> lines) {
      try {
         BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
         Throwable var3 = null;

         try {
            Iterator var4 = lines.iterator();

            while(var4.hasNext()) {
               String line = (String)var4.next();
               writer.write(line);
               writer.newLine();
            }
         } catch (Throwable var14) {
            var3 = var14;
            throw var14;
         } finally {
            if (writer != null) {
               if (var3 != null) {
                  try {
                     writer.close();
                  } catch (Throwable var13) {
                     var3.addSuppressed(var13);
                  }
               } else {
                  writer.close();
               }
            }

         }
      } catch (IOException var16) {
         System.err.println("Ошибка при записи файла: " + var16.getMessage());
      }

   }

   public static void appendToFile(String filePath, String text) {
      try {
         BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
         Throwable var3 = null;

         try {
            writer.write(text);
            writer.newLine();
         } catch (Throwable var13) {
            var3 = var13;
            throw var13;
         } finally {
            if (writer != null) {
               if (var3 != null) {
                  try {
                     writer.close();
                  } catch (Throwable var12) {
                     var3.addSuppressed(var12);
                  }
               } else {
                  writer.close();
               }
            }

         }
      } catch (IOException var15) {
         System.err.println("Ошибка при добавлении текста в файл: " + var15.getMessage());
      }

   }

   public static List<String> searchInFile(String filePath, String searchText) {
      ArrayList<String> result = new ArrayList();
      List<String> lines = readFile(filePath);
      Iterator var4 = lines.iterator();

      while(var4.hasNext()) {
         String line = (String)var4.next();
         if (line.contains(searchText)) {
            result.add(line);
         }
      }

      return result;
   }

   public static void removeLines(String filePath, String textToRemove) {
      List<String> lines = readFile(filePath);
      lines.removeIf((line) -> {
         return line.contains(textToRemove);
      });
      writeFile(filePath, lines);
   }
}

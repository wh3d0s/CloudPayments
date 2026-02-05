package ru.clouddonate.cloudpaymentslegacy.json;

import com.google.gson.Gson;
import java.io.Reader;

public final class JSONConverterService implements ConverterService {
   public final Gson gson = new Gson();

   public String serialize(Object object) {
      return this.gson.toJson(object);
   }

   public <T> T deserialize(Reader reader, Class<T> clazz) {
      return this.gson.fromJson(reader, clazz);
   }
}

package ru.clouddonate.cloudpaymentslegacy.json;

import java.io.Reader;

public interface ConverterService {
   String serialize(Object var1);

   <T> T deserialize(Reader var1, Class<T> var2);
}

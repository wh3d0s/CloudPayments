package ru.clouddonate.cloudpaymentslegacy.messengers.api;

public interface MessengerService {
   void connect() throws ConnectException;

   void disconnect();

   void sendMessage(String var1);
}

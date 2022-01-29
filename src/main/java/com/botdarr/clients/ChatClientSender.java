package com.botdarr.clients;

public interface ChatClientSender<T extends ChatClientData, Z extends ChatClientResponse> {
    public void send(Z response, T data);
}

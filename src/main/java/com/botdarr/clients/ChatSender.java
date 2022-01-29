package com.botdarr.clients;

public interface ChatSender<T extends ChatClientResponse> {
    void send(T chatClientResponse);
}
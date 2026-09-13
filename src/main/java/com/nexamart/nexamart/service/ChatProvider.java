package com.nexamart.nexamart.service;

public interface ChatProvider {
    String getReply(String userMessage, String context);
}
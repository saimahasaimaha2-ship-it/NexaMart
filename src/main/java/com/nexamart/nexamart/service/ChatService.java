package com.nexamart.nexamart.service;

import com.nexamart.nexamart.exception.ServiceException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatService {
    private static final int MAX_MESSAGE_LENGTH = 500;
    private static final int RATE_LIMIT_PER_MINUTE = 10;

    private final ChatProvider provider;
    private final Map<String, List<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private final Map<String, String> responseCache = new ConcurrentHashMap<>();

    public ChatService() {
        this.provider = new MockChatProvider();
    }

    public ChatService(ChatProvider provider) {
        this.provider = provider;
    }

    public String chat(String sessionId, String userMessage) throws ServiceException {
        if (userMessage == null || userMessage.isBlank()) {
            throw new ServiceException("VALIDATION_ERROR", "Message cannot be empty");
        }
        if (userMessage.length() > MAX_MESSAGE_LENGTH) {
            throw new ServiceException("VALIDATION_ERROR", "Message too long (max " + MAX_MESSAGE_LENGTH + " characters)");
        }

        enforceRateLimit(sessionId);

        String cacheKey = sessionId + ":" + userMessage.trim().toLowerCase();
        if (responseCache.containsKey(cacheKey)) {
            return responseCache.get(cacheKey);
        }

        try {
            String reply = provider.getReply(userMessage, "NexaMart e-commerce marketplace");
            responseCache.put(cacheKey, reply);
            return reply;
        } catch (Exception e) {
            return "Sorry, I'm having trouble answering right now. Please try again later.";
        }
    }

    private void enforceRateLimit(String sessionId) throws ServiceException {
        long now = System.currentTimeMillis();
        long oneMinuteAgo = now - 60_000;

        List<Long> timestamps = requestTimestamps.computeIfAbsent(sessionId, k -> new java.util.ArrayList<>());
        synchronized (timestamps) {
            timestamps.removeIf(t -> t < oneMinuteAgo);
            if (timestamps.size() >= RATE_LIMIT_PER_MINUTE) {
                throw new ServiceException("RATE_LIMITED", "Too many messages. Please wait a moment before trying again.");
            }
            timestamps.add(now);
        }
    }
}
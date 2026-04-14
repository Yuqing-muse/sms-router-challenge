package com.sinch.smsrouter.repository;

import com.sinch.smsrouter.model.Message;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MessageRepository {

    private final ConcurrentHashMap<UUID, Message> store = new ConcurrentHashMap<>();

    public void save(Message message) {
        store.put(message.getId(), message);
    }

    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public void clear() {
        store.clear();
    }
}

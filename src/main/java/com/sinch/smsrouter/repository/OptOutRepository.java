package com.sinch.smsrouter.repository;

import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OptOutRepository {

    private final Set<String> optedOutNumbers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void add(String phoneNumber) {
        optedOutNumbers.add(phoneNumber);
    }

    public boolean contains(String phoneNumber) {
        return optedOutNumbers.contains(phoneNumber);
    }

    public void clear() {
        optedOutNumbers.clear();
    }
}

package com.s18947440.CPEN431.A2.rr;

import java.util.HashMap;
import java.util.Map;

public class RequestCache {

        private Map<String, byte[]> cache;

        public RequestCache() {
            cache = new HashMap<>();
        }

        public byte[] get(String MessageId) {
            return cache.get(MessageId);
        }

        public void put(String requestID, byte[] theReqFromClient) {
            cache.put(requestID, theReqFromClient);
        }

    }

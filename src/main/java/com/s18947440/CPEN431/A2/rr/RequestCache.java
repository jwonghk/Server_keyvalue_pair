package com.s18947440.CPEN431.A2.rr;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RequestCache {
        private static final int MAX_ENTRIES = 5000;
        private Map<String, byte[]> cache;

        /*
        public RequestCache() {
            cache = new HashMap<>();
        }*/
        public RequestCache() {
            cache = new LinkedHashMap<String, byte[]>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                    return size() > MAX_ENTRIES;
                }
            };
        }

        public byte[] get(String MessageId) {
            return cache.get(MessageId);
        }

        public void put(String requestID, byte[] theReqFromClient) {
            cache.put(requestID, theReqFromClient);
        }

        public void clear() {
            cache.clear();
        }

    }

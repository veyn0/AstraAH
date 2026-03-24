package dev.veyno.astraAH.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IDLocks {

    private static Map<UUID, Object> locks = new ConcurrentHashMap<>();

    public static Object getLock(UUID uuid){
        if(locks.containsKey(uuid)) return locks.get(uuid);
        Object lock = new Object();
        locks.put(uuid, lock);
        return getLock(uuid);
    }
}

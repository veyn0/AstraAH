package dev.veyno.astraAH.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InteractionCooldown {

    private static Map<UUID, Long> interactionsBlockedUntil = new ConcurrentHashMap<>();

    private static long lockTime = 1_000_000_000;

    public static void onInteract(UUID playerId){
        interactionsBlockedUntil.put(playerId, System.nanoTime()+lockTime);
    }

    public static boolean allowInteract(UUID playerID){

        return true;
//
//        Long blockedUntil = interactionsBlockedUntil.get(playerID);
//
//        if (blockedUntil == null) {
//            return true;
//        }
//
//        return System.nanoTime() >= blockedUntil;
    }

}

package dev.veyno.astraAH.data.repository;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.data.YamlStorage;
import dev.veyno.astraAH.data.dto.PlayerData;
import dev.veyno.astraAH.data.serialization.PlayerDataBase64Serializer;
import dev.veyno.astraAH.util.IDLocks;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * YAML-backed {@link PlayerDataRepository} that stores one file per player
 * under {@code <dataFolder>/playerdata/<uuid>.yml}.
 * <p>
 * Each access opens a short-lived {@link YamlStorage} instance; this class
 * holds no in-memory state and is therefore safe to share. Caching and
 * join/leave handling are expected to be done by the service layer.
 * <p>
 * Concurrent access is guarded by per-player locks from {@link IDLocks};
 * {@link YamlStorage}'s own per-filename lock provides additional safety
 * against any other component that might touch the same file.
 */
public class YamlPlayerDataRepository implements PlayerDataRepository {

    private static final String SUBFOLDER = "playerdata";
    private static final String DATA_KEY = "data";

    private final AstraAH plugin;

    public YamlPlayerDataRepository(AstraAH plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Reads
    // -------------------------------------------------------------------------

    @Override
    public PlayerData getPlayerData(UUID playerId) {
        if (playerId == null) throw new IllegalArgumentException("playerId == null");

        synchronized (IDLocks.getLock(playerId)) {
            File file = fileFor(playerId);
            if (!file.exists()) {
                return null;
            }
            YamlStorage storage = openStorage(playerId);
            String encoded = storage.getFileConfiguration().getString(DATA_KEY);
            if (encoded == null || encoded.isEmpty()) {
                return null;
            }
            return PlayerDataBase64Serializer.fromBase64(encoded);
        }
    }

    @Override
    public List<PlayerData> getPlayerData() {
        File dir = playerDataFolder();
        File[] files = dir.listFiles((f, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) return Collections.emptyList();

        List<PlayerData> result = new ArrayList<>(files.length);
        for (File f : files) {
            UUID id = parseUuid(f.getName());
            if (id == null) continue;
            PlayerData data = getPlayerData(id);
            if (data != null) result.add(data);
        }
        return result;
    }

    @Override
    public List<PlayerData> getPlayerData(List<UUID> playerIds) {
        if (playerIds == null || playerIds.isEmpty()) return Collections.emptyList();
        List<PlayerData> result = new ArrayList<>(playerIds.size());
        for (UUID id : playerIds) {
            PlayerData data = getPlayerData(id);
            if (data != null) result.add(data);
        }
        return result;
    }

    @Override
    public List<PlayerData> getPlayerData(UUID... playerIds) {
        if (playerIds == null || playerIds.length == 0) return Collections.emptyList();
        return getPlayerData(Arrays.asList(playerIds));
    }

    // -------------------------------------------------------------------------
    // Writes
    // -------------------------------------------------------------------------

    @Override
    public void setPlayerData(PlayerData playerData) {
        if (playerData == null) throw new IllegalArgumentException("playerData == null");
        UUID id = playerData.getPlayerId();
        if (id == null) throw new IllegalArgumentException("playerData.playerId == null");

        synchronized (IDLocks.getLock(id)) {
            YamlStorage storage = openStorage(id);
            String encoded = PlayerDataBase64Serializer.toBase64(playerData);
            storage.getFileConfiguration().set(DATA_KEY, encoded);
            storage.saveFile();
        }
    }

    @Override
    public void setPlayerData(List<PlayerData> playerData) {
        if (playerData == null) return;
        for (PlayerData d : playerData) setPlayerData(d);
    }

    @Override
    public void setPlayerData(PlayerData... playerData) {
        if (playerData == null) return;
        for (PlayerData d : playerData) setPlayerData(d);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a short-lived storage handle for the given player. No scheduled
     * autosave is attached — the caller saves explicitly.
     */
    private YamlStorage openStorage(UUID playerId) {
        // fileName relative to plugin data folder, no ".yml" suffix (YamlStorage appends it)
        String fileName = SUBFOLDER + File.separator + playerId;
        return new YamlStorage(plugin, fileName, false, -1);
    }

    private File playerDataFolder() {
        File dir = new File(plugin.getDataFolder(), SUBFOLDER);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private File fileFor(UUID playerId) {
        return new File(playerDataFolder(), playerId + ".yml");
    }

    private static UUID parseUuid(String fileName) {
        String raw = fileName.endsWith(".yml")
                ? fileName.substring(0, fileName.length() - 4)
                : fileName;
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
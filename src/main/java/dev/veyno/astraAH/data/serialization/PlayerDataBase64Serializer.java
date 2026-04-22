package dev.veyno.astraAH.data.serialization;

import dev.veyno.astraAH.data.dto.AllowedActions;
import dev.veyno.astraAH.data.dto.Category;
import dev.veyno.astraAH.data.dto.PlayerData;
import dev.veyno.astraAH.data.dto.Preferences;
import dev.veyno.astraAH.data.dto.Transaction;
import dev.veyno.astraAH.app.dto.ActionState;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Binary, versioned (de)serializer for {@link PlayerData}.
 * <p>
 * Format overview (big-endian via {@link DataOutputStream}):
 * <pre>
 *   int    version
 *   UUID   playerId            (2x long)
 *   -- Preferences --
 *   bool   showCategories
 *   bool   showHistory
 *   int    categoryCount
 *     foreach category:
 *        int      filterCount
 *          foreach filter: UTF string
 *        int      previewBytesLen       (-1 = null)
 *        byte[]   previewBytes          (ItemStack#serializeAsBytes)
 *   -- AllowedActions --
 *   12x byte (ActionState ordinal per field, in declared order)
 *   -- Transactions --
 *   int    transactionCount
 *     foreach transaction:
 *        UUID     entryId
 *        int      contentBytesLen       (-1 = null)
 *        byte[]   contentBytes
 *        UUID     playerId
 *        UUID     sellerId
 *        double   price
 *        long     epochSecond
 *        int      nanoAdjustment
 * </pre>
 *
 * <p>Write new versions by branching on the version int in {@code fromBase64}.</p>
 */
public final class PlayerDataBase64Serializer {

    private static final int CURRENT_VERSION = 1;

    private PlayerDataBase64Serializer() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public static String toBase64(PlayerData playerData) {
        if (playerData == null) throw new IllegalArgumentException("playerData == null");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(baos)) {

            out.writeInt(CURRENT_VERSION);
            writePlayerData(out, playerData);

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize PlayerData", e);
        }
    }

    public static PlayerData fromBase64(String data) {
        if (data == null || data.isEmpty()) throw new IllegalArgumentException("data is empty");
        byte[] raw = Base64.getDecoder().decode(data);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(raw);
             DataInputStream in = new DataInputStream(bais)) {

            int version = in.readInt();
            return switch (version) {
                case 1 -> readPlayerDataV1(in);
                default -> throw new IllegalStateException("Unsupported PlayerData version: " + version);
            };
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize PlayerData", e);
        }
    }

    // -------------------------------------------------------------------------
    // Writers
    // -------------------------------------------------------------------------

    private static void writePlayerData(DataOutputStream out, PlayerData d) throws IOException {
        writeUUID(out, d.getPlayerId());
        writePreferences(out, d.getPreferences());
        writeAllowedActions(out, d.getAllowedActions());
        writeTransactions(out, d.getTransactions());
    }

    private static void writePreferences(DataOutputStream out, Preferences p) throws IOException {
        // Defensive defaults if caller passed null
        if (p == null) {
            out.writeBoolean(false);
            out.writeBoolean(false);
            out.writeInt(0);
            return;
        }
        out.writeBoolean(p.isShowCategories());
        out.writeBoolean(p.isShowHistory());

        List<Category> categories = p.getCategories();
        if (categories == null) {
            out.writeInt(0);
        } else {
            out.writeInt(categories.size());
            for (Category c : categories) {
                writeCategory(out, c);
            }
        }
    }

    private static void writeCategory(DataOutputStream out, Category c) throws IOException {
        List<String> filters = c.getFilter();
        if (filters == null) {
            out.writeInt(0);
        } else {
            out.writeInt(filters.size());
            for (String f : filters) {
                out.writeUTF(f == null ? "" : f);
            }
        }
        writeItemStack(out, c.getPreview());
    }

    private static void writeAllowedActions(DataOutputStream out, AllowedActions a) throws IOException {
        if (a == null) {
            // 12 default states
            for (int i = 0; i < 12; i++) out.writeByte(0);
            return;
        }
        writeActionState(out, a.getCategories());
        writeActionState(out, a.getSettings());
        writeActionState(out, a.getMyListings());
        writeActionState(out, a.getRefresh());
        writeActionState(out, a.getSort());
        writeActionState(out, a.getSearch());
        writeActionState(out, a.getHistory());
        writeActionState(out, a.getShowAdvancedCategories());
        writeActionState(out, a.getShowAdvancedHistory());
        writeActionState(out, a.getReloadOnOpen());
        writeActionState(out, a.getDefaultFilter());
        writeActionState(out, a.getDefaultSort());
    }

    private static void writeActionState(DataOutputStream out, ActionState s) throws IOException {
        // ordinal fits in a byte; -1 = null
        out.writeByte(s == null ? -1 : s.ordinal());
    }

    private static void writeTransactions(DataOutputStream out, List<Transaction> txs) throws IOException {
        if (txs == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(txs.size());
        for (Transaction t : txs) {
            writeTransaction(out, t);
        }
    }

    private static void writeTransaction(DataOutputStream out, Transaction t) throws IOException {
        writeUUID(out, t.getEntryId());
        writeItemStack(out, t.getContent());
        writeUUID(out, t.getPlayerId());
        writeUUID(out, t.getSellerID());
        out.writeDouble(t.getPrice());

        Instant ts = t.getTimeStamp();
        if (ts == null) ts = Instant.EPOCH;
        out.writeLong(ts.getEpochSecond());
        out.writeInt(ts.getNano());
    }

    // -------------------------------------------------------------------------
    // Readers (v1)
    // -------------------------------------------------------------------------

    private static PlayerData readPlayerDataV1(DataInputStream in) throws IOException {
        PlayerData d = new PlayerData();
        d.setPlayerId(readUUID(in));
        d.setPreferences(readPreferencesV1(in));
        d.setAllowedActions(readAllowedActionsV1(in));
        d.setTransactions(readTransactionsV1(in));
        return d;
    }

    private static Preferences readPreferencesV1(DataInputStream in) throws IOException {
        Preferences p = new Preferences();
        p.setShowCategories(in.readBoolean());
        p.setShowHistory(in.readBoolean());

        int catCount = in.readInt();
        List<Category> categories = new ArrayList<>(catCount);
        for (int i = 0; i < catCount; i++) {
            categories.add(readCategoryV1(in));
        }
        p.setCategories(categories);
        return p;
    }

    private static Category readCategoryV1(DataInputStream in) throws IOException {
        Category c = new Category();

        int filterCount = in.readInt();
        List<String> filters = new ArrayList<>(filterCount);
        for (int i = 0; i < filterCount; i++) {
            filters.add(in.readUTF());
        }
        c.setFilter(filters);
        c.setPreview(readItemStack(in));
        return c;
    }

    private static AllowedActions readAllowedActionsV1(DataInputStream in) throws IOException {
        AllowedActions a = new AllowedActions();
        a.setCategories(readActionState(in));
        a.setSettings(readActionState(in));
        a.setMyListings(readActionState(in));
        a.setRefresh(readActionState(in));
        a.setSort(readActionState(in));
        a.setSearch(readActionState(in));
        a.setHistory(readActionState(in));
        a.setShowAdvancedCategories(readActionState(in));
        a.setShowAdvancedHistory(readActionState(in));
        a.setReloadOnOpen(readActionState(in));
        a.setDefaultFilter(readActionState(in));
        a.setDefaultSort(readActionState(in));
        return a;
    }

    private static ActionState readActionState(DataInputStream in) throws IOException {
        int ordinal = in.readByte();
        if (ordinal < 0) return null;
        ActionState[] values = ActionState.values();
        if (ordinal >= values.length) {
            throw new IOException("Unknown ActionState ordinal: " + ordinal);
        }
        return values[ordinal];
    }

    private static List<Transaction> readTransactionsV1(DataInputStream in) throws IOException {
        int count = in.readInt();
        List<Transaction> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(readTransactionV1(in));
        }
        return list;
    }

    private static Transaction readTransactionV1(DataInputStream in) throws IOException {
        Transaction t = new Transaction();
        t.setEntryId(readUUID(in));
        t.setContent(readItemStack(in));
        t.setPlayerId(readUUID(in));
        t.setSellerID(readUUID(in));
        t.setPrice(in.readDouble());

        long epochSecond = in.readLong();
        int nano = in.readInt();
        t.setTimeStamp(Instant.ofEpochSecond(epochSecond, nano));
        return t;
    }

    // -------------------------------------------------------------------------
    // Primitives
    // -------------------------------------------------------------------------

    private static void writeUUID(DataOutputStream out, UUID uuid) throws IOException {
        if (uuid == null) {
            out.writeLong(0L);
            out.writeLong(0L);
        } else {
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        }
    }

    private static UUID readUUID(DataInputStream in) throws IOException {
        long msb = in.readLong();
        long lsb = in.readLong();
        if (msb == 0L && lsb == 0L) return null;
        return new UUID(msb, lsb);
    }

    private static void writeItemStack(DataOutputStream out, ItemStack item) throws IOException {
        if (item == null) {
            out.writeInt(-1);
            return;
        }
        byte[] bytes = item.serializeAsBytes();
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static ItemStack readItemStack(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len < 0) return null;
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        return ItemStack.deserializeBytes(bytes);
    }
}
package dev.veyno.astraAH.data.serialization;

import dev.veyno.astraAH.data.dto.Listing;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/**
 * Binary, versioned (de)serializer for {@link Listing}.
 * <p>
 * Format overview (big-endian via {@link DataOutputStream}):
 * <pre>
 *   int    version
 *   UUID   listingId             (2x long)
 *   UUID   sellerId              (2x long)
 *   int    contentBytesLen       (-1 = null)
 *   byte[] contentBytes          (ItemStack#serializeAsBytes)
 *   long   createdAt
 *   double price
 *   UTF    currency              (empty string = null)
 *   int    status
 * </pre>
 *
 * <p>Write new versions by branching on the version int in {@code fromBase64}.</p>
 */
public final class ListingBase64Serializer {

    private static final int CURRENT_VERSION = 1;

    private ListingBase64Serializer() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public static String toBase64(Listing listing) {
        if (listing == null) throw new IllegalArgumentException("listing == null");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(baos)) {

            out.writeInt(CURRENT_VERSION);
            writeListing(out, listing);

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize Listing", e);
        }
    }

    public static Listing fromBase64(String data) {
        if (data == null || data.isEmpty()) throw new IllegalArgumentException("data is empty");
        byte[] raw = Base64.getDecoder().decode(data);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(raw);
             DataInputStream in = new DataInputStream(bais)) {

            int version = in.readInt();
            return switch (version) {
                case 1 -> readListingV1(in);
                default -> throw new IllegalStateException("Unsupported Listing version: " + version);
            };
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize Listing", e);
        }
    }

    // -------------------------------------------------------------------------
    // Writers
    // -------------------------------------------------------------------------

    private static void writeListing(DataOutputStream out, Listing l) throws IOException {
        writeUUID(out, l.getListingId());
        writeUUID(out, l.getSellerId());
        writeItemStack(out, l.getContent());
        out.writeLong(l.getCreatedAt());
        out.writeDouble(l.getPrice());
        out.writeUTF(l.getCurrency() == null ? "" : l.getCurrency());
        out.writeInt(l.getStatus());
    }

    // -------------------------------------------------------------------------
    // Readers (v1)
    // -------------------------------------------------------------------------

    private static Listing readListingV1(DataInputStream in) throws IOException {
        UUID listingId = readUUID(in);
        UUID sellerId = readUUID(in);
        ItemStack content = readItemStack(in);
        long createdAt = in.readLong();
        double price = in.readDouble();
        String currencyRaw = in.readUTF();
        String currency = currencyRaw.isEmpty() ? null : currencyRaw;
        int status = in.readInt();

        return new Listing(listingId, sellerId, content, createdAt, price, currency, status);
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
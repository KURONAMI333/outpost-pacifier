package com.kuronami.outpostpacifier;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Persistent list of "pacified zones" (square XZ area, any Y, per
 * dimension) where natural hostile spawns are blocked. Vanilla
 * {@link SavedData} so zones survive restarts ({@code
 * world/data/outpostpacifier.dat}).
 *
 * <p>1.20.1 variant: pre-registries {@code SavedData} signatures —
 * {@code save(CompoundTag)} (no {@code HolderLookup.Provider}), no
 * {@code Factory}, and {@code computeIfAbsent(loadFn, ctorFn, NAME)}
 * (loader first).
 */
public class PacifyData extends SavedData {

    private static final String NAME = "outpostpacifier";

    /** Square zone: centre (x,z), half-extent radius, any Y, in {@code dim}. */
    public record Zone(String dim, int x, int y, int z, int radius) {
        boolean contains(String d, int px, int pz) {
            return dim.equals(d)
                && Math.abs(px - x) <= radius
                && Math.abs(pz - z) <= radius;
        }
    }

    /** Hard cap so an op (or a script) repeatedly running /pacify can't
     *  bloat the save or the per-spawn O(zones) scan without bound. */
    private static final int MAX_ZONES = 256;

    private final List<Zone> zones = new ArrayList<>();

    public static PacifyData get(MinecraftServer server) {
        return server.overworld().getDataStorage()
            .computeIfAbsent(PacifyData::load, PacifyData::new, NAME);
    }

    /** @return true if added; false if a duplicate or the cap was hit. */
    public boolean add(Zone z) {
        for (Zone existing : zones) {
            if (existing.dim().equals(z.dim()) && existing.x() == z.x()
                    && existing.z() == z.z() && existing.radius() == z.radius()) {
                return false; // exact duplicate — no-op
            }
        }
        if (zones.size() >= MAX_ZONES) {
            return false; // cap reached
        }
        zones.add(z);
        setDirty();
        return true;
    }

    /** Remove every zone covering (dim, x, z); returns how many were removed. */
    public int removeAt(String dim, int x, int z) {
        int before = zones.size();
        zones.removeIf(zo -> zo.contains(dim, x, z));
        if (zones.size() != before) {
            setDirty();
        }
        return before - zones.size();
    }

    public boolean isPacified(String dim, int x, int z) {
        for (Zone zo : zones) {
            if (zo.contains(dim, x, z)) {
                return true;
            }
        }
        return false;
    }

    public List<Zone> zones() {
        return zones;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag lt = new ListTag();
        for (Zone z : zones) {
            CompoundTag c = new CompoundTag();
            c.putString("dim", z.dim());
            c.putInt("x", z.x());
            c.putInt("y", z.y());
            c.putInt("z", z.z());
            c.putInt("r", z.radius());
            lt.add(c);
        }
        tag.put("zones", lt);
        return tag;
    }

    private static PacifyData load(CompoundTag tag) {
        PacifyData d = new PacifyData();
        ListTag lt = tag.getList("zones", Tag.TAG_COMPOUND);
        for (int i = 0; i < lt.size(); i++) {
            CompoundTag c = lt.getCompound(i);
            d.zones.add(new Zone(c.getString("dim"), c.getInt("x"),
                c.getInt("y"), c.getInt("z"), c.getInt("r")));
        }
        return d;
    }
}

package com.kuronami.outpostpacifier;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
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
 */
public class PacifyData extends SavedData {

    private static final String NAME = "outpostpacifier";

    private static final Factory<PacifyData> FACTORY =
        new Factory<>(PacifyData::new, PacifyData::load, null);

    /** Square zone: centre (x,z), half-extent radius, any Y, in {@code dim}. */
    public record Zone(String dim, int x, int y, int z, int radius) {
        boolean contains(String d, int px, int pz) {
            return dim.equals(d)
                && Math.abs(px - x) <= radius
                && Math.abs(pz - z) <= radius;
        }
    }

    private final List<Zone> zones = new ArrayList<>();

    public static PacifyData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    public void add(Zone z) {
        zones.add(z);
        setDirty();
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
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
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

    private static PacifyData load(CompoundTag tag, HolderLookup.Provider registries) {
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

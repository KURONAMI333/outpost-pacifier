package com.kuronami.outpostpacifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit test for the zone-containment judgment ({@link PacifyData}) that the
 * Fabric {@code MobFinalizeSpawnMixin} (and the NeoForge/Forge
 * {@code FinalizeSpawnEvent} handlers) all delegate to for "is this spawn
 * position pacified?". This is the fallback the task asked for when a real
 * GameTest reproduction of natural spawning isn't in scope: this repo has
 * no GameTest infrastructure at all yet (NeoForge/ModDevGradle's
 * PLAYBOOK_GAMETEST.md doesn't transfer to Fabric Loom, and there is no
 * existing gametest/JUnit setup anywhere under mod-017-outpost-pacifier),
 * so standing one up from scratch was judged out of scope for a spawn-cancel
 * bugfix. {@link PacifyData} needs no Minecraft server/world instance to
 * construct or exercise (it has an implicit no-arg constructor and the
 * zone logic is plain Java), so it is exercised directly here instead of
 * through reflection into the Mixin.
 *
 * <p>What this test does NOT verify (would require a real spawn — either
 * GameTest infra or {@code runClient}, both out of scope here): that the
 * Mixin's {@code checkSpawnRules} injection actually fires for a live
 * {@link net.minecraft.world.entity.monster.Monster} spawn and reaches this
 * same {@code isPacified} check with the mob's real position/dimension.
 */
class PacifyDataTest {

    private static final String OVERWORLD = "overworld";
    private static final String NETHER = "the_nether";

    @Test
    void insideSquareRadiusIsPacified() {
        PacifyData data = new PacifyData();
        assertTrue(data.add(new PacifyData.Zone(OVERWORLD, 0, 64, 0, 10)));

        assertTrue(data.isPacified(OVERWORLD, 0, 0), "center");
        assertTrue(data.isPacified(OVERWORLD, 10, 0), "on the +x edge");
        assertTrue(data.isPacified(OVERWORLD, -10, 0), "on the -x edge");
        assertTrue(data.isPacified(OVERWORLD, 10, 10), "corner of the square");
    }

    @Test
    void outsideSquareRadiusIsNotPacified() {
        PacifyData data = new PacifyData();
        assertTrue(data.add(new PacifyData.Zone(OVERWORLD, 0, 64, 0, 10)));

        assertFalse(data.isPacified(OVERWORLD, 11, 0), "one block past the +x edge");
        assertFalse(data.isPacified(OVERWORLD, 0, -11), "one block past the -z edge");
        assertFalse(data.isPacified(OVERWORLD, 100, 100), "far outside");
    }

    @Test
    void differentDimensionDoesNotMatch() {
        PacifyData data = new PacifyData();
        assertTrue(data.add(new PacifyData.Zone(OVERWORLD, 0, 64, 0, 10)));

        assertFalse(data.isPacified(NETHER, 0, 0), "same coords, different dimension");
    }

    @Test
    void removeAtLiftsThePacification() {
        PacifyData data = new PacifyData();
        data.add(new PacifyData.Zone(OVERWORLD, 0, 64, 0, 10));
        assertTrue(data.isPacified(OVERWORLD, 5, 5));

        int removed = data.removeAt(OVERWORLD, 5, 5);

        assertEquals(1, removed);
        assertFalse(data.isPacified(OVERWORLD, 5, 5), "no longer pacified after removeAt");
    }

    @Test
    void removeAtOutsideAnyZoneRemovesNothing() {
        PacifyData data = new PacifyData();
        data.add(new PacifyData.Zone(OVERWORLD, 0, 64, 0, 10));

        assertEquals(0, data.removeAt(OVERWORLD, 500, 500));
        assertTrue(data.isPacified(OVERWORLD, 0, 0), "untouched zone still active");
    }

    @Test
    void exactDuplicateZoneIsRejected() {
        PacifyData data = new PacifyData();
        assertTrue(data.add(new PacifyData.Zone(OVERWORLD, 0, 64, 0, 10)));
        assertFalse(data.add(new PacifyData.Zone(OVERWORLD, 0, 64, 0, 10)), "exact duplicate is a no-op");

        assertEquals(1, data.zones().size());
    }

    @Test
    void overlappingZoneWithDifferentRadiusIsNotADuplicate() {
        PacifyData data = new PacifyData();
        assertTrue(data.add(new PacifyData.Zone(OVERWORLD, 0, 64, 0, 10)));
        assertTrue(data.add(new PacifyData.Zone(OVERWORLD, 0, 64, 0, 20)), "different radius, same center — distinct zone");

        assertEquals(2, data.zones().size());
    }
}

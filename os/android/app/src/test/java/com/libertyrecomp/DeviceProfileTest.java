package com.libertyrecomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceProfileTest {

    @Test
    public void cvarNames() {
        assertEquals("audio_maxqframes", DeviceProfile.cvarName("--audio_maxqframes=48"));
        assertEquals("audio_mute", DeviceProfile.cvarName("--audio_mute"));
        assertEquals("audio_mute", DeviceProfile.cvarName("--no-audio_mute"));
        assertEquals("x", DeviceProfile.cvarName("--x="));
        assertNull(DeviceProfile.cvarName("# comment"));
        assertNull(DeviceProfile.cvarName("--"));
        assertNull(DeviceProfile.cvarName("--=1"));
        assertNull(DeviceProfile.cvarName(null));
    }

    @Test
    public void defaultsFillOnlyWhatIsMissing() {
        List<String> args = new ArrayList<>(Arrays.asList(
                "--log_level=warn", "--audio_maxqframes=64", "--gta4_fps_guard=0"));
        List<String> added = DeviceProfile.addMissing(args, DeviceProfile.NON_ADRENO_DEFAULTS);

        assertTrue(args.contains("--audio_maxqframes=64"));
        assertTrue("the player's value is kept", !args.contains("--audio_maxqframes=48"));
        assertEquals(3, added.size());
        assertTrue(args.contains("--kernel_spinlock_spins=0"));
        assertTrue(args.contains("--android_thermal_guard_headroom_hot=0.85"));
    }

    @Test
    public void noCvarEndsUpTwice() {
        List<String> args = new ArrayList<>(Arrays.asList("--no-kernel_spinlock_spins"));
        DeviceProfile.addMissing(args, DeviceProfile.NON_ADRENO_DEFAULTS);
        DeviceProfile.addMissing(args, DeviceProfile.NON_ADRENO_DEFAULTS);
        List<String> names = new ArrayList<>();
        for (String a : args) {
            String n = DeviceProfile.cvarName(a);
            assertTrue("duplicate " + n, !names.contains(n));
            names.add(n);
        }
    }
}

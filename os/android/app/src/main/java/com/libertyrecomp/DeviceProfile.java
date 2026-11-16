package com.libertyrecomp;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Launch defaults for GPUs the port was not tuned on (no KGSL device: Samsung
 * Xclipse, ARM Mali), from measurements on a Galaxy S24 (Exynos 2400): the
 * CPU limits fell from 2.9 to 1.25 GHz within four minutes and the game then
 * ran CPU-bound with audio gaps in busy scenes.
 *
 * <p>Every default is added only when args.txt does not set the same cvar,
 * so a player's own line always wins (and the runtime's parser rejects a
 * cvar given twice). Plain Java for host unit tests.</p>
 */
final class DeviceProfile {

    /**
     * <ul>
     *   <li>kernel_spinlock_spins=0: a contended guest spinlock gives up the
     *       core at once instead of spinning; one guest thread spun ~25,000
     *       times a second on the S24, CPU time that only adds heat. 0 is the
     *       runtime's original behaviour (the safe-mode value).</li>
     *   <li>audio_maxqframes=48: ~255 ms of audio queued instead of ~170 ms,
     *       more room for decode stalls when the CPU is throttled.</li>
     *   <li>android_thermal_guard_headroom_hot/cool: the thermal guard acts
     *       at a forecast headroom of 0.85 instead of 0.95; at 0.95 the S24
     *       was already throttling and never cooled back below 0.75.</li>
     * </ul>
     */
    static final List<String> NON_ADRENO_DEFAULTS = Collections.unmodifiableList(Arrays.asList(
            "--kernel_spinlock_spins=0",
            "--audio_maxqframes=48",
            "--android_thermal_guard_headroom_hot=0.85",
            "--android_thermal_guard_headroom_cool=0.70"));

    private DeviceProfile() {}

    /** Whether the device has the Adreno (KGSL) GPU the port was tuned on. */
    static boolean isAdreno() {
        return new File("/dev/kgsl-3d0").exists() || new File("/sys/class/kgsl").exists();
    }

    /** "--name=value", "--name" or "--no-name" -> "name"; null when not a cvar line. */
    static String cvarName(String argument) {
        if (argument == null || !argument.startsWith("--") || argument.length() <= 2) return null;
        String rest = argument.substring(2);
        int equals = rest.indexOf('=');
        String name = equals >= 0 ? rest.substring(0, equals) : rest;
        if (equals < 0 && name.startsWith("no-")) name = name.substring(3);
        return name.isEmpty() ? null : name;
    }

    /**
     * Appends each default whose cvar {@code arguments} does not set yet.
     * Returns the defaults that were added.
     */
    static List<String> addMissing(List<String> arguments, List<String> defaults) {
        List<String> added = new ArrayList<>();
        for (String setting : defaults) {
            String name = cvarName(setting);
            if (name == null) continue;
            boolean present = false;
            for (String argument : arguments) {
                if (name.equals(cvarName(argument))) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                arguments.add(setting);
                added.add(setting);
            }
        }
        return added;
    }
}

package nl.enjarai.doahackedroll.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public enum KineticDamage {
    VANILLA,
    HIGH_SPEED,
    NONE,
    INSTANT_KILL;

    public static final Codec<KineticDamage> CODEC = Codec.STRING.comapFlatMap(name -> {
        try {
            return DataResult.success(valueOf(name));
        } catch (IllegalArgumentException e) {
            return DataResult.error(() -> "Unknown kinetic damage type: " + name);
        }
    }, KineticDamage::name);
}

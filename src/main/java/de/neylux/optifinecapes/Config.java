package de.neylux.optifinecapes;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue PRIORITIZE_MOJANG_CAPES = BUILDER
            .comment("Whether to prioritize mojang or optifine capes")
            .define("prioritizeMojangCapes", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}

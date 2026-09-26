package de.neylux.optifinecapes;

import net.neoforged.fml.config.ModConfig;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(OptifineCapes.MOD_ID)
public class OptifineCapes {
    public static final String MOD_ID = "optifinecapes";

    public OptifineCapes(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}

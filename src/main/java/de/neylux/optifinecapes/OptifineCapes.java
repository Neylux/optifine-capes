package de.neylux.optifinecapes;

import com.mojang.logging.LogUtils;
import de.neylux.optifinecapes.utils.CapeManager;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(OptifineCapes.MOD_ID)
public class OptifineCapes {
    public static final String MOD_ID = "optifinecapes";

    public OptifineCapes(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.addListener(this::onClientDisconnect);
        modEventBus.addListener(this::onTextureStitch);
    }

    private void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        CapeManager.getInstance().invalidateAll();
    }

    private void onTextureStitch(TextureAtlasStitchedEvent event) {
        CapeManager.getInstance().invalidateAll();
    }
}

package de.neylux.optifinecapes;

import de.neylux.optifinecapes.utils.CapeManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

@EventBusSubscriber(modid = OptifineCapes.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    static void onTextureStitch(TextureAtlasStitchedEvent event) {
        CapeManager.getInstance().invalidateAll();
    }

    @SubscribeEvent
    static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        CapeManager.getInstance().invalidateAll();
    }
}
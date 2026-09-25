package de.neylux.optifinecapes.mixin;

import com.mojang.authlib.GameProfile;
import de.neylux.optifinecapes.utils.CapeManager;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = PlayerInfo.class, priority = 2000)
public class PlayerInfoMixin {
    @Shadow
    @Final
    private GameProfile profile;

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void injectOptifineCape(CallbackInfoReturnable<PlayerSkin> callback) {
        PlayerSkin defaultSkin = callback.getReturnValue();
        if (defaultSkin == null) return;

        // Skip if the player already has an official cape equipped
        if (defaultSkin.cape() != null) return;

        var capeFuture = CapeManager.getInstance().getCapeTexture(this.profile.name());
        capeFuture.getNow(Optional.empty()).ifPresent(capeTexture -> {
            callback.setReturnValue(new PlayerSkin(
                    defaultSkin.body(),
                    capeTexture,
                    capeTexture,
                    defaultSkin.model(),
                    defaultSkin.secure()
            ));
        });
    }
}

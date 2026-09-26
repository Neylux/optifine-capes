package de.neylux.optifinecapes.utils;

import de.neylux.optifinecapes.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.core.ClientAsset;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class CapeManager {
    private static final CapeManager INSTANCE = new CapeManager();

    public static CapeManager getInstance() {
        return INSTANCE;
    }

    private final ConcurrentHashMap<String, CompletableFuture<Optional<ClientAsset.Texture>>> cache =
            new ConcurrentHashMap<>();

    public CompletableFuture<Optional<ClientAsset.Texture>> getCapeTexture(String username, boolean hasMojangCape) {
        // Return empty if user has mojang cape and prioritization is enabled
        if (hasMojangCape && Config.PRIORITIZE_MOJANG_CAPES.get()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        String key = username.toLowerCase();

        return cache.computeIfAbsent(key, k -> {
            // Warning: Fetching capes by name is case-sensitive
            return CapeUtil.fetchCapeTexture(username);
        });
    }

    private void freeTexture(CompletableFuture<Optional<ClientAsset.Texture>> future) {
        if (future != null) {
            future.thenAccept(optionalTexture -> {
                optionalTexture.ifPresent(texture -> {
                    var _ = Minecraft.getInstance().submit(() -> {
                                Minecraft.getInstance().getTextureManager().release(texture.id());
                            }
                    );
                });
            });
        }
    }

    public void invalidateAll() {
        cache.values().forEach(this::freeTexture);
        cache.clear();
    }
}
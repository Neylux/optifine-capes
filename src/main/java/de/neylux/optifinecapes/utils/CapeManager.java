package de.neylux.optifinecapes.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.ClientAsset;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.ConcurrentLinkedQueue;

public final class CapeManager {
    private static final CapeManager INSTANCE = new CapeManager();
    private static final int MAX_CAPES = 250;

    public static CapeManager getInstance() {
        return INSTANCE;
    }

    private final ConcurrentHashMap<String, CompletableFuture<Optional<ClientAsset.Texture>>> cache =
            new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> insertionOrder = new ConcurrentLinkedQueue<>();

    public CompletableFuture<Optional<ClientAsset.Texture>> getCapeTexture(String username) {
        String key = username.toLowerCase();

        return cache.computeIfAbsent(key, k -> {
            insertionOrder.add(k);

            if (cache.size() > MAX_CAPES) {
                String oldestKey = insertionOrder.poll();
                if (oldestKey != null) {
                    var capeTextureFuture = cache.remove(oldestKey);
                    freeTexture(capeTextureFuture);
                }
            }

            return CapeUtil.fetchCapeTexture(k);
        });
    }

    private void freeTexture(CompletableFuture<Optional<ClientAsset.Texture>> future) {
        if (future != null && future.isDone()) {
            future.getNow(Optional.empty()).ifPresent(texture -> {
                Minecraft.getInstance().submit(() -> Minecraft.getInstance().getTextureManager().release(texture.id()));
            });
        }
    }
    public void invalidateAll() {
        cache.values().forEach(this::freeTexture);

        cache.clear();
        insertionOrder.clear();
    }
}
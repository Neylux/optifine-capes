package de.neylux.optifinecapes.utils;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import de.neylux.optifinecapes.OptifineCapes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public final class CapeUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String OPTIFINE_URL = "http://s.optifine.net/capes/%s.png";

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(
            runnable -> {
                Thread thread = new Thread(runnable, "CapeManager");
                thread.setDaemon(true);
                return thread;
            }
    );

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .executor(EXECUTOR)
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private CapeUtil() {}

    public static CompletableFuture<Optional<ClientAsset.Texture>> fetchCapeTexture(String username) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(OPTIFINE_URL.formatted(username)))
                .timeout(Duration.ofSeconds(4))
                .GET()
                .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        return Optional.<ClientAsset.Texture>empty();
                    }
                    return handleResponse(username, response.body());
                })
                .exceptionally(ex -> {
                    LOGGER.error("Unexpected error occurred when fetching cape for: {}", username, ex);
                    return Optional.empty();
                });
    }

    private static Optional<ClientAsset.Texture> handleResponse(String username, byte[] body) {
        try {
            var image = resizeCape(NativeImage.read(new ByteArrayInputStream(body)));
            var imgIdentifier = Identifier.fromNamespaceAndPath(
                    OptifineCapes.MOD_ID,
                    "cape_" + username.toLowerCase()
            );

            var _ = Minecraft.getInstance().submit(() -> {
                try {
                    Minecraft.getInstance()
                            .getTextureManager()
                            .register(imgIdentifier, new DynamicTexture(imgIdentifier::toString, image));
                } catch (Exception e) {
                    LOGGER.error("Failed to register cape texture for: {}", username, e);
                    image.close();
                }
            });

            var clientTexture = new ClientAsset.DownloadedTexture(imgIdentifier, "");
            return Optional.of(clientTexture);

        } catch (Exception e) {
            LOGGER.error("Failed to resize cape for: {}", username, e);
            return Optional.empty();
        }
    }

    /**
     * Resizes an OptiFine cape image to the next power‑of‑two canvas that fits it.
     * <p>
     * Default OptiFine capes are 46×22. The canvas starts at 64×32 and doubles
     * until it is at least as large as the source image in both dimensions.
     */
    private static NativeImage resizeCape(NativeImage image) {
        int srcWidth = image.getWidth();
        int srcHeight = image.getHeight();

        if (srcWidth <= 0 || srcHeight <= 0) {
            return image;
        }

        int canvasWidth = 64;
        int canvasHeight = 32;

        while (canvasWidth < srcWidth || canvasHeight < srcHeight) {
            canvasWidth *= 2;
            canvasHeight *= 2;
        }

        // No resize needed
        if (canvasWidth == srcWidth && canvasHeight == srcHeight) {
            return image;
        }

        var resized = new NativeImage(canvasWidth, canvasHeight, true);

        long bytesPerRow = (long) srcWidth * 4L; // RGBA = 4 bytes
        long srcPtr = image.getPointer();
        long dstPtr = resized.getPointer();

        for (int y = 0; y < srcHeight; y++) {
            long srcRow = srcPtr + y * bytesPerRow;
            long dstRow = dstPtr + y * bytesPerRow;
            MemoryUtil.memCopy(srcRow, dstRow, bytesPerRow);
        }

        image.close();
        return resized;
    }
}
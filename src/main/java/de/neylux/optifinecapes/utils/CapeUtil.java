package de.neylux.optifinecapes.utils;

import com.mojang.blaze3d.platform.NativeImage;
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

import org.jetbrains.annotations.NotNull;

public final class CapeUtil {

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

    private CapeUtil() {
    }

    public static CompletableFuture<Optional<ClientAsset.Texture>> fetchCapeTexture(String username) {
        return CompletableFuture
                .supplyAsync(() -> URI.create("http://s.optifine.net/capes/" + username + ".png"))
                .thenCompose(uri -> {
                    var request = HttpRequest.newBuilder()
                            .uri(uri)
                            .timeout(Duration.ofSeconds(4))
                            .GET()
                            .build();

                    return HTTP_CLIENT
                            .sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                            .thenApply(response -> {
                                if (response.statusCode() != 200) {
                                    return Optional.<ClientAsset.Texture>empty();
                                }

                                return handleResponse(username, response.body());
                            });
                })
                .exceptionally(ex -> {
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
                    image.close();
                }
            });

            var clientTexture = new ClientAsset.DownloadedTexture(imgIdentifier, "");
            return Optional.of(clientTexture);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Resizes an OptiFine cape image to the next power‑of‑two canvas that fits it.
     * <p>
     * Default OptiFine capes are 46×22. The canvas starts at 64×32 and doubles
     * until it is at least as large as the source image in both dimensions.
     */
    private static @NotNull NativeImage resizeCape(@NotNull NativeImage image) {
        int imageWidth = 64;
        int imageHeight = 32;
        int imageSrcWidth = image.getWidth();
        int imageSrcHeight = image.getHeight();

        // Invalid image sizes
        if (imageSrcWidth <= 0 || imageSrcHeight <= 0) {
            return image;
        }

        while (imageWidth < imageSrcWidth || imageHeight < imageSrcHeight) {
            imageWidth *= 2;
            imageHeight *= 2;
        }

        NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
        for (int x = 0; x < imageSrcWidth; x++) {
            for (int y = 0; y < imageSrcHeight; y++) {
                imgNew.setPixel(x, y, image.getPixel(x, y));
            }
        }
        image.close();
        return imgNew;
    }
}
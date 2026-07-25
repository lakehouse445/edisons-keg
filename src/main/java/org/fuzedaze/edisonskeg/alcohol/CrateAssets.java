package org.fuzedaze.edisonskeg.alcohol;

import net.minecraft.resources.ResourceLocation;
import org.fuzedaze.edisonskeg.EdisonsKeg;

/**
 * Resolves which crate model, texture, and animations to draw for a given set of
 * {@link CrateContents} — the bare crate when empty, or the beverage's own crate assets
 * filled to the right level.
 *
 * <p>Every filled crate resolves through {@link AlcoholType}, so a new beverage needs no
 * code here: just {@code geo/crate/<id>_0..12.geo.json} and {@code textures/crate/<id>.png}.
 */
public final class CrateAssets {
    public static final ResourceLocation EMPTY_MODEL =
            new ResourceLocation(EdisonsKeg.MODID, "geo/crate/empty.geo.json");
    public static final ResourceLocation EMPTY_TEXTURE =
            new ResourceLocation(EdisonsKeg.MODID, "textures/crate/empty.png");
    public static final ResourceLocation EMPTY_ANIMATIONS =
            new ResourceLocation(EdisonsKeg.MODID, "animations/crate/empty.animation.json");

    public static ResourceLocation modelFor(CrateContents contents) {
        return contents.isEmpty() ? EMPTY_MODEL : contents.type().crateModel(contents.bottles());
    }

    public static ResourceLocation textureFor(CrateContents contents) {
        return contents.isEmpty() ? EMPTY_TEXTURE : contents.type().crateTexture();
    }

    public static ResourceLocation animationsFor(CrateContents contents) {
        return contents.isEmpty() ? EMPTY_ANIMATIONS : contents.type().crateAnimations();
    }

    private CrateAssets() {
    }
}

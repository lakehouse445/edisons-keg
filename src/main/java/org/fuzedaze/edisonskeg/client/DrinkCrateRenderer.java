package org.fuzedaze.edisonskeg.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.fuzedaze.edisonskeg.block.DrinkCrateBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DrinkCrateRenderer extends GeoBlockRenderer<DrinkCrateBlockEntity> {
    public DrinkCrateRenderer(BlockEntityRendererProvider.Context context) {
        super(new DrinkCrateModel());
    }
}

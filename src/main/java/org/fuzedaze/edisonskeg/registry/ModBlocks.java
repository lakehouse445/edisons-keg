package org.fuzedaze.edisonskeg.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.fuzedaze.edisonskeg.EdisonsKeg;
import org.fuzedaze.edisonskeg.block.DrinkCrateBlock;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, EdisonsKeg.MODID);

    /** One crate for every beverage; what it holds lives on its block entity. */
    public static final RegistryObject<Block> DRINK_CRATE = BLOCKS.register("drink_crate",
            () -> new DrinkCrateBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)));

    private ModBlocks() {
    }
}

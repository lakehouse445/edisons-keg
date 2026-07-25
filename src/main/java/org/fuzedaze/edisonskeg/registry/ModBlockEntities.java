package org.fuzedaze.edisonskeg.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.fuzedaze.edisonskeg.EdisonsKeg;
import org.fuzedaze.edisonskeg.block.DrinkCrateBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, EdisonsKeg.MODID);

    /** Shared by every drink crate; each instance reads its beverage from its block. */
    public static final RegistryObject<BlockEntityType<DrinkCrateBlockEntity>> DRINK_CRATE =
            BLOCK_ENTITIES.register("drink_crate",
                    () -> BlockEntityType.Builder.of(DrinkCrateBlockEntity::new, ModBlocks.BEER_CRATE.get()).build(null));

    private ModBlockEntities() {
    }
}

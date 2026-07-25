package org.fuzedaze.edisonskeg;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.fuzedaze.edisonskeg.network.ModNetworking;
import org.fuzedaze.edisonskeg.registry.ModBlockEntities;
import org.fuzedaze.edisonskeg.registry.ModBlocks;
import org.fuzedaze.edisonskeg.registry.ModCreativeTabs;
import org.fuzedaze.edisonskeg.registry.ModItems;
import org.fuzedaze.edisonskeg.registry.ModRecipes;

@Mod(EdisonsKeg.MODID)
public class EdisonsKeg {
    public static final String MODID = "edisonskeg";

    public EdisonsKeg() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModRecipes.TYPES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);

        ModNetworking.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, EKConfig.SPEC);
    }
}

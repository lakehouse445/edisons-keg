package org.fuzedaze.edisonskeg.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.fuzedaze.edisonskeg.EdisonsKeg;
import org.fuzedaze.edisonskeg.alcohol.AlcoholTypes;
import org.fuzedaze.edisonskeg.block.DrinkCrateBlock;
import org.fuzedaze.edisonskeg.item.AlcoholicDrinkItem;
import org.fuzedaze.edisonskeg.item.DrinkCrateBlockItem;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EdisonsKeg.MODID);

    public static final RegistryObject<Item> BEER = ITEMS.register("beer",
            () -> new AlcoholicDrinkItem(AlcoholTypes.BEER, new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> BEER_CRATE = ITEMS.register("beer_crate",
            () -> new DrinkCrateBlockItem((DrinkCrateBlock) ModBlocks.BEER_CRATE.get(), new Item.Properties()));

    private ModItems() {
    }
}

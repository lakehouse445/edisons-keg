package org.fuzedaze.edisonskeg.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.fuzedaze.edisonskeg.EdisonsKeg;
import org.fuzedaze.edisonskeg.alcohol.AlcoholTypes;
import org.fuzedaze.edisonskeg.item.AlcoholicDrinkItem;
import org.fuzedaze.edisonskeg.item.DrinkCrateBlockItem;

/**
 * Every drink item is registered under its {@link org.fuzedaze.edisonskeg.alcohol.AlcoholType}
 * id, which is how a type finds its bottle again — see {@code AlcoholType.drinkItem()}.
 */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EdisonsKeg.MODID);

    public static final RegistryObject<Item> BEER = ITEMS.register(AlcoholTypes.BEER.id(),
            () -> new AlcoholicDrinkItem(AlcoholTypes.BEER, new Item.Properties().stacksTo(16)));

    // Crates are bulky enough to be carried with both hands, so they don't stack.
    public static final RegistryObject<Item> DRINK_CRATE = ITEMS.register("drink_crate",
            () -> new DrinkCrateBlockItem(ModBlocks.DRINK_CRATE.get(), new Item.Properties().stacksTo(1)));

    private ModItems() {
    }
}

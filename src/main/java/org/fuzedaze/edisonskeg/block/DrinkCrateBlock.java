package org.fuzedaze.edisonskeg.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.fuzedaze.edisonskeg.alcohol.AlcoholType;
import org.fuzedaze.edisonskeg.alcohol.CrateContents;
import org.fuzedaze.edisonskeg.item.AlcoholicDrinkItem;

/**
 * A crate that holds bottles of one beverage. It is crafted empty and takes on whatever
 * drink is first put in, so a single block serves every {@link AlcoholType}.
 *
 * <p>Right-click with a matching bottle to put one in, or with anything else to take one
 * out. What it holds lives on the {@link DrinkCrateBlockEntity} rather than in the block
 * state — a block state could not carry drink type and fill level for every beverage
 * without exploding combinatorially.
 */
public class DrinkCrateBlock extends Block implements EntityBlock {
    // Matches the geo model's footprint: GeckoLib draws it centred on the block at floor level.
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D);

    public DrinkCrateBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Drawn by the GeckoLib block entity renderer rather than a baked JSON model.
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DrinkCrateBlockEntity(pos, state);
    }

    /** Middle-clicking a crate gives you a crate holding the same thing. */
    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
        if (level.getBlockEntity(pos) instanceof DrinkCrateBlockEntity crate)
            crate.getContents().writeToStack(stack);
        return stack;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof DrinkCrateBlockEntity crate))
            return InteractionResult.PASS;

        CrateContents contents = crate.getContents();
        ItemStack held = player.getItemInHand(hand);

        if (held.getItem() instanceof AlcoholicDrinkItem drink)
            return putIn(level, pos, player, crate, contents, held, drink.getAlcoholType());

        return takeOut(level, pos, player, crate, contents);
    }

    private InteractionResult putIn(Level level, BlockPos pos, Player player, DrinkCrateBlockEntity crate,
                                    CrateContents contents, ItemStack held, AlcoholType drink) {
        if (contents.holdsSomethingElse(drink)) {
            if (!level.isClientSide)
                player.displayClientMessage(Component.translatable("message.edisonskeg.crate_wrong_drink"), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // A full crate of the same drink: nothing to add, so hand one back instead.
        if (!contents.accepts(drink))
            return takeOut(level, pos, player, crate, contents);

        if (!level.isClientSide) {
            if (!player.getAbilities().instabuild)
                held.shrink(1);
            crate.setContents(contents.plusOne(drink));
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private InteractionResult takeOut(Level level, BlockPos pos, Player player, DrinkCrateBlockEntity crate,
                                      CrateContents contents) {
        if (contents.isEmpty()) {
            if (!level.isClientSide)
                player.displayClientMessage(Component.translatable("message.edisonskeg.crate_empty"), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            ItemStack bottle = new ItemStack(contents.type().drinkItem());
            crate.setContents(contents.minusOne());
            if (!player.getInventory().add(bottle))
                player.drop(bottle, false);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.7F,
                    0.9F + level.getRandom().nextFloat() * 0.2F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}

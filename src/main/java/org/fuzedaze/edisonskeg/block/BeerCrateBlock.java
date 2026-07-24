package org.fuzedaze.edisonskeg.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A crate holding bottles of a drink. Right-click to take a bottle; right-click
 * while holding a matching bottle to put it back. The fill level survives being
 * broken and re-placed (copied through the loot table's {@code copy_state} function).
 */
public class BeerCrateBlock extends Block {
    public static final int CAPACITY = 12;
    public static final IntegerProperty BOTTLES = IntegerProperty.create("bottles", 0, CAPACITY);
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    private final Supplier<? extends Item> drink;

    public BeerCrateBlock(Supplier<? extends Item> drink, Properties properties) {
        super(properties);
        this.drink = drink;
        registerDefaultState(this.stateDefinition.any().setValue(BOTTLES, CAPACITY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BOTTLES);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        int bottles = state.getValue(BOTTLES);
        ItemStack held = player.getItemInHand(hand);

        // Returning a bottle to the crate
        if (held.is(this.drink.get())) {
            if (bottles >= CAPACITY)
                return InteractionResult.PASS;

            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild)
                    held.shrink(1);
                level.setBlock(pos, state.setValue(BOTTLES, bottles + 1), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Taking a bottle out
        if (bottles <= 0) {
            if (!level.isClientSide)
                player.displayClientMessage(Component.translatable("message.edisonskeg.crate_empty"), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(BOTTLES, bottles - 1), Block.UPDATE_ALL);
            ItemStack bottle = new ItemStack(this.drink.get());
            if (!player.getInventory().add(bottle))
                player.drop(bottle, false);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.7F,
                    0.9F + level.getRandom().nextFloat() * 0.2F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}

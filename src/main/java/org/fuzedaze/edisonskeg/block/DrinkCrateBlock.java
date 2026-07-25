package org.fuzedaze.edisonskeg.block;

import java.util.function.Supplier;
import javax.annotation.Nullable;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.fuzedaze.edisonskeg.alcohol.AlcoholType;

/**
 * A crate of bottles. Right-click to take one out, right-click holding a matching bottle to
 * put one back, and the crate visibly empties as it goes: the {@link #BOTTLES} state picks
 * which geo model is drawn.
 *
 * <p>Not beer-specific — construct one with any {@link AlcoholType} and it will look for
 * that type's crate assets.
 */
public class DrinkCrateBlock extends Block implements EntityBlock {
    public static final int CAPACITY = 12;
    public static final IntegerProperty BOTTLES = IntegerProperty.create("bottles", 0, CAPACITY);
    // Matches the geo model's footprint: GeckoLib draws it centred on the block at floor level.
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D);

    private final AlcoholType type;
    private final Supplier<? extends Item> drink;

    public DrinkCrateBlock(AlcoholType type, Supplier<? extends Item> drink, Properties properties) {
        super(properties);
        this.type = type;
        this.drink = drink;
        registerDefaultState(this.stateDefinition.any().setValue(BOTTLES, CAPACITY));
    }

    /** The beverage this crate holds; also selects its models and texture. */
    public AlcoholType getAlcoholType() {
        return this.type;
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
    public RenderShape getRenderShape(BlockState state) {
        // Drawn by the GeckoLib block entity renderer rather than a baked JSON model.
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DrinkCrateBlockEntity(pos, state);
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

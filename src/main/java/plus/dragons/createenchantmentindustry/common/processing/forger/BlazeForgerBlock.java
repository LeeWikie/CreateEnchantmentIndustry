package plus.dragons.createenchantmentindustry.common.processing.forger;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import plus.dragons.createenchantmentindustry.foundation.blaze.CEIBlazeBlock;
import plus.dragons.createenchantmentindustry.registry.CEIBlockEntityTypes;

public class BlazeForgerBlock extends CEIBlazeBlock<BlazeForgerBlockEntity> {
    public static final MapCodec<BlazeForgerBlock> CODEC = simpleCodec(BlazeForgerBlock::new);

    public BlazeForgerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlazeForgerBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return InteractionResult.PASS;
        }
        ItemStack extracted = blockEntity.extractItem(false);
        if (extracted.isEmpty()) {
            return InteractionResult.PASS;
        }
        player.getInventory().placeItemBackInInventory(extracted);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        InteractionResult fuelResult = super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        if (fuelResult.consumesAction()) {
            return fuelResult;
        }
        BlazeForgerBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return InteractionResult.PASS;
        }
        ItemStack remainder = blockEntity.insertItem(stack, false);
        if (ItemStack.isSameItemSameComponents(stack, remainder) && stack.getCount() == remainder.getCount()) {
            return InteractionResult.PASS;
        }
        player.setItemInHand(hand, remainder);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MapCodec<BlazeForgerBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<BlazeForgerBlockEntity> getBlockEntityClass() {
        return BlazeForgerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BlazeForgerBlockEntity> getBlockEntityType() {
        return CEIBlockEntityTypes.BLAZE_FORGER;
    }
}

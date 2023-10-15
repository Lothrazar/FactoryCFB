package com.lothrazar.factorycfb.content;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.cookingforblockheads.ItemUtils;
import net.blay09.mods.cookingforblockheads.block.CookingTableBlock;
import net.blay09.mods.cookingforblockheads.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class HackBlock extends CookingTableBlock {

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new HackTile(pos, state);
  }

  @Override
  public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
    HackTile tileEntity = (HackTile) level.getBlockEntity(pos);
    if (tileEntity != null && !state.is(newState.getBlock())) {
      ItemUtils.spawnItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, tileEntity.getNoFilterBook());
    }
    //    super.onRemove(state, level, pos, newState, isMoving);
    if (state.hasBlockEntity() && (!state.is(state.getBlock()) || !state.hasBlockEntity())) {
      level.removeBlockEntity(pos);
    }
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
    ItemStack heldItem = player.getItemInHand(hand);
    HackTile blockEntity = (HackTile) level.getBlockEntity(pos);
    if (!heldItem.isEmpty()) {
      if (blockEntity != null) {
        if (tryRecolorBlock(state, heldItem, level, pos, player, rayTraceResult)) {
          return InteractionResult.SUCCESS;
        }
        if (!blockEntity.hasNoFilterBook() && heldItem.getItem() == ModItems.noFilterBook) {
          blockEntity.setNoFilterBook(heldItem.split(1));
          return InteractionResult.SUCCESS;
        }
      }
    }
    else if (player.isShiftKeyDown()) {
      if (blockEntity != null) {
        ItemStack noFilterBook = blockEntity.getNoFilterBook();
        if (!noFilterBook.isEmpty()) {
          if (!player.getInventory().add(noFilterBook)) {
            player.drop(noFilterBook, false);
          }
          blockEntity.setNoFilterBook(ItemStack.EMPTY);
          return InteractionResult.SUCCESS;
        }
      }
    }
    if (!level.isClientSide) {
      Balm.getNetworking().openGui(player, blockEntity);
    }
    return InteractionResult.SUCCESS;
  }
}

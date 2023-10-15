package com.lothrazar.factorycfb.content;

import net.blay09.mods.cookingforblockheads.client.gui.screen.RecipeBookScreen;
import net.blay09.mods.cookingforblockheads.menu.RecipeBookMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class HackScreen extends RecipeBookScreen {

  public HackScreen(RecipeBookMenu container, Inventory playerInventory, Component displayName) {
    super(container, playerInventory, displayName);
  }
}

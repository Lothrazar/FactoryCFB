package com.lothrazar.factorycfb.content;

import com.lothrazar.factorycfb.FactoryRegistry;
import net.blay09.mods.cookingforblockheads.menu.RecipeBookMenu;
import net.minecraft.world.entity.player.Player;

public class HackMenu extends RecipeBookMenu {

  public HackMenu(int windowId, Player player) {
    super(FactoryRegistry.MENU_TABLE.get(), windowId, player);
  }
}

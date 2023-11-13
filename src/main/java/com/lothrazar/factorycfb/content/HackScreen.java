package com.lothrazar.factorycfb.content;

import java.util.List;
import java.util.Optional;
import org.lwjgl.glfw.GLFW;
import com.google.common.collect.Lists;
import com.lothrazar.factorycfb.FactoryCFB;
import net.blay09.mods.balm.mixin.AbstractContainerScreenAccessor;
import net.blay09.mods.cookingforblockheads.CookingForBlockheadsConfig;
import net.blay09.mods.cookingforblockheads.api.FoodRecipeWithStatus;
import net.blay09.mods.cookingforblockheads.api.ISortButton;
import net.blay09.mods.cookingforblockheads.api.RecipeStatus;
import net.blay09.mods.cookingforblockheads.client.gui.SortButton;
import net.blay09.mods.cookingforblockheads.client.gui.screen.RecipeBookScreen;
import net.blay09.mods.cookingforblockheads.menu.RecipeBookMenu;
import net.blay09.mods.cookingforblockheads.menu.slot.CraftMatrixFakeSlot;
import net.blay09.mods.cookingforblockheads.menu.slot.RecipeFakeSlot;
import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.blay09.mods.cookingforblockheads.registry.FoodRecipeType;
import net.blay09.mods.cookingforblockheads.registry.FoodRecipeWithIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class HackScreen extends RecipeBookScreen {

  private static final int SCROLLBAR_COLOR = 0xFFAAAAAA;
  private static final int SCROLLBAR_Y = 8;
  private static final int SCROLLBAR_WIDTH = 7;
  private static final int SCROLLBAR_HEIGHT = 77;
  private static final ResourceLocation GUI = new ResourceLocation(FactoryCFB.MODID, "textures/gui.png");
  private static final ResourceLocation THREEX = new ResourceLocation(FactoryCFB.MODID, "textures/threex.png");
  private static final ResourceLocation SMELTING = new ResourceLocation(FactoryCFB.MODID, "textures/smelting.png");
  private static final int VISIBLE_ROWS = 4;
  private final RecipeBookMenu container;
  private int scrollBarScaledHeight;
  private int scrollBarXPos;
  private int scrollBarYPos;
  private int currentOffset;
  private double mouseClickY = -1;
  private int indexWhenClicked;
  private int lastNumberOfMoves;
  private Button btnNextRecipe;
  private Button btnPrevRecipe;
  private EditBox searchBar;
  private final List<SortButton> sortButtons = Lists.newArrayList();
  private final String[] noIngredients;
  private final String[] noSelection;

  public HackScreen(RecipeBookMenu container, Inventory playerInventory, Component displayName) {
    super(container, playerInventory, displayName);
    this.container = container;
    noIngredients = I18n.get("gui.cookingforblockheads:no_ingredients").split("\\\\n");
    noSelection = I18n.get("gui.cookingforblockheads:no_selection").split("\\\\n");
  }

  @Override
  protected void init() {
    imageHeight = 174;
    //    super.init();
    btnPrevRecipe = Button.builder(Component.literal("<"), it -> container.nextSubRecipe(-1))
        .pos(width / 2 - 79, height / 2 - 51).size(13, 20).build();
    btnPrevRecipe.visible = false;
    addRenderableWidget(btnPrevRecipe);
    btnNextRecipe = Button.builder(Component.literal(">"), it -> container.nextSubRecipe(1))
        .pos(width / 2 - 9, height / 2 - 51).size(13, 20).build();
    btnNextRecipe.visible = false;
    addRenderableWidget(btnNextRecipe);
    searchBar = new EditBox(minecraft.font, leftPos + imageWidth - 78, topPos - 5, 70, 10, searchBar, Component.empty());
    setInitialFocus(searchBar);
    int yOffset = -80;
    for (ISortButton button : CookingRegistry.getSortButtons()) {
      SortButton sortButton = new SortButton(width / 2 + 87, height / 2 + yOffset, button, it -> {
        container.setSortComparator(((SortButton) it).getComparator(Minecraft.getInstance().player));
      });
      addRenderableWidget(sortButton);
      sortButtons.add(sortButton);
      yOffset += 20;
    }
    recalculateScrollBar();
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (delta == 0) {
      return false;
    }
    if (container.getSelection() != null && mouseX >= leftPos + 7 && mouseY >= topPos + 17 && mouseX < leftPos + 92 && mouseY < topPos + 95) {
      Slot slot = ((AbstractContainerScreenAccessor) this).getHoveredSlot();
      if (slot instanceof CraftMatrixFakeSlot && ((CraftMatrixFakeSlot) slot).getVisibleStacks().size() > 1) {
        ((CraftMatrixFakeSlot) slot).scrollDisplayList(delta > 0 ? -1 : 1);
      }
    }
    else {
      setCurrentOffset(delta > 0 ? currentOffset - 1 : currentOffset + 1);
    }
    return true;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int state) {
    boolean result = super.mouseReleased(mouseX, mouseY, state);
    if (state != -1 && mouseClickY != -1) {
      mouseClickY = -1;
      indexWhenClicked = 0;
      lastNumberOfMoves = 0;
    }
    return result;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    super.mouseClicked(mouseX, mouseY, button);
    if (button == 1 && mouseX >= searchBar.getX() && mouseX < searchBar.getX() + searchBar.getWidth() && mouseY >= searchBar.getY() && mouseY < searchBar.getY() + searchBar.getHeight()) {
      searchBar.setValue("");
      container.search(null);
      container.populateRecipeSlots();
      setCurrentOffset(currentOffset);
      return true;
    }
    else {
      if (searchBar.mouseClicked(mouseX, mouseY, button)) {
        return true;
      }
    }
    if (mouseX >= scrollBarXPos && mouseX <= scrollBarXPos + SCROLLBAR_WIDTH && mouseY >= scrollBarYPos && mouseY <= scrollBarYPos + scrollBarScaledHeight) {
      mouseClickY = mouseY;
      indexWhenClicked = currentOffset;
    }
    Slot mouseSlot = ((AbstractContainerScreenAccessor) this).getHoveredSlot();
    if (mouseSlot instanceof CraftMatrixFakeSlot) {
      if (button == 0) {
        ItemStack itemStack = mouseSlot.getItem();
        FoodRecipeWithStatus recipe = container.findAvailableRecipe(itemStack);
        if (recipe != null) {
          container.setSelectedRecipe(recipe, false);
          setCurrentOffset(container.getSelectedRecipeIndex());
        }
        else if (!CookingRegistry.getFoodRecipes(itemStack).isEmpty()) {
          container.setSelectedRecipe(new FoodRecipeWithStatus(itemStack, RecipeStatus.MISSING_INGREDIENTS), true);
        }
      }
      else if (button == 1) {
        ((CraftMatrixFakeSlot) mouseSlot).setLocked(!((CraftMatrixFakeSlot) mouseSlot).isLocked());
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean charTyped(char c, int keyCode) {
    boolean result = super.charTyped(c, keyCode);
    container.search(searchBar.getValue());
    container.populateRecipeSlots();
    setCurrentOffset(currentOffset);
    return result;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
      minecraft.player.closeContainer();
      return true;
    }
    if (searchBar.keyPressed(keyCode, scanCode, modifiers) || searchBar.isFocused()) {
      container.search(searchBar.getValue());
      container.populateRecipeSlots();
      setCurrentOffset(currentOffset);
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
    if (container.isDirty()) {
      setCurrentOffset(currentOffset);
      container.setDirty(false);
    }
    //    guiGraphics.setColor(1f, 1f, 1f, 1f);
    guiGraphics.blit(GUI, leftPos, topPos - 10, 0, 0, imageWidth, imageHeight + 10);
    if (mouseClickY != -1) {
      float pixelsPerFilter = (SCROLLBAR_HEIGHT - scrollBarScaledHeight) / (float) Math.max(1,
          (int) Math.ceil(container.getItemListCount() / 3f) - VISIBLE_ROWS);
      if (pixelsPerFilter != 0) {
        int numberOfFiltersMoved = (int) ((mouseY - mouseClickY) / pixelsPerFilter);
        if (numberOfFiltersMoved != lastNumberOfMoves) {
          setCurrentOffset(indexWhenClicked + numberOfFiltersMoved);
          lastNumberOfMoves = numberOfFiltersMoved;
        }
      }
    }
    btnPrevRecipe.visible = container.hasVariants();
    btnPrevRecipe.active = container.getSelectionIndex() > 0;
    btnNextRecipe.visible = container.hasVariants();
    btnNextRecipe.active = container.getSelectionIndex() < container.getRecipeCount() - 1;
    boolean hasRecipes = container.getItemListCount() > 0;
    for (Button sortButton : sortButtons) {
      sortButton.active = hasRecipes;
    }
    //    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    var font = minecraft.font;
    FoodRecipeWithIngredients selection = container.getSelection();
    if (selection == null) {
      int curY = topPos + 79 / 2 - noSelection.length / 2 * font.lineHeight;
      for (String s : noSelection) {
        guiGraphics.drawString(font, s, leftPos + 23 + 27 - font.width(s) / 2, curY, 0xFFFFFFFF, true);
        curY += font.lineHeight + 5;
      }
    }
    else { //selection is not null
      for (CraftMatrixFakeSlot slot : container.getCraftingMatrixSlots()) {
        if (slot.isLocked() && slot.getVisibleStacks().size() > 1) {
          guiGraphics.blit(GUI, leftPos + slot.x, topPos + slot.y, 176, 60, 16, 16);
        }
      }
      if (selection.getRecipeType() == FoodRecipeType.SMELTING) {
        guiGraphics.blit(SMELTING, leftPos + 23, topPos + 19, 0, 0, 18, 34);
      }
      else {
        guiGraphics.blit(THREEX, leftPos + 23, topPos + 19, 0, 0, 54, 54);
      }
    }

    guiGraphics.fill(scrollBarXPos, scrollBarYPos, scrollBarXPos + SCROLLBAR_WIDTH, scrollBarYPos + scrollBarScaledHeight, SCROLLBAR_COLOR);
    if (container.getItemListCount() == 0) {
      guiGraphics.fill(leftPos + 97, topPos + 7, leftPos + 168, topPos + 85, 0xAA222222);
      int curY = topPos + 79 / 2 - noIngredients.length / 2 * font.lineHeight;
      for (String s : noIngredients) {
        guiGraphics.drawString(font, s, leftPos + 97 + 36 - font.width(s) / 2, curY, 0xFFFFFFFF, true);
        curY += font.lineHeight + 5;
      }
    }
    searchBar.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
  }

  @Override
  protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    guiGraphics.setColor(1f, 1f, 1f, 1f);
    if (CookingForBlockheadsConfig.getActive().showIngredientIcon) {
      var poseStack = guiGraphics.pose();
      poseStack.pushPose();
      poseStack.translate(0, 0, 300);
      for (Slot slot : container.slots) {
        if (slot instanceof RecipeFakeSlot) {
          if (CookingRegistry.isNonFoodRecipe(slot.getItem())) {
            guiGraphics.blit(GUI, slot.x, slot.y, 176, 76, 16, 16);
          }
          FoodRecipeWithStatus recipe = ((RecipeFakeSlot) slot).getRecipe();
          if (recipe != null && recipe.getStatus() == RecipeStatus.MISSING_TOOLS) {
            guiGraphics.blit(GUI, slot.x, slot.y, 176, 92, 16, 16);
          }
        }
      }
      poseStack.popPose();
    }
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    super.render(guiGraphics, mouseX, mouseY, partialTicks);
    var poseStack = guiGraphics.pose();
    poseStack.pushPose();
    poseStack.translate(0, 0, 300);
    for (Slot slot : container.slots) {
      if (slot instanceof CraftMatrixFakeSlot) {
        if (!((CraftMatrixFakeSlot) slot).isAvailable() && !slot.getItem().isEmpty()) {
          guiGraphics.fillGradient(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, 0x77FF4444, 0x77FF5555);
        }
      }
    }
    poseStack.popPose();
    container.updateSlots(partialTicks);
    for (Button sortButton : this.sortButtons) {
      if (sortButton instanceof SortButton && sortButton.isMouseOver(mouseX, mouseY) && sortButton.active) {
        guiGraphics.renderTooltip(font, ((SortButton) sortButton).getTooltipLines(), Optional.empty(), mouseX, mouseY);
      }
    }
    this.renderTooltip(guiGraphics, mouseX, mouseY);
  }

  private void recalculateScrollBar() {
    int scrollBarTotalHeight = SCROLLBAR_HEIGHT - 1;
    this.scrollBarScaledHeight = (int) (scrollBarTotalHeight * Math.min(1f, (VISIBLE_ROWS / (Math.ceil(container.getItemListCount() / 3f)))));
    this.scrollBarXPos = leftPos + imageWidth - SCROLLBAR_WIDTH - 9;
    this.scrollBarYPos = topPos + SCROLLBAR_Y + ((scrollBarTotalHeight - scrollBarScaledHeight) * currentOffset / Math.max(1,
        (int) Math.ceil((container.getItemListCount() / 3f)) - VISIBLE_ROWS));
  }

  private void setCurrentOffset(int currentOffset) {
    this.currentOffset = Math.max(0, Math.min(currentOffset, (int) Math.ceil(container.getItemListCount() / 3f) - VISIBLE_ROWS));
    container.setScrollOffset(this.currentOffset);
    recalculateScrollBar();
  }

  @Override
  public Button[] getSortingButtons() {
    return sortButtons.toArray(new SortButton[0]);
  }

}

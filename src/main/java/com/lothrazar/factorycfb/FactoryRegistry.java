package com.lothrazar.factorycfb;

import com.lothrazar.factorycfb.content.HackBlock;
import com.lothrazar.factorycfb.content.HackMenu;
import com.lothrazar.factorycfb.content.HackTile;
import net.blay09.mods.cookingforblockheads.KitchenMultiBlock;
import net.blay09.mods.cookingforblockheads.menu.RecipeBookMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FactoryRegistry {

  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FactoryCFB.MODID);
  public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FactoryCFB.MODID);
  public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, FactoryCFB.MODID);
  public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FactoryCFB.MODID);
  public static final RegistryObject<Block> TABLE = BLOCKS.register("factory_table", () -> new HackBlock());
  public static final RegistryObject<Item> ITEM_TABLE = ITEMS.register("factory_table", () -> new BlockItem(TABLE.get(), new Item.Properties()));
  public static final RegistryObject<BlockEntityType<HackTile>> TILE_TABLE = TILE_ENTITIES.register("factory_table", () -> BlockEntityType.Builder.of(HackTile::new, TABLE.get()).build(null));
  public static final RegistryObject<MenuType<RecipeBookMenu>> MENU_TABLE = CONTAINERS.register("factory_table",
      () -> IForgeMenuType.create((windowId, inv, data) -> new HackMenu(windowId, inv.player)
      .allowCrafting()
      .setKitchenMultiBlock(KitchenMultiBlock.buildFromLocation(inv.player.level(), data.readBlockPos()))));
}

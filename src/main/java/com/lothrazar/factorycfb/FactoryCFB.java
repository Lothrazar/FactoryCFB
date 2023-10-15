package com.lothrazar.factorycfb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FactoryCFB.MODID)
public class FactoryCFB {

  public static final String MODID = "factorycfb";
  public static final Logger LOGGER = LogManager.getLogger();

  public FactoryCFB() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    FactoryRegistry.BLOCKS.register(bus);
    FactoryRegistry.ITEMS.register(bus);
    FactoryRegistry.TILE_ENTITIES.register(bus);
    FactoryRegistry.CONTAINERS.register(bus);
    bus.addListener(this::setupClient);
  }

  private void setupClient(final FMLClientSetupEvent event) {
    //for client side only setup
  }
}
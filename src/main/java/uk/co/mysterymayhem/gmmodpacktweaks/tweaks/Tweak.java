/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

/**
 *
 * @author Thomas
 */
public abstract class Tweak {
  
  protected final String MODID;
  
  protected Tweak(String str) {
    this.MODID = str;
  }

  public void init(FMLInitializationEvent event) {}
  public void preInit(FMLPreInitializationEvent event) {}
  public void postInit(FMLPostInitializationEvent event) {}
  public boolean isModLoaded() {
    return Loader.isModLoaded(MODID);
  }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict;

/**
 * Not using this as the ore drops a bunch of naquadah when mined
 * @author Thomas
 */
public class SGCraft extends Tweak{

  public SGCraft() {
    super("SGCraft");
  }
  

  @Override
  public void init(FMLInitializationEvent event) {
    if (!this.isModLoaded()) {
      return;
    }
    OreDict.register(OreDict.DictType.INGOT, "Naquadah", MODID, "naquadah", 0);
  } 
  
}

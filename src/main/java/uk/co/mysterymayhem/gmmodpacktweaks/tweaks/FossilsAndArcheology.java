/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict;

/**
 *
 * @author Thomas
 */
public class FossilsAndArcheology extends Tweak{

  public FossilsAndArcheology() {
    this.MODID = "fossil";
  }

  @Override
  public void init(FMLInitializationEvent event) {
    OreDict.register(OreDict.DictType.ORE, "AncientAmber", MODID, "amberOre", 0);
  }
  
}

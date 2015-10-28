/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict;

/**
 *
 * @author Thomas
 */
public class RFTools extends Tweak {

  public RFTools() {
    super("rftools");
  }
  

  @Override
  public void init(FMLInitializationEvent event) {
    if (!this.isModLoaded()) return;
    // Ore smelts into 4 crystal, adding the crystal to gem dict produces ore->2crystal
    //OreDict.regMany("DimensionalShard", MODID, new String[]{"dimensionalShardBlock", "dimensionalShardItem"}, new int[]{0,0}, OreDict.DictType.ORE, OreDict.DictType.GEM);
    // Registered so that digital miner picks it (and it's UBC variants) up with *ore* filer
    OreDict.register(OreDict.DictType.ORE, "DimensionalShard", MODID, "dimensionalShardBlock", 0);
  }
  
}

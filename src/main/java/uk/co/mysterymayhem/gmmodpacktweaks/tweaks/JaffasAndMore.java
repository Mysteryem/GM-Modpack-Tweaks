/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import java.util.HashSet;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.DictType.BLOCK;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.DictType.DUST;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.DictType.GEM;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.DictType.INGOT;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.DictType.ORE;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.regMany;

/**
 *
 * @author Thomas
 */
public class JaffasAndMore extends Tweak{

  public JaffasAndMore() {
    super("Jaffas-Technic");
  }
  
  
  
  @Override
  public void init (FMLInitializationEvent event) {
    if (!this.isModLoaded()) return;
    regMany("Jaffarrol", "Jaffas-Technic", new String[]{"tile.jaffas.blockOfJaffarrol", "tile.jaffas.jaffarrolOre", "tile.jaffas.jaffarrolOre", "item.jaffas.jaffarrol", "item.jaffas.jaffarrolDust"}, new int[]{0,0,1,0,0}, BLOCK, ORE, ORE, INGOT, DUST);
    regMany("Limsew", "Jaffas-Technic", new String[]{"tile.jaffas.blockOfLimsew", "tile.jaffas.limsewOre", "item.jaffas.limsewDust"}, new int[]{0,0,0}, BLOCK, ORE, GEM);
  }

}

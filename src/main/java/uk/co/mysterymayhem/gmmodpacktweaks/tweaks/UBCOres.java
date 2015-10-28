/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks;

import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict;

/**
 *
 * @author Thomas
 */
public class UBCOres extends Tweak{

  public UBCOres() {
    super("ubcores");
  }
  
  @Override
  public void postInit(FMLPostInitializationEvent event) {
    if (!this.isModLoaded()) return;
    
    String[] dictNames = new String[]{"Shadow", "OreShadow", "Electrotine", "projectred.exploration.ore.6", "Vinteum", "arsmagica2:ores", "Chimerite", "arsmagica2:ores.1", "BlueTopaz", "arsmagica2:ores.2", "Moonstone", "arsmagica2:ores.3", "Dilithium", "dilithium_ore", "Tritanium", "tritanium_ore", "Fossil", "fossil", "AncientAmber", "amberOre"};
    String[] blockPrefixes = new String[]{"igneous_", "metamorphic_", "sedimentary_"};
    int maxMeta = 7;
    
    for (int dictNameIndex = 0; dictNameIndex < dictNames.length; dictNameIndex++) {
      // Gets 0, then increments to 1 for getting suffix
      String dictName = dictNames[dictNameIndex++];
      String blockSuffix = dictNames[dictNameIndex];
      for (String blockPrefix : blockPrefixes) {
        for (int meta = 0; meta <= maxMeta; meta++) {
          OreDict.register(OreDict.DictType.ORE, dictName, MODID, blockPrefix + blockSuffix, meta);
        }
      }
    }
    
//    Block findBlock = GameRegistry.findBlock(MODID, "igneous_OreShadow");
//    if (findBlock != null){
//      findBlock.setBlockName("UBC_Ore_Shadow_Igneous");
//    }
  }
}

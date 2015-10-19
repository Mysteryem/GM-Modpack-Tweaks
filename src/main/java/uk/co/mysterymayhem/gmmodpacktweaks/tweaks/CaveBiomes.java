/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks;

import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 *
 * @author Thomas
 */
public class CaveBiomes extends Tweak{
  public CaveBiomes() {
    this.MODID = "CaveBiomes";
  }
  
  @Override
  public void postInit(FMLPostInitializationEvent event) {
    if (!this.isModLoaded()) return;
    Block sedimentarySand = GameRegistry.findBlock("CaveBiomes", "sedimentarySand");
    Block mossyIgneousStone = GameRegistry.findBlock("CaveBiomes", "mossy_igneous_stone");
    Block mossyMetamorphicStone = GameRegistry.findBlock("CaveBiomes", "mossy_metamorphic_stone");
    Block mossySedimentaryStone = GameRegistry.findBlock("CaveBiomes", "mossy_sedimentary_stone");
    
    for (int i = 0; i < 8; i++) {
      OreDictionary.registerOre("sand", new ItemStack(sedimentarySand, 1, i));
      OreDictionary.registerOre("stoneMossy", new ItemStack(mossyIgneousStone, 1, i));
      OreDictionary.registerOre("stoneMossy", new ItemStack(mossyMetamorphicStone, 1, i));
      OreDictionary.registerOre("stoneMossy", new ItemStack(mossySedimentaryStone, 1, i));
    }
  }
}

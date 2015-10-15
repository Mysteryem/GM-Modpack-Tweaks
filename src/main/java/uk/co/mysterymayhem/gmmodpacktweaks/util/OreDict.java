/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.util;

import cpw.mods.fml.common.registry.GameRegistry;
import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.Misc.log;

/**
 *
 * @author Thomas
 */
public class OreDict {
  public static enum DictType {
      INGOT(false),
      ORE(true),
      BLOCK(true),
      DUST(false),
      ITEM(false),
      GEM(false);
      
      final private boolean block;
      
      private DictType(boolean isBlock) {
        this.block = isBlock;
      }
      
      @Override
      public String toString() {
        return this.name().toLowerCase(Locale.ENGLISH);
      }
      
      public boolean isBlock() {
        return this.block;
      }
    }
  public static void regMany(String oreDictBaseName, String MODID, String[] names, int[] metadata, DictType... types) {
      if (names.length != metadata.length) {
        log("Failed for " + oreDictBaseName + " as names.length != metadata.length");
        return;
      }
      if (types.length == 1) {
        for (int i = 0; i < names.length; i++) {
          register(types[0], oreDictBaseName, MODID, names[i], metadata[i]);
        }
      } else {
        for (int i = 0; i < types.length; i++) {
          register(types[i], oreDictBaseName, MODID, names[i], metadata[i]);
        }
      }
    }
  
    
    /**
     * 
   * @param type
     * @param dictName name to register.
     * @param MODID mod id that block/item belongs to.
     * @param name name of block/item.
     * @param metadata metadata of block/item.
     */
    public static void register(DictType type, String dictName, String MODID, String name, int metadata) {
      ItemStack stack;
      if (type.isBlock()) {
        Block findBlock = GameRegistry.findBlock(MODID, name);
        if (findBlock == null) {
          log("Could not find Block <" + MODID + ":" + name + ":?>");
          return;
        }
        
        stack = new ItemStack(findBlock, 1, metadata);
      } else {
        Item findItem = GameRegistry.findItem(MODID, name);
        if (findItem == null) {
          log("Could not find Item <" + MODID + ":" + name + ":?>");
          return;
        }
        stack = new ItemStack(findItem, 1, metadata);
      }
      // e.g. ingot + Iron -> ingotIron
      String fullName = type + dictName;
      
      // Check if already registered under this name
      int[] oreIDs = OreDictionary.getOreIDs(stack);
      for (int oreID : oreIDs) {
        if (OreDictionary.getOreName(oreID).equals(fullName)) {
          // ItemStack is already registered under this name, cancelling
          log(MODID + ":" + name + (metadata != 0 ? (":" + metadata) : "") + " is already registered as " + fullName);
          return;
        }
      }
      OreDictionary.registerOre(type + dictName, stack);
      log("Registered <" + MODID + ":" + name + (metadata != 0 ? (":" + metadata) : "") + "> as " + fullName);
      
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.util;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/**
 *
 * @author Thomas
 */
public class Renamer {
  public static void renameBlock(Block block, String newName) {
    block.setBlockName(newName);
  }
}

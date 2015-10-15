/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs;

import exterminatorJeff.undergroundBiomes.api.UBStoneCodes;
import net.minecraft.world.World;

/**
 *
 * @author Thomas
 */
public interface IBlockChanger {
  void changeBlock(UBStoneCodes strata, World world, int x, int y, int z);
}

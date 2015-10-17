package uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs;

import exterminatorJeff.undergroundBiomes.api.UBStoneCodes;
import net.minecraft.world.World;

/**
 * Used to provide an interface for block changing with lambda expressions.
 * @author Mysteryem
 */
@FunctionalInterface
public interface IBlockChanger {
  void changeBlock(UBStoneCodes strata, World world, int x, int y, int z);
}

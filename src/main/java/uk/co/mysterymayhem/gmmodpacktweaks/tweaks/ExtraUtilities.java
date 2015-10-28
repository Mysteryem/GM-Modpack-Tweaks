/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks;

import com.rwtema.extrautils.ExtraUtils;
import com.rwtema.extrautils.modintegration.TConIntegration;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author Thomas
 */
public class ExtraUtilities extends Tweak{
  
  //private static final int UNSTABLE_MATERIAL_ID = 314;
  

  public ExtraUtilities() {
    super("ExtraUtilities");
  }

  @Override
  public void init(FMLInitializationEvent event) {
    if (this.isModLoaded()) {
      NBTTagCompound tag = new NBTTagCompound();
      tag.setString("FluidName", TConIntegration.unstable.getName());
      tag.setInteger("MaterialId", ExtraUtils.tcon_unstable_material_id);
      FMLInterModComms.sendMessage("TConstruct", "addPartCastingMaterial", tag);
    }
  }
  
}

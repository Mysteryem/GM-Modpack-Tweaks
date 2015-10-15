/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.util;

import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import static uk.co.mysterymayhem.gmmodpacktweaks.GMModpackTweaks.MODID;

/**
 *
 * @author Thomas
 */
public class Misc {
  public static void log(String message) {
      FMLLog.log(MODID, Level.INFO, message);
    }
}

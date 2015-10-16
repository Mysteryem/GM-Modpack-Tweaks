/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;

/**
 *
 * @author Thomas
 */
public class ChunkRef implements Serializable {

  private static final HashMap<Integer, HashMap<Integer, ChunkRef>> allRefs = new HashMap<>();

  public static ChunkRef get(int x, int z) {
    HashMap<Integer, ChunkRef> get = allRefs.get(x);
    if (get == null) {
      get = new HashMap<>();
      allRefs.put(x, get);
    }
    ChunkRef ref = get.get(z);
    if (ref == null) {
      ref = new ChunkRef(x, z);
      get.put(z, ref);
    }
    return ref;
  }

  public static ChunkRef get(Chunk chunk) {
    return get(chunk.xPosition, chunk.zPosition);
  }

  public static ChunkRef remove(int x, int z) {
    HashMap<Integer, ChunkRef> get = allRefs.get(x);
    if (get == null) {
      // No ChunkRefs exist for this x value
      return null;
    }
    return get.remove(z);
  }

  public static ChunkRef remove(Chunk chunk) {
    return remove(chunk.xPosition, chunk.zPosition);
  }

  public static ChunkRef remove(ChunkRef ref) {
    return remove(ref.x, ref.z);
  }

  public static ChunkRef get(PopulateChunkEvent.Post event) {
    return get(event.chunkX, event.chunkZ);
  }

//    public static boolean isTracked(int x, int z) {
//      
//    }
  public static ArrayList<ChunkRef> getAll() {
    ArrayList<ChunkRef> valid = new ArrayList<>();
    //ArrayList<ChunkRef> invalid = new ArrayList<>();
    allRefs.values().stream().forEach((firstLayer) -> {
      firstLayer.values().stream().forEach((ref) -> {
        //          if (ref.allGenFinished && (!ref.notifyOnPostPopulate.isEmpty() || !ref.waitingOn.isEmpty())) {
//            invalid.add(ref);
//          }
//          else if (ref.)
        valid.add(ref);
      });
    });
    return valid;
  }

  public static void setAll(Collection<ChunkRef> collection) {
    // Clean up
    for (HashMap<Integer, ChunkRef> inner : allRefs.values()) {
      for (ChunkRef ref : inner.values()) {
        ref.notifyOnPostPopulate.clear();
        ref.waitingOn.clear();
      }
      inner.clear();
    }
    allRefs.clear();

    // Add the new values
    for (ChunkRef ref : collection) {
      HashMap<Integer, ChunkRef> get = allRefs.get(ref.x);
      if (get == null) {
        get = new HashMap<>();
        allRefs.put(ref.x, get);
      }
      get.put(ref.z, ref);
    }
  }

  public final int x;
  public final int z;
  public boolean allGenFinished = false;
  final HashSet<ChunkRef> waitingOn = new HashSet<>();
  final HashSet<ChunkRef> notifyOnPostPopulate = /*Collections.synchronizedSet(*/ new HashSet<>()/*)*/;

  private ChunkRef(int chunkX, int chunkZ) {
    this.x = chunkX;
    this.z = chunkZ;
  }

  private ChunkRef(PopulateChunkEvent.Post event) {
    this(event.chunkX, event.chunkZ);
  }

  private ChunkRef(Chunk chunk) {
    this(chunk.xPosition, chunk.zPosition);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ChunkRef) {
      ChunkRef other = (ChunkRef) obj;
      return this.x == other.x && this.z == other.z;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + this.x;
    hash = 79 * hash + this.z;
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    return builder.append("(").append(this.x).append(", ").append(this.z).append(")").toString();
  }

  public String detailedOutput() {
    StringBuilder builder = new StringBuilder();
    builder.append(this).append(this.allGenFinished ? ", block replacement complete, " : ", yet to do block replacement, ").append("waiting on: ");
    for (ChunkRef ref : this.waitingOn) {
      builder.append(ref).append(", ");
    }
    builder.append("to notify: ");
    for (ChunkRef ref : this.notifyOnPostPopulate) {
      builder.append(ref).append(", ");
    }
    return builder.toString();
  }

}

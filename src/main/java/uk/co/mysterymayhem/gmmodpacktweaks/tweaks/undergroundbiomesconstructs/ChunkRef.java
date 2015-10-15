/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;

/**
 *
 * @author Thomas
 */
  public class ChunkRef {

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

    public final int x;
    public final int z;
    final HashSet<ChunkRef> waitingOn = new HashSet<>();
    final Set<ChunkRef> notifyOnPostPopulate = Collections.synchronizedSet(new HashSet<>());

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

  }

package me.ethernova.ultimateffa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

public class BlockRestoreManager {
   private final Main plugin;
   private final Map<String, List<BlockRestoreManager.BlockSnapshot>> changes = new HashMap();

   public BlockRestoreManager(Main plugin) {
      this.plugin = plugin;
   }

   public void recordBlock(String arenaId, Block block) {
      this.changes.computeIfAbsent(arenaId, (k) -> {
         return new ArrayList();
      });
      ((List)this.changes.get(arenaId)).add(new BlockRestoreManager.BlockSnapshot(block.getLocation(), block.getType(), block.getBlockData()));
   }

   public void recordBlockState(String arenaId, BlockState state) {
      this.changes.computeIfAbsent(arenaId, (k) -> {
         return new ArrayList();
      });
      ((List)this.changes.get(arenaId)).add(new BlockRestoreManager.BlockSnapshot(state.getLocation(), state.getType(), state.getBlockData()));
   }

   public void restoreArena(String arenaId) {
      if (this.changes.containsKey(arenaId)) {
         List<BlockRestoreManager.BlockSnapshot> snapshots = new ArrayList((Collection)this.changes.get(arenaId));
         this.changes.remove(arenaId);
         Collections.reverse(snapshots);
         Iterator var3 = snapshots.iterator();

         BlockRestoreManager.BlockSnapshot snap;
         Block b;
         while(var3.hasNext()) {
            snap = (BlockRestoreManager.BlockSnapshot)var3.next();
            b = snap.loc.getBlock();
            b.setType(snap.material, false);
            b.setBlockData(snap.data, false);
         }

         var3 = snapshots.iterator();

         while(var3.hasNext()) {
            snap = (BlockRestoreManager.BlockSnapshot)var3.next();
            b = snap.loc.getBlock();
            b.setType(snap.material, true);
            b.setBlockData(snap.data, true);
         }

      }
   }

   public boolean hasChanges(String arenaId) {
      return this.changes.containsKey(arenaId) && !((List)this.changes.get(arenaId)).isEmpty();
   }

   public void restoreAll() {
      Iterator var1 = (new ArrayList(this.changes.keySet())).iterator();

      while(var1.hasNext()) {
         String arena = (String)var1.next();
         this.restoreArena(arena);
      }

   }

   private static class BlockSnapshot {
      Location loc;
      Material material;
      BlockData data;

      public BlockSnapshot(Location loc, Material material, BlockData data) {
         this.loc = loc;
         this.material = material;
         this.data = data;
      }
   }
}

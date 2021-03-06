package com.nukkitx.fakeinventories;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import com.nukkitx.fakeinventories.inventory.FakeInventories;
import com.nukkitx.fakeinventories.inventory.FakeInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeInventoriesListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDataPacketSend(DataPacketSendEvent event) {
        DataPacket packet = event.getPacket();
        if (packet instanceof UpdateBlockPacket) {
            UpdateBlockPacket updateBlock = (UpdateBlockPacket) packet;
            BlockVector3[] positions = FakeInventories.getFakeInventoryPositions(event.getPlayer());
            if (positions != null) {
                for (BlockVector3 blockVector : positions) {
                    if (blockVector.x == updateBlock.x && blockVector.y == updateBlock.y && blockVector.z == updateBlock.z) {
                        event.setCancelled();
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryTransaction(InventoryTransactionEvent event) {
        Map<FakeInventory, List<SlotChangeAction>> actions = new HashMap<>();
        Player source = event.getTransaction().getSource();

        for (InventoryAction action : event.getTransaction().getActions()) {
            if (action instanceof SlotChangeAction) {
                SlotChangeAction slotChange = (SlotChangeAction) action;
                if (slotChange.getInventory() instanceof FakeInventory) {
                    FakeInventory inventory = (FakeInventory) slotChange.getInventory();
                    List<SlotChangeAction> slotChanges = actions.computeIfAbsent(inventory, __ -> new ArrayList<>());

                    slotChanges.add(slotChange);
                }
            }
        }

        boolean cancel = false;
        for (Map.Entry<FakeInventory, List<SlotChangeAction>> entry : actions.entrySet()) {
            for (SlotChangeAction action : entry.getValue()) {
                if (entry.getKey().onSlotChange(source, action)) {
                    cancel = true;
                }
            }
        }

        if (cancel) {
            event.setCancelled();
        }
    }
}

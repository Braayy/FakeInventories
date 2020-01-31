package com.nukkitx.fakeinventories.inventory;

import cn.nukkit.Player;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.math.BlockVector3;
import com.nukkitx.fakeinventories.inventory.inventories.*;

import java.util.Optional;

public class FakeInventories {

    public static BlockVector3[] getFakeInventoryPositions(Player player) {
        return getFakeInventory(player).map(inventory -> inventory.getPosition(player)).orElse(null);
    }

    public static Optional<FakeInventory> getFakeInventory(Player player) {
        return Optional.ofNullable(FakeInventory.open.get(player));
    }

    public static FakeInventory createFakeInventory(InventoryType type, InventoryHolder holder, String title) {
        switch (type) {
            case CHEST:
                return new ChestFakeInventory(InventoryType.CHEST, holder, title);
            case DOUBLE_CHEST:
                return new DoubleChestFakeInventory(holder, title);
            case HOPPER:
                return new HopperFakeInventory(holder, title);
            case DISPENSER:
                return new DispenserFakeInventory(holder, title);
            case DROPPER:
                return new DropperFakeInventory(holder, title);
            default:
                throw new RuntimeException(type.name() + " is not supported.");
        }
    }

}

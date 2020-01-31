package com.nukkitx.fakeinventories.inventory.inventories;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.math.BlockVector3;
import com.nukkitx.fakeinventories.inventory.FakeInventory;

public final class HopperFakeInventory extends FakeInventory {

    public HopperFakeInventory(InventoryHolder holder, String title) {
        super(InventoryType.HOPPER, holder, title);
    }

    @Override
    protected BlockVector3[] onOpenBlock(Player who) {
        BlockVector3 blockPosition = new BlockVector3(who.getFloorX(), who.getFloorY() + 2, who.getFloorZ());

        placeBlock(who, blockPosition, BlockID.HOPPER_BLOCK, BlockEntity.HOPPER);

        return new BlockVector3[] { blockPosition };
    }

}
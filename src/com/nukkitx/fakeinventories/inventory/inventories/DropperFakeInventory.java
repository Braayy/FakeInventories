package com.nukkitx.fakeinventories.inventory.inventories;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.math.BlockVector3;
import com.nukkitx.fakeinventories.inventory.FakeInventory;

public final class DropperFakeInventory extends FakeInventory {

    public DropperFakeInventory(InventoryHolder holder, String title) {
        super(InventoryType.DROPPER, holder, title);
    }

    @Override
    protected BlockVector3[] onOpenBlock(Player who) {
        BlockVector3 blockPosition = new BlockVector3(who.getFloorX(), who.getFloorY() + BLOCK_OFFSET, who.getFloorZ());

        placeBlock(who, blockPosition, BlockID.DROPPER, BlockEntity.DROPPER);

        return new BlockVector3[] { blockPosition };
    }

}
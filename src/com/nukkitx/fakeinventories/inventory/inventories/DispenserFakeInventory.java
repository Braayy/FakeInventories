package com.nukkitx.fakeinventories.inventory.inventories;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.math.BlockVector3;
import com.nukkitx.fakeinventories.inventory.FakeInventory;

public final class DispenserFakeInventory extends FakeInventory {

    public DispenserFakeInventory(InventoryHolder holder, String title) {
        super(InventoryType.DISPENSER, holder, title);
    }

    @Override
    protected BlockVector3[] onOpenBlock(Player who) {
        BlockVector3 blockPosition = new BlockVector3(who.getFloorX(), who.getFloorY() + BLOCK_OFFSET, who.getFloorZ());

        placeBlock(who, blockPosition, BlockID.DISPENSER, BlockEntity.DISPENSER);

        return new BlockVector3[] { blockPosition };
    }

}
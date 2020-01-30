package com.nukkitx.fakeinventories.inventory;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BlockEntityDataPacket;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public final class DoubleChestFakeInventory extends ChestFakeInventory {

    DoubleChestFakeInventory(InventoryHolder holder, String title) {
        super(InventoryType.DOUBLE_CHEST, holder, title);
    }

    @Override
    public void onOpen(Player who) {
        this.viewers.add(who);

        BlockVector3[] blocks = onOpenBlock(who);
        blockPositions.put(who, blocks);

        Server.getInstance().getScheduler().scheduleDelayedTask(() -> {
            onFakeOpen(who, blocks);
        }, 5);
    }

    @Override
    protected BlockVector3[] onOpenBlock(Player who) {
        BlockVector3 blockPositionA = new BlockVector3(who.getFloorX(), who.getFloorY() + 2, who.getFloorZ());
        BlockVector3 blockPositionB = blockPositionA.add(1, 0, 0);

        placeChest(who, blockPositionA);
        placeChest(who, blockPositionB);

        pair(who, blockPositionA, blockPositionB);
        pair(who, blockPositionB, blockPositionA);

        return new BlockVector3[] { blockPositionA, blockPositionB };
    }

    private void pair(Player who, BlockVector3 posA, BlockVector3 posB) {
        BlockEntityDataPacket blockEntityData = new BlockEntityDataPacket();
        blockEntityData.x = posA.x;
        blockEntityData.y = posA.y;
        blockEntityData.z = posA.z;
        blockEntityData.namedTag = getDoubleNbt(posA, posB, getTitle());

        who.dataPacket(blockEntityData);
    }

    private static byte[] getDoubleNbt(BlockVector3 pos, BlockVector3 pairPos, String name) {
        CompoundTag tag = new CompoundTag()
                .putString("id", BlockEntity.CHEST)
                .putInt("x", pos.x)
                .putInt("y", pos.y)
                .putInt("z", pos.z)
                .putInt("pairx", pairPos.x)
                .putInt("pairz", pairPos.z)
                .putString("CustomName", name == null ? "Chest" : name);

        try {
            return NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create NBT for chest");
        }
    }
}

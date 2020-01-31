package com.nukkitx.fakeinventories.inventory;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.inventory.ContainerInventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BlockEntityDataPacket;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class FakeInventory extends ContainerInventory {
    private static final BlockVector3 ZERO = new BlockVector3(0, 0, 0);
    protected static final int BLOCK_OFFSET = -5;

    static final Map<Player, FakeInventory> open = new ConcurrentHashMap<>();

    protected final Map<Player, BlockVector3[]> blockPositions = new HashMap<>();
    private final List<Consumer<FakeSlotChangeEvent>> listeners = new CopyOnWriteArrayList<>();
    private boolean closed = false;
    private String title;

    protected FakeInventory(InventoryType type, InventoryHolder holder, String title) {
        super(holder, type);

        this.title = title == null ? type.getDefaultTitle() : title;
    }

    @Override
    public void onOpen(Player who) {
        checkForClosed();
        this.viewers.add(who);
        if (open.putIfAbsent(who, this) != null) {
            throw new IllegalStateException("Inventory was already open");
        }

        BlockVector3[] blocks = onOpenBlock(who);
        blockPositions.put(who, blocks);

        onFakeOpen(who, blocks);
    }

    protected void onFakeOpen(Player who, BlockVector3[] blocks) {
        BlockVector3 blockPosition = blocks.length > 0 ? blocks[0] : ZERO;

        try {
            ContainerOpenPacket containerOpen = new ContainerOpenPacket();
            containerOpen.windowId = who.getWindowId(this);
            containerOpen.type = this.getType().getNetworkType();
            containerOpen.x = blockPosition.x;
            containerOpen.y = blockPosition.y;
            containerOpen.z = blockPosition.z;

            who.dataPacket(containerOpen);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Server.getInstance().getScheduler().scheduleDelayedTask(() -> {
            this.sendContents(who);
        }, 5);
    }

    protected abstract BlockVector3[] onOpenBlock(Player who);

    @Override
    public void onClose(Player who) {
        super.onClose(who);
        open.remove(who, this);

        BlockVector3[] blocks = blockPositions.get(who);

        for (int i = 0; i < blocks.length; i++) {
            final int index = i;
            Server.getInstance().getScheduler().scheduleDelayedTask(() -> {
                Vector3 blockPosition = blocks[index].asVector3();
                UpdateBlockPacket updateBlock = new UpdateBlockPacket();
                updateBlock.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(who.getLevel().getBlock(blockPosition).getFullId());
                updateBlock.flags = UpdateBlockPacket.FLAG_ALL_PRIORITY;
                updateBlock.x = blockPosition.getFloorX();
                updateBlock.y = blockPosition.getFloorY();
                updateBlock.z = blockPosition.getFloorZ();

                who.dataPacket(updateBlock);
            }, 2 + i, false);
        }
    }

    public BlockVector3[] getPosition(Player player) {
        checkForClosed();

        return blockPositions.getOrDefault(player, null);
    }

    public void addListener(Consumer<FakeSlotChangeEvent> listener) {
        Preconditions.checkNotNull(listener);
        checkForClosed();
        listeners.add(listener);
    }

    public void removeListener(Consumer<FakeSlotChangeEvent> listener) {
        checkForClosed();
        listeners.remove(listener);
    }

    public boolean onSlotChange(Player source, SlotChangeAction action) {
        if (!listeners.isEmpty()) {
            FakeSlotChangeEvent event = new FakeSlotChangeEvent(source, this, action);
            for (Consumer<FakeSlotChangeEvent> listener : listeners) {
                listener.accept(event);
            }
            return event.isCancelled();
        }
        return false;
    }

    private void checkForClosed() {
        Preconditions.checkState(!closed, "Already closed");
    }

    void close() {
        checkForClosed();
        getViewers().forEach(player -> player.removeWindow(this));
        closed = true;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setTitle(String title) {
        if (title == null) {
            this.title = type.getDefaultTitle();
        } else {
            this.title = title;
        }
    }

    protected void placeBlock(Player who, BlockVector3 pos, int blockId, String blockEntityId) {
        UpdateBlockPacket updateBlock = new UpdateBlockPacket();
        updateBlock.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(blockId, 0);
        updateBlock.flags = UpdateBlockPacket.FLAG_ALL_PRIORITY;
        updateBlock.x = pos.x;
        updateBlock.y = pos.y;
        updateBlock.z = pos.z;

        who.dataPacket(updateBlock);

        BlockEntityDataPacket blockEntityData = new BlockEntityDataPacket();
        blockEntityData.x = pos.x;
        blockEntityData.y = pos.y;
        blockEntityData.z = pos.z;
        blockEntityData.namedTag = getNbt(pos, blockEntityId, getTitle());

        who.dataPacket(blockEntityData);
    }

    protected static byte[] getNbt(BlockVector3 pos, String blockEntityId, String name) {
        CompoundTag tag = new CompoundTag()
                .putString("id", blockEntityId)
                .putInt("x", pos.x)
                .putInt("y", pos.y)
                .putInt("z", pos.z)
                .putString("CustomName", name == null ? blockEntityId : name);

        try {
            return NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create NBT for " + blockEntityId);
        }
    }
}

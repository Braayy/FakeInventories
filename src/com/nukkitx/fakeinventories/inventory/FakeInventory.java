package com.nukkitx.fakeinventories.inventory;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.inventory.ContainerInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.InventoryContentPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import com.google.common.base.Preconditions;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class FakeInventory extends ContainerInventory {
    private static final BlockVector3 ZERO = new BlockVector3(0, 0, 0);

    private static Field WINDOW_CNT_FIELD, WINDOWS_FIELD;

    static {
        try {
            WINDOW_CNT_FIELD = Player.class.getDeclaredField("windowCnt");
            WINDOW_CNT_FIELD.setAccessible(true);

            WINDOWS_FIELD = Player.class.getDeclaredField("windows");
            WINDOWS_FIELD.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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

    @SuppressWarnings("unchecked")
    protected void onFakeOpen(Player who, BlockVector3[] blocks) {
        BlockVector3 blockPosition = blocks.length > 0 ? blocks[0] : ZERO;

        try {
            final int windowCnt = WINDOW_CNT_FIELD.getInt(who);
            final int windowId = Math.max(4, (windowCnt + 1) % 99);

            WINDOW_CNT_FIELD.setInt(who, windowId);

            final Map<Inventory, Integer> windows = (Map<Inventory, Integer>) WINDOWS_FIELD.get(who);

            windows.put(this, windowId);

            ContainerOpenPacket containerOpen = new ContainerOpenPacket();
            containerOpen.windowId = windowId;
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

    @Override
    public void sendContents(Player... players) {
        InventoryContentPacket pk = new InventoryContentPacket();
        pk.slots = new Item[this.getSize()];
        for (int i = 0; i < this.getSize(); ++i) {
            pk.slots[i] = this.getItem(i);
        }

        for (Player player : players) {
            int id = player.getWindowId(this);
            if (!player.spawned) {
                this.close(player);
                continue;
            }
            pk.inventoryId = id;
            player.dataPacket(pk);
        }
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
        Preconditions.checkState(!closed, "Already closed");
        getViewers().forEach(player -> player.removeWindow(this));
        closed = true;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null) {
            this.title = type.getDefaultTitle();
        } else {
            this.title = title;
        }
    }
}

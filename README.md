# Fake Inventories

Easily create fake inventories that players can interact with.

##### [Download](https://github.com/Braayy/FakeInventories/releases)

## Usage

```java
    FakeInventory inventory = FakeInventories.createFakeInventory(InventoryType.CHEST, null, "Testing...");

    inventory.addItem(Item.get(Item.BAKED_POTATO));

    inventory.addListener(event -> {
        event.getPlayer().sendMessage("You cliked on the inventory :D");    
    })

    player.addWindow(inventory);
``` 

## TODO
- Add a `FakeInventory#update()` to update a inventory to all viewers.
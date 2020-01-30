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

    inventory.open(player);
``` 

## Todo

- Add Ender Chest support
- Add Furnace support
- Add Crafting Table support
- Add Anvil support
- Add Dispenser support
- Add Dropper support
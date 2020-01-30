package com.nukkitx.fakeinventories;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.service.ServicePriority;
import com.nukkitx.fakeinventories.inventory.FakeInventories;

public class FakeInventoriesPlugin extends PluginBase {

    @Override
    public void onEnable() {
        // register listener
        getServer().getPluginManager().registerEvents(new FakeInventoriesListener(), this);
    }

}

package com.nukkitx.fakeinventories;

import cn.nukkit.plugin.PluginBase;

public class FakeInventoriesPlugin extends PluginBase {

    @Override
    public void onEnable() {
        // register listener
        getServer().getPluginManager().registerEvents(new FakeInventoriesListener(), this);
    }

}

/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IServerListAdditionalMethods;
import meteordevelopment.meteorclient.mixininterface.ISyncedServerInfo;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.UUID;

@Mixin(ServerList.class)
public class ServerListMixin implements IServerListAdditionalMethods {


    @Shadow
    @Final
    private List<ServerInfo> servers;

    @Shadow
    @Final
    private List<ServerInfo> hiddenServers;

    @Override
    public @Nullable ServerInfo get(UUID id) {
        for (ServerInfo serverInfo : this.servers) {
            var serverId = ((ISyncedServerInfo) serverInfo).getId();
            if (serverId != null && serverId.equals(id)) {
                return serverInfo;
            }
        }

        for (ServerInfo serverInfo : this.hiddenServers) {
            var serverId = ((ISyncedServerInfo) serverInfo).getId();
            if (serverId != null && serverId.equals(id)) {
                return serverInfo;
            }
        }

        return null;
    }
}

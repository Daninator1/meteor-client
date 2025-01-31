/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package meteordevelopment.meteorclient.systems.friends;

import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.misc.SyncedServerInfo;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.LOG;

public class ServerSync extends System<ServerSync> {

    public boolean enabled = false;
    public String server = "";
    public String apiKey = "";
    public SyncedServerInfo[] syncedServerInfos;

    public ServerSync() {
        super("server-sync");
    }

    @Override
    public void init() {
        this.syncedServerInfos = new SyncedServerInfo[0];
    }

    public static ServerSync get() {
        return Systems.get(ServerSync.class);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putBoolean("enabled", this.enabled);
        tag.putString("server", this.server);
        tag.putString("apiKey", this.apiKey);
        return tag;
    }

    @Override
    public ServerSync fromTag(NbtCompound tag) {
        this.enabled = tag.getBoolean("enabled");
        this.server = tag.getString("server");
        this.apiKey = tag.getString("apiKey");
        return this;
    }

    public SyncedServerInfo[] getServers() {
        try {
            SyncedServerInfo[] response = Http
                .get(this.server + "/server")
                .apiKey(this.apiKey)
                .sendJson(SyncedServerInfo[].class);

            if (response == null) return new SyncedServerInfo[0];

            return response;
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

        return new SyncedServerInfo[0];
    }

    public void addOrUpdateServer(SyncedServerInfo serverInfo) {
        try {
            Http.post(this.server + "/server")
                .apiKey(this.apiKey)
                .bodyJson(serverInfo)
                .send();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    public void removeServer(UUID id) {
        try {
            Http.delete(this.server + "/server/" + id)
                .apiKey(this.apiKey)
                .send();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }
}

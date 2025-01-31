/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package meteordevelopment.meteorclient.systems.friends;

import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.PlayStatusEntry;
import meteordevelopment.meteorclient.utils.misc.PlayStatusPosition;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;

import static meteordevelopment.meteorclient.MeteorClient.LOG;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PlayStatus extends System<PlayStatus> {

    public boolean enabled = false;
    public String name = "";
    public String server = "";
    public String apiKey = "";
    public int updateIntervalInSeconds = 10;
    public PlayStatusEntry[] playStatusEntries;
    private float counter;

    public PlayStatus() {
        super("play-status");
    }

    @Override
    public void init() {
        this.counter = 0;
        this.playStatusEntries = new PlayStatusEntry[0];
    }

    public static PlayStatus get() {
        return Systems.get(PlayStatus.class);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putBoolean("enabled", this.enabled);
        tag.putString("name", this.name);
        tag.putString("server", this.server);
        tag.putString("apiKey", this.apiKey);
        tag.putInt("updateInterval", this.updateIntervalInSeconds);
        return tag;
    }

    @Override
    public PlayStatus fromTag(NbtCompound tag) {
        this.enabled = tag.getBoolean("enabled");
        this.name = tag.getString("name");
        this.server = tag.getString("server");
        this.apiKey = tag.getString("apiKey");
        this.updateIntervalInSeconds = tag.getInt("updateInterval");
        return this;
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        if (!this.enabled) return;
        this.sendOwnPlayStatus();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (!this.enabled) return;
        this.removePlayStatus();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        var tickRate = TickRate.INSTANCE.getTickRate();
        if (!this.enabled || tickRate == 0.0) return;

        var secondsPassed = 1.0f / tickRate;
        this.counter += secondsPassed;

        if (this.counter >= this.updateIntervalInSeconds) {
            this.counter = 0;
            MeteorExecutor.execute(() -> {
                this.sendOwnPlayStatus();
                var entries = this.fetchPlayStatusEntries();
                synchronized (this) {
                    this.playStatusEntries = entries;
                }
            });
        }
    }

    public PlayStatusEntry[] fetchPlayStatusEntries() {
        try {
            PlayStatusEntry[] response = Http
                .get(server + "/playstatus")
                .apiKey(this.apiKey)
                .sendJson(PlayStatusEntry[].class);

            if (response == null) return new PlayStatusEntry[0];

            return response;
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

        return new PlayStatusEntry[0];
    }

    private void sendOwnPlayStatus() {
        PlayStatusEntry entry = null;
        if (mc.player != null && mc.world != null) {
            entry = new PlayStatusEntry(
                this.name,
                mc.getSession().getUsername(),
                Utils.getWorldName(),
                new PlayStatusPosition(mc.player.getX(), mc.player.getY(), mc.player.getZ()),
                mc.world.getDimensionEntry().getIdAsString()
            );
        }

        try {
            Http.post(server + "/playstatus")
                .apiKey(this.apiKey)
                .bodyJson(entry)
                .send();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    private void removePlayStatus() {
        try {
            Http.delete(server + "/playstatus/" + this.name)
                .apiKey(this.apiKey)
                .send();
            LOG.debug("Removed current play status");
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }
}

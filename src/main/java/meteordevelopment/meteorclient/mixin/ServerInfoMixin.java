/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.ISyncedServerInfo;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ServerInfo.class)
public class ServerInfoMixin implements ISyncedServerInfo {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Unique
    private UUID id;

    @Inject(method = "toNbt", at = @At("TAIL"))
    private void onToNbt(CallbackInfoReturnable<NbtCompound> cir) {
        var nbtCompound = cir.getReturnValue();

        if (this.id != null) {
            nbtCompound.putString("id", this.id.toString());

        }
    }

    @Inject(method = "fromNbt", at = @At("TAIL"))
    private static void onFromNbt(NbtCompound root, CallbackInfoReturnable<ServerInfo> cir) {
        var serverInfo = cir.getReturnValue();

        if (root.contains("id", 8)) {
            try {
                ((ISyncedServerInfo) serverInfo).setId(UUID.fromString(root.getString("id")));
            } catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.warn("Malformed server id", illegalArgumentException);
            }
        }
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void onCopyFrom(ServerInfo serverInfo, CallbackInfo ci) {
        this.id = ((ISyncedServerInfo) serverInfo).getId();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}



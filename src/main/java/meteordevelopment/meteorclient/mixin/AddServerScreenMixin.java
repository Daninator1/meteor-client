/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.ISyncedServerInfo;
import meteordevelopment.meteorclient.systems.friends.ServerSync;
import meteordevelopment.meteorclient.utils.misc.SyncedServerInfo;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(AddServerScreen.class)
public class AddServerScreenMixin extends Screen {

    @Shadow
    private TextFieldWidget serverNameField;

    @Shadow
    @Final
    private ServerInfo server;

    @Unique
    private Boolean syncWithServer = false;

    protected AddServerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        this.addDrawableChild(
            new ButtonWidget.Builder(Text.literal("Use last"), button -> ((AddServerScreenAccessor) this).getAddressField().setText(this.client != null ? this.client.options.lastServer : ""))
                .position(this.width / 2 + 104, 106)
                .size(50, 20)
                .build()
        );

        if (ServerSync.get().enabled) {
            this.syncWithServer = this.server != null && ((ISyncedServerInfo) this.server).getId() != null;

            // if this is already true, it means that the server is already synced which cannot be changed except by removing the server
            if (this.syncWithServer) {
                var syncedText = "✅ Synced";
                this.addDrawableChild(
                    new TextWidget(
                        this.width / 2 + 104,
                        71,
                        this.textRenderer.getWidth(syncedText),
                        this.textRenderer.fontHeight,
                        Text.literal(syncedText),
                        this.textRenderer)
                );
            } else {
                this.addDrawableChild(
                    CheckboxWidget.builder(Text.literal("Sync"), this.textRenderer)
                        .pos(this.width / 2 + 104, 68)
                        .callback((checkboxWidget, isChecked) -> this.syncWithServer = isChecked)
                        .checked(this.syncWithServer)
                        .build()
                );
            }
        }
    }

    @Inject(
        method = "init",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screen/multiplayer/AddServerScreen;serverNameField:Lnet/minecraft/client/gui/widget/TextFieldWidget;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER)
    )
    private void onInitIncreaseServerNameMaxLength(CallbackInfo info) {
        this.serverNameField.setMaxLength(1000);
    }

    @Inject(method = "addAndClose", at = @At("HEAD"))
    private void onAddAndClose(CallbackInfo info) {
        if (!ServerSync.get().enabled) return;

        if (this.syncWithServer) {
            if (((ISyncedServerInfo) this.server).getId() == null) {
                ((ISyncedServerInfo) this.server).setId(UUID.randomUUID());
            }

            MeteorExecutor.execute(() -> ServerSync.get().addOrUpdateServer(new SyncedServerInfo(((ISyncedServerInfo) this.server).getId(), this.server.name, this.server.address)));
        } else {
            ((ISyncedServerInfo) this.server).setId(null);
        }
    }
}

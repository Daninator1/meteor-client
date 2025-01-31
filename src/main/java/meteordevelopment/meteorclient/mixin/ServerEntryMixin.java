/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.ISyncedServerInfo;
import meteordevelopment.meteorclient.systems.friends.PlayStatus;
import meteordevelopment.meteorclient.systems.friends.ServerSync;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public class ServerEntryMixin {

    @Shadow
    @Nullable
    private Identifier statusIconTexture;
    @Shadow
    @Final
    private ServerInfo server;
    @Shadow
    @Final
    private MultiplayerScreen screen;
    private int indexModifier;

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;Lnet/minecraft/client/network/ServerInfo;)V")
    private void init(MultiplayerServerListWidget multiplayerServerListWidget, MultiplayerScreen screen, ServerInfo server, CallbackInfo ci) {
        this.indexModifier = GetIndexModifier();
    }

    @ModifyVariable(method = "render(Lnet/minecraft/client/gui/DrawContext;IIIIIIIZF)V", at = @At("HEAD"), ordinal = 0)
    private int onRenderHead(int index) {
        return index - this.indexModifier;
    }

    @Redirect(method = "keyPressed(III)Z", at = @At(value = "INVOKE", target = "Ljava/util/List;indexOf(Ljava/lang/Object;)I", ordinal = 0))
    private int onKeyPressed(List<MultiplayerServerListWidget.Entry> children, Object object) {
        return children.indexOf(object) - this.indexModifier;
    }

    @Inject(method = "keyPressed(III)Z", at = @At(value = "INVOKE", target = "Ljava/util/List;indexOf(Ljava/lang/Object;)I", shift = At.Shift.BY, by = 2), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void OnKeyPressed2(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir, MultiplayerServerListWidget multiplayerServerListWidget, int i) {
        if (i < 0) cir.setReturnValue(true);
    }

    @Redirect(method = "mouseClicked(DDI)Z", at = @At(value = "INVOKE", target = "Ljava/util/List;indexOf(Ljava/lang/Object;)I", ordinal = 1))
    private int onMouseClicked(List<MultiplayerServerListWidget.Entry> children, Object object) {
        return children.indexOf(object) - this.indexModifier;
    }

    @ModifyArg(method = "swapEntries(II)V", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"), index = 0)
    private int onSwapEntries(int j) {
        return j + this.indexModifier;
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIIIIIIZF)V", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (!ServerSync.get().enabled) return;

        if (((ISyncedServerInfo) this.server).getId() != null) {
            int i = x + entryWidth - 10 - 5;
            int yOffset = 10;
            int width = 10;
            int height = 10;

            context.drawTexture(RenderLayer::getGuiTextured, MeteorClient.identifier("textures/cloud.png"), i, y + yOffset, 0, 0, width, height, width, height, width, height);

            if (mouseX >= i && mouseX <= i + width && mouseY >= y + yOffset && mouseY <= y + height + yOffset) {
                this.screen.setTooltip(Text.literal("Server synced"));
            }
        }
    }

    private int GetIndexModifier() {
        if (!PlayStatus.get().enabled) return 0;

        var playStatusEntries = PlayStatus.get().fetchPlayStatusEntries();
        if (playStatusEntries == null || playStatusEntries.length == 0) return 0;
        return playStatusEntries.length + 1;
    }
}



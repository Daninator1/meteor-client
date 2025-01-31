/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.mixininterface.IServerListAdditionalMethods;
import meteordevelopment.meteorclient.mixininterface.ISyncedServerInfo;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.ServerSync;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;
import meteordevelopment.meteorclient.systems.proxies.Proxies;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import meteordevelopment.meteorclient.utils.misc.PlayStatusSeparatorEntry;
import meteordevelopment.meteorclient.utils.misc.PlayStatusServerEntry;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    @Shadow
    protected MultiplayerServerListWidget serverListWidget;

    @Shadow
    protected ButtonWidget buttonJoin;

    @Shadow
    protected abstract void connect(ServerInfo entry);

    @Shadow
    private ServerList serverList;
    @Unique
    private int textColor1;
    @Unique
    private int textColor2;

    @Unique
    private String loggedInAs;
    @Unique
    private int loggedInAsLength;

    public MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        textColor1 = Color.fromRGBA(255, 255, 255, 255);
        textColor2 = Color.fromRGBA(175, 175, 175, 255);

        loggedInAs = "Logged in as ";
        loggedInAsLength = textRenderer.getWidth(loggedInAs);

        var additionalSpacing = 0;

        if (Config.get().accountSwitcher.get()) {
            addDrawableChild(
                new ButtonWidget.Builder(Text.literal("Accounts"), button -> client.setScreen(GuiThemes.get().accountsScreen()))
                    .position(this.width - 75 - 3, 3)
                    .size(75, 20)
                    .build()
            );

            additionalSpacing = 75 + 2;

        }

        addDrawableChild(
            new ButtonWidget.Builder(Text.literal("Proxies"), button -> client.setScreen(GuiThemes.get().proxiesScreen()))
                .position(this.width - 75 - 3 - additionalSpacing, 3)
                .size(75, 20)
                .build()
        );
    }

    @Inject(method = "init", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V",
        ordinal = 0,
        shift = At.Shift.BEFORE))
    private void onInitSyncServers(CallbackInfo info) {
        if (!ServerSync.get().enabled) return;

        var syncedServerInfos = ServerSync.get().getServers();

        for (meteordevelopment.meteorclient.utils.misc.SyncedServerInfo syncedServerInfo : syncedServerInfos) {

            var existingServerInfo = ((IServerListAdditionalMethods) this.serverList).get(syncedServerInfo.id);

            if (existingServerInfo != null) {
                existingServerInfo.name = syncedServerInfo.name;
                existingServerInfo.address = syncedServerInfo.address;
            } else {
                var newServerInfo = new ServerInfo(syncedServerInfo.name, syncedServerInfo.address, ServerInfo.ServerType.OTHER);
                ((ISyncedServerInfo) newServerInfo).setId(syncedServerInfo.id);
                this.serverList.add(newServerInfo, false);
            }
        }

        this.serverList.saveFile();
    }

    @Inject(method = "removeEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/ServerList;remove(Lnet/minecraft/client/network/ServerInfo;)V"))
    private void onRemoveEntry(boolean confirmedAction, CallbackInfo ci, @Local(ordinal = 0) MultiplayerServerListWidget.Entry entry) {
        if (!ServerSync.get().enabled) return;

        var serverInfo = ((MultiplayerServerListWidget.ServerEntry) entry).getServer();
        var syncedServerInfo = (ISyncedServerInfo) serverInfo;

        MeteorExecutor.execute(() -> ServerSync.get().removeServer(syncedServerInfo.getId()));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int x = 3;
        int y = 3;

        // Logged in as
        context.drawTextWithShadow(mc.textRenderer, loggedInAs, x, y, textColor1);
        context.drawTextWithShadow(mc.textRenderer, Modules.get().get(NameProtect.class).getName(client.getSession().getUsername()), x + loggedInAsLength, y, textColor2);

        y += textRenderer.fontHeight + 2;

        // Proxy
        Proxy proxy = Proxies.get().getEnabled();

        String left = proxy != null ? "Using proxy " : "Not using a proxy";
        String right = proxy != null ? (proxy.name.get() != null && !proxy.name.get().isEmpty() ? "(" + proxy.name.get() + ") " : "") + proxy.address.get() + ":" + proxy.port.get() : null;

        context.drawTextWithShadow(mc.textRenderer, left, x, y, textColor1);
        if (right != null)
            context.drawTextWithShadow(mc.textRenderer, right, x + textRenderer.getWidth(left), y, textColor2);
    }

    @Inject(method = "connect()V", at = @At("TAIL"))
    private void onConnect(CallbackInfo info) {
        MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        if (entry instanceof PlayStatusServerEntry) {
            LanServerInfo lanServerInfo = ((PlayStatusServerEntry) entry).getLanServerEntry();
            this.connect(new ServerInfo(lanServerInfo.getMotd(), lanServerInfo.getAddressPort(), ServerInfo.ServerType.OTHER));
        }
    }

    @Inject(method = "updateButtonActivationStates()V", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onUpdateButtonActivationStates(CallbackInfo info, MultiplayerServerListWidget.Entry entry) {
        if (entry instanceof PlayStatusSeparatorEntry) {
            this.buttonJoin.active = false;
        }
    }
}

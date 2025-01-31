/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.PlayStatus;
import meteordevelopment.meteorclient.systems.friends.ServerSync;
import net.minecraft.client.gui.screen.Screen;

public class ServerFeaturesTab extends Tab {
    public ServerFeaturesTab() {
        super("Server Features");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new PlayStatusScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof PlayStatusScreen;
    }

    private static class PlayStatusScreen extends WindowTabScreen {
        private final Settings settings;

        public PlayStatusScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
            this.settings = this.createSettings();
            this.settings.onActivated();
        }

        private Settings createSettings() {
            var settings = new Settings();
            this.createGeneralSettings(settings.getDefaultGroup());
            this.createPlayStatusSettings(settings.createGroup("Play Status"));
            this.CreateServerSyncSettings(settings.createGroup("Server Sync"));
            return settings;
        }

        private void createGeneralSettings(SettingGroup sgGeneral) {
            sgGeneral.add(
                new StringSetting.Builder()
                    .name("server")
                    .description("The server to use for play status.")
                    .onChanged(server -> {
                        PlayStatus.get().server = server;
                        ServerSync.get().server = server;
                    })
                    .onModuleActivated(stringSetting -> {
                        stringSetting.set(PlayStatus.get().server);
                        stringSetting.set(ServerSync.get().server);
                    })
                    .build()
            );

            sgGeneral.add(
                new StringSetting.Builder()
                    .name("api-key")
                    .description("The API key to use for the server.")
                    .onChanged(apiKey -> {
                        PlayStatus.get().apiKey = apiKey;
                        ServerSync.get().apiKey = apiKey;
                    })
                    .onModuleActivated(stringSetting -> {
                        stringSetting.set(PlayStatus.get().apiKey);
                        stringSetting.set(ServerSync.get().apiKey);
                    })
                    .build()
            );
        }

        private void createPlayStatusSettings(SettingGroup sgPlayStatus) {
            sgPlayStatus.add(
                new BoolSetting.Builder()
                    .name("enabled")
                    .description("Whether to enable sending/receiving play status.")
                    .defaultValue(false)
                    .onChanged(enabled -> PlayStatus.get().enabled = enabled)
                    .onModuleActivated(booleanSetting -> booleanSetting.set(PlayStatus.get().enabled))
                    .build()
            );

            sgPlayStatus.add(
                new StringSetting.Builder()
                    .name("name")
                    .description("Your name.")
                    .onChanged(name -> PlayStatus.get().name = name)
                    .onModuleActivated(stringSetting -> stringSetting.set(PlayStatus.get().name))
                    .build()
            );

            sgPlayStatus.add(
                new IntSetting.Builder()
                    .name("update-interval")
                    .description("The update interval in seconds.")
                    .onChanged(updateInterval -> PlayStatus.get().updateIntervalInSeconds = updateInterval)
                    .onModuleActivated(intSetting -> intSetting.set(PlayStatus.get().updateIntervalInSeconds))
                    .defaultValue(10)
                    .min(1)
                    .build()
            );
        }

        private void CreateServerSyncSettings(SettingGroup sgServerSync) {
            sgServerSync.add(
                new BoolSetting.Builder()
                    .name("enabled")
                    .description("Whether to enable server syncing.")
                    .defaultValue(false)
                    .onChanged(enabled -> ServerSync.get().enabled = enabled)
                    .onModuleActivated(booleanSetting -> booleanSetting.set(ServerSync.get().enabled))
                    .build()
            );
        }

        @Override
        public void initWidgets() {
            // Settings
            add(theme.settings(settings)).minWidth(400).expandX();
        }
    }
}

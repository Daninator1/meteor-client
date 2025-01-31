/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixininterface;

import net.minecraft.client.network.ServerInfo;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IServerListAdditionalMethods {
    @Nullable
    ServerInfo get(UUID id);
}

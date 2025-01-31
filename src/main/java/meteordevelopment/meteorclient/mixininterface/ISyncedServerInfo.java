/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixininterface;

import java.util.UUID;

public interface ISyncedServerInfo {
    UUID getId();
    void setId(UUID id);
}

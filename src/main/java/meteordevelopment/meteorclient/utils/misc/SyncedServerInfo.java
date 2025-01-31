/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package meteordevelopment.meteorclient.utils.misc;

import java.util.UUID;

public class SyncedServerInfo {
    public UUID id;
    public String name;
    public String address;

    public SyncedServerInfo(UUID id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }
}

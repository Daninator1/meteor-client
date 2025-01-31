/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package meteordevelopment.meteorclient.utils.misc;

public class PlayStatusEntry {
    public String name;
    public String playerName;
    public String server;
    public String dimension;
    public PlayStatusPosition position;

    public PlayStatusEntry(String name, String playerName, String server, PlayStatusPosition position, String dimension) {

        this.name = name;
        this.playerName = playerName;
        this.server = server;
        this.position = position;
        this.dimension = dimension;
    }
}

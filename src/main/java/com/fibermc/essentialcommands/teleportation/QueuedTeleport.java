package com.fibermc.essentialcommands.teleportation;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public abstract class QueuedTeleport {

    private int ticksRemaining;
    private final PlayerData playerData;
    private final Text destName;
    public final Vec3d initialPosition;
    public final Vec3d initialVehiclePosition;

    public QueuedTeleport(PlayerData playerData, Text destName) {
        this.playerData = playerData;
        this.destName = destName;
        this.ticksRemaining = CONFIG.TELEPORT_DELAY_TICKS;
        this.initialPosition = playerData.getPlayer().getPos();
        this.initialVehiclePosition = null;
    }

    public QueuedTeleport(PlayerData playerData, Text destName, int delay) {
        this.playerData = playerData;
        this.destName = destName;
        this.ticksRemaining = delay;
        this.initialPosition = playerData.getPlayer().getPos();
        this.initialVehiclePosition = null;
    }

    public QueuedTeleport(PlayerData playerData, Entity vehicle, Text destName) {
        this.playerData = playerData;
        this.destName = destName;
        this.initialPosition = playerData.getPlayer().getPos();
        this.initialVehiclePosition = vehicle.getPos();
    }

    public QueuedTeleport(PlayerData playerData, Entity vehicle, Text destName, int delay) {
        this.playerData = playerData;
        this.destName = destName;
        this.ticksRemaining = delay;
        this.initialPosition = playerData.getPlayer().getPos();
        this.initialVehiclePosition = vehicle.getPos();
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public void tick(MinecraftServer server) {
        this.ticksRemaining--;
    }

    public abstract MinecraftLocation getDest();

    public MutableText getDestName() {
        return (MutableText) destName;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public void complete() {
        ((ServerPlayerEntityAccess) playerData.getPlayer()).ec$endQueuedTeleport();
    }
}

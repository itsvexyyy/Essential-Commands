package com.fibermc.essentialcommands.mixin;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(Entity.class)
public class EntityMixin {

    @Unique
    private MinecraftLocation ec$cachedPrevLocation;

    private static final Logger LOGGER = LoggerFactory.getLogger("EntityMixin");

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FFZ)Z", at = @At("HEAD"))
    public void onTeleportStart(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch, boolean resetCamera, CallbackInfoReturnable<Boolean> cir) {
        Entity thisEntity = (Entity)(Object)this;

        ec$cachedPrevLocation = new MinecraftLocation(
            thisEntity.getWorld().getRegistryKey(),
            thisEntity.getPos().getX(),
            thisEntity.getPos().getY(),
            thisEntity.getPos().getZ()
        );
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FFZ)Z", at = @At("TAIL"))
    private void onTeleportEnd(ServerWorld world, double x, double y, double z, Set<PositionFlag> flags, float yaw, float pitch, boolean resetCamera, CallbackInfoReturnable<Boolean> cir) {
        Entity thisEntity = (Entity)(Object)this;
        if (ec$cachedPrevLocation != null) {
            LOGGER.info("[TeleportEnd] Entity {} moved passengers from previous location: {}", thisEntity.getName().getString(), ec$cachedPrevLocation);

            for (Entity passenger : thisEntity.getPassengerList()) {
                if (passenger instanceof ServerPlayerEntity player) {
                    var targetPlayerData = ((ServerPlayerEntityAccess)player).ec$getPlayerData();
                    targetPlayerData.setPreviousLocation(ec$cachedPrevLocation);
                }
            }

            ec$cachedPrevLocation = null;
        } else {
            LOGGER.warn("[TeleportEnd] No cached previous location for vehicle {}", thisEntity.getName().getString());
        }
    }
}

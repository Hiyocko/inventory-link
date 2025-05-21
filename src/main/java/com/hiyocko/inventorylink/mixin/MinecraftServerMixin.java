package com.hiyocko.inventorylink.mixin;

import com.hiyocko.inventorylink.Inventory_link;
import com.hiyocko.inventorylink.Inventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "shutdown", at = @At("HEAD"))
    private void onStopServer(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        Collection<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity player : players) {
            Inventory.leaveServer(player, Inventory_link.mySQL);
        }
        Inventory.es.shutdown();
        Inventory_link.mySQL.closeConnection();
    }
}

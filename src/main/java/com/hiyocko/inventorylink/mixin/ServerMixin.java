package com.hiyocko.inventorylink.mixin;

import com.hiyocko.inventorylink.Inventory_link;
import com.hiyocko.inventorylink.Inventory;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerMixin {

    @Shadow public abstract ServerPlayerEntity getPlayer();

    @Inject(method = "onDisconnected", at = @At("HEAD"))
    private void onDisconnect(DisconnectionInfo details, CallbackInfo ci) {
        if (this.getPlayer() != null) Inventory.leaveServer(this.getPlayer(), Inventory_link.mySQL);
    }
}

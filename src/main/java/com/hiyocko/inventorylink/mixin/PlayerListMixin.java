package com.hiyocko.inventorylink.mixin;

import com.hiyocko.inventorylink.Inventory_link;
import com.hiyocko.inventorylink.Inventory;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerListMixin {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onConnectPlayer(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData cookie, CallbackInfo ci) {
        Inventory.joinServer(player, Inventory_link.mySQL);
    }
}

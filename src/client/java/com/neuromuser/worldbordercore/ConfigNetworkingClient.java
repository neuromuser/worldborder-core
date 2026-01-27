package com.neuromuser.worldbordercore;

import com.neuromuser.worldbordercore.config.ConfigManager;
import com.neuromuser.worldbordercore.config.ConfigNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ConfigNetworkingClient {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(
            ConfigNetworking.ConfigPayload.ID,
            (payload, context) -> context.client().execute(() ->
                ConfigManager.receiveServerConfig(payload.json())
            )
        );
    }
}

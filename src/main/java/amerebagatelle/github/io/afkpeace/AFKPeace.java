package amerebagatelle.github.io.afkpeace;

import amerebagatelle.github.io.afkpeace.settings.SettingsManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

@Environment(EnvType.CLIENT)
public class AFKPeace implements ClientModInitializer {
	private static final MinecraftClient mc = MinecraftClient.getInstance();

	private static final ConnectionManager connectionManager = new ConnectionManager(mc);

	public static boolean isReconnecting = false;
	public static ServerInfo currentServerEntry;

	@Override
	public void onInitializeClient() {
		SettingsManager.initSettings();
		ClientTickCallback.EVENT.register(event -> {
			if (isReconnecting) {
				connectionManager.finishReconnect();
				isReconnecting = false;
			}
		});
	}

	public static ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	// Dev notes
	// TODO: Mixin to the pause menu and add a player shadow button (maybe better as another mod?)
}
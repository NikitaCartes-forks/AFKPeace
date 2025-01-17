package amerebagatelle.github.io.afkpeace;

import amerebagatelle.github.io.afkpeace.util.ReconnectThread;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ConnectionManager {
    private static ReconnectThread reconnectThread;
    public static boolean isDisconnecting = false;

    /**
     * Attempts to reconnect the client to the target server.
     *
     * @param target The server to connect to.
     */
    public static void startReconnect(ServerInfo target) {
        Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getConnection().disconnect(new TranslatableText("reconnecting"));
        MinecraftClient.getInstance().disconnect();
        reconnectThread = new ReconnectThread(target);
        reconnectThread.start();
        MinecraftClient.getInstance().setScreen(new DisconnectedScreen(new MultiplayerScreen(new TitleScreen()), new LiteralText("AFKPeaceReconnection"), new TranslatableText("afkpeace.reconnect.reason")));
    }

    /**
     * Called by the Reconnection thread to finish reconnecting to the server.
     */
    public static void finishReconnect() {
        connectToServer(AFKPeaceClient.currentServerEntry);
    }

    /**
     * Called by the Reconnection thread if it cannot connect to the target server.
     */
    public static void cancelReconnect() {
        try {
            reconnectThread.join();
        } catch (InterruptedException ignored) {
        }
        AFKPeaceClient.LOGGER.debug("Reconnecting cancelled.");
        MinecraftClient.getInstance().setScreen(new DisconnectedScreen(new MultiplayerScreen(new TitleScreen()), new LiteralText("AFKPeaceDisconnect"), new TranslatableText("afkpeace.reconnectfail")));
    }

    /**
     * Connects the client to the target server.
     *
     * @param targetInfo Server to connect to.
     */
    public static void connectToServer(ServerInfo targetInfo) {
        ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), MinecraftClient.getInstance(), ServerAddress.parse(targetInfo.address), targetInfo);
    }

    /**
     * Disconnects the client from the current server.
     *
     * @param reason Why the client disconnected.
     */
    public static void disconnectFromServer(Text reason) {
        if (!AFKPeaceClient.CONFIG.reconnectOnDamageLogout) {
            isDisconnecting = true;
            Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getConnection().disconnect(reason);
            MinecraftClient.getInstance().disconnect();
        MinecraftClient.getInstance().setScreen(new DisconnectedScreen(new MultiplayerScreen(new TitleScreen()), new LiteralText("AFKPeaceDisconnect"), reason));
        } else {
            if (AFKPeaceClient.currentServerEntry != null) startReconnect(AFKPeaceClient.currentServerEntry);
        }
    }
}

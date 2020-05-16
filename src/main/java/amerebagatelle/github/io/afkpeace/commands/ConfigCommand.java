package amerebagatelle.github.io.afkpeace.commands;

import amerebagatelle.github.io.afkpeace.settings.SettingsManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cottonmc.clientcommands.ClientCommandPlugin;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static io.github.cottonmc.clientcommands.ArgumentBuilders.argument;
import static io.github.cottonmc.clientcommands.ArgumentBuilders.literal;

@Environment(EnvType.CLIENT)
public class ConfigCommand implements ClientCommandPlugin {

    @Override
    public void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<CottonClientCommandSource> afkpeace = literal("afkpeace")
                .then(literal("reconnectEnabled")
                        .executes(ctx -> {
                            MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("reconnectEnabled is set to" + SettingsManager.loadSetting("reconnectEnabled")));
                            return 1;
                        })
                        .then(argument("setpoint", bool())
                                .executes(ctx -> {
                                    SettingsManager.writeSetting("reconnectEnabled", Boolean.toString(BoolArgumentType.getBool(ctx, "setpoint")));
                                    MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("reconnectEnabled set to" + SettingsManager.loadSetting("reconnectEnabled")));
                                    return 1;
                                })))
                .then(literal("damageLogoutEnabled")
                        .executes(ctx -> {
                            MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("damageLogoutEnabled is set to " + SettingsManager.loadSetting("damageLogoutEnabled")));
                            return 1;
                        })
                        .then(argument("setpoint", bool())
                                .executes(ctx -> {
                                    SettingsManager.writeSetting("damageLogoutEnabled", Boolean.toString(BoolArgumentType.getBool(ctx, "setpoint")));
                                    MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("damageLogoutEnabled set to " + SettingsManager.loadSetting("damageLogoutEnabled")));
                                    return 1;
                                })))
                .then(literal("secondsBetweenReconnectionAttempts")
                        .executes(ctx -> {
                            MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("secondsBetweenReconnectionAttempts is set to " + SettingsManager.loadSetting("secondsBetweenReconnectAttempts")));
                            return 1;
                        })
                        .then(argument("setpoint", integer())
                                .executes(ctx -> {
                                    SettingsManager.writeSetting("secondsBetweenReconnectionAttempts", Integer.toString(IntegerArgumentType.getInteger(ctx, "setpoint")));
                                    MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("secondsBetweenReconnectionAttempts set to " + SettingsManager.loadSetting("secondsBetweenReconnectAttempts")));
                                    return 1;
                                })))
                .then(literal("reconnectAttemptNumber")
                        .executes(ctx -> {
                            MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("reconnectAttemptNumber is set to " + SettingsManager.loadSetting("reconnectAttemptNumber")));
                            return 1;
                        })
                        .then(argument("setpoint", integer())
                                .executes(ctx -> {
                                    SettingsManager.writeSetting("reconnectAttemptNumber", Integer.toString(IntegerArgumentType.getInteger(ctx, "setpoint")));
                                    MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("reconnectAttemptNumber set to " + SettingsManager.loadSetting("reconnectAttemptNumber")));
                                    return 1;
                                })))
                .then(literal("afkmode")
                        .then(literal("enable")
                                .executes(ctx -> {
                                    SettingsManager.activateAFKMode();
                                    return 1;
                                }))
                        .then(literal("disable")
                                .executes(ctx -> {
                                    SettingsManager.disableAFKMode();
                                    return 1;
                                })));

        dispatcher.register(afkpeace);
    }

}
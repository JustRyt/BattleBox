package net.justryt.battlebox.commands.language;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.justryt.battlebox.user.UserManager;
import net.justryt.battlebox.utils.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.Set;

public class LanguageCommand {
    public static LiteralCommandNode<CommandSourceStack> build() {
        var root = Commands
                .literal("language")
                .requires(src -> src.getSender().hasPermission("bb.language") && src.getSender() instanceof Player);

        root.then(Commands
                .argument("language", StringArgumentType.word())
                .suggests((_, builder) -> {
                    Messages.getAvailableLanguages().forEach(builder::suggest);
                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    var langKey = StringArgumentType.getString(ctx, "language").toLowerCase();
                    var player = (Player) ctx.getSource().getSender();

                    if (!Messages.getAvailableLanguages().contains(langKey)) {
                        Messages.sendMessage(player, "lang-cmd-arg-lang-not-found");
                        return Command.SINGLE_SUCCESS;
                    }

                    UserManager
                            .getUser(player.getUniqueId())
                            .thenAccept(user -> user.setLanguage(langKey));

                    Messages.sendMessage(player, "lang-cmd-lang-set-success", TagResolver.resolver("lang", Tag.inserting(Component.text(langKey))));
                    return Command.SINGLE_SUCCESS;
                })
        );

        return root.build();
    }

    public static Set<String> getAliases() {
        return Set.of("lang");
    }
}

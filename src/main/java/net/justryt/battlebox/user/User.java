package net.justryt.battlebox.user;

import lombok.Getter;
import lombok.Setter;
import net.justryt.battlebox.stats.PlayerStat;
import net.justryt.battlebox.utils.Messages;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Getter
public class User {
    private final UUID playerId;
    private final Map<PlayerStat, Integer> stats = new EnumMap<>(PlayerStat.class);
    @Setter
    private String language;

    public User(@NotNull UUID uuid) {
        this.playerId = uuid;
        this.language = Messages.getDefaultLanguage();
    }

    public int getStat(@NotNull PlayerStat stat) {
        return stats.get(stat);
    }

    public void modifyStat(@NotNull PlayerStat stat, @NotNull Function<Integer, Integer> modifier) {
        stats.put(stat, modifier.apply(getStat(stat)));
    }
}

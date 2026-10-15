package net.justryt.battlebox.user;

import lombok.Getter;
import net.justryt.battlebox.stats.PlayerStat;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class User {
    private final UUID playerId;
    private final Map<PlayerStat, Integer> stats = new EnumMap<>(PlayerStat.class);

    public User(@NotNull UUID uuid) {
        this.playerId = uuid;
    }

    public void setStatValue(@NotNull PlayerStat statField, int value) {
        this.stats.put(statField, value);
    }

    public int getStatValue(@NotNull PlayerStat statField) {
        return this.stats.getOrDefault(statField, 0);
    }
}

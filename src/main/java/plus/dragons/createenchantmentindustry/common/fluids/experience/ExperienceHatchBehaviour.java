package plus.dragons.createenchantmentindustry.common.fluids.experience;

import net.minecraft.world.entity.player.Player;

public final class ExperienceHatchBehaviour {
    public static final int LIQUID_EXPERIENCE_PER_POINT = 1;

    private ExperienceHatchBehaviour() {
    }

    public static int getExperienceForNextLevel(int level) {
        if (level >= 30) {
            return 9 * level - 158;
        }
        if (level >= 15) {
            return 5 * level - 38;
        }
        return 2 * level + 7;
    }

    public static int getExperienceForTotalLevel(int level) {
        if (level == 0) {
            return 0;
        }
        if (level >= 31) {
            return (9 * level * level - 325 * level) / 2 + 2220;
        }
        if (level >= 16) {
            return (5 * level * level - 91 * level) / 2 + 360;
        }
        return level * level + 6 * level;
    }

    public static int getExperienceForPlayer(Player player) {
        int experience = getExperienceForTotalLevel(player.experienceLevel);
        experience += Math.round(player.experienceProgress * getExperienceForNextLevel(player.experienceLevel));
        return experience;
    }

    public static long getLiquidFromExperience(long experiencePoints) {
        return experiencePoints * LIQUID_EXPERIENCE_PER_POINT;
    }

    public static int getExperienceFromLiquid(long liquidAmount) {
        return (int) Math.min(Integer.MAX_VALUE, liquidAmount / LIQUID_EXPERIENCE_PER_POINT);
    }
}

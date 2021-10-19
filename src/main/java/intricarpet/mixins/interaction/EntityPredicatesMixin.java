package intricarpet.mixins.interaction;

import java.util.function.Predicate;

import com.google.common.base.Predicates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import intricarpet.helpers.Interactions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.scoreboard.AbstractTeam;

@Mixin(EntityPredicates.class)
@SuppressWarnings("unchecked")
public class EntityPredicatesMixin
{
    @Overwrite
    public static Predicate<Entity> canBePushedBy(Entity entity) {
        AbstractTeam abstractTeam = entity.getScoreboardTeam();
        AbstractTeam.CollisionRule collisionRule = abstractTeam == null ? AbstractTeam.CollisionRule.ALWAYS : abstractTeam.getCollisionRule();
        return (Predicate<Entity>)(collisionRule == AbstractTeam.CollisionRule.NEVER ? Predicates.alwaysFalse() : EntityPredicates.EXCEPT_SPECTATOR.and((entity2) -> {
            if(entity2 instanceof PlayerEntity)
            {
                String playerName = entity2.getName().getString();
                if(Interactions.onlinePlayerMap.containsKey(playerName) &&
                    Interactions.onlinePlayerMap.get(playerName).contains("entities"))
                    return false;
            }
            if(entity instanceof PlayerEntity)
            {
                String playerName = entity.getName().getString();
                if(Interactions.onlinePlayerMap.containsKey(playerName) &&
                    Interactions.onlinePlayerMap.get(playerName).contains("entities"))
                    return false;
            }
            if (!entity2.isPushable()) {
                return false;
            } else if (entity.world.isClient && (!(entity2 instanceof PlayerEntity) || !((PlayerEntity)entity2).isMainPlayer())) {
                return false;
            } else {
                AbstractTeam abstractTeam2 = entity2.getScoreboardTeam();
                AbstractTeam.CollisionRule collisionRule2 = abstractTeam2 == null ? AbstractTeam.CollisionRule.ALWAYS : abstractTeam2.getCollisionRule();
                if (collisionRule2 == AbstractTeam.CollisionRule.NEVER) {
                    return false;
                } else {
                    boolean bl = abstractTeam != null && abstractTeam.isEqual(abstractTeam2);
                    if ((collisionRule == AbstractTeam.CollisionRule.PUSH_OWN_TEAM || collisionRule2 == AbstractTeam.CollisionRule.PUSH_OWN_TEAM) && bl) {
                        return false;
                    } else {
                        return collisionRule != AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS && collisionRule2 != AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS || bl;
                    }
                }
            }
        }));
     }
}

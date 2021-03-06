package ontology.effects.unary;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;

import java.util.ArrayList;

/**
 * Created by Diego on 18/02/14.
 */
public class SpawnIfHasMore extends Effect {

    public int spend;
    public String resource;
    public int resourceId;
    public int limit;
    public String stype;
    public int itype;

    public SpawnIfHasMore(InteractionContent cnt) {
        resourceId = -1;
        spend = 0;
        this.parseParameters(cnt);
        resourceId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(resource);
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game) {
        applyScore = false;

        if (game.getRandomGenerator().nextDouble() >= prob) return;

        if (sprite1.getAmountResource(resourceId) >= limit) {
            game.addSprite(itype, sprite1.getPosition());
            applyScore = true;

            sprite1.modifyResource(resourceId, -spend); //0 by default.
        }
    }

    @Override
    public ArrayList<String> getEffectSprites() {
        ArrayList<String> result = new ArrayList<String>();
        if (stype != null) result.add(stype);

        return result;
    }
}

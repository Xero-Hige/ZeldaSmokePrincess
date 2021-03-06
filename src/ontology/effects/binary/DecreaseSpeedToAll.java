package ontology.effects.binary;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 03/12/13
 * Time: 16:17
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class DecreaseSpeedToAll extends Effect {
    public String stype; // decreases the speed of all sprites of type stype
    public int itype;
    public double value = 0.1;

    public DecreaseSpeedToAll(InteractionContent cnt) {
        is_stochastic = true;
        this.parseParameters(cnt);
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game) {

        ArrayList<Integer> subtypes = game.getSubTypes(itype);
        for (Integer i : subtypes) {
            Iterator<VGDLSprite> spriteIt = game.getSpriteGroup(i);
            if (spriteIt != null) while (spriteIt.hasNext()) {
                try {
                    VGDLSprite s = spriteIt.next();
                    if (s.speed - value < 0) s.speed = 0;
                    else s.speed -= value;
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

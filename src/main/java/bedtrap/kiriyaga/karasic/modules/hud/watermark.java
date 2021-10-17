package bedtrap.kiriyaga.karasic.modules.hud;

import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

public class watermark extends DoubleTextHudElement {
    public watermark(HUD hud) {
        super(hud, "Watermark", "Displays karasic watermark", "karasic addon", true);
    }

    @Override
    protected String getRight() {
        return "";
    }

}

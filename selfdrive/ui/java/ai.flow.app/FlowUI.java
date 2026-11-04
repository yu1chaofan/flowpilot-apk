package ai.flow.app;

import ai.flow.common.ParamsInterface;
import ai.flow.common.Path;
import ai.flow.hardware.HardwareManager;
import ai.flow.launcher.Launcher;
import ai.flow.modeld.ModelExecutor;
import ai.flow.sensor.SensorInterface;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import org.nd4j.linalg.factory.Nd4j;
import org.opencv.core.Core;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class FlowUI extends Game {
    final String API_ENDPOINT = "https://staging-api.flowdrive.ai";
    final String AUTH_ENDPOINT = API_ENDPOINT + "/auth";
    final boolean RECORD_VIDEOS = false;
    public ShapeRenderer shapeRenderer;
    public BitmapFont font;
    public Skin skin;
    public int pid;
    public Launcher launcher;
    public Map<String, SensorInterface> sensors;
    public ModelExecutor modelExecutor;
    public SensorInterface onroadManager;
    // reuse common screens
    public SettingsScreen settingsScreen;
    public OnRoadScreen onRoadScreen;
    public ParamsInterface params = ParamsInterface.getInstance();
    public boolean isOnRoad = false;
    public boolean prevIsOnRoad = false;
    public Thread updateOnroadThread;
    Sound engageSound, disengageSound, promptSound, promptDistractedSound,
            refuseSound, warningImmediate, warningSoft;
    public HardwareManager hardwareManager;

    public FlowUI(Launcher launcher, HardwareManager hardwareManager, int pid) {
        this.pid = pid;
        this.launcher = launcher;
        this.modelExecutor = launcher.modeld;
        this.sensors = launcher.sensors;
        this.hardwareManager = hardwareManager;

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Nd4j.zeros(1); // init nd4j (any better ways?)

        updateOnroadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
//                    isOnRoad = params.existsAndCompare("IsOnroad", true);
                    isOnRoad = true;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (prevIsOnRoad != isOnRoad) {
                        if (!isOnRoad) {
                            modelExecutor.stop();
                            launcher.managers.get("onroad").stop();
                        } else {
                            launcher.managers.get("onroad").start();
                            OnRoadScreen.HideInfoTable = true;
                            modelExecutor.start();
                        }
                    }
                    prevIsOnRoad = isOnRoad;
                }
            }
        });
    }

    public static void loadFont(String fontPath, String fontName, int size, Skin skin){
        FreeTypeFontGenerator fontGen = new FreeTypeFontGenerator(Gdx.files.internal(fontPath));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.genMipMaps = true;
        parameter.magFilter = Texture.TextureFilter.MipMapLinearLinear;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.size = size;
        BitmapFont font = fontGen.generateFont(parameter);
        fontGen.dispose();
        skin.add(fontName, font);
    }

    public void loadInternalFonts(Skin skin){
        float widthScale = Gdx.app.getGraphics().getWidth()/1200.f;
        loadFont("fonts/Inter-Regular.ttf", "default-font-16", Math.round(16*widthScale), skin);
        loadFont("fonts/Inter-Regular.ttf", "default-font-20", Math.round(20*widthScale), skin);
        loadFont("fonts/Inter-Regular.ttf", "default-font-25", Math.round(25*widthScale), skin);
        loadFont("fonts/Inter-Regular.ttf", "default-font-30", Math.round(30*widthScale), skin);
        loadFont("fonts/Inter-Regular.ttf", "default-font", Math.round(36*widthScale), skin);
        loadFont("fonts/Inter-Regular.ttf", "default-font-64", Math.round(64*widthScale), skin);
        loadFont("fonts/opensans_bold.ttf", "default-font-bold", Math.round(20*widthScale), skin);
        loadFont("fonts/opensans_bold.ttf", "default-font-bold-med", Math.round(45*widthScale), skin);
        loadFont("fonts/opensans_bold.ttf", "default-font-bold-large", Math.round(100*widthScale), skin);
    }

    public void loadSounds() {
        engageSound = Gdx.audio.newSound(Gdx.files.internal("sounds/engage.wav"));
        disengageSound = Gdx.audio.newSound(Gdx.files.internal("sounds/disengage.wav"));
        promptSound = Gdx.audio.newSound(Gdx.files.internal("sounds/prompt.wav"));
        promptDistractedSound = Gdx.audio.newSound(Gdx.files.internal("sounds/prompt_distracted.wav"));
        refuseSound = Gdx.audio.newSound(Gdx.files.internal("sounds/refuse.wav"));
        warningImmediate = Gdx.audio.newSound(Gdx.files.internal("sounds/warning_immediate.wav"));
        warningSoft = Gdx.audio.newSound(Gdx.files.internal("sounds/warning_soft.wav"));
    }

    public static TextButton getPaddedButton(String text, Skin skin, String styleName, int padding){
        TextButton button = new TextButton(text, skin, styleName);
        button.getLabelCell().pad(padding);
        return button;
    }

    public static TextButton getPaddedButton(String text, Skin skin, int padding){
        TextButton button = new TextButton(text, skin);
        button.getLabelCell().pad(padding);
        return button;
    }

    @Override
    public void create() {
        params.putInt("FlowpilotPID", pid);

        updateOnroadThread.start();
        System.out.println("FlowUi create 1");

        if (Gdx.gl != null) { // else headless mode
            shapeRenderer = new ShapeRenderer();
            font = new BitmapFont();
            font.setColor(0f, 1f, 0f, 1f);
            font.getData().setScale(5);
            skin = new Skin(new TextureAtlas(Gdx.files.internal("skins/uiskin.atlas")));
            for (Texture texture: skin.getAtlas().getTextures())
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            loadInternalFonts(skin);
            loadSounds();
            skin.load(Gdx.files.internal("skins/uiskin.json"));

            settingsScreen = new SettingsScreen(this);
            onRoadScreen = new OnRoadScreen(this);

            this.launcher.startSensorD();
            this.launcher.startAllD();
            setScreen(onRoadScreen);
        }
        else{
            launcher.startSensorD();
            launcher.startAllD();
        }
    }

    @Override
    public void dispose() {
        if (Gdx.gl != null) { // else headless mode
            shapeRenderer.dispose();
            font.dispose();
            launcher.dispose();
        }
        params.dispose();
    }
}

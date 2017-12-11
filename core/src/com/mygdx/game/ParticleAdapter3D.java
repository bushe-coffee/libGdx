package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class ParticleAdapter3D extends ApplicationAdapter {

    public OrthographicCamera cam;
    public ModelBatch modelBatch;
    public Environment environment;
    private AnimationController anim;
    public AssetManager assets;
    public Array<ModelInstance> instances = new Array<ModelInstance>();
    private Stage stage;
    private Skin skin;
    private Label info;
    private ButtonGroup animBg,fxBg;

    public ModelInstance slug;
    public int animNb=0;
    public float timePassed;
    public String[] anims={"Idle","Move","Attack","Explode"};
    private Vector2 mPos;
    private Vector3
            axis=new Vector3(0,1,0);
    private float angle;
    private Plane plane;
    private Vector3 mWPos,slugPos;
    private String animName,effectName;

    //private ParticleEffect currentEffects;
    //private ParticleSystem particleSystem;
    private Matrix4 targetMatrix;

    private String[] effectPaths={"data/point.pfx","data/dust.pfx"};

    @Override
    public void create () {
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.5f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.9f, -1f, -0.8f, -0.2f));

        // Setting the camera way off center.  This is for me to understand better how camera vs object position works.
        cam = new OrthographicCamera(18.0f, 18.0f);
        cam.position.set(0f, 1.5f, 2f);
        cam.lookAt(0f,-2f,2f);
        cam.near = 0.1f;
        cam.far = 200f;
        cam.translate(9f, 1f, -5f);
        cam.update();

        skin=new Skin(Gdx.files.internal("data/ui/uiskin.json"),new TextureAtlas("data/ui/uiskin.atlas"));

        // Two sets of buttons are created.  One controls what animation is displayed and the other what effect is used when mouse pressed
        stage=new Stage();
        Table mainTable=new Table();
        mainTable.setFillParent(true);
        animBg=new ButtonGroup();
        animBg.setUncheckLast(true);
        for(int i=0;i<anims.length;i++) {
            TextButton tb=new TextButton(anims[i],skin);
            tb.setSize(1000f,580f);
            tb.setName(anims[i]);
            animBg.add(tb);
            if (i==0) animBg.setChecked(tb.getName());
            mainTable.add(tb).left().bottom().expandY();
            tb.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (!animName.equals(actor.getName())) {
                        animName=actor.getName();
                        anim.setAnimation(animName);
                        adjustLabel();
                    }
                }
            });
        }
        info=new Label(anims[0]+"-",skin);
        mainTable.add(info).expandX().bottom().expandY();
        fxBg=new ButtonGroup();
        fxBg.setUncheckLast(true);
        for(int i=0;i<effectPaths.length;i++) {
            TextButton tb=new TextButton(effectPaths[i].substring(effectPaths[i].lastIndexOf('/')+1, effectPaths[i].lastIndexOf('.')),skin);
            tb.setName(effectPaths[i]);
            fxBg.add(tb);
            mainTable.add(tb).right().bottom().expandY();
            tb.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
//                    if (currentEffects!=null) {
//                        particleSystem.remove(currentEffects);
//                        currentEffects.dispose();
//                    }
//                    currentEffects=assets.get(actor.getName(),ParticleEffect.class).copy();
//                    currentEffects.init();
//                    particleSystem.add(currentEffects);
                    effectName=((TextButton)actor).getText().toString();
                    adjustLabel();
                }
            });
        }
        stage.addActor(mainTable);

        // Process stage widgets first then process the slug orientation and effects.
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new InputAdapter () {
            public boolean touchDown (int x, int y, int pointer, int button) {
//                if (currentEffects!=null) {
//                    currentEffects.start();
//                }

                calculateNewPos(x,y);
                return true;
            }

            public boolean touchDragged(int x,int y, int button) {
                calculateNewPos(x,y);
                return true;
            }
            public boolean touchUp (int x, int y, int pointer, int button) {
//                if (currentEffects!=null) {
//                    currentEffects.end();
//                }
                return true;
            }
            private void calculateNewPos(int x, int y) {
                Ray ray=cam.getPickRay(Gdx.input.getX(),Gdx.input.getY());
                Intersector.intersectRayPlane(ray, plane, mWPos);
                angle=mPos.set(mWPos.x-slugPos.x,slugPos.z-mWPos.z).angle()-90;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);

        assets = new AssetManager();
        assets.load("data/slug.g3db", Model.class);
        mPos=new Vector2();
        // Slug is positioned within camera view toward the bottom of the screen.
        slugPos=new Vector3(9f,0,-3f);
        mWPos=new Vector3(slugPos);
        plane=new Plane(new Vector3(0,0,0), new Vector3(0,0,50), new Vector3(50,0,50));

        // ParticleSystem initial setup
//        particleSystem = ParticleSystem.get();
//        PointSpriteParticleBatch pointSpriteBatch = new PointSpriteParticleBatch();
//        pointSpriteBatch.setCamera(cam);
//        particleSystem = ParticleSystem.get();
//        particleSystem.add(pointSpriteBatch);
//        ParticleEffectLoader.ParticleEffectLoadParameter loadParam = new ParticleEffectLoader.ParticleEffectLoadParameter(particleSystem.getBatches());
//        ParticleEffectLoader loader = new ParticleEffectLoader(new InternalFileHandleResolver());
//        assets.setLoader(ParticleEffect.class, loader);
//        for(int i=0;i<effectPaths.length;i++) assets.load(effectPaths[i], ParticleEffect.class, loadParam);
        targetMatrix=new Matrix4();
    }

    /**
     * Set the text of the label showing what anim and effect are in play.
     */
    private void adjustLabel() {
//        info.setText(animBg.getChecked().getName()+"-"+(currentEffects==null?"":effectName));
    }

    /**
     * Called when asset manager is done loading and initialize model and animation
     */
    private void doneLoading() {
        slug = new ModelInstance(assets.get("data/slug.g3db", Model.class));
        slug.transform.translate(slugPos);
        anim=new AnimationController(slug);
        anim.setAnimation(anims[0],-1);
        animName=anims[0];
        System.out.println("Done loading");
    }

    @Override
    public void render () {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);
        float delta=Gdx.graphics.getDeltaTime();
        modelBatch.begin(cam);
        if (slug==null && assets.update()) {
            doneLoading();
        } else if (slug!=null) {
            slug.transform.idt();
            slug.transform.translate(slugPos.x,0,slugPos.z);
            slug.transform.rotate(axis, angle);
//            if (currentEffects!=null) {
//                targetMatrix.idt();
//                targetMatrix.translate(mWPos);
//                currentEffects.setTransform(targetMatrix);
//            }
            timePassed+=delta;
            anim.update(delta);

            modelBatch.render(slug, environment);
            if (timePassed>5f) {
                timePassed=timePassed-5f;
                animNb++;
                animNb=animNb%4;
                anim.animate(anims[animNb],-1,null, 0.1f);
            }
//            particleSystem.update();
//            particleSystem.begin();
//            particleSystem.draw();
//            particleSystem.end();
//            modelBatch.render(particleSystem);
        }
        modelBatch.end();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose () {
//        if (currentEffects!=null) currentEffects.dispose();
        modelBatch.dispose();
        instances.clear();
        assets.dispose();
    }
}

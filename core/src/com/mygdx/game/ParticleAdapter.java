package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;


public class ParticleAdapter extends ApplicationAdapter {

    private SpriteBatch drawContent;
    private ArrayList<ParticleEmitter> emitters;  // 存放不同的粒子发射器
    private ParticleEffect effect;

    private CustomerInputProcess inputProcessor;
    private float position_X = 337.5f, position_Y = 1460.9249f;

    private boolean isShow = false;
    public static boolean isTouch = false;

    @Override
    public void create() {
        super.create();

        effect = new ParticleEffect();
        //.p 是资源的 配置文件
        effect.load(Gdx.files.internal("data/test.p"), Gdx.files.internal("data/"));
        inputProcessor = new CustomerInputProcess(position_X, position_Y);

        //注册 监听 手动触摸事件
        Gdx.input.setInputProcessor(inputProcessor);

        drawContent = new SpriteBatch();
    }

    @Override
    public void render() {
        //清空画板，等待下一步绘制,
        Gdx.gl.glClearColor(0, 0, 0, 0.5f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        System.out.println("sunboyuan  " + isTouch + "   " + isShow);

        // 设置粒子的 绘制的 地方   是 手指 拖拽的 坐标
        synchronized (effect) {
            if (isTouch) {
                effect.setPosition(inputProcessor.x, inputProcessor.y);
            } else {
                effect.setPosition(position_X, position_Y);
            }

            drawContent.begin();
            float delta = Gdx.graphics.getDeltaTime();
            if (isShow) {
                effect.draw(drawContent, delta);
            } else {

            }

            drawContent.end();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        drawContent.dispose();
        //千万别忘了释放内存
        effect.dispose();
    }

    public void setPosition(boolean b, float x, float y) {
        synchronized (effect) {
            isShow = b;
            if (isShow) {
                position_X = x * Gdx.graphics.getWidth() + 10.0f;
                position_Y = (1.0f - y) * Gdx.graphics.getHeight() - 10.0f;
            }
        }

        System.out.println("position  " + position_X + "   " + position_Y);

    }
}

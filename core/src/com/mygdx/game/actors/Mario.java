package com.mygdx.game.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;


public class Mario extends Actor {

    private float positionX;
    private float positionY;

    // 加载 动画
    private float animationTime;
    private TextureRegion currentRegion;
    private Texture texture;
    private float stateTime;

    //定义控件
    public ImageButton buttonRight;
    public ImageButton buttonLeft;

    //定义三个 动画
    Animation<TextureRegion> right;  // 向右走动画
    Animation<TextureRegion> left;  // 向左走动画
    Animation<TextureRegion> idle; // 空闲

    // 定义三个状态
    enum STATE {
        left,right,idle
    }

    private STATE state;

    //构造方法
    public Mario(float x, float y) {
        this.positionX = x;
        this.positionY = y;
        state = STATE.right;
        stateTime = 0f;

        init();
    }

    //初始化 资源
    public void init() {
        //加载 图片
        texture = new Texture(Gdx.files.internal("data/big_mario.png"));
        //图片分割 处理
        TextureRegion [][]split = TextureRegion.split(texture, 16,32);
        TextureRegion [][]miror = TextureRegion.split(texture, 16,32);

        for (TextureRegion[] region1 : miror) {
            for (TextureRegion region2 : region1){
                // 反转图片，参数1 是X轴，参数2 是Y轴
                // 翻转 图片 得到向左走 动画
                region2.flip(true,false);
            }
        }

        //构建向右走的 动画
        TextureRegion []regionRight = new TextureRegion[5];
        regionRight[0] = split[0][1];
        regionRight[1] = split[0][2];
        regionRight[2] = split[0][3];
        regionRight[3] = split[0][4];
        regionRight[4] = split[0][5];
        // 每0.1s 一帧图片
        right = new Animation<TextureRegion>(0.1f, regionRight);

        TextureRegion []regionLeft = new TextureRegion[5];
        regionLeft[0] = miror[0][1];
        regionLeft[1] = miror[0][2];
        regionLeft[2] = miror[0][3];
        regionLeft[3] = miror[0][4];
        regionLeft[4] = miror[0][5];
        left = new Animation<TextureRegion>(0.1f, regionLeft);

        TextureRegion []regionIdle = new TextureRegion[1];
        regionIdle[0] = split[0][0];
        idle = new Animation<TextureRegion>(0.1f, regionIdle);

        buttonRight = new ImageButton(new TextureRegionDrawable(split[0][6]));
        buttonLeft = new ImageButton(new TextureRegionDrawable(miror[0][6]));

        buttonLeft.setPosition(20,20);
        buttonLeft.setSize(60,60);
        buttonRight.setPosition(20,100);
        buttonRight.setSize(60,60);

        // 设置button的监听事件
        buttonLeft.addListener(leftListener);
        buttonRight.addListener(rightListener);

    }

    // 更新状态
    public void update() {
        if (state == STATE.left) {
            this.positionX -= 1.5;
            if (this.positionX < 50) {
                this.positionX = 50;
            }
        } else if (state == STATE.right) {
            this.positionX += 1.5;
            if (this.positionX > 400) {
                this.positionX = 400;
            }
        }
    }

    public void animationCheck() {
        if (state == STATE.left) {
            currentRegion = left.getKeyFrame(stateTime, true);
        } else if (state == STATE.right) {
            currentRegion = right.getKeyFrame(stateTime, true);
        } else {
            currentRegion = idle.getKeyFrame(stateTime, true);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time
        this.update();
        this.animationCheck();

        batch.draw(currentRegion,positionX,positionY);
    }

    // 添加一个 动作
    @Override
    public void addAction(Action action) {
        super.addAction(action);
    }

    // 跟新 actor 的状态
    @Override
    public void act(float delta) {
        super.act(delta);
    }

    InputListener rightListener = new InputListener(){
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            state = STATE.idle;
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            state = STATE.right;
            super.touchUp(event, x, y, pointer, button);
        }
    };

    InputListener leftListener = new InputListener(){
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            state = STATE.idle;
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            state = STATE.left;
            super.touchUp(event, x, y, pointer, button);
        }
    };
}

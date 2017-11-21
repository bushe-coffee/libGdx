package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


public class AnimationAdapter extends ApplicationAdapter {

    // Constant rows and columns of the sprite sheet
    private static final int FRAME_COLS = 6, FRAME_ROWS = 5;

    // Objects used
    Animation<TextureRegion> walkAnimation; // Must declare frame type (TextureRegion)
    Texture walkSheet;
    SpriteBatch spriteBatch;

    // A variable for tracking elapsed time for the animation
    float stateTime;
    private int positionX = 0;
    private int screenWidth;

    public AnimationAdapter() {

    }

    @Override
    public void create() {
        // Load the sprite sheet as a Texture
        walkSheet = new Texture(Gdx.files.internal("data/animation_sheet.png"));
        screenWidth = Gdx.graphics.getWidth();

        // 图片分割 方法。一系列的工作图片在一张图片里面，
        // 此方法是 分割这张图片5行6列，组合成一系列的动作
        TextureRegion[][] tmp = TextureRegion.split(walkSheet,
                walkSheet.getWidth() / FRAME_COLS,
                walkSheet.getHeight() / FRAME_ROWS);

        // 声明一个 TextureRegion 数组
        TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                walkFrames[index++] = tmp[i][j];
            }
        }

        // the time between frames in seconds.
        walkAnimation = new Animation<TextureRegion>(0.025f, walkFrames);
        walkAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // 从时间 0开始
        spriteBatch = new SpriteBatch();
        stateTime = 0f;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 0, 0, 0.2f);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear screen
        // render 执行一次的 间隔 时间
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time

        // Get current frame of animation for the current stateTime
        TextureRegion currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        spriteBatch.begin();
        spriteBatch.draw(currentFrame, (positionX += 10) % screenWidth, 0);
        spriteBatch.end();

    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        walkSheet.dispose();
    }

}

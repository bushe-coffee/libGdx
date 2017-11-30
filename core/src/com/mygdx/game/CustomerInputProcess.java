package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;


public class CustomerInputProcess implements InputProcessor {

    public float x;
    public float y;

    public CustomerInputProcess(float x1, float y1) {
        this.x = x1;
        this.y = y1;

    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // 处理 拖拽 方法
//   手机屏幕左上角为（0，0） screenX, screenY 是坐标   pointer: 手指指向
        x = screenX;
        y = Gdx.graphics.getHeight() - screenY;

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}

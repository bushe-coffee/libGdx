package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.actors.Mario;


// Actor 类，控制 角色的 动作 ，属性 添加，移除，绘制，移动，旋转， 更新状态
// 有一个 相关连的类人 stage （场景，舞台）

public class ActorAdapter extends ApplicationAdapter {

//    TextureRegion 用于一些图片处理的类， 图片等分（AnimationAdapter类），反转（flip方法）

    Stage stage;

    @Override
    public void create () {
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        Mario mario = new Mario(100,190);

        // 吧需要绘制的 东西放到 场景中
        stage.addActor(mario);
        stage.addActor(mario.buttonLeft);
        stage.addActor(mario.buttonRight);
    }

    @Override
    public void render () {
        // 场景绘制， 两句话
        stage.act();
        stage.draw();
    }

    @Override
    public void dispose () {
    }

}

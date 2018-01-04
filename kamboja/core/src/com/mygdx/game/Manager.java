package com.mygdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.objects.GameMusic;
import com.mygdx.game.states.CreditsState;
import com.mygdx.game.states.GameState;
import com.mygdx.game.states.HelpState;
import com.mygdx.game.states.MapSelectState;
import com.mygdx.game.states.MenuState;
import com.mygdx.game.states.OptionsState;
import com.mygdx.game.states.PlayerSelectState;
import com.mygdx.game.states.PostGameState;

public class Manager implements ControllerListener, InputProcessor{
	
	private ArrayList<State> states;
	private int currentState = MENU_STATE;
	private boolean disposed = false; //so it doesnt dispose the same state twice
	
	public static final int GAME_STATE = 0;
	public static final int MENU_STATE = 1;
	public static final int PLAYER_SELECT_STATE = 2;
	public static final int MAP_SELECT_STATE = 3;
	public static final int HELP_STATE = 4;
	public static final int CREDITS_STATE = 5;
	public static final int POST_GAME_STATE = 6;
	public static final int OPTIONS_STATE = 7;
	
	public Manager(){
		states = new ArrayList<State>();
		states.add(new GameState(this));
		states.add(new MenuState(this));
		states.add(new PlayerSelectState(this));
		states.add(new MapSelectState(this));
		states.add(new HelpState(this));
		states.add(new CreditsState(this));
		states.add(new PostGameState(this));
		states.add(new OptionsState(this));
		
		Controllers.clearListeners();
		Controllers.addListener(this);	
		Gdx.input.setInputProcessor(this);
	}

	public void reAddControllers(){
		Controllers.clearListeners();
		Controllers.addListener(this);
	}
	
	public void create(){
		states.get(currentState).create();
	}
	
	public void render(SpriteBatch sb){
		if(!disposed){
		states.get(currentState).render(sb);
		}
		else{
			disposed = false;
		}
	}
	
	public void update(float delta){
		GameMusic.update();
		
		if(!disposed)
		states.get(currentState).update(delta);
	}

	public void connected(Controller controller) {
		states.get(currentState).connected(controller);
	}

	public void disconnected(Controller controller) {
		states.get(currentState).disconnected(controller);
	}

	public boolean buttonDown(Controller controller, int buttonCode) {
		if(Controllers.getControllers().contains(controller, false))
		return states.get(currentState).buttonDown(controller, buttonCode);
		else return false;
	}

	public boolean buttonUp(Controller controller, int buttonCode) {
		if(Controllers.getControllers().contains(controller, false))
		return states.get(currentState).buttonUp(controller, buttonCode);
		else return false;
	}

	public boolean axisMoved(Controller controller, int axisCode, float value) {
		if(Controllers.getControllers().contains(controller, false))
		return states.get(currentState).axisMoved(controller, axisCode, value);
		else return false;
	}

	public boolean povMoved(Controller controller, int povCode, PovDirection value) {
		return states.get(currentState).povMoved(controller, povCode, value);
	}

	public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
		return states.get(currentState).xSliderMoved(controller, sliderCode, value);
	}

	public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
		return states.get(currentState).ySliderMoved(controller, sliderCode, value);
	}

	public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
		return states.get(currentState).accelerometerMoved(controller, accelerometerCode, value);
	}
	public void dispose(){
		states.get(currentState).dispose();
	}
	
	public void changeState(int state){
		KambojaMain.saveGame();
		dispose();
		currentState = state;
		create();
		disposed = true;
	}

	public void resize(int width, int height) {
		states.get(currentState).resize(width, height);
	}

	@Override
	public boolean keyDown(int keycode) {
		return states.get(currentState).keyDown(keycode);
	}

	@Override
	public boolean keyUp(int keycode) {
		return states.get(currentState).keyUp(keycode);
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {

		return false;
	}

}

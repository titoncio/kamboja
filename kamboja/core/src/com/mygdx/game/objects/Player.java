package com.mygdx.game.objects;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;
import com.mygdx.game.KambojaMain;
import com.mygdx.game.controllers.Gamecube;
import com.mygdx.game.controllers.GenericController;
import com.mygdx.game.controllers.Playstation3;
import com.mygdx.game.controllers.XBox;
import com.mygdx.game.objects.shift.Barrier;
import com.mygdx.game.objects.shift.Shift;
import com.mygdx.game.objects.shift.Turret;
import com.mygdx.game.objects.weapon.Bazooka;
import com.mygdx.game.objects.weapon.DoublePistol;
import com.mygdx.game.objects.weapon.Flamethrower;
import com.mygdx.game.objects.weapon.Laser;
import com.mygdx.game.objects.weapon.Minigun;
import com.mygdx.game.objects.weapon.Mp5;
import com.mygdx.game.objects.weapon.Pistol;
import com.mygdx.game.objects.weapon.Shotgun;
import com.mygdx.game.objects.weapon.Weapon;
import com.mygdx.game.states.GameState;

public class Player implements Steerable<Vector2>{
	
	protected Body body;
	protected Vector2 angle = new Vector2();
	protected float maxLife = 70;
	protected float life = maxLife;
	protected float stamina = 0;
	protected int maxStamina = 10;
	protected float mana = 100;
	protected float speed = 10;
	
	float staminaWrnScl = 2;
	float staminaWrnAlpha = 0;

	private int id;
	private int buff = -1;
	private int deaths = 0;
	
	private int kills;
	private float angle_walking = 0;
	private float sprintCooldown = 0;
	private float hitTimer = 0;
	protected float spd = 1;
	private float atk = 1;
	private float def = 1;
	private float score = 0;
	private float buffTimer = 0;
	private float fallingTimer = 1;
	private float deathTimer = 0;
	private float imunity = 0;
	private float opacity = 1;
	private float flameTimer = 0;
	private float flameAtk = 1;
	private float gruntTimer = 0;
	private boolean inSpace = false;
	private boolean wearSpaceSuit = false;
		
	private Texture aim;
	private Texture atkTex, defTex, spdTex;
	private Texture stamina_on, stamina_off;
	private Texture space_skin;
	private TextureRegion space;
	private static Texture[] players;
	private TextureRegion player;
	private Animation<TextureRegion> legsAnimation;
	
	private boolean dead = false;
	private boolean throwBlood = false;
	private boolean isFalling = false;
	private boolean inputBlocked = false;	
	private boolean keyboard = false;
	public boolean said_ko = false;
	
	private Vector2 axisVel = new Vector2();
	private BitmapFont font;
	private GlyphLayout layout;
	private GameState state;
	private Weapon weapon;
	private Shift shift;
	protected ShapeRenderer sr;
	private static Sound sprint;
	private static Sound grunt[] = new Sound[5];
	private float legTimer = 0;
	private float legAngle = 0;
	private float dashImpulse = 3;
	private float spillTimer = 0;
	
	ParticleEffect fire;
	ParticleEffect wind;
	ParticleEffect shield;
	
	ArrayList<Ghost> ghosts;
	
	static{
		for(int i = 0; i < 5; i ++){
			grunt[i] = Gdx.audio.newSound(Gdx.files.internal("audio/grunt"+(i+1)+".ogg"));
		}

		sprint = Gdx.audio.newSound(Gdx.files.internal("audio/run.ogg"));
	}
	

	class PlayerFall{
		
		Player player;
		public PlayerFall(Player player){
			this.player = player;
		}
		
	}
	
	public void setInSpace(boolean inSpace, boolean wearSpaceSuit) {
		this.inSpace = inSpace;
		this.wearSpaceSuit = wearSpaceSuit;
	}
	
	public static int getSkinPositionByWeapon(int weapon) {
		switch (weapon) {
		case 0:
			return 2;
		case 1:
			return 0;
		default:
			return 1;
		}
	}

	public float getAngle(){
		return angle.angle() - (float)Math.sin(angle_walking)*5;
	}

	public float getShootingAngle(){
		return angle.angle();
	}
	
	public Vector2 getAngleVector(){
		return angle;
	}
	
	public void setAngle(Vector2 angle){
		this.angle = angle;
		this.legAngle  = angle.angle();
	}
	
	public TextureRegion getSpaceTexture(int posid) {
		return new TextureRegion(space_skin, (posid % 5)*32, (posid/5)*32, 32, 32);
	}
	
	public static TextureRegion getTexture(int playerid, int posid){
		if(players == null){
			players = new Texture[KambojaMain.getPlayerSkinsSize()];
			for(int i = 0; i < players.length; i ++){
				players[i] = new Texture("player/"+(i+1)+".png");
			}
		}
		return new TextureRegion(players[playerid], (posid % 5)*32, (posid/5)*32, 32, 32);
	}
	
	public void setFalling(){
		if(!isFalling){
			isFalling = true;
			inputBlocked = true;
			setFallingTimer(1);
			body.setLinearVelocity(0, 0);
			axisVel.set(0,0);
		}
		
	}
	
	public void dispose(){
		aim.dispose();
		getWeapon().dispose();
		atkTex.dispose();
		defTex.dispose();
		spdTex.dispose();
		font.dispose();
		sr.dispose();
		if(getShift() != null){
			getShift().dispose();
		}
	}
	
	public Player(Body body, int id, GameState state){
		this.body = body;
		this.setId(id);
		this.setState(state);
		body.setLinearDamping(30);
		body.setFixedRotation(true);
		
		stamina = 0;
		
		space_skin = new Texture("player/space_skin.png");
		
		stamina_off = new Texture("player/stamina_off.png");
		stamina_on = new Texture("player/stamina_on.png");
		
		ghosts = new ArrayList<Ghost>();
		
		
				
		fire = new ParticleEffect();
		fire.load(Gdx.files.internal("particles/firebuff.par"), Gdx.files.internal("particles"));
		fire.scaleEffect(1f/GameState.UNIT_SCALE / 3f);
		
		wind = new ParticleEffect();
		wind.load(Gdx.files.internal("particles/fast.par"), Gdx.files.internal("particles"));
		wind.scaleEffect(1f/GameState.UNIT_SCALE / 3f);
		
		shield = new ParticleEffect();
		shield.load(Gdx.files.internal("particles/shield.par"), Gdx.files.internal("particles"));
		shield.scaleEffect(1f/GameState.UNIT_SCALE / 3f);
		
		if(players == null){
			players = new Texture[KambojaMain.getPlayerSkinsSize()];
			for(int i = 0; i < players.length; i ++){
				players[i] = new Texture("player/"+(i+1)+".png");
			}
		}

		TextureRegion[] legFrames = new TextureRegion[10];
		
		for(int i = 0; i < 5; i ++){
			for(int j = 0; j < 2; j ++){
				legFrames[i + j*5] = new TextureRegion(players[id], i*32, j*32, 32, 32);
			}
		}
	
		
		legsAnimation = new Animation<TextureRegion>(1f,
				legFrames[5],
				legFrames[6],
				legFrames[7],
				legFrames[8],
				legFrames[7],
				legFrames[6],
				legFrames[5],
				legFrames[9],
				legFrames[4],
				legFrames[3],
				legFrames[4]
						
						);
		legsAnimation.setPlayMode(PlayMode.LOOP);
		
		Shape s = new CircleShape();
		s.setRadius(3/GameState.UNIT_SCALE);
		Fixture f = body.createFixture(s, 0.8f);
		f.setUserData(new PlayerFall(this));
		s.dispose();

		aim = new Texture("player/aim.png");
		
		if(KambojaMain.getControllers().get(id) instanceof KeyboardController){
			keyboard = true;
		}

		player = getTexture(KambojaMain.getControllers().get(id).player, 0);

		switch(KambojaMain.getControllers().get(id).weapon){
		case 0:
			setWeapon(new Pistol(body.getWorld(), this));
			player = getTexture(KambojaMain.getControllers().get(id).player, 2);
			space = getSpaceTexture(2);
			speed = (float) (Math.pow(KambojaMain.SENSITIVITY, -Pistol.WEIGHT) * 10);
			break;
		case 1:
			setWeapon(new DoublePistol(body.getWorld(), this));
			player = getTexture(KambojaMain.getControllers().get(id).player, 0);
			space = getSpaceTexture(0);
			speed = (float) (Math.pow(KambojaMain.SENSITIVITY, -DoublePistol.WEIGHT) * 10);
			break;
		case 2:
			setWeapon(new Minigun(body.getWorld(), this));
			player = getTexture(KambojaMain.getControllers().get(id).player, 1);
			space = getSpaceTexture(1);
			speed = (float) (Math.pow(KambojaMain.SENSITIVITY, -Minigun.WEIGHT) * 10);
			break;
		case 3:
			setWeapon(new Shotgun(body.getWorld(), this));
			player = getTexture(KambojaMain.getControllers().get(id).player, 1);
			space = getSpaceTexture(1);
			speed = (float) (Math.pow(KambojaMain.SENSITIVITY, -Shotgun.WEIGHT) * 10);
			break;
		case 4:
			setWeapon(new Mp5(body.getWorld(), this));
			player = getTexture(KambojaMain.getControllers().get(id).player, 1);
			space = getSpaceTexture(1);
			speed = (float) (Math.pow(KambojaMain.SENSITIVITY, -Mp5.WEIGHT) * 10);
			break;
		case 5:
			setWeapon(new Flamethrower(body.getWorld(), this));
			player = getTexture(KambojaMain.getControllers().get(id).player, 1);
			space = getSpaceTexture(1);
			speed = (float) (Math.pow(KambojaMain.SENSITIVITY, -Flamethrower.WEIGHT) * 10);
			break;
		case 6:
			setWeapon(new Bazooka(body.getWorld(), this));
			player = getTexture(KambojaMain.getControllers().get(id).player, 1);
			space = getSpaceTexture(1);
			speed = (float) (Math.pow(KambojaMain.SENSITIVITY, -Bazooka.WEIGHT) * 10);
			break;
		case 7:
			setWeapon(new Laser(body.getWorld(), this));
			player = getTexture(KambojaMain.getControllers().get(id).player, 1);
			space = getSpaceTexture(1);
			speed = (float) (Math.pow(KambojaMain.SENSITIVITY, -Laser.WEIGHT) * 10);
			break;
		}
		
		atkTex = new Texture("imgs/attack.png");

		defTex = new Texture("imgs/shield.png");

		spdTex = new Texture("imgs/speed.png");
				
		FreeTypeFontGenerator ftfg;
		FreeTypeFontParameter param;
		ftfg = new FreeTypeFontGenerator(Gdx.files.internal("fonts/outlander.ttf"));
		param = new FreeTypeFontParameter();
		param.size = (int) (50 * Gdx.graphics.getDensity());
		param.color = new Color(1, 0.9f, 0.9f, 1);
		param.shadowColor = new Color(0, 0, 0, 0.7f);
		param.shadowOffsetX = 3;
		param.shadowOffsetY = 3;
		font = ftfg.generateFont(param);
		ftfg.dispose();
		
		if(layout == null)
		layout = new GlyphLayout();
		
		sr = new ShapeRenderer();
		
		if(calculateDifferenceBetweenAngles(getAngle(), legAngle) > 30){
			legAngle = getAngle() + 30;
		}
		if(calculateDifferenceBetweenAngles(getAngle(), legAngle) < -30){
			legAngle = getAngle() - 30;
		}
		
	}
	
	public static double calculateDifferenceBetweenAngles(double firstAngle, double secondAngle){
	        double difference = secondAngle - firstAngle;
	        while (difference < -180) difference += 360;
	        while (difference > 180) difference -= 360;
	        return difference;
	}
	
	public float getScore() {
		return score;
	}
	
	public void reduceScore(float amount) {
		score -= amount;
	}
	
	public void takeDamage(float amount, Player owner, boolean showBlood){
		if(imunity <= 0){
			life -= amount * def;
			
			if(owner != null) {
				owner.score += amount*def;
			}
						
			if(showBlood)
			state.showBlood(body.getWorldCenter());

			hitTimer = 1f;
			if(getLife() <= 0){
				if(!isDead()){
					deaths++;
					if(owner != null){
						owner.kills ++;
						owner.ghosts.add(new Ghost(getId(), getPosition()));
						owner.score += 100;
					}
					setDead(true);
					body.getFixtureList().get(0).setSensor(true);
					getState().showSkull(body.getWorldCenter(), getAngle());
				}
			}
			if(gruntTimer < 0){
				if(GameState.SFX)
				grunt[(int)(Math.random()*5)].play();
				gruntTimer = 0.5f;
			}
		}
	}
	
	public Body getBody(){
		return body;
	}

	public float getWidth(){
		return player.getRegionWidth();
	}
	
	public void render(SpriteBatch sb){
		font.setColor(1, 1, 1, 1);

		gruntTimer -= Gdx.graphics.getDeltaTime();
		
		sb.setProjectionMatrix(getState().getCamera().combined);
		
		if(getShift() != null)
		getShift().render(sb);
				
		sb.begin();
		sb.setColor(1-Math.max(0, Math.min(imunity, 1)), 1 - hitTimer, 1 - hitTimer, opacity);
		TextureRegion leg = legsAnimation.getKeyFrame(legTimer);
		sb.draw(leg,
				body.getWorldCenter().x - leg.getRegionWidth()/2 / GameState.UNIT_SCALE,
				body.getWorldCenter().y - leg.getRegionHeight()/2 / GameState.UNIT_SCALE,
				leg.getRegionWidth()/2 /GameState.UNIT_SCALE,
				leg.getRegionHeight()/2 /GameState.UNIT_SCALE,
				leg.getRegionWidth() /GameState.UNIT_SCALE,
				leg.getRegionHeight() /GameState.UNIT_SCALE,
				isFalling() ? Math.max(0.3f, getFallingTimer()) : 1,
				isFalling() ? Math.max(0.3f, getFallingTimer()) : 1,
				270 - legAngle);
		

		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);


		sb.draw(player,
				body.getWorldCenter().x - player.getRegionWidth()/2 / GameState.UNIT_SCALE,
				body.getWorldCenter().y - player.getRegionHeight()/2 / GameState.UNIT_SCALE,
				player.getRegionWidth()/2 /GameState.UNIT_SCALE,
				player.getRegionHeight()/2 /GameState.UNIT_SCALE,
				player.getRegionWidth() /GameState.UNIT_SCALE,
				player.getRegionHeight() /GameState.UNIT_SCALE,
				isFalling() ? Math.max(0.3f, getFallingTimer()) : 1,
				isFalling() ? Math.max(0.3f, getFallingTimer()) : 1,
				270 - getAngle());
		
		if(wearSpaceSuit) {
			sb.draw(space,
					body.getWorldCenter().x - space.getRegionWidth()/2 / GameState.UNIT_SCALE,
					body.getWorldCenter().y - space.getRegionHeight()/2 / GameState.UNIT_SCALE,
					space.getRegionWidth()/2 /GameState.UNIT_SCALE,
					space.getRegionHeight()/2 /GameState.UNIT_SCALE,
					space.getRegionWidth() /GameState.UNIT_SCALE,
					space.getRegionHeight() /GameState.UNIT_SCALE,
					isFalling() ? Math.max(0.3f, getFallingTimer()) : 1,
					isFalling() ? Math.max(0.3f, getFallingTimer()) : 1,
					270 - getAngle());
		}
		sb.end();

		sb.setShader(null);

		Gdx.gl.glDisable(GL20.GL_BLEND);
	
		
		sb.begin();

		for(int i = ghosts.size() - 1; i >= 0; i --){
			Ghost g = ghosts.get(i);
			
			switch(g.color){
			case 0:
				sb.setColor(0.7f, 0.7f, 1f, opacity);
				break;
			case 1:
				sb.setColor(1f, 0.7f, 0.7f, opacity);
				break;
			case 2:
				sb.setColor(0.7f, 1f, 0.7f, opacity);
				break;
			case 3:
				sb.setColor(1f, 1f, 0.7f, opacity);
				break;
				
			}
			
			g.render(sb);
		}
		
		sb.setColor(1, 1, 1, 0.4f);
		float mag = 0.5f;
		float scl = 0.3f;
		for(int i = 0; i < maxStamina; i ++){
			
			Vector2 posAng = getPosition().cpy().add((float)Math.cos(i * (Math.PI*2)/maxStamina) * mag, (float)Math.sin(i * (Math.PI*2)/maxStamina) * mag);
			
			sb.draw(stamina_off,
					posAng.x - (stamina_off.getWidth() / GameState.UNIT_SCALE * scl) /2,
					posAng.y - (stamina_off.getHeight() / GameState.UNIT_SCALE * scl)/2,
					stamina_off.getWidth() / GameState.UNIT_SCALE * scl,
					stamina_off.getHeight() / GameState.UNIT_SCALE * scl);
		}
		sb.setColor(1, 1, 1, 1);
		
		for(int i = 0; i <(int)stamina; i ++){
			
			Vector2 posAng = getPosition().cpy().add((float)Math.cos(i * (Math.PI*2)/maxStamina) * mag, (float)Math.sin(i * (Math.PI*2)/maxStamina) * mag);
			
			sb.draw(stamina_on,
					posAng.x - (stamina_on.getWidth() / GameState.UNIT_SCALE * scl) /2,
					posAng.y - (stamina_on.getHeight() / GameState.UNIT_SCALE * scl)/2,
					stamina_on.getWidth() / GameState.UNIT_SCALE * scl,
					stamina_on.getHeight() / GameState.UNIT_SCALE * scl);
		}
		
		Vector2 posAng = getPosition().cpy().add((float)Math.cos((int)(stamina-1) * (Math.PI*2)/maxStamina) * mag, (float)Math.sin((int)(stamina-1) * (Math.PI*2)/maxStamina) * mag);
		
		sb.setColor(1, 1, 1, staminaWrnAlpha);
		
		sb.draw(stamina_on,
				posAng.x - (stamina_on.getWidth() / GameState.UNIT_SCALE * scl * staminaWrnScl) /2,
				posAng.y - (stamina_on.getHeight() / GameState.UNIT_SCALE * scl * staminaWrnScl)/2,
				stamina_on.getWidth() / GameState.UNIT_SCALE * scl * staminaWrnScl,
				stamina_on.getHeight() / GameState.UNIT_SCALE * scl * staminaWrnScl);

		sb.setColor(1, 1, 1, 1);
		
		if(buff == Item.ATTACK && buffTimer >= 0){
			if(fire.isComplete()){
				fire.reset();
			}
		}
		else{
			fire.allowCompletion();
		}
		
		if(buff == Item.SPEED && buffTimer >= 0){
			if(wind.isComplete()){
				wind.reset();
			}
		}
		else{
			wind.allowCompletion();
		}
		
		if(buff == Item.DEFFENSE && buffTimer >= 0){
			if(shield.isComplete()){
				shield.reset();
			}
		}
		else{
			shield.allowCompletion();
		}
		
		sb.setColor(1, 1, 1, opacity);
		fire.draw(sb);
		wind.draw(sb);
		shield.draw(sb);
		
		sb.setColor(1, 1, 1, 1);
		
		sb.end();

		getWeapon().render(sb);
			
		sb.setProjectionMatrix(getState().getCamera().combined);
		sb.begin();
		
		
		
		switch(getId()){
		case 0:
			sb.setColor(0, 0, 1, 1);
			break;
		case 1:
			sb.setColor(1, 0, 0, 1);
			break;
		case 2:
			sb.setColor(0, 1, 0, 1);
			break;
		case 3:
			sb.setColor(1, 1, 0, 1);
			break;
		}
		
		sb.setColor(1, 1, 1, 1);
		

		
		if(isDead() && !throwBlood){
			for(int i = 0; i < 5; i ++)
			getState().showBlood(body.getWorldCenter());
			throwBlood = true;
		}
		
		sb.setProjectionMatrix(Util.getNormalProjection());
		
		if(keyboard){
			switch(getId()){
			case 0:
				sb.setColor(0, 0, 1, 1);
				break;
			case 1:
				sb.setColor(1, 0, 0, 1);
				break;
			case 2:
				sb.setColor(0, 1, 0, 1);
				break;
			case 3:
				sb.setColor(1, 1, 0, 1);
			}
			sb.draw(aim,
					Gdx.input.getX(),
					1080 - Gdx.input.getY() - aim.getHeight()*3,
					aim.getWidth()*3,
					aim.getHeight()*3
					);
			
			sb.setColor(1, 1, 1, 1);
			
			if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)){
				if(getState().getTimer() > 5 && !(getState().isIntro() || getState().isOutro())){
					getState().pauseUnpause();
				}
			}
		}

		switch(getId()){
		case 0:
			font.setColor(90/255f, 56/255f, 207/255f, 1f);
			break;
		case 1:
			font.setColor(203/255f, 30/255f, 48/255f, 1f);
			break;
		case 2:
			font.setColor(43/255f, 179/255f, 169/255f, 1f);
			break;
		case 3:
			font.setColor(247/255f, 215/255f, 71/255f, 1f);
		}
		
		if(!isDead()){
		layout.setText(font, KambojaMain.getControllers().get(getId()).getName());
		font.draw(sb, KambojaMain.getControllers().get(getId()).getName(),
				(body.getWorldCenter().x - getState().getCamera().position.x) / getState().getCamera().zoom + 1920/2 - layout.width/2,
				(body.getWorldCenter().y - getState().getCamera().position.y) / getState().getCamera().zoom + 1080/2 + layout.height*3);
		}
		sb.end();
		
		
	}

	public void revive(){
		
		setDead(false);
		setFallingTimer(1);
		setFalling(false);
		axisVel.set(0, 0);
		weapon.analog = 0;
		inputBlocked = false;
		throwBlood = false;		
		imunity = 1;
		life = maxLife;
		body.getFixtureList().get(0).setSensor(false);
		
		MapLayer ml = getState().getTiledMap().getLayers().get("Player");
		for(MapObject mo : ml.getObjects()){
			if(mo.getProperties().get("type").equals("player"+getId())){
				float x = mo.getProperties().get("x", Float.class);
				float y = mo.getProperties().get("y", Float.class);
				float width = mo.getProperties().get("width", Float.class);
				float height = mo.getProperties().get("height", Float.class);
				float pAngle = mo.getProperties().get("angle", Float.class);
				body.setTransform(new Vector2((x+width/2) / GameState.UNIT_SCALE, (y+height/2) / GameState.UNIT_SCALE), 0);

				angle = new Vector2((float)Math.sin(Math.toRadians(pAngle)), (float)Math.cos(Math.toRadians(pAngle)));
			}
		}
	}
	
	public void setBuff(int id){
		if(id == Item.LIFE){
			life += 30;
			if(getLife() > maxLife) life = maxLife;
		}
		else if(id == Item.TURRET){
			setShift(new Turret(this));
		}
		else if(id == Item.BARRIER){
			setShift(new Barrier(this));
		}
		else{
			buffTimer = 5;
			buff = id;
		}
	}
	
	Player flamePlayer;
	public void applyFlame(float atk, Player flamePlayer){
		flameTimer = 0.5f;
		
		this.flamePlayer = flamePlayer;
		
		if(atk > flameAtk)
		flameAtk = atk;
	}
	
	float sptCooldown = 0.1f;
	
	public void update(float delta){
		if(!isDead())
		body.applyForceToCenter(axisVel.cpy().nor().scl(speed * spd * (inSpace ? 0.1f : 1f)), true);
		
		if(inSpace) {
			if(body.getLinearVelocity().len() > 3) {
				body.setLinearVelocity(body.getLinearVelocity().cpy().nor().scl(3));
			}
		}
		
		if(isFalling()){
			setFallingTimer(getFallingTimer() - delta);
			setAngle(new Vector2((float)Math.sin(getFallingTimer()*10), (float)Math.cos(getFallingTimer()*10)));
			
			if(getFallingTimer() <= 0){
				takeDamage(1000, null, false);
			}
		}

		for(int i = ghosts.size() - 1; i >= 0; i --){
			Ghost g = ghosts.get(i);
			g.update(delta);
			g.setTargetPosition(getPosition());
		}
		
		fire.update(delta);
		fire.setPosition(body.getWorldCenter().x, body.getWorldCenter().y);
		wind.update(delta);
		wind.setPosition(body.getWorldCenter().x, body.getWorldCenter().y);
		shield.update(delta);
		shield.setPosition(body.getWorldCenter().x, body.getWorldCenter().y);
		
		if(!dead){
			spillTimer -= delta;
			if(spillTimer < 0){
				spillTimer = life/maxLife;
				
				if(life < maxLife){
					if(body.getLinearVelocity().len() > 1){
						getState().showBloodSpill(body.getWorldCenter(), ((maxLife - life)/maxLife) * 0.5f);
					}
				}
			}
		}
		
		legTimer += body.getLinearVelocity().len()/10.0f;
		
		if(calculateDifferenceBetweenAngles(getAngle(), legAngle) > 30){
			legAngle = getAngle() + 30;
		}
		if(calculateDifferenceBetweenAngles(getAngle(), legAngle) < -30){
			legAngle = getAngle() - 30;
		}

		flameTimer -= delta;

		if(flameTimer > 0){
			takeDamage(Flamethrower.DAMAGE * flameAtk, flamePlayer, false);
			state.screenshake(0.03f);
		}
		else{
			flameAtk = 1;
		}
		
		int lastStamina = (int) stamina;
		
		stamina += delta;
		if(stamina > maxStamina) stamina = maxStamina;
		
		if((int) stamina != lastStamina){
			staminaWrnAlpha = 1;
			staminaWrnScl = 1;
		}
		
		staminaWrnAlpha += (0 - staminaWrnAlpha)/10.0f;
		staminaWrnScl += (2 - staminaWrnScl)/10.0f;
		
		
		if(getShift() != null)
		getShift().update(delta);
		
		setSprintCooldown(getSprintCooldown() - delta);

		mana += 1f * delta;
		
		if(mana > 100) mana = 100;
		
		if(keyboard && !isDead() && !inputBlocked){

			Vector2 mouseTransformed = new Vector2(
			getState().getCamera().position.x + (Gdx.input.getX() - 1920/2)*getState().getCamera().zoom,
			getState().getCamera().position.y + (1080 - Gdx.input.getY() - 1080/2)*getState().getCamera().zoom);
			
			angle = mouseTransformed.cpy().sub(body.getWorldCenter().cpy()).nor();
			angle.y = -angle.y;
			
			if(Gdx.input.isTouched() && !isDead() && !inputBlocked){
				getWeapon().analog = 1.0f;
			}
			else{
				getWeapon().analog = 0.0f;
			}
			
			if(!isDead() && !inputBlocked){
			axisVel.set(
					Gdx.input.isKeyPressed(Keys.A) ? -1 : (Gdx.input.isKeyPressed(Keys.D) ? 1 : 0),
					Gdx.input.isKeyPressed(Keys.W) ? 1 : (Gdx.input.isKeyPressed(Keys.S) ? -1 : 0)
						);
			
			axisVel.nor();
			}
			
			if(Gdx.input.isKeyJustPressed(Keys.SPACE) && !isDead() && !inputBlocked){
				if(getSprintCooldown() < 0){
					dash();
					}
			}

		}
		
		if(!isDead()){
			if(body.getLinearVelocity().len2() < 0.1){
				angle_walking += (-angle_walking)/5f;
			}
			else{
				angle_walking += delta*6f;
				if(angle_walking > Math.PI*2){
					angle_walking = 0;
				}
			}
		}
		
		buffTimer -= delta;
		
		if(buffTimer <= 0){
			setBuff(-1);
		}
		
		setAtk(1);
		def = 1;
		spd = 1;
		
		switch(getBuff()){
		case Item.ATTACK:
			setAtk(2);
			break;
		case Item.DEFFENSE:
			def = 2;
			break;
		case Item.SPEED:
			spd = 1.6f;
			break;
		}
		
		if(getLife() <= 0 && !isDead()){
			setDead(true);
			body.getFixtureList().get(0).setSensor(true);
			//deaths++;
			System.out.println("Not dead yet, killing");
		}
		
		if(isDead()){
			getWeapon().analog = 0;
			
			body.setLinearVelocity(body.getLinearVelocity().cpy().scl(0.9f));
			
			deathTimer += delta;
			
			if(deathTimer > 2){
				if(KambojaMain.getDeathsNumber() == -1){
					revive();
				}
				else{
					if(getDeaths() < KambojaMain.getDeathsNumber()){
						revive();
					}
				}
				deathTimer = 0;
			}
			
			opacity -= delta;
			if(opacity < 0) opacity = 0;
		}
		else{
			deathTimer = 0;
			
			opacity += delta;
			if(opacity > 1) opacity = 1;
		}
		
		if(!getState().isPause()){
			imunity -= delta;
		}

		body.setTransform(body.getWorldCenter(), (float)(2*Math.PI - angle.angleRad()));
		if(!isDead())
		getWeapon().update(delta);
		
		hitTimer -= delta;
		if(hitTimer < 0) hitTimer = 0;
		
		if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) && keyboard && !isDead() && !inputBlocked){
			if(getShift() != null)
			getShift().fire();
		}
		if(Float.isNaN(body.getWorldCenter().x)){
			System.out.println("NaN detected, exiting");
			System.out.println("Velocity: " + body.getLinearVelocity());
			System.exit(0);
		}
	}
	
	public void dash() {
		if(stamina >= 1){
			body.applyLinearImpulse(body.getLinearVelocity().cpy().nor().scl(dashImpulse, dashImpulse), body.getWorldCenter(), true);
			setSprintCooldown(sptCooldown);
			if(GameState.SFX)
			sprint.play(GameState.VOLUME);
			stamina --;
		}
	}
	
	public void connected(Controller controller) {
		
	}

	public void disconnected(Controller controller) {
		
	}

	public boolean buttonDown(Controller controller, int buttonCode) {
		if(!isDead() && !inputBlocked){
			int z = 0;
			
			if(controller.getName().equals(Gamecube.getID())){
				z = Gamecube.Z;
			}
			if(controller.getName().equals(XBox.getID())){				
				z = XBox.BUTTON_RB;
			}
			else if(controller.getName().toUpperCase().contains("SONY") || controller.getName().toUpperCase().contains("PLAYSTATION")){
				z = Playstation3.R1;
			}
			else{
				z = GenericController.R1;
				
				if(buttonCode == GenericController.R2){
					if(getSprintCooldown() < 0){
						dash();
					}
					
				}
				if(buttonCode == GenericController.L2){
					getWeapon().analog = 1;
				}
			}
			
			if(buttonCode == z){
				if(getShift() != null)
				getShift().fire();
			}

		}

		
		return false;
	}

	public boolean buttonUp(Controller controller, int buttonCode) {
		
		if(controller.getName().equals(Gamecube.getID())){
		}
		if(controller.getName().equals(XBox.getID())){				
		}
		else if(controller.getName().toUpperCase().contains("SONY") || controller.getName().toUpperCase().contains("PLAYSTATION")){
		}
		else{
			if(buttonCode == GenericController.L2){
				getWeapon().analog = 0;
			}
		}
		
		return false;
	}

	
	public boolean axisMoved(Controller controller, int axisCode, float value) {
		if(!isDead() && !inputBlocked){
			int xAxis = 0;
			int yAxis = 0;
			
			int xCam = 0;
			int yCam = 0;
			
			if(controller.getName().equals(Gamecube.getID())){
				xAxis = Gamecube.MAIN_X;
				yAxis = Gamecube.MAIN_Y;
				
				xCam = Gamecube.CAMERA_X;
				yCam = Gamecube.CAMERA_Y;
				
				if(axisCode == Gamecube.ANAL_R){
					if(value > 0.7){
						if(getSprintCooldown() < 0){
							dash();
						}
					}
				}
				if(axisCode == Gamecube.ANAL_L){
					getWeapon().analog = value;
				}
			}
			else if(controller.getName().toUpperCase().contains("XBOX") && controller.getName().contains("360")){
				xAxis = XBox.AXIS_LEFT_X;
				yAxis = XBox.AXIS_LEFT_Y;
				
				xCam = XBox.AXIS_RIGHT_X;
				yCam = XBox.AXIS_RIGHT_Y;
				
				if(axisCode == XBox.AXIS_RIGHT_TRIGGER){
					if(value < -0.7){
						if(getSprintCooldown() < 0){
							dash();
						}
					}
				}
				if(axisCode == XBox.AXIS_LEFT_TRIGGER){
					if(value >= 0){
						getWeapon().analog = value;
					}
				}
			}
			else if(controller.getName().toUpperCase().contains("SONY") || controller.getName().toUpperCase().contains("PLAYSTATION")){
				xAxis = Playstation3.LEFT_X;
				yAxis = Playstation3.LEFT_Y;
				
				xCam = Playstation3.RIGHT_X;
				yCam = Playstation3.RIGHT_Y;
				
				if(axisCode == Playstation3.R2){
					if(value < -0.7){
						if(getSprintCooldown() < 0){
							dash();
						}
					}
				}
				if(axisCode == Playstation3.L2){
					if(value >= 0){
						getWeapon().analog = value;
					}
				}
			}
			else{
				xAxis = GenericController.LEFT_X;
				yAxis = GenericController.LEFT_Y;
				
				xCam = GenericController.RIGHT_X;
				yCam = GenericController.RIGHT_Y;
			}
			
			if(axisCode == xAxis){
				if(Math.abs(value) > 0.1){
					axisVel.x = value;
				}
				else{
					axisVel.x = 0;
				}
			}
			
			if(axisCode == yAxis){
				if(Math.abs(value) > 0.1){
					axisVel.y = -value;
				}
				else{
					axisVel.y = 0;
				}
			}
			
			if(axisCode == xCam){
				angle.x = value;
			}
			
			if(axisCode == yCam){
				angle.y = value;
			}
		}
		
		return false;
	}

	public boolean povMoved(Controller controller, int povCode, PovDirection value) {
		return false;
	}

	public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
		return false;
	}

	public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
		return false;
	}

	public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
		return false;
	}

	public float getMaxLinearSpeed() {return 0;}
	public void setMaxLinearSpeed(float maxLinearSpeed) {}
	public float getMaxLinearAcceleration() {return 0;}
	public void setMaxLinearAcceleration(float maxLinearAcceleration) {}
	public float getMaxAngularSpeed() {return 0;}
	public void setMaxAngularSpeed(float maxAngularSpeed) {}
	public float getMaxAngularAcceleration() {return 0;}
	public void setMaxAngularAcceleration(float maxAngularAcceleration) {}
	public Vector2 getPosition() {return body.getWorldCenter();}
	public float getOrientation() {return body.getAngle();}
	public Vector2 getLinearVelocity() {return body.getLinearVelocity();}
	public float getAngularVelocity() {return body.getAngularVelocity();}
	public float getBoundingRadius() {return 0;}
	public boolean isTagged() {return false;}
	public void setTagged(boolean tagged) {}
	public Vector2 newVector() {return null;}
	public float vectorToAngle(Vector2 vector) {return 0;}
	public Vector2 angleToVector(Vector2 outVector, float angle) {return null;}

	public float getLife() {
		return life;
	}
	public float getMana() {
		return mana;
	}

	public void endSound(){
		getWeapon().endSound();
	}

	public void decreaseMana(float dec) {
		mana -= dec;
	}

	public int getDeaths() {
		return deaths;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	public Weapon getWeapon() {
		return weapon;
	}

	public void setWeapon(Weapon weapon) {
		this.weapon = weapon;
	}

	public boolean isFalling() {
		return isFalling;
	}

	public void setFalling(boolean isFalling) {
		this.isFalling = isFalling;
	}

	public float getAtk() {
		return atk;
	}

	public void setAtk(float atk) {
		this.atk = atk;
	}

	public GameState getState() {
		return state;
	}

	public void setState(GameState state) {
		this.state = state;
	}

	public Shift getShift() {
		return shift;
	}

	public void setShift(Shift shift) {
		this.shift = shift;
	}

	public float getFallingTimer() {
		return fallingTimer;
	}

	public void setFallingTimer(float fallingTimer) {
		this.fallingTimer = fallingTimer;
	}

	public int getBuff() {
		return buff;
	}

	public float getSprintCooldown() {
		return sprintCooldown;
	}

	public void setSprintCooldown(float sprintCooldown) {
		this.sprintCooldown = sprintCooldown;
	}

	public int getKills() {
		return kills;
	}

	public void setKills(int kills) {
		this.kills = kills;
	}
	

}

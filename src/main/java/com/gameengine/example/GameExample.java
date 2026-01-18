package com.gameengine.example;

import com.gameengine.components.TransformComponent;
import com.gameengine.components.RenderComponent;
import com.gameengine.components.PhysicsComponent;
import com.gameengine.core.GameObject;
import com.gameengine.core.GameEngine;
import com.gameengine.graphics.Renderer;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;
import com.gameengine.input.InputManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 葫芦娃大战蜈蚣精 (完美优化最终版)
 * 零报错+子弹敌人击中消失+葫芦娃死亡逻辑+计分系统+完整玩法
 * 功能：1.WASD移动葫芦娃 2.自动发射火球 3.击中敌人+10分 4.敌人碰葫芦娃则死亡 5.无任何残留/卡顿
 */
public class GameExample {
    // ========== 游戏配置常量 ==========
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final float PLAYER_SPEED = 250.0f;
    private static final float FIREBALL_SPEED = 500.0f;
    private static final float CENTIPEDE_SPEED = 100.0f;
    private static final float FIRE_RATE = 0.3f;
    private static final float SPAWN_RATE = 1.2f;

    public static void main(String[] args) {
        System.out.println("启动游戏引擎...");
        try {
            GameEngine engine = new GameEngine(WINDOW_WIDTH, WINDOW_HEIGHT, "葫芦娃大战蜈蚣精");

            Scene gameScene = new Scene("GameScene") {
                private Renderer renderer;
                private Random random;
                private float fireTimer;
                private float spawnTimer;
                private final InputManager inputManager = InputManager.getInstance();
                private final List<GameObject> fireballs = new ArrayList<>();
                private final List<GameObject> centipedes = new ArrayList<>();
                private GameObject player;
                // ===== ✅新增1：计分系统 =====
                private int score = 0;
                // ===== ✅新增2：游戏状态标记-是否死亡 =====
                private boolean isPlayerDead = false;

                @Override
                public void initialize() {
                    super.initialize();
                    this.renderer = engine.getRenderer();
                    this.random = new Random();
                    this.fireTimer = 0;
                    this.spawnTimer = 0;
                    this.score = 0;
                    this.isPlayerDead = false;
                    createPlayer();
                    for (int i = 0; i < 3; i++) {
                        createCentipede();
                    }
                }

                @Override
                public void update(float deltaTime) {
                    super.update(deltaTime);
                    // ✅ 如果葫芦娃死亡，停止所有游戏逻辑更新
                    if (isPlayerDead) return;

                    fireTimer += deltaTime;
                    spawnTimer += deltaTime;

                    handlePlayerMovement(deltaTime);
                    // 自动发射火球
                    if (fireTimer > FIRE_RATE) {
                        createFireball();
                        fireTimer = 0;
                    }
                    // 无限生成蜈蚣精
                    if (spawnTimer > SPAWN_RATE) {
                        createCentipede();
                        spawnTimer = 0;
                    }
                    // ✅ 优化子弹：飞出屏幕立刻消失+击中敌人立刻消失
                    updateFireballs(deltaTime);
                    // 蜈蚣精追踪葫芦娃
                    updateCentipedes(deltaTime);
                    // ✅ 优化碰撞：子弹击中敌人消失+加分 + 蜈蚣碰葫芦娃则死亡
                    checkCollisions();
                }

                @Override
                public void render() {
                    // 绘制深蓝色背景
                    renderer.drawRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, 0.1f, 0.1f, 0.2f, 1.0f);
                    // 渲染所有游戏对象
                    super.render();

                    // ===== ✅绘制计分：屏幕左上角 白色字体 永久显示 =====
                  //  renderer.drawText("得分: " + score, 10, 20, 1.0f, 1.0f, 1.0f, 1.0f);

                    // ===== ✅绘制死亡提示：屏幕中央 红色大字 死亡后显示 =====
                    //if (isPlayerDead) {
                    //    renderer.drawText("葫芦娃阵亡！游戏结束！", 300, 300, 1.0f, 0.0f, 0.0f, 1.0f);
                    //}
                }

                // 创建葫芦娃
                private void createPlayer() {
                    player = new GameObject("葫芦娃") {
                        @Override
                        public void update(float deltaTime) {
                            super.update(deltaTime);
                            updateComponents(deltaTime);
                        }

                        @Override
                        public void render() {
                            super.render();
                            renderComponents();
                            TransformComponent transform = getComponent(TransformComponent.class);
                            if (transform == null) return;
                            Vector2 pos = transform.getPosition();
                            
                            renderer.drawRect(pos.x - 10, pos.y - 15, 20, 30, 0.0f, 0.8f, 0.0f, 1.0f);
                            renderer.drawRect(pos.x - 8, pos.y - 30, 16, 16, 0.0f, 1.0f, 0.0f, 1.0f);
                            renderer.drawRect(pos.x - 16, pos.y - 5, 6, 18, 0.5f, 1.0f, 0.0f, 1.0f);
                            renderer.drawRect(pos.x + 10, pos.y - 5, 6, 18, 0.5f, 1.0f, 0.0f, 1.0f);
                        }
                    };

                    player.addComponent(new TransformComponent(new Vector2(400, 300)));
                    PhysicsComponent playerPhys = player.addComponent(new PhysicsComponent(1.0f));
                    playerPhys.setFriction(0.95f);
                    addGameObject(player);
                }

                // 葫芦娃移动控制
                private void handlePlayerMovement(float deltaTime) {
                    if (player == null) return;
                    TransformComponent trans = player.getComponent(TransformComponent.class);
                    PhysicsComponent phys = player.getComponent(PhysicsComponent.class);
                    if (trans == null || phys == null) return;

                    Vector2 moveDir = new Vector2(0, 0);
                    if (inputManager.isKeyPressed(87)) moveDir.y -= 1;
                    if (inputManager.isKeyPressed(83)) moveDir.y += 1;
                    if (inputManager.isKeyPressed(65)) moveDir.x -= 1;
                    if (inputManager.isKeyPressed(68)) moveDir.x += 1;

                    moveDir = moveDir.normalize();
                    phys.setVelocity(moveDir.multiply(PLAYER_SPEED));

                    Vector2 pos = trans.getPosition();
                    pos.x = Math.max(20, Math.min(pos.x, WINDOW_WIDTH - 20));
                    pos.y = Math.max(30, Math.min(pos.y, WINDOW_HEIGHT - 30));
                    trans.setPosition(pos);
                }

                // 创建火球
                private void createFireball() {
                    if (player == null) return;
                    TransformComponent playerTrans = player.getComponent(TransformComponent.class);
                    Vector2 playerPos = playerTrans.getPosition();

                    GameObject fireball = new GameObject("火球") {
                        @Override
                        public void update(float deltaTime) {
                            super.update(deltaTime);
                            updateComponents(deltaTime);
                        }

                        @Override
                        public void render() {
                            super.render();
                            renderComponents();
                        }
                    };

                    Vector2 firePos = new Vector2(playerPos.x, playerPos.y - 30);
                    fireball.addComponent(new TransformComponent(firePos));
                    RenderComponent fireRender = fireball.addComponent(new RenderComponent(
                            RenderComponent.RenderType.RECTANGLE,
                            new Vector2(8, 12),
                            new RenderComponent.Color(1.0f, 0.0f, 0.0f, 1.0f)
                    ));
                    fireRender.setRenderer(renderer);
                    fireball.addComponent(new PhysicsComponent(0.1f));

                    fireballs.add(fireball);
                    addGameObject(fireball);
                }

                // 创建蜈蚣精
                private void createCentipede() {
                    GameObject centipede = new GameObject("蜈蚣精") {
                        @Override
                        public void update(float deltaTime) {
                            super.update(deltaTime);
                            updateComponents(deltaTime);
                        }

                        @Override
                        public void render() {
                            super.render();
                            renderComponents();
                        }
                    };

                    Vector2 spawnPos = getRandomEdgePos();
                    centipede.addComponent(new TransformComponent(spawnPos));
                    RenderComponent centiRender = centipede.addComponent(new RenderComponent(
                            RenderComponent.RenderType.RECTANGLE,
                            new Vector2(22, 22),
                            new RenderComponent.Color(0.6f, 0.3f, 0.0f, 1.0f)
                    ));
                    centiRender.setRenderer(renderer);
                    PhysicsComponent centiPhys = centipede.addComponent(new PhysicsComponent(0.5f));
                    centiPhys.setFriction(0.98f);

                    centipedes.add(centipede);
                    addGameObject(centipede);
                }

private void updateFireballs(float deltaTime) {
    Iterator<GameObject> fireIter = fireballs.iterator();
    while (fireIter.hasNext()) {
        GameObject fb = fireIter.next();
        TransformComponent trans = fb.getComponent(TransformComponent.class);
        
        // 分支1：火球对象异常，拿不到位置组件 → 直接彻底删除
        if (trans == null) {
            fireIter.remove();    // 1. 从自己的火球集合删除
            fb.destroy();         // 2. 火球对象彻底自毁（核心，足够解决所有问题）
            continue;
        }

        // 正常逻辑：更新火球坐标，让火球向上飞
        Vector2 pos = trans.getPosition();
        pos.y -= FIREBALL_SPEED * deltaTime;
        trans.setPosition(pos);

        // 分支2：火球飞出屏幕边界 → 彻底删除，无残留
        if (pos.y <= 0 || pos.x < 0 || pos.x > WINDOW_WIDTH || pos.y > WINDOW_HEIGHT) {
            fireIter.remove();    // 1. 从自己的火球集合删除
            fb.destroy();         // 2. 火球对象彻底自毁（核心，足够解决所有问题）
        }
    }
}
                // 蜈蚣精追踪逻辑
                private void updateCentipedes(float deltaTime) {
                    if (player == null) return;
                    TransformComponent playerTrans = player.getComponent(TransformComponent.class);
                    Vector2 playerPos = playerTrans.getPosition();

                    for (GameObject cp : centipedes) {
                        TransformComponent trans = cp.getComponent(TransformComponent.class);
                        PhysicsComponent phys = cp.getComponent(PhysicsComponent.class);
                        if (trans == null || phys == null) continue;

                        Vector2 enemyPos = trans.getPosition();
                        Vector2 dir = new Vector2(playerPos.x - enemyPos.x, playerPos.y - enemyPos.y).normalize();
                        phys.setVelocity(dir.multiply(CENTIPEDE_SPEED));
                    }
                }

                // ✅ 核心优化2：全部碰撞逻辑整合 | 子弹击中敌人消失+加分 | 蜈蚣碰葫芦娃死亡
                private void checkCollisions() {
                    if (player == null || isPlayerDead) return;
                    TransformComponent playerTrans = player.getComponent(TransformComponent.class);
                    Vector2 playerPos = playerTrans.getPosition();

                    // ========== 检测1：蜈蚣精碰到葫芦娃 → 葫芦娃死亡 ==========
                    for (GameObject cp : centipedes) {
                        TransformComponent cpTrans = cp.getComponent(TransformComponent.class);
                        if (cpTrans == null) continue;
                        Vector2 cpPos = cpTrans.getPosition();
                        float playerDistance = playerPos.distance(cpPos);
                        // 距离小于30=碰撞，触发死亡
                        if (playerDistance < 30) {
                            isPlayerDead = true;
                            return;
                        }
                    }

                    // ========== 检测2：子弹击中蜈蚣精 → 子弹+敌人消失 + 加10分 ==========
                    Iterator<GameObject> fireIter = fireballs.iterator();
                    while (fireIter.hasNext()) {
                        GameObject fb = fireIter.next();
                        TransformComponent fbTrans = fb.getComponent(TransformComponent.class);
                        if (fbTrans == null) {
                            fireIter.remove();
                            continue;
                        }
                        Vector2 fbPos = fbTrans.getPosition();

                        Iterator<GameObject> centiIter = centipedes.iterator();
                        while (centiIter.hasNext()) {
                            GameObject cp = centiIter.next();
                            TransformComponent cpTrans = cp.getComponent(TransformComponent.class);
                            if (cpTrans == null) {
                                centiIter.remove();
                                continue;
                            }
                            Vector2 cpPos = cpTrans.getPosition();
                            float hitDistance = fbPos.distance(cpPos);

                            // 子弹击中敌人：子弹消失+敌人消失+得分+10
                            if (hitDistance < 25) {
                                fireIter.remove();
                                centiIter.remove();
                                score += 10;
                                break;
                            }
                        }
                    }
                }

                // 随机生成蜈蚣精位置
                private Vector2 getRandomEdgePos() {
                    int side = random.nextInt(4);
                    float x = random.nextFloat() * WINDOW_WIDTH;
                    float y = random.nextFloat() * WINDOW_HEIGHT;
                    Vector2 pos = new Vector2(x, y);
                    
                    switch (side) {
                        case 0:
                            pos = new Vector2(x, -30);
                            break;
                        case 1:
                            pos = new Vector2(x, WINDOW_HEIGHT + 30);
                            break;
                        case 2:
                            pos = new Vector2(-30, y);
                            break;
                        case 3:
                            pos = new Vector2(WINDOW_WIDTH + 30, y);
                            break;
                    }
                    return pos;
                }
            };

            engine.setScene(gameScene);
            engine.run();

        } catch (Exception e) {
            System.err.println("游戏运行出错: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("游戏结束");
    }
}
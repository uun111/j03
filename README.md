# 简单Java游戏引擎

一个基于Java Swing的简单游戏引擎，提供了基本的游戏开发功能。

## 功能特性

- **核心引擎**: 游戏循环、场景管理、对象生命周期管理
- **渲染系统**: 基于Swing的2D渲染，支持矩形、圆形、线条绘制
- **输入处理**: 键盘和鼠标输入管理
- **数学工具**: 2D向量运算
- **场景管理**: 游戏对象的添加、移除、查找
- **碰撞检测**: 基本的距离检测
- **组件系统**: 基于泛型的组件-实体系统(ECS)

## 项目结构

```
src/main/java/com/gameengine/
├── core/           # 核心引擎类
│   ├── GameEngine.java    # 游戏引擎主类
│   ├── GameObject.java    # 游戏对象基类
│   ├── Component.java    # 组件基类
│   └── GameLogic.java    # 游戏规则处理
├── components/     # 组件系统
│   ├── TransformComponent.java  # 变换组件
│   ├── PhysicsComponent.java    # 物理组件
│   └── RenderComponent.java     # 渲染组件
├── graphics/       # 渲染系统
│   └── Renderer.java      # 渲染器
├── input/          # 输入处理
│   └── InputManager.java  # 输入管理器
├── math/           # 数学工具
│   └── Vector2.java       # 2D向量类
├── scene/          # 场景管理
│   └── Scene.java         # 场景类
└── example/        # 示例代码
    └── GameExample.java   # 主程序入口
```

## 快速开始

### 1. 环境要求

- Java 11 或更高版本

### 2. 运行示例

```bash
# 编译并运行游戏
./run.sh

# 或者分步执行
./compile.sh
java -cp build/classes com.gameengine.example.GameExample
```

### 3. 游戏控制

- **WASD** 或 **方向键**: 移动玩家（绿色方块）
- 玩家需要避免与橙色敌人碰撞
- 碰撞后玩家会重置到中心位置

## 使用指南

### 组件系统(ECS)

这个引擎使用组件-实体系统(ECS)设计模式：

```java
// 创建游戏对象
GameObject player = new GameObject("Player");

// 添加变换组件
TransformComponent transform = player.addComponent(
    new TransformComponent(new Vector2(100, 100))
);

// 添加渲染组件
RenderComponent render = player.addComponent(
    new RenderComponent(
        RenderComponent.RenderType.RECTANGLE,
        new Vector2(20, 20),
        new RenderComponent.Color(0.0f, 1.0f, 0.0f, 1.0f)
    )
);

// 添加物理组件
PhysicsComponent physics = player.addComponent(
    new PhysicsComponent(1.0f)
);
```

### 创建自定义场景

```java
Scene gameScene = new Scene("MyGameScene") {
    private GameLogic gameLogic;
    
    @Override
    public void initialize() {
        super.initialize();
        this.gameLogic = new GameLogic(this);
        
        // 创建游戏对象
        createPlayer();
        createEnemies();
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        // 使用游戏逻辑类处理游戏规则
        gameLogic.handlePlayerInput();
        gameLogic.updatePhysics();
        gameLogic.checkCollisions();
    }
    
    private void createPlayer() {
        // 创建玩家逻辑
    }
    
    private void createEnemies() {
        // 创建敌人逻辑
    }
};
```

### 使用游戏逻辑类

```java
// 创建游戏逻辑实例
GameLogic gameLogic = new GameLogic(scene);

// 在场景更新中调用游戏逻辑
@Override
public void update(float deltaTime) {
    super.update(deltaTime);
    
    // 处理玩家输入
    gameLogic.handlePlayerInput();
    
    // 更新物理系统
    gameLogic.updatePhysics();
    
    // 检查碰撞
    gameLogic.checkCollisions();
}
```

### 处理输入

```java
InputManager input = InputManager.getInstance();

// 检查按键是否被按下
if (input.isKeyPressed(87)) { // W键
    // 处理W键按下
}

// 检查方向键
if (input.isKeyPressed(38)) { // 上箭头
    // 处理上箭头按下
}

// 获取鼠标位置
Vector2 mousePos = input.getMousePosition();
```

### 渲染图形

```java
// 绘制矩形
renderer.drawRect(x, y, width, height, r, g, b, a);

// 绘制圆形
renderer.drawCircle(x, y, radius, r, g, b, a);

// 绘制线条
renderer.drawLine(x1, y1, x2, y2, r, g, b, a);
```

## 示例游戏说明

示例游戏是一个简单的躲避游戏：

- 玩家（绿色方块）可以通过WASD或方向键移动
- 敌人（橙色方块）会随机移动
- 玩家需要避免与敌人碰撞
- 碰撞后玩家会重置到中心位置
- 蓝色小圆点是装饰元素

## 架构设计

### 职责分离

```
GameExample (游戏设定)
    ↓ 使用
GameLogic (游戏规则)
    ↓ 操作
Scene (场景管理)
    ↓ 管理
GameObject + Components (游戏对象)
```

### 核心组件

- **GameEngine**: 游戏引擎主类，管理游戏循环
- **Scene**: 场景管理，负责游戏对象的生命周期
- **GameLogic**: 游戏规则处理，包含输入、物理、碰撞逻辑
- **GameObject**: 游戏对象基类，使用组件系统
- **Component**: 组件基类，实现ECS架构

### 设计优势

- **单一职责**: 每个类职责明确
- **易于扩展**: 可以轻松添加新的游戏规则
- **代码复用**: GameLogic可以在不同场景中重用
- **维护性**: 游戏逻辑集中管理
- **测试性**: 可以独立测试游戏逻辑

## 扩展功能

这个游戏引擎提供了基础功能，你可以在此基础上扩展：

- 添加纹理和精灵渲染
- 实现更复杂的物理系统
- 添加音频支持
- 实现粒子系统
- 添加UI系统
- 实现资源管理
- 添加更多组件类型

## 技术特点

- **无外部依赖**: 只使用Java标准库
- **简单构建**: 使用shell脚本编译，无需Maven
- **组件化设计**: 基于ECS架构，易于扩展
- **职责分离**: Scene负责场景管理，GameLogic负责游戏规则
- **跨平台**: 基于Swing，支持所有Java平台
- **易于维护**: 代码结构清晰，职责明确

## 许可证

本项目仅供学习和参考使用。


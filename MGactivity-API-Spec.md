# MGActivity API 清单（已完成对接）

> 更新日期：2026-08-23
> MGActivitys 版本：1.0.1
> 对接状态：✅ 全部完成

---

## 一、API 入口

### 1. 命令方式（KBBSToper 当前使用）

统一命令前缀：mgactivity

所有命令通过游戏控制台或玩家执行，参数中 %PLAYER% 由 KBBSToper 运行时替换为玩家游戏内名（name）。

### 2. Java API 方式（可选）

`java
import cn.gmzc.mgactivitys.api.MGActivityApi;

MGActivityApi api = MGActivityApi.getInstance();
if (api != null) {
    api.setGrowthMultiplier("Steve", 1.25);
}
`

包路径：cn.gmzc.mgactivitys.api.MGActivityApi

---

## 二、API 清单

### 成长值倍率

| 命令 | 作用 | 参数示例 | 返回值 |
|------|------|----------|--------|
| mgactivity setgrowthmultiplier %PLAYER% <倍率> | 设置玩家当日成长值倍率 | mgactivity setgrowthmultiplier Steve 1.25 | 成功提示 / 失败提示 |
| mgactivity getgrowthmultiplier %PLAYER% | 查询当前成长值倍率 | mgactivity getgrowthmultiplier Steve | 1.25（纯数字） |
| mgactivity resetgrowthmultiplier %PLAYER% | 重置成长值倍率（次日自动恢复） | mgactivity resetgrowthmultiplier Steve | 重置成功提示 |

### 经验值倍率

| 命令 | 作用 | 参数示例 | 返回值 |
|------|------|----------|--------|
| mgactivity setexperiencemultiplier %PLAYER% <倍率> | 设置玩家当日经验值倍率 | mgactivity setexperiencemultiplier Steve 1.25 | 成功提示 / 失败提示 |
| mgactivity getexperiencemultiplier %PLAYER% | 查询当前经验值倍率 | mgactivity getexperiencemultiplier Steve | 1.25（纯数字） |
| mgactivity resetexperiencemultiplier %PLAYER% | 重置经验值倍率（次日自动恢复） | mgactivity resetexperiencemultiplier Steve | 重置成功提示 |

### 生命值上限

| 命令 | 作用 | 参数示例 | 返回值 |
|------|------|----------|--------|
| mgactivity setmaxhp %PLAYER% <数值> | 设置生命值上限 | mgactivity setmaxhp Steve 50 | 设置后的实际值（经 clamp 处理） |
| mgactivity getmaxhp %PLAYER% | 查询当前生命值上限 | mgactivity getmaxhp Steve | 30（纯数字） |

### 连续签到中断

| 命令 | 作用 | 参数示例 | 返回值 |
|------|------|----------|--------|
| mgactivity addstreakbreak %PLAYER% <下降值> | 记录连续签到中断 | mgactivity addstreakbreak Steve 2 | 成功/失败提示 |

### 成长值 / 星光点（可选）

| 命令 | 作用 | 参数示例 | 返回值 |
|------|------|----------|--------|
| mgactivity addgrowthpoints %PLAYER% <数值> | 直接增加成长值 | mgactivity addgrowthpoints Steve 100 | 成功/失败提示 |
| mgactivity addstarlightpoints %PLAYER% <数值> | 增加星光点 | mgactivity addstarlightpoints Steve 300 | **暂未实现** |

---

## 三、设计约束确认

| 约束项 | 实现状态 | 说明 |
|--------|----------|------|
| 倍率不叠加 | ✅ | 同一玩家同一天多来源只取 Math.max，不累乘 |
| 次日自动恢复 | ✅ | 成长/经验值倍率在 lastActiveDate 跨天时自动重置为 1.0 |
| 生命值硬顶 | ✅ | clamp(30, 50, value)：低于30返回30，高于50返回50 |
| 断签即扣减 | ✅ | ddStreakBreak 收到后立即扣减 totalActivity 和 dynamicActivity，不跨天排队 |
| 线程安全 | ✅ | 所有 API 方法均使用 synchronized，主线程调用 |
| 参数校验 | ✅ | 非法参数（null、NaN、负数）静默返回 false/默认值，不抛异常 |

---

## 四、Java API 接口签名

`java
public class MGActivityApi {
    // 成长值倍率
    boolean setGrowthMultiplier(String playerName, double multiplier);
    double getGrowthMultiplier(String playerName);
    boolean resetGrowthMultiplier(String playerName);

    // 经验值倍率
    boolean setExperienceMultiplier(String playerName, double multiplier);
    double getExperienceMultiplier(String playerName);
    boolean resetExperienceMultiplier(String playerName);

    // 生命值上限
    int setMaxHp(String playerName, int maxHp);  // 返回实际设置值
    int getMaxHp(String playerName);

    // 连续签到中断
    boolean addStreakBreak(String playerName, int breakCount);

    // 成长值/星光点
    boolean addGrowthPoints(String playerName, double points);
    boolean addStarlightPoints(String playerName, long points);  // 暂未实现
}
`

---

## 五、KBBSToper 对接方式

### 命令模式（当前已支持）

KBBSToper 通过控制台命令调用，无需编译依赖。示例：

`java
Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mgactivity setgrowthmultiplier " + playerName + " 1.25");
`

### API 模式（推荐，需要编译依赖）

1. 将 MGActivitys-1.0.0.jar 添加到 KBBSToper 的编译 classpath
2. 在 KBBSToper 的 plugin.yml 中添加 softdepend: [MGActivitys]
3. 代码中调用：

`java
MGActivityApi api = MGActivityApi.getInstance();
if (api != null) {
    api.setGrowthMultiplier(playerName, 1.25);
} else {
    // 回退到命令方式
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mgactivity setgrowthmultiplier " + playerName + " 1.25");
}
`

---

## 六、测试验证场景

| 场景 | 预期 |
|------|------|
| 首顶奖励（setgrowthmultiplier 1.25 + setmaxhp +2 + addgrowthpoints 100） | 成长值倍率1.25x，生命上限+2，成长值+100 |
| 次日重置 | 成长/经验值倍率自动恢复1.0x，生命上限保持 |
| 倍率取最高 | 先设1.25x再设1.5x，最终为1.5x |
| 断签扣减 | addstreakbreak 2 → totalActivity 和 dynamicActivity 立即减少 |
| 生命值clamp | setmaxhp 60 → 实际设置50；setmaxhp 20 → 实际设置30 |

---

## 七、附件

- MGActivityApi.java — API 入口类源码
- MGActivitys-1.0.0.jar — 已包含 API 的构建产物
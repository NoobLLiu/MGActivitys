# MGactivity 对外 API 数据导出接口清单（KBBSToper 对接）

> 版本：本文基于 `dev-plugins\MGActivitys`（plugin.yml name=`MGActivitys`，version=1.0.1）源码。
> 命令统一命名为 `mgactivity`，注册权限为 `none`（KBBSToper 通过 `Bukkit.getConsoleSender()` 派发，无需权限）。
> 所有命令参数中 `%PLAYER%` 为占位符，由调用方（KBBSToper）在派发前替换为**玩家游戏内名（name）**。
> 数值参数均为纯数字/小数，无需引号包裹。

## 一、接口总览

| # | 接口 | 作用 | 参数 | 是否支持 `%PLAYER%` | 说明 |
| --- | --- | --- | --- | --- | --- |
| 1 | `mgactivity setgrowthmultiplier %PLAYER% <倍率>` | 设置玩家当日成长值倍率 | name, 倍率(小数≥0) | 是 | 当日生效，次日自动恢复 1x；多来源取最高，不累乘 |
| 2 | `mgactivity getgrowthmultiplier %PLAYER%` | 查询当前成长值倍率 | name | 是 | 返回当前值（默认 1.0） |
| 3 | `mgactivity resetgrowthmultiplier %PLAYER%` | 将成长值倍率恢复默认 1x | name | 是 | 立即置为 1x，次日自动恢复机制同生效 |
| 4 | `mgactivity setexperiencemultiplier %PLAYER% <倍率>` | 设置玩家当日经验值倍率 | name, 倍率(小数≥0) | 是 | 当日生效，次日自动恢复 1x；多来源取最高，不累乘 |
| 5 | `mgactivity getexperiencemultiplier %PLAYER%` | 查询当前经验倍率 | name | 是 | 返回当前值（默认 1.0） |
| 6 | `mgactivity resetexperiencemultiplier %PLAYER%` | 将经验倍率恢复默认 1x | name | 是 | 立即置为 1x |
| 7 | `mgactivity setmaxhp %PLAYER% <数值>` | 设置玩家生命值上限 | name, 数值(整数) | 是 | 硬顶 50，基础下限 30；持久化，无重置命令 |
| 8 | `mgactivity getmaxhp %PLAYER%` | 查询当前生命值上限 | name | 是 | 返回当前值（默认 30） |
| 9 | `mgactivity addstreakbreak %PLAYER% <下降值>` | 记录一次"连续签到中断" | name, 下降值(整数>0) | 是 | 相关活跃度按每日 -2 扣减，详见下方说明 |

---

## 二、参数与取值说明

### 1) 成长值倍率 / 经验值倍率（Growth / Experience Multiplier）
- `<倍率>`：`double`，`≥ 0`，示例 `1.25`、`2.0`。
- **不叠加、取最高**：当日同一玩家多次设置同类型倍率时，MGactivity 仅保留**最大值**，不做累乘。例如先 `set 1.25` 再 `set 1.5`，最终为 `1.5`；再 `set 1.1` 仍为 `1.5`。
- **次日自动恢复默认**：当玩家条目的 `lastActiveDate` 跨天时，`getPlayerData()` 会先把成长倍率与经验倍率都重置为 `1.0` 再返回（即跨天首个操作会触发恢复）。
- `reset`：立即把对应倍率置为 `1.0`。

### 2) 生命值上限（Max HP）
- `<数值>`：整数，会做 **硬钳制**：`clamp(30, 50, value)`，即 `value<30` 返回 `30`（基础下限），`value>50` 返回 `50`（上限）。
- 基准 30，每次顶贴奖励 +2 由调用方（KBBSToper）累加后传入最终目标值；MGactivity 只负责钳制到硬顶并持久化。
- **可叠加但有硬顶**：传入绝对目标值，MGactivity 保存后 `getmaxhp` 返回实际生效值（被钳制后的值）。

### 3) 连续签到中断（Streak Break）
- `<下降值>`：整数 `> 0`，示例 `2`。
- **当前实现语义**：`addstreakbreak %PLAYER% <下降值>` 会把该玩家的"待扣减天数计数器" `streakBreakCount += <下降值>`。此后每次跨天，只要 `streakBreakCount > 0`，就对 `totalActivity` 与 `dynamicActivity` 各 **-2**（向下取整到 0.1，低于 0.1 归 0），并把 `streakBreakCount` **减 1**。
- 因此 `addstreakbreak Steve 2` 表示 Steve 将连续 **2 天**每天被扣 2 点活跃度。
- ⚠️ **需要确认**：需求原文为"按配置下降（如每日 -2）"，`<下降值>` 命名偏向"每日下降值"。当前实现把它当作"扣减天数"。若你期望 `2` 表示"每日下降 2、且仅代表一次断签"，请告知，我再把语义调整（当前版本未改动，避免影响既有逻辑）。

---

## 三、用法示例

下列命令均以玩家名 `Steve` 为例，`%PLAYER%` 已替换为 `Steve`；命令行直接发送即可（KBBSToper 用 `Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)`）。

### 生长/经验倍率
```
mgactivity setgrowthmultiplier Steve 1.25
mgactivity getgrowthmultiplier Steve
mgactivity resetgrowthmultiplier Steve
mgactivity setexperiencemultiplier Steve 1.25
mgactivity getexperiencemultiplier Steve
mgactivity resetexperiencemultiplier Steve
```

### 生命值上限
```
mgactivity setmaxhp Steve 50
mgactivity getmaxhp Steve
```

### 连续签到中断
```
mgactivity addstreakbreak Steve 2
```

### 实际返回示例（控制台/命令回显）
- `setgrowthmultiplier Steve 1.25` → `Set growth multiplier for "Steve" to 1.25`
- `getgrowthmultiplier Steve` → `1.25`
- `resetgrowthmultiplier Steve` → `Reset growth multiplier for "Steve" to default`
- `setmaxhp Steve 60` → `Set max hp for "Steve" to 50`（体现硬顶 50）
- `getmaxhp Steve` → `50`
- `addstreakbreak Steve 2` → `Recorded streak break for "Steve" (-2/day)`

"参数错误/缺参"时统一回显 usage：
```
usage: mgactivity <set|get|reset>growthmultiplier|experiencemultiplier|maxhp|addstreakbreak %PLAYER% [value]
```

---

## 四、持久化与解耦说明

- MGactivity 将这些状态保存在 `plugins/MGActivitys/playerdata.json`（Gson 序列化），按玩家名（name）索引。
- 与 KBBSToper 数据库（如 `bbstoper.db`）完全解耦：KBBSToper 只负责"算数值 + 发接口"，不维护这些展示逻辑；MGactivity 负责"写入/修改玩家活跃度记录"。
- 默认值：`growthMultiplier=1.0`、`experienceMultiplier=1.0`、`maxHp=30`、`streakBreakCount=0`。

---

## 五、代码位置（便于复核）

| 内容 | 文件 | 关键位置 |
| --- | --- | --- |
| 命令注册 | `plugin.yml` → `commands.mgactivity`；`MGActivitysPlugin.onEnable()` → `regCommand("mgactivity", new ApiExportCommand(this))` | |
| 命令解析/分发 | `src/cn/gmzc/mgactivitys/command/ApiExportCommand.java` | `onCommand` switch 覆盖 9 个接口 |
| 状态读写/持久化 | `src/cn/gmzc/mgactivitys/data/ActivityManager.java` | `set/get/reset GrowthMultiplier`、`set/get ExperienceMultiplier`、`set/getMaxHp`、`addStreakBreak`、`resolvePlayerName`、`getPlayerData`（含次日恢复与断签扣减） |
| 数据模型 | `src/cn/gmzc/mgactivitys/model/ActivityData.java` | 倍率/生命/断签字段 |

### 关键实现片段
```java
// 非叠加取最高
data.setGrowthMultiplier(Math.max(data.getGrowthMultiplier(), value));

// 生命上限硬顶 [30,50]
int clamped = Math.max(30, Math.min(50, value));
data.setMaxHp(clamped);

// 次日自动恢复默认倍率（在 getPlayerData() 跨天分支内）
if (playerData.getLastActiveDate() == null || !playerData.getLastActiveDate().equals(today)) {
    playerData.setGrowthMultiplier(1.0);
    playerData.setExperienceMultiplier(1.0);
    ...
}
```

---

## 六、附注 / 待办

- 上述接口本次已在 **测试服 StarCity-test** 部署新版 `MGActivitys-1.0.0.jar` 并实测通过（含非叠加、取最高、reset、maxhp 硬顶、断签、持久化）。
- ⚠️ 目前 `growthMultiplier` / `experienceMultiplier` / `maxHp` 由 MGactivity **持久化存储**。将其**施加到实际玩法**（如成长奖励按倍率缩放、进入服务器时应用最大生命值）属于另一项改动，本接口清单暂未包含；如需一并实现，请确认后我再补。
- 需要在 KBBSToper 的 `reward.commands` 等节点把上述接口写成配置命令（`%PLAYER%` 占位符）才会在顶贴时被派发。


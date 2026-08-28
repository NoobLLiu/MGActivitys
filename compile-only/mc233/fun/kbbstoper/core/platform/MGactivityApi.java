package mc233.fun.kbbstoper.core.platform;

/**
 * KBBSToper 与 MGActivitys 的原生对接接口。
 *
 * <p>本文件为编译期存根：接口的正式定义由 KBBSToper core 模块提供并随其 jar 发布，
 * 运行时由 KBBSToper 插件类加载器加载（MGActivitys 通过 softdepend 保证 KBBSToper
 * 先加载）。因此本文件<b>不得</b>打入 MGActivitys 的发布 jar，否则两端会加载到
 * 不同的 Class 对象，ServicesManager 按 Class 精确匹配将失败。</p>
 *
 * <p>包名与类名必须与 KBBSToper 侧逐字一致。</p>
 */
public interface MGactivityApi {

    /** 设置玩家成长值倍率（取最大值，不叠加；每日自动归位基准值 1.0）。 */
    void setGrowthMultiplier(String player, double value);

    /** 设置玩家经验值倍率（取最大值，不叠加；每日自动归位基准值 1.0）。 */
    void setExperienceMultiplier(String player, double value);

    /** 设置玩家生命值上限（绝对值写入并持久化，跨天保留；MGActivitys 防御性钳制 [30, 50]）。 */
    void setMaxHp(String player, int value);

    /** 增加玩家连签中断计数（增量累加，立即生效）。 */
    void addStreakBreak(String player, int value);

    /**
     * 增加玩家星光点（增量累加、立即生效、value 非负）。
     *
     * <p>默认空实现：仅用于兼容旧版 KBBSToper（运行时未覆写时星光点不会到账，
     * 但不会报错）。MGActivitys 实现类需覆写本方法以真实累加并持久化。</p>
     */
    default void addStarlightPoints(String player, long value) {
    }
}
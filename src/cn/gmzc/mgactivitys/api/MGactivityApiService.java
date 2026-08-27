package cn.gmzc.mgactivitys.api;

import cn.gmzc.mgactivitys.MGActivitysPlugin;
import mc233.fun.kbbstoper.core.platform.MGactivityApi;
import org.bukkit.plugin.ServicePriority;

/**
 * 将 {@link MGactivityApi} 实现注册到 Bukkit ServicesManager，供 KBBSToper
 * 运行时直接调用（原生对接）。
 *
 * <p>接口类由 KBBSToper 提供：MGActivitys 在 plugin.yml 中声明 softdepend
 * KBBSToper，保证 KBBSToper 先加载，两端共享同一个接口 Class 对象。</p>
 *
 * <p>当 KBBSToper 未安装或版本过旧（jar 中不含该接口）时，接口类无法加载，
 * 此处捕获 {@link Throwable}（NoClassDefFoundError 等）后安全跳过——MGActivitys
 * 照常启用，KBBSToper 会自动回退到既有的 {@code mgactivity} 控制台命令对接。</p>
 */
public final class MGactivityApiService {

    private MGactivityApiService() {
    }

    public static void register(MGActivitysPlugin plugin) {
        try {
            MGactivityApi implementation =
                new MGactivityApiImpl(plugin.getActivityManager(), plugin.getLogger());
            plugin.getServer().getServicesManager()
                .register(MGactivityApi.class, implementation, plugin, ServicePriority.Normal);
            plugin.getLogger().info("[MGactivityApi] 已注册 ServicesManager 服务（KBBSToper 原生对接）");
        } catch (Throwable t) {
            // KBBSToper 未安装或版本过旧（接口类不存在）：保持命令式对接，不影响插件启用。
            plugin.getLogger().info("[MGactivityApi] KBBSToper 未提供对接接口（未安装或版本过旧），保持命令式回退: " + t);
        }
    }

    public static void unregister(MGActivitysPlugin plugin) {
        try {
            plugin.getServer().getServicesManager().unregisterAll(plugin);
        } catch (Throwable ignored) {
            // 注销失败不应阻断关服流程。
        }
    }
}
package com.zary.sniffer.agent.core.consts;

public class AdmxAgentConsts {

    public static final String banner_chars = " ____  ____  ____ ___  _      ____  ____  _     ___  _      ____  _____ _____ _      _____ \n" +
            "/_   \\/  _ \\/  __\\\\  \\//     /  _ \\/  _ \\/ \\__/|\\  \\//     /  _ \\/  __//  __// \\  /|/__ __\\\n" +
            " /   /| / \\||  \\/| \\  /_____ | / \\|| | \\|| |\\/|| \\  /_____ | / \\|| |  _|  \\  | |\\ ||  / \\  \n" +
            "/   /_| |-|||    / / / \\____\\| |-||| |_/|| |  || /  \\\\____\\| |-||| |_//|  /_ | | \\||  | |  \n" +
            "\\____/\\_/ \\|\\_/\\_\\/_/        \\_/ \\|\\____/\\_/  \\|/__/\\\\     \\_/ \\|\\____\\\\____\\\\_/  \\|  \\_/  \n";
    public static final String agent_pakage = "com.zary.admx.agent.";
    /**
     * 探针输出日志标志
     */
    public static final String agent_log_head = "[ADM-AGENT]";
    /**
     * java生成的动态代理类的命名空间前缀，探针插桩时不会拦截该空间下的类
     */
    public static final String plugin_dir = "plugins";
    /**
     * 插件jar包中的插件定义文件名称(每个插件jar包中都有该文件)
     */
    public static final String plugin_define_file = "plugin.define";

}

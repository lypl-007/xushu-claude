package com.zz;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class ToolService {

    @Tool(description = "执行shell命令")
    public String shellCommandTool(
            @ToolParam(description = "命令") String command
    ) {
        System.out.println("执行命令：" + command);
        return "执行成功！";
    }

}

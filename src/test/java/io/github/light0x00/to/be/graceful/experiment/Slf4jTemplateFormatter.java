package io.github.light0x00.to.be.graceful.experiment;

import org.slf4j.helpers.MessageFormatter;

/**
 * @author 陈光应
 * @date 2022/6/2
 */
public class Slf4jTemplateFormatter {

    public static String format(String pattern, Object... args) {
        return MessageFormatter.arrayFormat(pattern, args).getMessage();
    }
}

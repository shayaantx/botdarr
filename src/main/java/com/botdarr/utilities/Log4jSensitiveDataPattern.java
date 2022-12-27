package com.botdarr.utilities;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Plugin(name="LogMaskingConverter", category = "Converter")
@ConverterKeys({"scrubbedMsg"})
public class Log4jSensitiveDataPattern extends LogEventPatternConverter {
    private static final Pattern API_KEY_PATTERN = Pattern.compile("apiKey=([0-9a-zA-z]+)");

    protected Log4jSensitiveDataPattern(String name, String style) {
        super(name, style);
    }
    public static Log4jSensitiveDataPattern newInstance(String[] options) {
        return new Log4jSensitiveDataPattern("scrubbedMsg", Thread.currentThread().getName());
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        String message = event.getMessage().getFormattedMessage();
        String maskedMessage = message;
        try {
            StringBuffer modifiedBuffer = new StringBuffer();
            Matcher matcher = API_KEY_PATTERN.matcher(message);
            while(matcher.find()) {
                matcher.appendReplacement(modifiedBuffer, "apiKey=****");
            }
            if (modifiedBuffer.length() > 0) {
                maskedMessage = modifiedBuffer.toString();
            }
        } catch (Exception e) {
            System.out.println("Error trying to mask message, message = " + e.getMessage());
            maskedMessage = message;
        }
        toAppendTo.append(maskedMessage);
    }
}

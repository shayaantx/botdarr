package com.botdarr.utilities;

import mockit.Expectations;
import mockit.Mocked;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.junit.Assert;
import org.junit.Test;

public class Log4jSensitiveDataPatternTests {
    @Test
    public void format_noApiKeyInString_noMaskedText() {
        String input = "http://localhost";
        LogEventPatternConverter patternConverter = new Log4jSensitiveDataPattern("test", "style");
        StringBuilder output = new StringBuilder();
        new Expectations() {{
           mockedEvent.getMessage().getFormattedMessage(); result = input;
        }};
        patternConverter.format(mockedEvent, output);
        //input should remain unchanged
        Assert.assertEquals(input, output.toString());
    }

    @Test
    public void format_apiKeyInString_noMaskedText() {
        String input = "http://localhost?apiKey=fdskjkjfd";
        LogEventPatternConverter patternConverter = new Log4jSensitiveDataPattern("test", "style");
        StringBuilder output = new StringBuilder();
        new Expectations() {{
            mockedEvent.getMessage().getFormattedMessage(); result = input;
        }};
        patternConverter.format(mockedEvent, output);
        //input should remain unchanged
        Assert.assertEquals("http://localhost?****", output.toString());
    }

    @Test
    public void format_access_tokenInString_noMaskedText() {
        String input = "http://localhost?access_token=fdskjkjfd";
        LogEventPatternConverter patternConverter = new Log4jSensitiveDataPattern("test", "style");
        StringBuilder output = new StringBuilder();
        new Expectations() {{
            mockedEvent.getMessage().getFormattedMessage(); result = input;
        }};
        patternConverter.format(mockedEvent, output);
        //input should remain unchanged
        Assert.assertEquals("http://localhost?****", output.toString());
    }

    @Mocked
    private Log4jLogEvent mockedEvent;
}

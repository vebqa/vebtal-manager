package org.vebqa.vebtal.logging;

import java.io.Serializable;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.vebqa.vebtal.GuiManager;

/**
 * An Appender that ignores log events. Use for compatibility with version 1.2.
 */
@Plugin(name = "MyGuiAppender", category = "Core", elementType = "appender", printObject = true)
public final class TextAreaAppender extends AbstractAppender {

    @PluginFactory
    public static TextAreaAppender createAppender(
    @PluginAttribute("name") final String name,
    @PluginElement("Layout") Layout<? extends Serializable> layout,
    @PluginElement("Filter") final Filter filter,
    @PluginAttribute("otherAttribute") String otherAttribute) {
        return new TextAreaAppender(name, filter, layout, true);
    }

    private TextAreaAppender(final String name, Filter filter,
            Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
        super(name, null, null);
    }

    @Override
    public void append(final LogEvent event) {
    	GuiManager.getinstance().writeLog(event.getMessage().getFormattedMessage());
    }
}
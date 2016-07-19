/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package fr.cls.atoll.motu.web.common.log.log4j;

import java.nio.charset.Charset;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.log.QueueLogInfo;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
@Plugin(name = "MotuCustomLayout", category = "Core", elementType = "layout", printObject = true)
public class MotuCustomLayout extends AbstractStringLayout {

    /**
     * .
     */
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_SIZE = 256;
    private static final int UPPER_LIMIT = 2048;

    private StringBuffer buf = new StringBuffer(DEFAULT_SIZE);

    protected MotuCustomLayout(boolean locationInfo, boolean properties, boolean complete, Charset charset) {
        super(charset);
    }

    @PluginFactory
    public static MotuCustomLayout createLayout(@PluginAttribute("locationInfo") boolean locationInfo,
                                                @PluginAttribute("properties") boolean properties,
                                                @PluginAttribute("complete") boolean complete,
                                                @PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset) {
        return new MotuCustomLayout(locationInfo, properties, complete, charset);
    }

    private String formatLog(QueueLogInfo queueLogInfo) {
        if (BLLManager.getInstance().getConfigManager().getMotuConfig().getLogFormat().contains(QueueLogInfo.TYPE_XML)) {
            buf.append(queueLogInfo.toXML());
        } else {
            buf.append(queueLogInfo.toCSV());
        }

        buf.append(System.lineSeparator());
        return buf.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String toSerializable(LogEvent event) {
        // Reset working buffer. If the buffer is too large, then we need a new
        // one in order to avoid the penalty of creating a large array.
        if (buf.capacity() > UPPER_LIMIT) {
            buf = new StringBuffer(DEFAULT_SIZE);
        } else {
            buf.setLength(0);
        }

        // if (event.getMessage() == null) {
        // return "MotuXMLLayout : message object is null";
        // } else {
        if (event.getMessage().getParameters() != null && event.getMessage().getParameters().length == 1
                && event.getMessage().getParameters()[0] instanceof QueueLogInfo) {
            return formatLog((QueueLogInfo) event.getMessage().getParameters()[0]);
        }
        // }
        // else if (event.getMessage() instanceof RunnableExtraction) {
        // RunnableExtraction runnableExtraction = (RunnableExtraction) event.getMessage();
        // return formatLog(runnableExtraction.getQueueLogInfo());
        // } else if (event.getMessage() instanceof QueueLogInfo) {
        // return formatLog((QueueLogInfo) event.getMessage());
        // }
        return "";
        // return event.toString(); // super.toSerializable(event);
    }

    /** {@inheritDoc} */
    @Override
    public String getContentType() {
        if (BLLManager.getInstance().getConfigManager().getMotuConfig().getLogFormat().contains(QueueLogInfo.TYPE_XML)) {
            return "text/xml; charset=UTF-8";
        } else {
            return "text/csv; charset=UTF-8";
        }

    }

}

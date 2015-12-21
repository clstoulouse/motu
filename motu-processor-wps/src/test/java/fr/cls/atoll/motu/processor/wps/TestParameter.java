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
package fr.cls.atoll.motu.processor.wps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geotoolkit.io.wkt.Formatter;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.Parameter;
import org.opengis.parameter.ParameterDescriptor;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class TestParameter {

    public class Liste extends ArrayList<String> {

    }

    /**
     * .
     * 
     * @param args
     */
    public static void main(String[] args) {

        testList();
    }

    public static void testList() {
        // TestParameter testParameter = new TestParameter();
        // TestParameter.Liste list = testParameter.new Liste();
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");

        final ParameterDescriptor<String> descriptor = new DefaultParameterDescriptor<String>("eee", String.class, null, null);
        final Parameter<String> parameter = new Parameter<String>(descriptor);
        parameter.setValue("a");

        System.out.println(parameter.stringValue());

        ParameterDescriptor<Liste> descriptor1a = new DefaultParameterDescriptor<Liste>("TTTTTTTT", (Class<Liste>) list.getClass(), null, null);
        ParameterDescriptor<List<String>> descriptor2 = new DefaultParameterDescriptor<List<String>>("TTTTTTTT", (Class<List<String>>) list
                .getClass(), null, null);

        Parameter<List<String>> parameter2 = new Parameter<List<String>>(descriptor2);
        System.out.println(descriptor2.getValueClass().toString());
        System.out.println(list.getClass().toString());

        final Class<List<String>> type = descriptor2.getValueClass();
        if (!type.isInstance(list)) {
            System.out.println("RRRRRRRRRRRr");

        }

        parameter2.setValue(list);

        System.out.println(parameter2.getValue().getClass());
        System.out.println(parameter2.getValue() instanceof Collection);
        Object object = null;
        if (parameter2.getValue() instanceof Collection) {
            object = parameter2.getValue().get(0);
            System.out.println(object.getClass());
        }

        System.out.println(parameter2.formatWKT(new Formatter()));
        System.out.println(parameter2.getValue().toString());

        ParameterDescriptor descriptor3 = new DefaultParameterDescriptor("ddd", null, object.getClass(), null, true);
        Parameter parameter3 = new Parameter(descriptor);
        parameter3.setValue(object);
        System.out.println(parameter3.getValue().getClass());
        System.out.println(parameter3.getValue().toString());

        // Class valueClass = object.getClass();
        // Parameter parameter4 = Parameter.create("Dynamic", valueClass, object);
    }

}

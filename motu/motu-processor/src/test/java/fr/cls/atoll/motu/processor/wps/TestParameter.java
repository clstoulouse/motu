package fr.cls.atoll.motu.processor.wps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geotoolkit.io.wkt.Formatter;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.Parameter;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.util.CodeList;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.processor.wps.framework.WPSFactory;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Soci�t� : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.2 $ - $Date: 2009-08-13 15:39:11 $
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
        //TestParameter testParameter = new TestParameter();
        //TestParameter.Liste list = testParameter.new Liste();
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");

        final ParameterDescriptor<String> descriptor = new DefaultParameterDescriptor<String>("eee", String.class, null, null);
        final Parameter<String> parameter = new Parameter<String>(descriptor);
        parameter.setValue("a");

        System.out.println(parameter.stringValue());

        ParameterDescriptor<Liste> descriptor1a = new DefaultParameterDescriptor<Liste>("TTTTTTTT", (Class<Liste>) list.getClass(), null, null);
        ParameterDescriptor<List<String>> descriptor2 = new DefaultParameterDescriptor<List<String>>("TTTTTTTT", (Class<List<String>>) list.getClass(), null, null);

        Parameter<List<String>> parameter2 = new Parameter<List<String>>(descriptor2);
        System.out.println(descriptor2.getValueClass().toString());
        System.out.println(list.getClass().toString());

        final Class<List<String>> type = descriptor2.getValueClass();
        if (!type.isInstance(list)) {
            System.out.println("RRRRRRRRRRRr");

        }

        parameter2.setValue(list);

        System.out.println (parameter2.getValue().getClass());
        System.out.println (parameter2.getValue() instanceof Collection);
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

        //Class valueClass = object.getClass();
        //Parameter parameter4 =  Parameter.create("Dynamic", valueClass, object);
    }
    


}

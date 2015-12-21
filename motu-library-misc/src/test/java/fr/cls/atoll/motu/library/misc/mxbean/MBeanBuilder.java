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
 *
 * Code from http://weblogs.java.net/blog/emcmanus/archive/2006/10/build_your_own.html
 * 
 * MBeanBuilder.java
 */

package fr.cls.atoll.motu.library.misc.mxbean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

public class MBeanBuilder {
    private final InterfaceClassLoader loader;

    public MBeanBuilder(ClassLoader parentLoader) {
        loader = new InterfaceClassLoader(parentLoader);
    }

    public StandardMBean buildMBean(Object x) {
        Class<?> c = x.getClass();
        Class<?> mbeanInterface = makeInterface(c);
        InvocationHandler handler = new MBeanInvocationHandler(x);
        return makeStandardMBean(mbeanInterface, handler);
    }

    private static <T> StandardMBean makeStandardMBean(Class<T> intf,
                                                       InvocationHandler handler) {
        Object proxy =
                Proxy.newProxyInstance(intf.getClassLoader(),
                                       new Class<?>[] {intf},
                                       handler);
        T impl = intf.cast(proxy);
        try {
            return new StandardMBean(impl, intf);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Class makeInterface(Class implClass) {
        String interfaceName = implClass.getName() + "$WrapperMBean";
        try {
            return Class.forName(interfaceName, false, loader);
        } catch (ClassNotFoundException e) {
            // OK, we'll build it
        }
        Set<XMethod> methodSet = new LinkedHashSet<XMethod>();
        for (Method m : implClass.getMethods()) {
            if (m.isAnnotationPresent(Managed.class))
                methodSet.add(new XMethod(m));
        }
        if (methodSet.isEmpty()) {
            throw new IllegalArgumentException("Class has no @Managed methods: "
                    + implClass);
        }
        XMethod[] methods = methodSet.toArray(new XMethod[0]);
        return loader.findOrBuildInterface(interfaceName, methods);
    }
}

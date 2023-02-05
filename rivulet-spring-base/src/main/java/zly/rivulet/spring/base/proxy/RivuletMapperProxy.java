package zly.rivulet.spring.base.proxy;

import zly.rivulet.base.Rivulet;
import zly.rivulet.base.RivuletManager;
import zly.rivulet.spring.base.scan.MapperKey;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

public class RivuletMapperProxy<T> implements InvocationHandler, Serializable {

    private final Rivulet rivuletTemplate;

    private final Class<T> mapperInterface;

    private final Map<Method, String> method_key_map;

    public RivuletMapperProxy(Rivulet rivuletTemplate, Class<T> mapperInterface) {
        this.rivuletTemplate = rivuletTemplate;
        this.mapperInterface = mapperInterface;
        RivuletManager rivuletManager = rivuletTemplate.getRivuletManager();
        HashMap<Method, String> method_key_map = new HashMap<>();
        Method[] methods = mapperInterface.getMethods();
        for (Method method : methods) {
            MapperKey mapperKey = method.getAnnotation(MapperKey.class);
            if (mapperKey != null) {
                String descKey = mapperKey.value();
                method_key_map.put(method, descKey);
                rivuletManager.methodBinding(method, descKey);
            }
        }
        Method[] declaredMethods = mapperInterface.getDeclaredMethods();
        for (Method method : declaredMethods) {
            MapperKey mapperKey = method.getAnnotation(MapperKey.class);
            if (mapperKey != null) {
                String descKey = mapperKey.value();
                method_key_map.put(method, descKey);
                rivuletManager.methodBinding(method, descKey);
            }
        }
        this.method_key_map = method_key_map;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {

            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }

            String descKey = this.method_key_map.get(method);
            if (descKey != null) {
                return rivuletTemplate.exec(method, descKey, args);
            }
//            if(method.isDefault()) {
                // jdk9可能会有问题，先不管
//            }
            return method.invoke(this, args);
        } catch (Throwable wrapped) {
            Throwable unwrapped = wrapped;
            while (true) {
                if (unwrapped instanceof InvocationTargetException) {
                    unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
                } else if (unwrapped instanceof UndeclaredThrowableException) {
                    unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
                } else {
                    throw unwrapped;
                }
            }

        }
    }
}

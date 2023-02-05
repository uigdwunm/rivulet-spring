package zly.rivulet.spring.base.proxy;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class RivuletMapperFactoryBean<T> extends RivuletDaoSupport implements FactoryBean<T> {

    private Class<T> mapperInterface;

    private T proxyInterface;

    public RivuletMapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public T getObject() {
        if (proxyInterface != null) {
            return proxyInterface;
        }
        RivuletMapperProxy<T> rivuletMapperProxy = new RivuletMapperProxy<>(super.getRivuletTemplate(), this.mapperInterface);
        this.proxyInterface = (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, rivuletMapperProxy);
        return this.proxyInterface;
    }

    @Override
    public Class<?> getObjectType() {
        return this.mapperInterface;
    }
}

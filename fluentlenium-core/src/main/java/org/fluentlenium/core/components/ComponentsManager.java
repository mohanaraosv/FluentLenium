package org.fluentlenium.core.components;

import com.sun.jna.WeakIdentityHashMap;
import org.fluentlenium.core.proxy.ProxyElementListener;
import org.fluentlenium.core.proxy.Proxies;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Manage living components for a WebDriver instance.
 * <p>
 * A component is an Object implementing no particular interface, but capable a wrapping
 * a {@link org.openqa.selenium.WebElement}.
 * <p>
 * {@link org.fluentlenium.core.domain.FluentWebElement} is the most common component.
 */
public class ComponentsManager implements ComponentInstantiator, ComponentAccessor, ProxyElementListener {

    private final WebDriver driver;
    private final ComponentInstantiator instantiator;

    //TODO: IdentityHashMap or WeakIdentityHashMap ?
    private Map<WebElement, Object> components = new WeakIdentityHashMap();

    public ComponentsManager(WebDriver driver) {
        this.driver = driver;
        this.instantiator = new DefaultComponentInstantiator(this.driver, this);
    }

    public ComponentsManager(WebDriver driver, ComponentInstantiator instantiator) {
        this.driver = driver;
        this.instantiator = instantiator;
    }

    public ComponentInstantiator getInstantiator() {
        return instantiator;
    }

    /**
     * Get the related component from the given element.
     *
     * @param element
     * @return
     */
    public Object getComponent(WebElement element) {
        return components.get(unwrapElement(element));
    }

    /**
     * Get all the component related to this webDriver.
     *
     * @return
     */
    public Collection<ComponentBean> getAllComponents() {
        List<ComponentBean> allComponents = new ArrayList<>();
        for (Map.Entry<WebElement, Object> entry : components.entrySet()) {
            allComponents.add(new ComponentBean(entry.getValue(), entry.getKey()));
        }
        return allComponents;
    }

    @Override
    public boolean isComponentClass(Class<?> componentClass) {
        return instantiator.isComponentClass(componentClass);
    }

    @Override
    public <T> T newComponent(Class<T> componentClass, WebElement element) {
        T component;
        try {
            component = instantiator.newComponent(componentClass, element);
        } catch (Exception e) {
            throw new ComponentException(componentClass.getName()
                    + " is not a valid component class. No valid constructor found (WebElement) or (WebElement, WebDriver)", e);
        }
        WebElement webElement = unwrapElement(element);
        Proxies.addProxyListener(webElement, this);
        components.put(webElement, component);
        return component;
    }

    @Override
    public void proxyElementSearch(WebElement proxy, ElementLocator locator) {
    }

    /**
     * When the underlying element of a WebElement Proxy is found, we have to update the components map.
     *
     *
     *  @param proxy proxy element.
     * @param locator
     * @param element found element.
     */
    @Override
    public synchronized void proxyElementFound(WebElement proxy, ElementLocator locator, WebElement element) {
        Object component = components.remove(proxy);
        if (component != null) {
            components.put(unwrapElement(proxy), component);
        }
    }

    private WebElement unwrapElement(WebElement element) {
        if (element instanceof WrapsElement) {
            WebElement wrappedElement = ((WrapsElement) element).getWrappedElement();
            if (wrappedElement != element && wrappedElement != null) {
                return unwrapElement(wrappedElement);
            }
        }
        return element;
    }

    /**
     * Release this manager.
     */
    public void release() {
        for (WebElement element : components.keySet()) {
            Proxies.removeProxyListener(element, this);
        }
        components.clear();
    }

}
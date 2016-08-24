package org.fluentlenium.core.domain;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.fluentlenium.core.action.Fill;
import org.fluentlenium.core.action.FillSelect;
import org.fluentlenium.core.components.ComponentInstantiator;
import org.fluentlenium.core.conditions.AtLeastOneElementConditions;
import org.fluentlenium.core.conditions.EachElementConditions;
import org.fluentlenium.core.conditions.FluentListConditions;
import org.fluentlenium.core.filter.Filter;
import org.fluentlenium.core.proxy.ListElementAccessor;
import org.fluentlenium.core.proxy.Proxies;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Map the list to a FluentList in order to offers some events like click(), submit(), value() ...
 */
public class FluentListImpl<E extends FluentWebElement> extends ArrayList<E> implements FluentList<E> {
    private Class<E> componentClass;
    private ComponentInstantiator instantiator;

    public FluentListImpl() {
    }

    public FluentListImpl(E... listFiltered) {
        super(new ArrayList<E>(Arrays.asList(listFiltered)));
    }

    public FluentListImpl(Collection<E> listFiltered) {
        super(listFiltered);
    }

    public FluentListImpl(Class<E> componentClass, ComponentInstantiator instantiator) {
        super();
        this.componentClass = componentClass;
        this.instantiator = instantiator;
    }

    public FluentListImpl(Class<E> componentClass, ComponentInstantiator instantiator, E... listFiltered) {
        super(new ArrayList<E>(Arrays.asList(listFiltered)));
        this.componentClass = componentClass;
        this.instantiator = instantiator;
    }

    public FluentListImpl(Class<E> componentClass, ComponentInstantiator instantiator, Collection<E> listFiltered) {
        super(listFiltered);
        this.componentClass = componentClass;
        this.instantiator = instantiator;
    }

    /**
     * Creates a FluentList from array of Selenium {@link WebElement}
     *
     * @param elements array of Selenium elements
     * @return FluentList of FluentWebElement
     */
    public static FluentListImpl<FluentWebElement> fromElements(ComponentInstantiator instantiator, WebElement... elements) {
        return fromElements(instantiator, Arrays.asList(elements));
    }

    /**
     * Creates a FluentList from an iterable of Selenium {@link WebElement}
     *
     * @param elements iterable of Selenium elements
     * @return FluentList of FluentWebElement
     */
    public static FluentListImpl<FluentWebElement> fromElements(ComponentInstantiator instantiator, Iterable<? extends WebElement> elements) {
        FluentListImpl<FluentWebElement> fluentWebElements = new FluentListImpl<>(FluentWebElement.class, instantiator);
        for (WebElement element : elements) {
            fluentWebElements.add(instantiator.newComponent(FluentWebElement.class, element));
        }
        return fluentWebElements;
    }

    @Override
    public List<WebElement> toElements() {
        ArrayList<WebElement> elements = new ArrayList<>();

        for (FluentWebElement fluentElement : this) {
            elements.add(fluentElement.getElement());
        }
        return elements;
    }

    @Override
    @ListElementAccessor(first = true)
    public E first() {
        if (this.size() == 0) {
            throw new NoSuchElementException("Element not found");
        }
        return get(0);
    }

    @Override
    @ListElementAccessor(last = true)
    public E last() {
        if (this.size() == 0) {
            throw new NoSuchElementException("Element not found");
        }
        return get(this.size() - 1);
    }

    @Override
    @ListElementAccessor(index = true)
    public E index(int index) {
        if (this.size() <= index) {
            throw new NoSuchElementException("Element not found");
        }
        return get(index);
    }

    @Override
    public boolean isPresent() {
        return Proxies.isPresent(this);
    }

    @Override
    public FluentList<E> now() {
        Proxies.now(this);
        if (this.size() == 0) {
            throw new NoSuchElementException("Element not found");
        }
        return this;
    }

    @Override
    public FluentList<E> reset() {
        Proxies.reset(this);
        return this;
    }

    @Override
    public boolean isLoaded() {
        return Proxies.isLoaded(this);
    }

    @Override
    public FluentList click() {
        if (this.size() == 0) {
            throw new NoSuchElementException("Element not found");
        }

        for (E fluentWebElement : this) {
            if (fluentWebElement.isEnabled()) {
                fluentWebElement.click();
            }
        }
        return this;
    }

    @Override
    public FluentList text(String... with) {
        if (this.size() == 0) {
            throw new NoSuchElementException("Element not found");
        }

        boolean atMostOne = false;
        if (with.length > 0) {
            int id = 0;
            String value;

            for (E fluentWebElement : this) {
                if (fluentWebElement.isDisplayed()) {
                    if (with.length > id) {
                        value = with[id++];
                    } else {
                        value = with[with.length - 1];
                    }
                    if (fluentWebElement.isEnabled()) {
                        atMostOne = true;
                        fluentWebElement.text(value);
                    }
                }
            }

            if (!atMostOne) {
                throw new NoSuchElementException("No element is displayed and enabled. Can't set a new value.");
            }
        }
        return this;
    }

    @Override
    public FluentList<E> clearAll() {
        if (this.size() == 0) {
            throw new NoSuchElementException("Element no found");
        }

        for (E fluentWebElement : this) {
            if (fluentWebElement.isEnabled()) {
                fluentWebElement.clear();
            }
        }

        return this;
    }

    @Override
    public void clear() {
        clearAll();
    }

    @Override
    public void clearList() {
        super.clear();
    }

    @Override
    public FluentListConditions each() {
        return new EachElementConditions(this);
    }

    @Override
    public FluentListConditions one() {
        return new AtLeastOneElementConditions(this);
    }

    @Override
    public FluentList<E> submit() {
        if (this.size() == 0) {
            throw new NoSuchElementException("Element no found");
        }

        for (E fluentWebElement : this) {
            if (fluentWebElement.isEnabled()) {
                fluentWebElement.submit();
            }
        }
        return this;
    }

    @Override
    public List<String> getValues() {
        return Lists.transform(this, new Function<E, String>() {
            public String apply(E webElement) {
                return webElement.getValue();
            }
        });
    }

    @Override
    public List<String> getIds() {
        return Lists.transform(this, new Function<E, String>() {
            public String apply(E webElement) {
                return webElement.getId();
            }
        });
    }

    @Override
    public List<String> getAttributes(final String attribute) {
        return Lists.transform(this, new Function<E, String>() {
            public String apply(E webElement) {
                return webElement.getAttribute(attribute);
            }
        });
    }

    @Override
    public List<String> getNames() {
        return Lists.transform(this, new Function<E, String>() {
            public String apply(E webElement) {
                return webElement.getName();
            }
        });
    }

    @Override
    public List<String> getTagNames() {
        return Lists.transform(this, new Function<E, String>() {
            public String apply(E webElement) {
                return webElement.getTagName();
            }
        });
    }

    @Override
    public List<String> getTextContents() {
        return Lists.transform(this, new Function<E, String>() {
            public String apply(E webElement) {
                return webElement.getTextContent();
            }
        });
    }

    @Override
    public List<String> getTexts() {
        return Lists.transform(this, new Function<E, String>() {
            public String apply(E webElement) {
                return webElement.getText();
            }
        });
    }

    @Override
    public String getValue() {
        if (this.size() > 0) {
            return this.get(0).getValue();
        }
        return null;
    }

    @Override
    public String getId() {
        if (this.size() > 0) {
            return this.get(0).getId();
        }
        return null;
    }

    @Override
    public String getAttribute(final String attribute) {
        if (this.size() > 0) {
            return this.get(0).getAttribute(attribute);
        }
        return null;
    }

    @Override
    public String getName() {
        if (this.size() > 0) {
            return this.get(0).getName();
        }
        return null;
    }

    @Override
    public String getTagName() {
        if (this.size() > 0) {
            return this.get(0).getTagName();
        }
        return null;
    }

    @Override
    public String getText() {
        if (this.size() > 0) {
            return this.get(0).getText();
        }
        return null;
    }

    @Override
    public String getTextContent() {
        if (this.size() > 0) {
            return this.get(0).getTextContent();
        }
        return null;
    }

    @Override
    public FluentList<E> $(String selector, Filter... filters) {
        return find(selector, filters);
    }

    @Override
    public FluentList<E> $(Filter... filters) {
        return find(filters);
    }

    @Override
    public FluentList<E> $(By locator, Filter... filters) {
        return find(locator, filters);
    }

    @Override
    public E $(Integer index, Filter... filters) {
        return find(index, filters);
    }

    @Override
    public FluentList<E> find(String selector, Filter... filters) {
        List<E> finds = new ArrayList<E>();
        for (FluentWebElement e : this) {
            finds.addAll((Collection<E>) e.find(selector, filters));
        }
        return new FluentListImpl<E>(componentClass, instantiator, finds);
    }

    @Override
    public FluentList<E> find(By locator, Filter... filters) {
        List<E> finds = new ArrayList<E>();
        for (FluentWebElement e : this) {
            finds.addAll((Collection<E>) e.find(locator, filters));
        }
        return new FluentListImpl<E>(componentClass, instantiator, finds);
    }

    @Override
    public FluentList<E> find(Filter... filters) {
        List<E> finds = new ArrayList<E>();
        for (FluentWebElement e : this) {
            finds.addAll((Collection<E>) e.find(filters));
        }
        return new FluentListImpl<E>(componentClass, instantiator, finds);
    }

    @Override
    public E find(String selector, Integer number, Filter... filters) {
        FluentList<E> fluentList = find(selector, filters);
        if (number >= fluentList.size()) {
            throw new NoSuchElementException(
                    "No such element with position: " + number + ". Number of elements available: " + fluentList.size()
                            + ". Selector: " + selector + ".");
        }
        return fluentList.get(number);
    }

    @Override
    public E find(By locator, Integer index, Filter... filters) {
        FluentList<E> fluentList = find(locator, filters);
        if (index >= fluentList.size()) {
            throw new NoSuchElementException(
                    "No such element with position: " + index + ". Number of elements available: " + fluentList
                            .size());
        }
        return fluentList.get(index);
    }

    @Override
    public E $(String selector, Integer index, Filter... filters) {
        return find(selector, index, filters);
    }

    @Override
    public E $(By locator, Integer index, Filter... filters) {
        return find(locator, index, filters);
    }

    @Override
    public E find(Integer index, Filter... filters) {
        FluentList<E> fluentList = find(filters);
        if (index >= fluentList.size()) {
            throw new NoSuchElementException(
                    "No such element with position: " + index + ". Number of elements available: " + fluentList.size()
                            + ".");
        }
        return fluentList.get(index);
    }

    @Override
    public E findFirst(By locator, Filter... filters) {
        return find(locator, 0, filters);
    }

    @Override
    public E findFirst(String selector, Filter... filters) {
        return find(selector, 0, filters);
    }

    @Override
    public E findFirst(Filter... filters) {
        return find(0, filters);
    }

    /**
     * Construct a FillConstructor in order to allow easy fill
     * Be careful - only the visible elements are filled
     *
     * @return fill constructor
     */
    public Fill fill() {
        return new Fill((FluentList<E>) this);
    }

    /**
     * Construct a FillSelectConstructor in order to allow easy list selection
     * Be careful - only the visible elements are filled
     *
     * @return fill constructor
     */
    public FillSelect fillSelect() {
        return new FillSelect(this);
    }

    @Override
    public <T extends FluentWebElement> FluentList<T> as(Class<T> componentClass) {
        List<T> elements = new ArrayList<>();

        for (E e : this) {
            elements.add(e.as(componentClass));
        }

        return new FluentListImpl<>(componentClass, instantiator, elements);
    }
}

/*
 * Copyright 2022 Javaxcel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javaxcel.converter.handler.registry.impl

import com.github.javaxcel.converter.handler.AbstractExcelTypeHandler
import com.github.javaxcel.converter.handler.ExcelTypeHandler
import com.github.javaxcel.converter.handler.impl.BooleanTypeHandler
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry
import io.github.imsejin.common.util.ReflectionUtils
import spock.lang.Specification

import static java.util.stream.Collectors.toList

class ExcelTypeHandlerRegistryImplSpec extends Specification {

    def "getHandler"() {
        given:
        def registry = new ExcelTypeHandlerRegistryImpl()
        def allTypes = registry.allTypes

        when:
        def handlers = allTypes.stream().map(registry::getHandler).filter(Objects::nonNull).collect toList()

        then:
        allTypes.size() == handlers.size()
    }

    def "getAllTypes"() {
        given:
        def registry = new ExcelTypeHandlerRegistryImpl()

        when:
        def allTypes = registry.allTypes

        then:
        allTypes != null
        allTypes.stream().noneMatch(Objects::isNull)
        def handlerMap = ReflectionUtils.getFieldValue(registry, registry.class.getDeclaredField("handlerMap")) as Map
        allTypes == handlerMap.keySet()
    }

    def "add"() {
        given:
        def registry = new ExcelTypeHandlerRegistryImpl()

        when: "Override type handler on class java.lang.Boolean"
        def overridden = registry.add(Boolean, new BooleanTypeHandler())

        then:
        overridden

        when: "Add new type handler on class java.lang.String"
        def added = !registry.add(Object, new ObjectTypeHandler())

        then:
        added
    }

    def "addAll"() {
        given:
        def registry = new ExcelTypeHandlerRegistryImpl()

        when: "Override type handlers with the same registry"
        def overridden = registry.addAll new ExcelTypeHandlerRegistryImpl()

        then:
        overridden

        when: "Add new type handler with new registry"
        def newRegistry = new TempExcelTypeHandlerRegistry()
        newRegistry.add(Object, new ObjectTypeHandler())
        def added = !registry.addAll(newRegistry)

        then:
        added
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private static class TempExcelTypeHandlerRegistry implements ExcelTypeHandlerRegistry {
        final Map<Class, ExcelTypeHandler> handlerMap = new HashMap<>()

        @Override
        <T> ExcelTypeHandler<T> getHandler(Class<T> type) {
            return this.handlerMap.get(type)
        }

        @Override
        Set<Class<?>> getAllTypes() {
            return this.handlerMap.keySet()
        }

        @Override
        <T> boolean add(Class<T> type, ExcelTypeHandler<T> handler) {
            return false
        }

        @Override
        boolean addAll(ExcelTypeHandlerRegistry registry) {
            return false
        }
    }

    private static class ObjectTypeHandler extends AbstractExcelTypeHandler<Object> {
        protected ObjectTypeHandler() {
            super(Object)
        }

        @Override
        protected String writeInternal(Object value, Object... args) throws Exception {
            return value.toString()
        }

        @Override
        Object read(String value, Object... args) throws Exception {
            return value
        }
    }

}

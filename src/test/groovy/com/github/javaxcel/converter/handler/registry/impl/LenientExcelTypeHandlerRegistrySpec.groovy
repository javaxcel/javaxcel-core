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
import com.github.javaxcel.converter.handler.impl.BigIntegerTypeHandler
import com.github.javaxcel.converter.handler.impl.DateTypeHandler
import com.github.javaxcel.converter.handler.impl.FileTypeHandler
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry
import io.github.imsejin.common.util.ReflectionUtils
import spock.lang.Specification

class LenientExcelTypeHandlerRegistrySpec extends Specification {

    def "getHandler"() {
        given:
        def registry = new LenientExcelTypeHandlerRegistry() as ExcelTypeHandlerRegistry
        def allTypes = registry.allTypes as List<Class<?>>

        when:
        def handlers = allTypes.collect { registry.getHandler it }

        then:
        allTypes.size() == handlers.size()
        allTypes == handlers.collect { it.type }

        expect:
        registry.getHandler(File).class == FileTypeHandler
        registry.getHandler(new File("") {}.class).class == FileTypeHandler
        registry.getHandler(BigInteger).class == BigIntegerTypeHandler
        registry.getHandler(new BigInteger("0") {}.class).class == BigIntegerTypeHandler
        registry.getHandler(Date).class == DateTypeHandler
        registry.getHandler(new Date() {}.class).class == DateTypeHandler
    }

    def "getAllTypes"() {
        given:
        def registry = new LenientExcelTypeHandlerRegistry() as ExcelTypeHandlerRegistry

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
        def registry = new LenientExcelTypeHandlerRegistry() as ExcelTypeHandlerRegistry

        when: "Add new type handler on class java.lang.Object"
        def added = registry.add(Object, new ObjectTypeHandler())

        then:
        added

        when: "Override type handler on class java.lang.Object"
        def overridden = registry.add(Object, new ObjectTypeHandler())

        then:
        !overridden
    }

    def "addAll"() {
        given:
        def registry = new LenientExcelTypeHandlerRegistry() as ExcelTypeHandlerRegistry
        def newRegistry = new TempExcelTypeHandlerRegistry()

        when: "Add empty registry to the other"
        def addedNone = !registry.addAll(registry)

        then:
        addedNone

        when: "Add new type handler with new registry"
        newRegistry.add(Object, new ObjectTypeHandler())
        def addedNew = registry.addAll newRegistry

        then:
        addedNew

        when: "Override type handlers with the same registry"
        def overridden = !registry.addAll(new LenientExcelTypeHandlerRegistry())

        then:
        overridden
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private static class TempExcelTypeHandlerRegistry implements ExcelTypeHandlerRegistry {
        final Map<Class, ExcelTypeHandler> handlerMap = new HashMap<>()

        @Override
        ExcelTypeHandler<?> getHandler(Class<?> type) {
            this.handlerMap.get(type)
        }

        @Override
        Set<Class<?>> getAllTypes() {
            this.handlerMap.keySet()
        }

        @Override
        <T> boolean add(Class<T> type, ExcelTypeHandler<T> handler) {
            def hasType = this.handlerMap.containsKey type
            if (!hasType) this.handlerMap.put(type, handler)

            hasType
        }

        @Override
        boolean addAll(ExcelTypeHandlerRegistry registry) {
            false
        }
    }

    private static class ObjectTypeHandler extends AbstractExcelTypeHandler<Object> {
        protected ObjectTypeHandler() {
            super(Object)
        }

        @Override
        protected String writeInternal(Object value, Object... args) throws Exception {
            value.toString()
        }

        @Override
        Object read(String value, Object... args) throws Exception {
            value
        }
    }

}

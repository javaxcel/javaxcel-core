package com.github.javaxcel

import com.github.javaxcel.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.converter.handler.registry.impl.ExcelTypeHandlerRegistryImpl
import com.github.javaxcel.internal.TimeUnitTypeHandler
import spock.lang.Specification

class JavaxcelSpec extends Specification {

    def "Creates an instance"() {
        given:
        def defaultRegistry = new DefaultExcelTypeHandlerRegistry()

        when:
        def javaxcel = Javaxcel.newInstance()

        then:
        javaxcel.@registry.allTypes == defaultRegistry.allTypes
    }

    def "Creates an instance with registry of handlers"() {
        given:
        def registry = new ExcelTypeHandlerRegistryImpl()
        registry.add(new TimeUnitTypeHandler())

        when:
        def javaxcel = Javaxcel.newInstance(registry)

        then:
        javaxcel.@registry.allTypes.size() == 1
    }

}

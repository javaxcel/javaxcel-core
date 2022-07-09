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

package com.github.javaxcel.in.strategy

import com.github.javaxcel.in.strategy.impl.KeyNames
import com.github.javaxcel.in.strategy.impl.Limit
import com.github.javaxcel.in.strategy.impl.Parallel
import spock.lang.Specification

class AbstractExcelReadStrategySpec extends Specification {

    def "test"() {
        given:
        def set = [] as Set<AbstractExcelReadStrategy>
        def strategy = new Limit(100)

        when:
        set.add null
        set.add strategy
        set.add new Parallel()

        then:
        set.size() == 3

        when:
        set.add null
        set.add strategy
        set.add new Parallel()
        set.add new KeyNames(["a"])

        then:
        set.size() == 4
    }

}

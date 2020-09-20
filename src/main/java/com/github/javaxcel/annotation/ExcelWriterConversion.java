package com.github.javaxcel.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelWriterConversion {

    String expression();

    /**
     * Class that has a converter method.
     *
     * @return class
     * @see #methodName()
     * @see #paramTypes()
     */
    Class<?> clazz() default Object.class;

    /**
     * Name of the method that converts a value
     * before {@link com.github.javaxcel.out.ExcelWriter} writes data.
     *
     * @return name of a converter method
     * @see #clazz()
     * @see #paramTypes()
     */
    String methodName() default "";

    /**
     * Parameter types of the converter method.
     *
     * @return parameter types
     * @see #clazz()
     * @see #methodName()
     */
    Class<?>[] paramTypes() default {};

}

package com.easydatabaseexport.ui.export.config;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Export
 *
 * @author lzy
 * @date 2022/11/18 8:31
 **/
@Target({FIELD})
@Retention(RUNTIME)
public @interface Export {

    /**
     * @see com.easydatabaseexport.common.CommonConstant COLUMN_HEAD_NAMES and INDEX_HEAD_NAMES
     */
    String name() default "";
}

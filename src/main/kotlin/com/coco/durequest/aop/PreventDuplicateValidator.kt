package com.coco.durequest.aop

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PreventDuplicateValidator(
    val includeFieldKeys: Array<String> = [],
    val optionalValues: Array<String> = [],
    val expiredTime: Long = 10_000L
)

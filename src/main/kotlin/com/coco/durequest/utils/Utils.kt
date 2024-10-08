package com.coco.durequest.utils

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.web.bind.annotation.RequestBody
import java.lang.reflect.Method
import java.security.MessageDigest
import jakarta.xml.bind.DatatypeConverter;

object Utils {

    private val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)

    fun extractRequestBody(pjp: ProceedingJoinPoint): Any? {
        for ((index, arg) in pjp.args.withIndex()) {
            if (isAnnotatedWithRequestBody(pjp, index)) {
                return arg
            }
        }
        return null
    }


    private fun isAnnotatedWithRequestBody(pjp: ProceedingJoinPoint, paramIndex: Int): Boolean {
        val method = getMethod(pjp)
        val parameterAnnotations = method.parameterAnnotations
        for (annotation: Annotation in parameterAnnotations[paramIndex]) {
            if (RequestBody::class.java.isAssignableFrom(annotation.annotationClass.java)) {
                return true
            }
        }
        return false

    }

    private fun getMethod(pjp: ProceedingJoinPoint): Method {
        val methodSignature = pjp.signature as MethodSignature
        return methodSignature.method

    }

    fun hashMD5(source: String): String {
        val messageDigest = MessageDigest.getInstance("MD5")
        val mdBytes = messageDigest.digest(source.toByteArray())
        return DatatypeConverter.printHexBinary(mdBytes)
    }
}
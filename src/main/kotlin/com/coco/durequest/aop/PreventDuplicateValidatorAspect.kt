package com.coco.durequest.aop

import com.coco.durequest.utils.Utils
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.connection.RedisStringCommands
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.types.Expiration
import org.springframework.stereotype.Component
import java.lang.RuntimeException


@Aspect
@Component
class PreventDuplicateValidatorAspect(
    //todo caffeine cache in toy project
    private val redisTemplate: RedisTemplate<Any, Any>,
    private val objectMapper: ObjectMapper
) {

    @Around(value = "@annotation(preventDuplicateValidator)", argNames = "pjp, preventDuplicateValidator")
    fun aroundAdvice(pjp: ProceedingJoinPoint, preventDuplicateValidator: PreventDuplicateValidator): Any {

        val includeFieldKeys = preventDuplicateValidator.includeFieldKeys
        val optionalValues = preventDuplicateValidator.optionalValues
        val expiredTime = preventDuplicateValidator.expiredTime

        if (includeFieldKeys.isEmpty()) {
            return pjp.proceed()
        }

        val requestBody = Utils.extractRequestBody(pjp) ?: return pjp.proceed()

        val requestBodyMap = convertJsonToMap(requestBody)

        val keyRedis = buildKeyRedisByIncludeKeys(includeFieldKeys, optionalValues, requestBodyMap);

        val keyRedisMD5 = Utils.hashMD5(keyRedis)

        deduplicateRequestByRedisKey(keyRedisMD5, expiredTime);

        return pjp.proceed();
    }

    private fun deduplicateRequestByRedisKey(key: String, expiredTime: Long) {
        val firstSet = redisTemplate.execute { connection: RedisConnection ->
            connection.set(
                key.toByteArray(), key.toByteArray(), Expiration.milliseconds(expiredTime),
                RedisStringCommands.SetOption.SET_IF_ABSENT
            )
        }

        if (firstSet == true) {
            return
        }
        throw RuntimeException("Duplicate Request")
    }


    private fun buildKeyRedisByIncludeKeys(
        includeKeys: Array<String>,
        optionalValues: Array<String>,
        requestBodyMap: Map<String, Any>
    ): String {
        val keyWithIncludeKey = includeKeys.mapNotNull {
            requestBodyMap[it]
        }.joinToString(separator = ":") { it.toString() }

        if (optionalValues.isNotEmpty()) {
            return keyWithIncludeKey + optionalValues.joinToString(separator = ":")
        }
        return keyWithIncludeKey
    }


    fun convertJsonToMap(jsonObject: Any): Map<String, Any> {
        return objectMapper.convertValue(jsonObject)
    }
}
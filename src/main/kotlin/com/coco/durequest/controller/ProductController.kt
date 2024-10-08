package com.coco.durequest.controller

import com.coco.durequest.aop.PreventDuplicateValidator
import com.coco.durequest.dto.ProductDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController {

    @PostMapping
    @PreventDuplicateValidator(
        includeFieldKeys = ["id", "name", "price"],
        optionalValues = ["createOne"],
        expiredTime = 40_000L
    )
    fun createOne(
        @RequestBody productDto: ProductDto
    ): ResponseEntity<ProductDto> {
        // do something
        return ResponseEntity.ok(productDto)
    }
}
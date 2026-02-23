package com.stukenvitalii.lab2.inventory.service.exceptions

class ItemNotFoundException(sku: String) : NoSuchElementException("SKU '$sku' не найден")


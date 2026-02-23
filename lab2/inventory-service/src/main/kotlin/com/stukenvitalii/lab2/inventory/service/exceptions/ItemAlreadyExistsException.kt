package com.stukenvitalii.lab2.inventory.service.exceptions

class ItemAlreadyExistsException(sku: String) : IllegalArgumentException("SKU '$sku' уже существует")


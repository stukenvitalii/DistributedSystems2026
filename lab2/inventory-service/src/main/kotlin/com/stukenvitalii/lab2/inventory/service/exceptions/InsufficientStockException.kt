package com.stukenvitalii.lab2.inventory.service.exceptions

class InsufficientStockException(sku: String) : IllegalStateException("Недостаточно товара для SKU '$sku'")


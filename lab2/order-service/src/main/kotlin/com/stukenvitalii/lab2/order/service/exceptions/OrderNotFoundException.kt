package com.stukenvitalii.lab2.order.service.exceptions

class OrderNotFoundException(id: Long) : NoSuchElementException("Заказ $id не найден")


package com.stukenvitalii.lab2.order.service.exceptions

class OrderAlreadyExistsException(externalId: String) : IllegalArgumentException("Заказ с externalId '$externalId' уже существует")


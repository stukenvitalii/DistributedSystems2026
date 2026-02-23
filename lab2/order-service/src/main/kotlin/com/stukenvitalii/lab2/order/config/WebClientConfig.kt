package com.stukenvitalii.lab2.order.config

import com.stukenvitalii.lab2.order.client.InventoryClientProperties
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig {

    @Bean
    fun inventoryWebClientBuilder(properties: InventoryClientProperties): WebClient.Builder {
        val tcpClient = TcpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.timeout.toMillis().toInt())
            .doOnConnected { connection ->
                connection.addHandlerLast(ReadTimeoutHandler(properties.timeout.toMillis(), TimeUnit.MILLISECONDS))
                connection.addHandlerLast(WriteTimeoutHandler(properties.timeout.toMillis(), TimeUnit.MILLISECONDS))
            }

        val httpClient = HttpClient.from(tcpClient)

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
    }
}


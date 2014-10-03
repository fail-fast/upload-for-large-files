package com.forfun.upload

import com.typesafe.config.ConfigFactory

class ServiceConfig {

  private [this] val config = ConfigFactory.load()

  class ApiConfig{

    val host = config.getString("api-service.host")
    val port = config.getInt("api-service.port")

  }

  val apiConfig = new ApiConfig()

}

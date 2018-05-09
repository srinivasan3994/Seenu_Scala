package com.sce.utils
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.Materializer
import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import spray.json._
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.errors.WakeupException;

import scala.concurrent.Future
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util._
import scala.collection.JavaConverters._

import com.sce.utils.AppConf._

  object KafkaConfig{
  
   val kafkaServer = config.getString("bootstrap.servers")
   val groupId = config.getString("group.id")
   
   def getProducerConfig : Properties = {
     val  props = new Properties()
     props.put("bootstrap.servers", kafkaServer)
     props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
     props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
     return props
   }
   
   def getConsumerConfig : Properties = {
     val  props = new Properties()
     props.put("bootstrap.servers", kafkaServer)
     props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
     props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
     props.put("group.id", groupId)
    
     return props
   }
  
}
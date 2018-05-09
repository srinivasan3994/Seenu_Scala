/*package com.sce.modules.akkaguice

import akka.actor.{Actor, IndirectActorProducer}
import com.google.inject.Injector


private[akkaguice] class ActorProducer[A <: Actor](injector: Injector, clazz: Class[A]) extends IndirectActorProducer {

  def actorClass = clazz

  def produce() = injector.getBinding(clazz).getProvider.get()

}
*/
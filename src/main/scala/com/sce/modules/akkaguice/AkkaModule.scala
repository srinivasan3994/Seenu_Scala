/*package com.sce.modules.akkaguice

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject.{AbstractModule, Inject, Injector, Provider}
import com.typesafe.config.Config
import com.sce.modules.akkaguice.AkkaModule.{ActorSystemProvider, MaterializerProvider}
import net.codingwell.scalaguice.ScalaModule

object AkkaModule {

  class ActorSystemProvider @Inject()(val config: Config, val injector: Injector) extends Provider[ActorSystem] {
    override def get() = {
      val system = ActorSystem("main-actor-system", config)
      // add the GuiceAkkaExtension to the system, and initialize it with the Guice injector
      GuiceAkkaExtension(system).initialize(injector)
      system
    }
  }

  class MaterializerProvider @Inject()(val system: ActorSystem) extends Provider[Materializer] {
    override def get() = ActorMaterializer()(system)
  }

}

class AkkaModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[ActorSystem].toProvider[ActorSystemProvider].asEagerSingleton()
    bind[Materializer].toProvider[MaterializerProvider].asEagerSingleton()
  }

}
*/
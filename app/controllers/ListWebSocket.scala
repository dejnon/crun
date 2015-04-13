package controllers

import akka.actor.{Props, ActorLogging, Actor, ActorRef}


object ListWebSocketActor {
  def props(out: ActorRef) = Props(new ListWebSocketActor(out))
}



class ListWebSocketActor(out: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case msg: String =>
      Api.listWs = out
      Api.refreshmachines
      Api.updateList
  }
}

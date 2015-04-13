package controllers

import akka.actor.{Props, ActorLogging, Actor, ActorRef}


object ListWebSocketActor {
  def props(out: ActorRef) = Props(new ListWebSocketActor(out))
}



class ListWebSocketActor(out: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case msg: String =>
      Api.listWs = out
      Api.updateList
      Thread.sleep(5000)
      out ! (
        """
          | [{
          |   "name": "Alfredo",
          |   "id": "3",
          |   "status": "Avilable"
          | }, {
          |   "name": "Bartus",
          |   "id": "4",
          |   "status": "Working"
          | }, {
          |   "name": "Celina",
          |   "id": "2",
          |   "status": "Offline"
          | }]
        """.stripMargin)
  }
}

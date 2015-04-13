package controllers
import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.BSONDocument
import scala.concurrent.Future
import java.io.{IOException, InputStreamReader, BufferedReader, InputStream}
import java.util.{Properties, UUID}

import scala.util.{Success, Failure}

//import javax.swing.JOptionPane
//import com.decodified.scalassh._
//import net.schmizz.sshj.transport.verification.HostKeyVerifier
//import akka.
import akka.actor._
import akka.contrib.pattern.{ClusterSingletonProxy, DistributedPubSubMediator}
import aws.{Exec, HelloCloud}
import com.google.common.base.Joiner
import com.typesafe.config.ConfigFactory
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.mvc._
import play.api.libs.json._
//import worker._
import fr.janalyse.ssh._
import jassh._

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._
import scala.concurrent.Future

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.PropertiesCredentials
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.DescribeInstancesResult
import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.ec2.model.Reservation
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.RunInstancesResult
import com.amazonaws.services.ec2.model.StopInstancesRequest
import com.amazonaws.services.ec2.model.StopInstancesResult
//import com.jcraft.jsch._

import reactivemongo.api._

import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection


case class Machine(name: String, instance: String)

object JsonFormats {
  import play.api.libs.json.Json
  import play.api.data._
  import play.api.data.Forms._

  implicit val machineFormat = Json.format[Machine]
}

object Api extends Controller with MongoController {
  var machines = scala.collection.mutable.Map[String, Instance]()
  val aws: HelloCloud = new HelloCloud(HelloCloud.setupCredentials)
  var listWs: ActorRef = null
  def machinesMongo: JSONCollection = db.collection[JSONCollection]("machines")

  def wsListMachines = WebSocket.acceptWithActor[String, String] { request => out =>
    ListWebSocketActor.props(out)
  }

  def refreshmachines = Action.async {
    val query = Json.obj()
    val cursor: Cursor[JsObject] = machinesMongo.find(query).cursor[JsObject]
    val futureMachinesList: Future[List[JsObject]] = cursor.collect[List]()
    val machinesMap = scala.collection.mutable.Map[String, Instance]()
    println(Api.machines)
    futureMachinesList.map {
      machines =>
        machines.map {
          machine => {
            val json = Json.parse(machine.toString)
            val name: String = (json \ "name").asOpt[String].get
            val instanceAddress: String = (json \ "instance").asOpt[String].get
            val instance = new Instance()
            instance.setPublicIpAddress(instanceAddress)
            instance.setInstanceId(name)
            machinesMap += name -> aws.updateDescription(instance)
          }
        }
        Api.machines = machinesMap
        println(Api.machines)
        Ok("OK")
    }
  }

  def updateList = {
    var mach = "["
    Api.machines.foreach({
      case (id: String, instance: Instance) => {
        mach ++= s"""{"id": "${id}", "name": "${id}"},"""
      }
    })
    if (mach.endsWith(",")) {
      mach = mach.dropRight(1)
    }
    mach ++= "]"
    if(listWs != null) {
      listWs ! mach
    }
  }

  def addmachine = Action {
    aws.printRunning

    var instance: Instance = aws.startInstance
    println(machines)
    // Wait while not running
    try {
      System.out.println("Waiting for " + instance.getInstanceId + " to start.")
      while (instance.getState.getName != "running") {
        Thread.sleep(5000)
        instance = aws.updateDescription(instance)
        System.out.println("Current state: " + instance.getState.getName)
      }
      if (instance.getState.getName == "running") {
        System.out.println("Running!")
      }
    }
    catch {
      case e: InterruptedException => {
      }
    }
    machines.put(instance.getInstanceId, instance)
    addmongo(instance.getInstanceId, instance)
    println(s"$machines")
    updateList
    Ok("OK")
  }

  def removemongo(name: String) = {
    println(s"removing ${name} from mongo")
    val selector = Json.obj(
      "name" -> name)

    val futureRemove = machinesMongo.remove(selector)

    futureRemove.onComplete {
      case Failure(e) => throw e
      case Success(lasterror) => {
        println("successfully removed document" + lasterror)
      }
    }
  }

  def addmongo(name: String, instance: Instance) = {
    val json = Json.obj(
      "name" -> name,
      "instance" -> instance.getPublicIpAddress,
      "created" -> new java.util.Date().getTime())

    machinesMongo.insert(json)
  }

  def removeall = Action {
    aws.printRunning
    machines.foreach {
      case (id: String, instance: Instance) =>
        removeone(id, instance)
    }
    println(s"$machines")
    machines = scala.collection.mutable.Map[String, Instance]()
    updateList
    Ok("OK")
  }

  def removeone(id: String, instance: Instance) = {
    if (instance != null) {
      var tempInstance = instance
      aws.stopInstance(tempInstance)
      // Wait for instance to stop
      try {
        System.out.println("Waiting for " + tempInstance.getInstanceId + " to stop.")
        while (tempInstance.getState.getName != "stopped") {
          Thread.sleep(5000)
          tempInstance = aws.updateDescription(tempInstance)
          System.out.println("Current state: " + tempInstance.getState.getName)
        }
        if (tempInstance.getState.getName == "stopped") {
          System.out.println("Stopped!")
          updateList
        }
      }
      catch {
        case e: InterruptedException => {
        }
      }
    }
    else {
      println(s"Didnt close instance $instance")
    }
    println("done")
    removemongo(id)
  }

  def wsScreens = WebSocket.acceptWithActor[String, String] { request => out =>
    ScreensWebSocketActor.props(out)
  }

}

//package aws
//
//import java.io.IOException
//import java.util
//
//import com.amazonaws.auth.{PropertiesCredentials, AWSCredentials}
//import com.amazonaws.services.ec2.AmazonEC2Client
//import com.amazonaws.services.ec2.model._
//
//class HelloCloud {
//
//  private val CREDENTIALS_FILE: String = "/AwsCredentials.properties"
//  private val IMAGE_ID: String = "ami-349b495d"
//  private val INSTANCE_TYPE: String = "t1.micro"
//
//  var ec2 = new AmazonEC2Client(credentials)
//
//  var credentials: AWSCredentials = {
//    try {
//      new PropertiesCredentials(classOf[HelloCloud].getResourceAsStream(CREDENTIALS_FILE))
//    }
//    catch {
//      case e: Exception => {
//        System.err.println("Failed to read credentials from file: " + CREDENTIALS_FILE + ", error was: " + e.getMessage)
//        System.exit(-1)
//        null
//      }
//    }
//  }
//
//  def printRunning {
//    val describeInstancesRequest: DescribeInstancesResult = ec2.describeInstances
//    val reservations: util.List[Reservation] = describeInstancesRequest.getReservations
//    val instances: util.HashSet[Instance] = new util.HashSet[Instance]
//    import scala.collection.JavaConversions._
//    for (reservation <- reservations) {
//      instances.addAll(reservation.getInstances)
//    }
//    System.out.println("This group has " + instances.size + " Amazon EC2 instance(s).")
//  }
//
//  def startInstance: Instance = {
//    val runRequest: RunInstancesRequest = new RunInstancesRequest().withInstanceType(INSTANCE_TYPE).withImageId(IMAGE_ID).withMinCount(1).withMaxCount(1)
//    val result: RunInstancesResult = ec2.runInstances(runRequest)
//    return result.getReservation.getInstances.iterator.next
//  }
//
//  def stopInstance(instance: Instance) {
//    val stopRequest: StopInstancesRequest = new StopInstancesRequest().withInstanceIds(instance.getInstanceId)
//    val result: StopInstancesResult = ec2.stopInstances(stopRequest)
//  }
//
//  def updateDescription(instance: Instance): Instance = {
//    val describeRequest: DescribeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(instance.getInstanceId)
//    val result: DescribeInstancesResult = ec2.describeInstances(describeRequest)
//    import scala.collection.JavaConversions._
//    for (r <- result.getReservations) {
//      import scala.collection.JavaConversions._
//      for (i <- r.getInstances) {
//        if (i.getInstanceId == instance.getInstanceId) {
//          return i
//        }
//      }
//    }
//    return null
//  }
//
//  def main(args: Array[String]) {
//    System.out.println("\n\n- - - - - - - - - - - - - - - \n")
//    val helloCloud: HelloCloud = new HelloCloud
//    helloCloud.printRunning
//    var instance: Instance = helloCloud.startInstance
//    try {
//      System.out.println("Waiting for " + instance.getInstanceId + " to start.")
//      while (true) {
//        if (instance.getState.getName == "running") {
//          System.out.println("Running!")
//        }
//        Thread.sleep(5000)
//        instance = helloCloud.updateDescription(instance)
//        System.out.println("Current state: " + instance.getState.getName)
//      }
//    }
//    catch {
//      case e: Exception => {
//      }
//    }
//    helloCloud.printRunning
//    helloCloud.stopInstance(instance)
//    try {
//      System.out.println("Waiting for " + instance.getInstanceId + " to stop.")
//      while (true) {
//        if (instance.getState.getName == "stopped") {
//          System.out.println("Stopped!")
//        }
//        Thread.sleep(5000)
//        instance = helloCloud.updateDescription(instance)
//        System.out.println("Current state: " + instance.getState.getName)
//      }
//    }
//    catch {
//      case e: Exception => {
//      }
//    }
//    System.out.println("\n\n- - - - - - - - - - - - - - - \n")
//  }
//}
//
//

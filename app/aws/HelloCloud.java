package aws;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;

/**
 * 
 * Hello-World example using Maven and AWS SDK. Based on example from AWS SDK.
 * Don't forget to configure your credentials in
 * src/main/resources/AwsCredentials.properties file before running this
 * example.
 * 
 * @author Daniel Espling (<a
 *         href="mailto:espling@cs.umu.se">espling@cs.umu.se</a>)
 * @author Johan Tordsson (<a
 *         href="mailto:tordsson@cs.umu.se">tordsson@cs.umu.se</a>)
 * 
 */
public class HelloCloud {

	/*
	 * IMPORTANT NOTE: If running this through Eclipse, you have to add the
	 * src/main/resources directory to the Eclipse build-path (right-click on it
	 * and select "Build-Path -> Use as Source Folder")
	 */

	// Credentials file
	private static final String CREDENTIALS_FILE = "/AwsCredentials.properties";

	// Type and size of Virtual machine to start
	private static final String IMAGE_ID = "ami-349b495d";
	private static final String INSTANCE_TYPE = "t1.micro";

	private AmazonEC2Client ec2;

	/**
	 * Create a new HelloCloud instance
	 * 
	 * @param credentials
	 *            The credentials to use when communicating with EC2
	 */
	public HelloCloud(AWSCredentials credentials) {
		this.ec2 = new AmazonEC2Client(credentials);
	}

	/**
	 * Prints the current amount of running instances
	 */
	public void printRunning() {
		DescribeInstancesResult describeInstancesRequest = ec2
				.describeInstances();
		List<Reservation> reservations = describeInstancesRequest
				.getReservations();
		Set<Instance> instances = new HashSet<Instance>();

		for (Reservation reservation : reservations) {
			instances.addAll(reservation.getInstances());
		}

		System.out.println("This group has " + instances.size()
				+ " Amazon EC2 instance(s).");
	}

	/**
	 * Start a new EC2 instance
	 */
	public Instance startInstance() {
		// Create request
		RunInstancesRequest runRequest = new RunInstancesRequest()
				.withInstanceType(INSTANCE_TYPE).withImageId(IMAGE_ID)
				.withMinCount(1)
				.withMaxCount(1)
                .withKeyName("ens14dka-keypair");

		// Request start
		RunInstancesResult result = ec2.runInstances(runRequest);

		// XXX Check return codes to ensure start was successful
		return result.getReservation().getInstances().iterator().next();
	}

	/**
	 * Stop an EC2 Instance
	 * 
	 * @param identifier
	 *            VM identifier
	 */
	public void stopInstance(Instance instance) {
		StopInstancesRequest stopRequest = new StopInstancesRequest()
				.withInstanceIds(instance.getInstanceId());
		StopInstancesResult result = ec2.stopInstances(stopRequest);
		// XXX Check return codes to ensure stop was successful
	}

	/**
	 * Update the state of this instance
	 * 
	 * @param instance
	 *            The instance to update
	 * @return The updated instance information
	 */
	public Instance updateDescription(Instance instance) {
		DescribeInstancesRequest describeRequest = new DescribeInstancesRequest()
				.withInstanceIds(instance.getInstanceId());
		DescribeInstancesResult result = ec2.describeInstances(describeRequest);

		for (Reservation r : result.getReservations()) {
			for (Instance i : r.getInstances()) {
				if (i.getInstanceId().equals(instance.getInstanceId())) {
					return i;
				}
			}
		}

		// XXX Error, do something.
		return null;
	}


    public static AWSCredentials setupCredentials() {
        // Read credentials from file
        AWSCredentials credentials = null;
        try {
            return credentials = new PropertiesCredentials(
                    HelloCloud.class.getResourceAsStream(CREDENTIALS_FILE));
        } catch (IOException e) {
            System.err.println("Failed to read credentials from file: "
                    + CREDENTIALS_FILE + ", error was: " + e.getMessage());
            System.exit(-1);
            return null;
        }

    }

    /**
     * Initiate, start a new VM, wait for it, then shut it down again.
     *
     * @param args
     *            (None expected)
     */
    public static void main(String[] args) {



        System.out.println("\n\n- - - - - - - - - - - - - - - \n");

        HelloCloud helloCloud = new HelloCloud(HelloCloud.setupCredentials());
        helloCloud.printRunning();

        Instance instance = helloCloud.startInstance();

        // Wait while not running
        try {
            System.out.println("Waiting for " + instance.getInstanceId()
                    + " to start.");
            while (true) {
                if (instance.getState().getName().equals("running")) {
                    System.out.println("Running!");
                    break;
                }

                Thread.sleep(5000);
                instance = helloCloud.updateDescription(instance);
                System.out.println("Current state: "
                        + instance.getState().getName());
            }
        } catch (InterruptedException e) {
        }

        // Shut down again
        helloCloud.printRunning();
        helloCloud.stopInstance(instance);

        // Wait for instance to stop
        try {
            System.out.println("Waiting for " + instance.getInstanceId()
                    + " to stop.");
            while (true) {
                if (instance.getState().getName().equals("stopped")) {
                    System.out.println("Stopped!");
                    break;
                }

                Thread.sleep(5000);
                instance = helloCloud.updateDescription(instance);
                System.out.println("Current state: "
                        + instance.getState().getName());
            }
        } catch (InterruptedException e) {
        }

        System.out.println("\n\n- - - - - - - - - - - - - - - \n");
    }
}

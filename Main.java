import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {
	static FileWriter fileWriter;
	static PrintWriter printWriter;
	static int disk1Cylinders, disk2Cylinders, head1Position, head2Position;

	static String[] cylindersWithRequests1, cylindersWithRequests2;

	static boolean debug = false;

	public static void main(String[] args) {
		System.out.println("Samuel vossen, 5.1.2023, assignment 6");
		String outputFileName = "Asg6Data_output.txt", inputFileName = "Asg6Data.txt", inputLine = null;
		FileReader fileReader;
		BufferedReader bufferedReader;
		try {
			File outputFile = new File(outputFileName);
			if (outputFile.exists()) {
				outputFile.delete();
			}
			fileWriter = new FileWriter(outputFileName, true);
			printWriter = new PrintWriter(fileWriter);
			fileReader = new FileReader(inputFileName);
			bufferedReader = new BufferedReader(fileReader);
			disk1Cylinders = Integer.parseInt(bufferedReader.readLine());
			head1Position = Integer.parseInt(bufferedReader.readLine());
			inputLine = bufferedReader.readLine();
			cylindersWithRequests1 = inputLine.split(" ");
			disk2Cylinders = Integer.parseInt(bufferedReader.readLine());
			head2Position = Integer.parseInt(bufferedReader.readLine());
			inputLine = bufferedReader.readLine();
			cylindersWithRequests2 = inputLine.split(" ");
			bufferedReader.close();
		} catch (IOException e) {
			System.out.println("Input file could not be found.");
		}

		doDiskScheduling(disk1Cylinders, head1Position, cylindersWithRequests1);
		doDiskScheduling(disk2Cylinders, head2Position, cylindersWithRequests2);
		printWriter.close();
	}

	public static void doDiskScheduling(int diskCylinders, int headPosition, String[] cylindersWithRequests) {

		int fcfsMovement = diskSchedulerFCFS(new DiskScheduler(diskCylinders, headPosition, cylindersWithRequests));
		int sstfMovement = diskSchedulerSSTF(new DiskScheduler(diskCylinders, headPosition, cylindersWithRequests));
		int scanMovement = diskSchedulerSCAN(new DiskScheduler(diskCylinders, headPosition, cylindersWithRequests));
		int cScanMovement = diskSchedulerCSCAN(new DiskScheduler(diskCylinders, headPosition, cylindersWithRequests));
		int lookMovement = diskSchedulerLOOK(new DiskScheduler(diskCylinders, headPosition, cylindersWithRequests));
		int cLookMovement = diskSchedulerCLOOK(new DiskScheduler(diskCylinders, headPosition, cylindersWithRequests));

		System.out.println("For FCFS, the total head movement was " + fcfsMovement + " cylinders.\n"
				+ "For SSTF, the total head movement was " + sstfMovement + " cylinders.\n"
				+ "For SCAN, the total head movement was " + scanMovement + " cylinders.\n"
				+ "For CSCAN, the total head movement was " + cScanMovement + " cylinders.\n"
				+ "For LOOK, the total head movement was " + lookMovement + " cylinders.\n"
				+ "For CLOOK, the total head movement was " + cLookMovement + " cylinders.\n");

		printWriter.println("For FCFS, the total head movement was " + fcfsMovement + " cylinders.\n"
				+ "For SSTF, the total head movement was " + sstfMovement + " cylinders.\n"
				+ "For SCAN, the total head movement was " + scanMovement + " cylinders.\n"
				+ "For CSCAN, the total head movement was " + cScanMovement + " cylinders.\n"
				+ "For LOOK, the total head movement was " + lookMovement + " cylinders.\n"
				+ "For CLOOK, the total head movement was " + cLookMovement + " cylinders.\n");
	}

	public static int diskSchedulerFCFS(DiskScheduler diskScheduler) {
		int headPosition = diskScheduler.getHeadPosition();
		int totalMovement = 0;

		for (int i = 0; i < diskScheduler.getRequests().length; i++) {
			totalMovement += Math.abs(headPosition - diskScheduler.getRequestLocation(i));
			headPosition = diskScheduler.getRequestLocation(i);
		}
		return totalMovement;
	}

	public static int diskSchedulerSSTF(DiskScheduler diskScheduler) {
		int totalMovement = 0;
		int[] reqDist = new int[diskScheduler.getRequests().length];

		int[] servSeq = new int[diskScheduler.getRequests().length];
		int shortestSeek = 0;

		for (int i = 0; i < diskScheduler.getRequests().length; i++) {
			servSeq[i] = diskScheduler.getRequestLocation(i);
			reqDist[i] = Math.abs(diskScheduler.getHeadPosition() - diskScheduler.getRequestLocation(i));
		}

		for (int i = 0; i < diskScheduler.getRequests().length; i++) {
			for (int j = i + 1; j < diskScheduler.getRequests().length; j++) {
				if (reqDist[i] > reqDist[j]) {
					shortestSeek = reqDist[i];
					reqDist[i] = reqDist[j];
					reqDist[j] = shortestSeek;

					shortestSeek = servSeq[i];
					servSeq[i] = servSeq[j];
					servSeq[j] = shortestSeek;
				}
			}
		}
		for (int i = 1; i < diskScheduler.getRequests().length; i++) {
			totalMovement += Math.abs(diskScheduler.getHeadPosition() - servSeq[i]);
			diskScheduler.setHeadPosition(servSeq[i]);
		}
		return totalMovement;
	}

	public static int diskSchedulerSCAN(DiskScheduler diskScheduler) {
		int[] requests = new int[diskScheduler.getRequests().length + 1];
		int totalMovement = 0;

		for (int i = 0; i < requests.length - 1; i++)
			requests[i] = diskScheduler.getRequestLocation(i);

		requests[requests.length - 1] = diskScheduler.getHeadPosition();
		DiskScheduler.sortRequests(requests);

		int maximum = requests[diskScheduler.getRequests().length];

		totalMovement = diskScheduler.getHeadPosition() + maximum;
		return totalMovement;
	}

	public static int diskSchedulerCSCAN(DiskScheduler diskScheduler) {
		int totalMovement = 0, tmpIndex = 0, moves = 0;
		int headReference = diskScheduler.getHeadPosition();
		int lowCount = 0, highCount = 0;
		for (int i = 0; i < diskScheduler.getRequests().length; i++) {
			if (diskScheduler.getRequestLocation(i) >= diskScheduler.getHeadPosition())
				highCount++;
			if (diskScheduler.getRequestLocation(i) < diskScheduler.getHeadPosition())
				lowCount++;
		}
		int[] lowPath = new int[lowCount];
		int[] highPath = new int[highCount];
		int[] serviceSeq = new int[lowCount + highCount];

		lowCount = 0;
		highCount = 0;
		for (int i = 0; i < diskScheduler.getRequests().length; i++) {
			if (diskScheduler.getRequestLocation(i) < diskScheduler.getHeadPosition()) {
				lowPath[lowCount] = diskScheduler.getRequestLocation(i);
				lowCount++;
			}
			if (diskScheduler.getRequestLocation(i) >= diskScheduler.getHeadPosition()) {
				highPath[highCount] = diskScheduler.getRequestLocation(i);
				highCount++;
			}
		}

		DiskScheduler.sortRequests(lowPath);
		DiskScheduler.sortRequests(highPath);

		if (Math.abs(diskScheduler.getHeadPosition() - diskScheduler.getNumCylinders() - 1) < Math
				.abs(diskScheduler.getHeadPosition() - 0)) {
			int j = 1;
			for (int i = 0; i < highCount; i++) {
				serviceSeq[j] = highPath[i];
				tmpIndex++;
				j++;
			}

			serviceSeq[tmpIndex] = diskScheduler.getNumCylinders() - 1;
			serviceSeq[tmpIndex + 1] = 0;

			j = (highCount + 3);
			for (int i = 0; i < lowCount; i++) {
				serviceSeq[j] = lowPath[i];
			}
		} else {
			int j = 1;
			tmpIndex = 0;
			for (int i = (lowCount - 1); i >= 0; i--) {
				serviceSeq[j] = lowPath[i];
				j++;
			}
			serviceSeq[j] = 0;
			serviceSeq[j + 1] = diskScheduler.getNumCylinders() - 1;

			j = (lowCount + 3);
			for (int i = (highCount - 1); i >= 0; i--) {
				serviceSeq[j] = highPath[i];
			}
		}
		serviceSeq[0] = diskScheduler.getHeadPosition();

		for (int i = 0; i < serviceSeq.length - 1; i++) {
			moves = Math.abs(serviceSeq[i + 1] - serviceSeq[i]);
			totalMovement += moves;
		}
		return totalMovement;
	}

	public static int diskSchedulerCLOOK(DiskScheduler theDisk) {
		int head = 0;
		int totalMovement = 0;
		int tmpIndex = 0;
		int[] serviceSeq;

		int[] requests = new int[theDisk.getRequests().length + 1];
		requests[0] = theDisk.getHeadPosition();
		serviceSeq = new int[requests.length];

		for (int i = 0; i < requests.length - 1; i++) {
			requests[i + 1] = theDisk.getRequestLocation(i);
		}

		DiskScheduler.sortRequests(requests);

		for (int i = 0; i < requests.length; i++) {
			if (theDisk.getHeadPosition() == requests[i]) {
				head = i;
				break;
			}
		}

		for (int i = head + 1; i < requests.length; i++) {
			serviceSeq[tmpIndex] = requests[i];
			tmpIndex++;
		}

		if (head != 0) {
			for (int i = 0; i < head; i++) {
				serviceSeq[tmpIndex] = requests[i];
				tmpIndex++;
			}
		}

		totalMovement = Math.abs(theDisk.getHeadPosition() - serviceSeq[0]);
		for (int i = 1; i < serviceSeq.length; i++) {
			totalMovement += Math.abs(serviceSeq[i] - serviceSeq[i - 1]);
		}

		return totalMovement;
	}

	public static int diskSchedulerLOOK(DiskScheduler theDisk) {
		int head = 0;
		int totalMovement = 0;
		int tmpIndex = 0;
		int[] serviceSeq;

		int[] requests = new int[theDisk.getRequests().length + 1];
		requests[0] = theDisk.getHeadPosition();
		serviceSeq = new int[requests.length];
		for (int i = 0; i < requests.length - 1; i++) {
			requests[i + 1] = theDisk.getRequestLocation(i);
		}
		DiskScheduler.sortRequests(requests);

		for (int i = 0; i < requests.length; i++) {
			if (theDisk.getHeadPosition() == requests[i])
				head = i;
		}

		int i2 = 0;
		if (theDisk.getNumCylinders() / 2 > theDisk.getHeadPosition()) {
			for (int i = head; i >= 0; i--) {
				serviceSeq[tmpIndex] = requests[i];
				tmpIndex += 1;
				i2 = tmpIndex;
			}
			tmpIndex = i2;

			for (int i = (head + 1); i < requests.length; i++) {
				serviceSeq[tmpIndex] = requests[i];
				tmpIndex += 1;
			}
			totalMovement = (theDisk.getHeadPosition() - requests[0]) + (requests[requests.length - 1] - requests[0]);
		} else {
			for (int i = head; i < requests.length; i++) {
				serviceSeq[tmpIndex] = requests[i];
				tmpIndex += 1;
				i2 = tmpIndex;
			}
			tmpIndex = i2;

			for (int i = (head - 1); i >= 0; i--) {
				serviceSeq[tmpIndex] = requests[i];
				tmpIndex += 1;
			}
			totalMovement = (requests[requests.length - 1] - theDisk.getHeadPosition()
					+ (requests[requests.length - 1] - requests[0]));
		}
		return totalMovement;
	}

}

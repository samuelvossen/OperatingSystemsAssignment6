public class DiskScheduler {
	private int numCylinders;
	private int headPosition;
	private int[] requests;
	private int[] cylinderReferences;

	public DiskScheduler(int numCylinders, int headPosition, String[] inputRequests) {
		this.numCylinders = numCylinders;
		this.headPosition = headPosition;
		requests = new int[inputRequests.length];
		cylinderReferences = new int[numCylinders];

		for (int i = 0; i < numCylinders; i++) {
			cylinderReferences[i] = i;
		}

		for (int i = 0; i < inputRequests.length; i++) {
			requests[i] = Integer.parseInt(inputRequests[i]);
			cylinderReferences[requests[i]] = 0;
		}
	}

	public int getNumCylinders() {
		return numCylinders;
	}

	public int getHeadPosition() {
		return headPosition;
	}

	public void setHeadPosition(int newHeadPosition) {
		headPosition = newHeadPosition;
	}

	public int getRequestLocation(int index) {
		if (index >= requests.length || index < 0) {
			return -1;
		}
		return requests[index];
	}

	public int[] getRequests() {
		return requests;
	}

	public static void sortRequests(int[] requestsToSort) {
		sort(requestsToSort, 0, requestsToSort.length - 1);
	}

	private static void sort(int[] arr, int left, int right) {
		if (left < right) {
			int pivot = partition(arr, left, right);
			sort(arr, left, pivot - 1);
			sort(arr, pivot + 1, right);
		}
	}

	private static int partition(int[] arr, int left, int right) {
		int lastLeft = left;
		int mid = (left + right) / 2;

		swap(arr, left, mid);
		int pivot = arr[left];

		for (int i = left + 1; i <= right; i++) {
			if (arr[i] < pivot) {
				lastLeft++;
				swap(arr, lastLeft, i);
			}
		}

		swap(arr, left, lastLeft);
		return lastLeft;
	}

	private static void swap(int[] arr, int i, int j) {
		int temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	}
}

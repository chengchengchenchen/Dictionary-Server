public class Main {
    public static void main(String[] args) {
        Status status = Status.OK;
        System.out.println("Status code: " + status.getCode()); // 输出: 200
        System.out.println("Status description: " + status.getDescription()); // 输出: OK
        System.out.println("Full status: " + status.toString()); // 输出: 200 OK

        Status statusFromCode = Status.fromCode(408);
        System.out.println("Status from code: " + statusFromCode); // 输出: 404 Not Found
    }
}
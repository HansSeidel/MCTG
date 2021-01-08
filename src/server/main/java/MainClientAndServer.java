public class MainClientAndServer {
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("server")) {
            MainServer.main(args);
        } else if (args.length == 1 && args[0].equals("client")) {
            MainClient.main(args);
        } else {
            System.out.println("invalid usage... start with 1 argument that should be either client or server");
        }
    }

}

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class CCS {
    private final int port;
    private final ExecutorService clientExecutor;
    private final ScheduledExecutorService statisticsExecutor;

    private final AtomicInteger totalClients = new AtomicInteger(0);
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger addOperations = new AtomicInteger(0);
    private final AtomicInteger subOperations = new AtomicInteger(0);
    private final AtomicInteger mulOperations = new AtomicInteger(0);
    private final AtomicInteger divOperations = new AtomicInteger(0);
    private final AtomicInteger errorOperations = new AtomicInteger(0);
    private final AtomicLong totalComputedSum = new AtomicLong(0);

    private final Statistics currentPeriodStats = new Statistics();

    public CCS(int port) {
        this.port = port;
        this.clientExecutor = Executors.newCachedThreadPool();
        this.statisticsExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        startUDPServer();

        startTCPServer();

        startStatisticsReporting();
    }

    private void startUDPServer() {
        Thread udpThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());

                    if (message.startsWith("CCS DISCOVER")) {
                        byte[] response = "CCS FOUND".getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(
                                response, response.length,
                                packet.getAddress(), packet.getPort()
                        );
                        socket.send(responsePacket);
                    }
                }
            } catch (IOException e) {
                System.err.println("UDP Server error: " + e.getMessage());
            }
        });
        udpThread.start();
    }

    private void startTCPServer() {
        Thread tcpThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    totalClients.incrementAndGet();
                    currentPeriodStats.incrementClients();
                    clientExecutor.submit(() -> handleClient(clientSocket));
                }
            } catch (IOException e) {
                System.err.println("TCP Server error: " + e.getMessage());
            }
        });
        tcpThread.start();
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                String result = processRequest(line);
                out.println(result);
            }
        } catch (IOException e) {
            System.err.println("Client handling error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private String processRequest(String request) {
        totalRequests.incrementAndGet();
        currentPeriodStats.incrementRequests();

        String[] parts = request.split(" ");
        if (parts.length != 3) {
            errorOperations.incrementAndGet();
            currentPeriodStats.incrementErrors();
            System.out.println("Request: " + request + " | Result: ERROR (invalid format)");
            return "ERROR";
        }

        try {
            String operation = parts[0];
            int arg1 = Integer.parseInt(parts[1]);
            int arg2 = Integer.parseInt(parts[2]);

            int result;
            switch (operation) {
                case "ADD":
                    addOperations.incrementAndGet();
                    currentPeriodStats.incrementAdd();
                    result = arg1 + arg2;
                    break;
                case "SUB":
                    subOperations.incrementAndGet();
                    currentPeriodStats.incrementSub();
                    result = arg1 - arg2;
                    break;
                case "MUL":
                    mulOperations.incrementAndGet();
                    currentPeriodStats.incrementMul();
                    result = arg1 * arg2;
                    break;
                case "DIV":
                    if (arg2 == 0) {
                        errorOperations.incrementAndGet();
                        currentPeriodStats.incrementErrors();
                        System.out.println("Request: " + request + " | Result: ERROR (division by zero)");
                        return "ERROR";
                    }
                    divOperations.incrementAndGet();
                    currentPeriodStats.incrementDiv();
                    result = arg1 / arg2;
                    break;
                default:
                    errorOperations.incrementAndGet();
                    currentPeriodStats.incrementErrors();
                    System.out.println("Request: " + request + " | Result: ERROR (unknown operation)");
                    return "ERROR";
            }

            totalComputedSum.addAndGet(result);
            currentPeriodStats.addToSum(result);
            System.out.println("Request: " + request + " | Result: " + result);
            return String.valueOf(result);

        } catch (NumberFormatException e) {
            errorOperations.incrementAndGet();
            currentPeriodStats.incrementErrors();
            System.out.println("Request: " + request + " | Result: ERROR (invalid numbers)");
            return "ERROR";
        }
    }


    private void startStatisticsReporting() {
        statisticsExecutor.scheduleAtFixedRate(() -> {
            System.out.println("\n=== Total Statistics ===");
            System.out.println("Total connected clients: " + totalClients.get());
            System.out.println("Total requests processed: " + totalRequests.get());
            System.out.println("Operations:");
            System.out.println("  ADD: " + addOperations.get());
            System.out.println("  SUB: " + subOperations.get());
            System.out.println("  MUL: " + mulOperations.get());
            System.out.println("  DIV: " + divOperations.get());
            System.out.println("Errors: " + errorOperations.get());
            System.out.println("Total sum of computed values: " + totalComputedSum.get());

            System.out.println("\n=== Last 10 Seconds Statistics ===");
            System.out.println("New clients: " + currentPeriodStats.getClients());
            System.out.println("Requests processed: " + currentPeriodStats.getRequests());
            System.out.println("Operations:");
            System.out.println("  ADD: " + currentPeriodStats.getAddOps());
            System.out.println("  SUB: " + currentPeriodStats.getSubOps());
            System.out.println("  MUL: " + currentPeriodStats.getMulOps());
            System.out.println("  DIV: " + currentPeriodStats.getDivOps());
            System.out.println("Errors: " + currentPeriodStats.getErrors());
            System.out.println("Sum of computed values: " + currentPeriodStats.getSum());
            System.out.println("=====================================\n");

            currentPeriodStats.reset();
        }, 10, 10, TimeUnit.SECONDS);
    }

    public void shutdown() {
        clientExecutor.shutdown();
        statisticsExecutor.shutdown();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar CCS.jar <port>");
            System.exit(1);
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
            if (port < 1024 || port > 65535) {
                throw new NumberFormatException("Port must be between 1024 and 65535");
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid port number. " + e.getMessage());
            System.exit(1);
            return;
        }

        CCS server = new CCS(port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            server.shutdown();
        }));

        System.out.println("Starting CCS server on port " + port);
        server.start();
    }
}
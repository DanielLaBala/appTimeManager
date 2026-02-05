import java.io.*;

public class Main {
    final static long entryTime = System.currentTimeMillis();

    static String processToSearch = "Discord.exe"; /* Test, si no se pasa args, se usara este, en mas de una instancia, ocurriran race conditions */
    static String cmdCommand;
    static String saveFileRoute;

    static long previousTime = 0; // El tiempo de sesiones anteriores.
    static long saveEvery = 10000;

    static boolean isRunning() throws IOException {
        Process p = Runtime.getRuntime().exec(cmdCommand);

        boolean running;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            running = br.readLine().split(",")[0].matches("^\"" + processToSearch + "\"$");
        }

        return running;
    }

    static long timePassed() {
        return (System.currentTimeMillis() - entryTime) + previousTime;
    }

    static File initializeSaveFile() throws IOException {
        File folder = new File("Data");

        if (!folder.exists()) {
            folder.mkdir();
        }

        File saveFile = new File(folder, saveFileRoute);

        if (!saveFile.exists()) {
            saveFile.createNewFile();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {
            String linea = br.readLine();
            if (linea != null && !linea.isEmpty()) {
                previousTime = Long.parseLong(linea);
            }
        }

        return saveFile;
    }

    static void updateSaveFile(File saveFile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile))) {
            bw.write(timePassed() + "");
        } catch (IOException e) {}
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length > 0) {
            processToSearch = args[0];
        }

        System.out.println("Iniciando conexion con el proceso " + processToSearch);

        cmdCommand = System.getenv("windir") + "/system32/tasklist.exe " + "/nh /fo csv /fi  \"IMAGENAME eq "+ processToSearch + "\"";
        saveFileRoute = processToSearch + "_timeProcessManager.txt";

        File saveFile = initializeSaveFile();

        boolean isRunning = true;

        while (isRunning) {
            isRunning = isRunning();

            if (isRunning) {
                Thread.sleep(saveEvery);
                updateSaveFile(saveFile);
            }
        }

        System.out.println("Se ha perdido la señal del proceso " + processToSearch);

        updateSaveFile(saveFile);
    }
}
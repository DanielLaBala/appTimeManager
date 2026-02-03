import java.io.*;

public class Main {
    static String processToSearch = "explorer.exe"; /* Test, si no se pasa args, se usara este */
    static String cmdCommand;
    static String saveFileRoute;

    static long time = 0;
    static long repeatEvery = 6000; // ms

    static boolean isRunning() throws IOException {
        Process p = Runtime.getRuntime().exec(cmdCommand);

        boolean running;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            running = br.readLine().split(",")[0].matches("^\"" + processToSearch + "\"$");
        }

        return running;
    }

    static File initializeSaveFile() throws IOException {
        File saveFile = new File(saveFileRoute);

        if (!saveFile.exists()) {
            saveFile.createNewFile();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {
            String linea = br.readLine();
            if (linea != null) {
                if (linea.isEmpty()) {
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile))) {
                        bw.write("0"); // 0 hara que escriba el caracter ascii 0
                    }
                } else {
                    time = Integer.parseInt(linea);
                }
            }
        }

        return saveFile;
    }

    static void updateSaveFile(File saveFile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile))) {
            bw.write(time + "");
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
                Thread.sleep(repeatEvery);
                time += repeatEvery;
                updateSaveFile(saveFile);
            }
        }

        System.out.println("Se ha perdido la señal del proceso " + processToSearch);

        updateSaveFile(saveFile);
    }
}
import java.io.*;

public class Main {
    static final String PROCESS_TO_SEARCH = "explorer.exe";
    static final String CMD_COMMAND = System.getenv("windir") + "/system32/tasklist.exe " + "/nh /fo csv /fi  \"IMAGENAME eq "+ PROCESS_TO_SEARCH + "\"";
    static final String saveFileRoute = PROCESS_TO_SEARCH + "_timeProcessManager.txt";
    //
    static long time = 0;
    static long repeatEvery = 1000; // ms
    static long saveEvery = 10000;

    static boolean isRunning() throws IOException {
        Process p = Runtime.getRuntime().exec(CMD_COMMAND);

        boolean running;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            running = br.readLine().split(",")[0].matches("^\"" + PROCESS_TO_SEARCH + "\"$");
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
        File saveFile = initializeSaveFile();

        boolean isRunning = true;

        while (isRunning) {
            isRunning = isRunning();

            if (isRunning) {
                Thread.sleep(repeatEvery);
                time += repeatEvery;

                if (time % saveEvery == 0) {
                    updateSaveFile(saveFile);
                }
            }
        }
    }
}
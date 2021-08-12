import java.io.*;
import java.util.ArrayList;

public class Reader {
    public ArrayList<String[]> readDatabase(File file) {
        ArrayList<String[]> data = new ArrayList<>();
        try {
            BufferedReader textReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = textReader.readLine()) != null) {
                data.add(line.split(" "));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}

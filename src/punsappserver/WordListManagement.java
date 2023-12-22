package punsappserver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class WordListManagement {

    static void loadWordsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("words_charades.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] wordsInLine = line.split(",\\s*"); // Split words by commas and optional spaces
                RoomServer.words.addAll(Arrays.asList(wordsInLine));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

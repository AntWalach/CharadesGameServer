package punsappserver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

//Operations for word list
public class WordListManagement {

    // Loads words from a file and adds them to the RoomServer.words list
    static void loadWordsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("words_charades.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] wordsInLine = line.split(",\\s*"); // Split words by commas and optional spaces
                RoomServer.words.addAll(Arrays.asList(wordsInLine)); // Add loaded words to the list
            }
        } catch (IOException e) {
            e.printStackTrace(); // Print error if file reading fails
        }
    }
}

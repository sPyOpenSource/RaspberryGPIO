/*
 * Copyright (C) 2026 xuyi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package test;

/**
 *
 * @author xuyi
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class G2PTokenizerTest {

    private final Map<String, Integer> tokenToId = new HashMap<>();
    private final Map<Integer, String> idToToken = new HashMap<>();
    
    // Speciale tokens
    private static final String UNK_TOKEN = "<unk>";
    private static final String START_TOKEN = "<s>";
    private static final String END_TOKEN = "</s>";

    public G2PTokenizerTest() throws IOException {
        loadVocab("/Users/xuyi/Downloads/sphinx/g2p-seq2seq-model-6.2/vocab.g2p");
    }

    // Bouw de indexen op basis van het vocab.g2p bestand
    private void loadVocab(String vocabPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(vocabPath))) {
            String line;
            int id = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    tokenToId.put(line, id);
                    idToToken.put(id, line);
                    id++;
                }
            }
        }
    }

    // Zet invoertekst om naar ID's voor het AI-model (Grapheme Encoding)
    private List<Integer> encode(String text) {
        List<Integer> ids = new ArrayList<>();
        
        // Optioneel: Voeg start token toe
        if (tokenToId.containsKey(START_TOKEN)) ids.add(tokenToId.get(START_TOKEN));

        // Splits tekst op in losse karakters (grafemen)
        for (char ch : text.toLowerCase().toCharArray()) {
            String character = String.valueOf(ch);
            if (tokenToId.containsKey(character)) {
                ids.add(tokenToId.get(character));
            } else {
                // Karakter is onbekend (bijv. een speciaal teken of cijfer)
                ids.add(tokenToId.getOrDefault(UNK_TOKEN, 1));
            }
        }

        // Optioneel: Voeg eind token toe
        if (tokenToId.containsKey(END_TOKEN)) ids.add(tokenToId.get(END_TOKEN));

        return ids;
    }

    // Zet de ID's uit het AI-model om naar leesbare fonemen (Phoneme Decoding)
    private String decode(List<Integer> ids) {
        List<String> phonemes = new ArrayList<>();

        for (int id : ids) {
            String token = idToToken.getOrDefault(id, UNK_TOKEN);
            
            // Negeer speciale tokens in de uiteindelijke uitspraak-output
            if (!token.equals(UNK_TOKEN) && !token.equals(START_TOKEN) && 
                !token.equals(END_TOKEN) && !token.equals("<pad>")) {
                phonemes.add(token);
            }
        }
        return String.join(" ", phonemes);
    }

    // Test de Tokenizer
    @Test
    public void test() {
        try {
            // Initialiseer met het bestand
            G2PTokenizerTest tokenizer = new G2PTokenizerTest();

            // 1. ENCODING (Invoer voor uw model)
            String inputWord = "hallo";
            List<Integer> encodedIds = tokenizer.encode(inputWord);
            System.out.println("Invoerwoord: " + inputWord);
            System.out.println("Model Invoer ID's: " + encodedIds);

            // 2. DECODING (Stel dat dit de output ID's van het AI-model zijn)
            // In een echt scenario genereert uw model deze array met ID's
            List<Integer> simulatedModelOutput = List.of(11, 12, 13, 14); // ID's voor HH AH L OW
            String decodedPhonemes = tokenizer.decode(simulatedModelOutput);
            System.out.println("\nModel Output ID's: " + simulatedModelOutput);
            System.out.println("Fonetische Uitspraak: " + decodedPhonemes);

        } catch (IOException e) {
            System.err.println("Fout bij het laden van vocab.g2p: " + e.getMessage());
        }
    }
}


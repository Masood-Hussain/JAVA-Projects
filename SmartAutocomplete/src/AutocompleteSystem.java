package SmartAutocomplete.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class AutocompleteSystem {
    private final Trie trie = new Trie();
    private final HashMap<String, Integer> frequencyMap = new HashMap<>();

    public void addWord(String word) {
        trie.insert(word);
        frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
    }

    public void useWord(String word) {
        if (trie.search(word)) {
            frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
        }
    }

    public List<String> getSuggestions(String prefix, int maxSuggestions) {
        List<String> results = new ArrayList<>();
        Trie.TrieNode node = trie.getRoot();
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) return results;
            node = node.children.get(c);
        }
        collectWords(node, prefix, results);
        PriorityQueue<WordFrequency> pq = new PriorityQueue<>();
        for (String word : results) {
            pq.add(new WordFrequency(word, frequencyMap.getOrDefault(word, 0)));
        }
        List<String> suggestions = new ArrayList<>();
        while (!pq.isEmpty() && suggestions.size() < maxSuggestions) {
            suggestions.add(pq.poll().word);
        }
        return suggestions;
    }

    private void collectWords(Trie.TrieNode node, String prefix, List<String> results) {
        if (node.isWord) results.add(prefix);
        for (Map.Entry<Character, Trie.TrieNode> entry : node.children.entrySet()) {
            collectWords(entry.getValue(), prefix + entry.getKey(), results);
        }
    }
}

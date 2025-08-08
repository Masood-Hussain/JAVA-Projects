package SmartAutocomplete.src;

import java.util.HashMap;

public class Trie {
    public static class TrieNode {
        public HashMap<Character, TrieNode> children = new HashMap<>();
        public boolean isWord = false;
    }

    private final TrieNode root = new TrieNode();

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isWord = true;
    }

    public boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (!node.children.containsKey(c)) return false;
            node = node.children.get(c);
        }
        return node.isWord;
    }

    public TrieNode getRoot() {
        return root;
    }
}

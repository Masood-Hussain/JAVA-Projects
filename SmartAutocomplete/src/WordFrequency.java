package SmartAutocomplete.src;

public class WordFrequency implements Comparable<WordFrequency> {
    public String word;
    public int frequency;

    public WordFrequency(String word, int frequency) {
        this.word = word;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(WordFrequency other) {
        return Integer.compare(other.frequency, this.frequency); // Descending
    }
}

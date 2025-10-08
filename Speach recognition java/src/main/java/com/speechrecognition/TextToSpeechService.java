package com.speechrecognition;

import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.CompletableFuture;

/**
 * Text to Speech Service
 * Uses system TTS and synthetic speech generation for cross-platform compatibility
 * Provides a simple and efficient way to convert text to speech without external dependencies
 */
public class TextToSpeechService {
    
    private boolean isInitialized = false;
    private boolean isSpeaking = false;
    private String currentOS;
    private String currentLanguage = "en";
    private String currentVoice = "en";
    
    public TextToSpeechService() {
        initialize();
    }
    
    private void initialize() {
        currentOS = System.getProperty("os.name").toLowerCase();
        isInitialized = true;
        System.out.println("TTS Service initialized for OS: " + currentOS);
    }
    
    public void speak(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        
        // Clean the text (remove placeholder text)
        String cleanText = cleanTextForSpeech(text);
        if (cleanText.isEmpty()) {
            return;
        }
        
        // Use a synchronized approach to handle multiple calls properly
        synchronized (this) {
            if (isSpeaking) {
                System.out.println("TTS: Already speaking, please wait...");
                return;
            }
            isSpeaking = true;
        }
        
        System.out.println("TTS: Starting to speak: " + cleanText.substring(0, Math.min(cleanText.length(), 50)));
        
        // Try system TTS first, fallback to synthetic speech
        CompletableFuture.runAsync(() -> {
            try {
                if (!speakWithSystemTTS(cleanText)) {
                    System.out.println("TTS: System TTS failed, trying synthetic...");
                    speakWithSyntheticTTS(cleanText);
                }
                System.out.println("TTS: Finished speaking");
            } catch (Exception e) {
                System.err.println("TTS Error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                synchronized (this) {
                    isSpeaking = false;
                }
                System.out.println("TTS: Ready for next speech");
            }
        });
    }
    
    private String cleanTextForSpeech(String text) {
        // Remove placeholder text and clean the input
        String cleaned = text.trim();
        
        // Remove common placeholder texts
        if (cleaned.contains("✨ Type your message here") || 
            cleaned.contains("🔊 SPEAK") ||
            cleaned.contains("click 'SPEAK'")) {
            return "";
        }
        
        // Remove emoji and special characters for better TTS
        cleaned = cleaned.replaceAll("[🎤🔊✨🎙️🗑️✅]", "");
        cleaned = cleaned.replaceAll("\n+", " ");
        cleaned = cleaned.trim();
        
        return cleaned;
    }
    
    public void setLanguage(String language) {
        this.currentLanguage = language;
        this.currentVoice = language; // Reset voice to default for language
        System.out.println("TTS: Language set to " + language);
    }
    
    public void setVoice(String voice) {
        this.currentVoice = voice;
        System.out.println("TTS: Voice set to " + voice);
    }
    
    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    public String getCurrentVoice() {
        return currentVoice;
    }
    
    public boolean isSpeaking() {
        return isSpeaking;
    }
    
    private boolean speakWithSystemTTS(String text) {
        try {
            ProcessBuilder pb;
            
            if (currentOS.contains("win")) {
                // Windows - use PowerShell with Speech API
                String command = String.format(
                    "Add-Type -AssemblyName System.Speech; " +
                    "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$speak.Speak('%s')", 
                    text.replace("'", "''").replace("\"", "'")
                );
                pb = new ProcessBuilder("powershell", "-Command", command);
            } else if (currentOS.contains("mac")) {
                // macOS - use say command
                pb = new ProcessBuilder("say", text);
            } else {
                // Linux - try different approaches for better audio compatibility
                if (isCommandAvailable("espeak") && isCommandAvailable("aplay")) {
                    // Use espeak with aplay for better audio compatibility
                    pb = new ProcessBuilder("sh", "-c", "espeak --stdout -s 160 -v " + currentVoice + " '" + text.replace("'", "'\\''") + "' | aplay");
                    System.out.println("TTS: Using espeak + aplay with voice: " + currentVoice);
                } else if (isCommandAvailable("espeak")) {
                    pb = new ProcessBuilder("espeak", "-s", "160", "-v", currentVoice, text);
                    System.out.println("TTS: Using espeak direct with voice: " + currentVoice);
                } else if (isCommandAvailable("spd-say")) {
                    // spd-say doesn't support voice selection the same way
                    pb = new ProcessBuilder("spd-say", "-r", "-10", text);
                    System.out.println("TTS: Using spd-say");
                } else if (isCommandAvailable("festival")) {
                    pb = new ProcessBuilder("sh", "-c", "echo '" + text.replace("'", "'\\''") + "' | festival --tts");
                    System.out.println("TTS: Using festival");
                } else {
                    System.out.println("TTS: No system TTS available");
                    return false;
                }
            }
            
            // Add environment cleanup
            pb.environment().remove("LD_LIBRARY_PATH");
            
            System.out.println("TTS: Executing command: " + pb.command());
            Process process = pb.start();
            
            // Capture error output for debugging
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("TTS Error output: " + line);
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }).start();
            
            // Wait for completion with timeout
            boolean finished = process.waitFor(15, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                System.err.println("TTS: Process timeout, destroying...");
                process.destroyForcibly();
                return false;
            }
            
            int exitCode = process.exitValue();
            System.out.println("TTS: Process finished with exit code: " + exitCode);
            return exitCode == 0;
            
        } catch (Exception e) {
            System.err.println("System TTS error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean isCommandAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("which", command);
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void speakWithSyntheticTTS(String text) {
        CompletableFuture.runAsync(() -> {
            try {
                generateSyntheticSpeech(text);
            } catch (Exception e) {
                System.err.println("Synthetic TTS error: " + e.getMessage());
                // Final fallback: system beep sequence
                playBeepSequence(text);
            }
        });
    }
    
    private void generateSyntheticSpeech(String text) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(22050, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        
        line.open(format);
        line.start();
        
        String[] words = text.toLowerCase().split("\\s+");
        
        for (String word : words) {
            if (word.trim().isEmpty()) continue;
            
            // Generate speech-like tones for each word
            generateWordTones(line, word, format);
            
            // Pause between words
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        line.drain();
        line.close();
    }
    
    private void generateWordTones(SourceDataLine line, String word, AudioFormat format) {
        int sampleRate = (int) format.getSampleRate();
        
        // Generate different tones for different syllables/characters
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            double frequency = getFrequencyForChar(c);
            int duration = 80 + (int)(Math.random() * 120); // Variable duration for naturalness
            
            generateTone(line, frequency, duration, sampleRate);
        }
    }
    
    private double getFrequencyForChar(char c) {
        // Map characters to frequencies that sound somewhat speech-like
        double baseFreq = 150.0; // Base frequency (around human speech range)
        
        // Vowels get lower frequencies
        if ("aeiou".indexOf(Character.toLowerCase(c)) != -1) {
            return baseFreq + (c % 5) * 50; // 150-350 Hz
        }
        
        // Consonants get higher frequencies
        return baseFreq + 200 + (c % 10) * 30; // 350-650 Hz
    }
    
    private void generateTone(SourceDataLine line, double frequency, int durationMs, int sampleRate) {
        int samples = (int) ((durationMs / 1000.0) * sampleRate);
        byte[] buffer = new byte[samples * 2]; // 16-bit samples
        
        for (int i = 0; i < samples; i++) {
            double time = (double) i / sampleRate;
            
            // Create a more complex waveform that sounds more speech-like
            double fundamental = Math.sin(2 * Math.PI * frequency * time);
            double harmonic2 = 0.3 * Math.sin(2 * Math.PI * frequency * 2 * time);
            double harmonic3 = 0.1 * Math.sin(2 * Math.PI * frequency * 3 * time);
            
            double amplitude = 0.2 * (fundamental + harmonic2 + harmonic3);
            
            // Apply envelope for natural sound
            double envelope = 1.0;
            if (i < samples * 0.1) {
                envelope = i / (samples * 0.1); // Fade in
            } else if (i > samples * 0.8) {
                envelope = (samples - i) / (samples * 0.2); // Fade out
            }
            
            amplitude *= envelope;
            
            // Add some noise for more natural sound
            amplitude += (Math.random() - 0.5) * 0.05;
            
            short sample = (short) (amplitude * 32767);
            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) (sample >> 8);
        }
        
        line.write(buffer, 0, buffer.length);
    }
    
    private void playBeepSequence(String text) {
        // Final fallback: play different beep patterns for different words
        try {
            String[] words = text.split("\\s+");
            for (String word : words) {
                if (word.trim().isEmpty()) continue;
                
                // Different beep patterns for different word lengths
                int beeps = Math.min(word.length() / 2 + 1, 5);
                for (int i = 0; i < beeps; i++) {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    Thread.sleep(100);
                }
                Thread.sleep(300); // Pause between words
            }
        } catch (Exception e) {
            // Single beep as ultimate fallback
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
    
    public void speakAsync(String text) {
        CompletableFuture.runAsync(() -> speak(text));
    }
    
    public boolean isAvailable() {
        return isInitialized;
    }
    
    public void shutdown() {
        // Nothing to clean up in this implementation
    }
    
    // Method to test TTS availability
    public String getTTSInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Operating System: ").append(currentOS).append("\n");
        
        if (currentOS.contains("win")) {
            info.append("TTS Method: Windows Speech API (PowerShell)\n");
        } else if (currentOS.contains("mac")) {
            info.append("TTS Method: macOS say command\n");
        } else {
            info.append("TTS Method: Linux TTS engines\n");
            info.append("Available engines: ");
            if (isCommandAvailable("espeak")) info.append("espeak ");
            if (isCommandAvailable("festival")) info.append("festival ");
            if (isCommandAvailable("spd-say")) info.append("spd-say ");
            info.append("\n");
        }
        
        info.append("Fallback: Synthetic speech generation");
        return info.toString();
    }
}

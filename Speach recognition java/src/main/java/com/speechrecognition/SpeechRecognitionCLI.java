package com.speechrecognition;

import java.util.Scanner;

/** Simple CLI fallback when GUI not available */
public class SpeechRecognitionCLI {
    public static void run() {
        System.out.println("=== Speech Recognition CLI (2025 AI) ===");
        System.out.println("Type 'rec' to capture 5s and transcribe with advanced AI. Type 'say <text>' for TTS. 'exit' to quit.");
        TextToSpeechService tts = new TextToSpeechService();
        SpeechToTextService speechService = new SpeechToTextService();
        Scanner sc = new Scanner(System.in);
        try {
            while (true) {
                System.out.print("> ");
                if (!sc.hasNextLine()) break;
                String line = sc.nextLine().trim();
                if (line.equalsIgnoreCase("exit")) break;
            if (line.startsWith("say ")) {
                String txt = line.substring(4).trim();
                tts.speak(txt);
                System.out.println("[TTS] speaking: " + txt);
                continue;
            }
            if (line.equalsIgnoreCase("rec")) {
                System.out.println("[Enhanced STT] Recording 5 seconds - speak clearly...");
                final StringBuilder collected = new StringBuilder();
                speechService.setOnResult(r -> { 
                    synchronized (collected) { 
                        collected.append(r).append(' ');
                        System.out.println("[RECOGNIZING] " + r);
                    } 
                });
                speechService.setOnError(e -> System.out.println("[ERR] " + e));
                speechService.setOnStatusChange(s -> System.out.println("[STATUS] " + s));
                speechService.startListening();
                sleep(5000);
                speechService.stopListening();
                synchronized (collected) {
                    String result = collected.toString().trim();
                    if (result.isEmpty()) {
                        result = "No speech detected - try speaking louder";
                    }
                    System.out.println("[FINAL RESULT] " + result);
                }
                continue;
            }
                System.out.println("Unknown command. Try: rec | say <text> | exit");
            }
        } finally {
            sc.close();
        }
        System.out.println("Bye.");
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}

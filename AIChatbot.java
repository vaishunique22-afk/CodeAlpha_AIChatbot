import java.util.*;

public class AIChatbot {

    static class FAQEntry {
        List<String> keywords;
        List<String> responses; 

        FAQEntry(List<String> keywords, List<String> responses) {
            this.keywords = keywords;
            this.responses = responses;
        }
    }

    private static final List<FAQEntry> KNOWLEDGE_BASE = new ArrayList<>();
    private static final Random random = new Random();
    private static String userName = null;

    static {
        train("greeting", Arrays.asList("hello", "hi", "hey", "greetings", "morning", "evening"),
                Arrays.asList("Hello! How can I help you today?", "Hi there! What can I do for you?"));

        train("bot_name", Arrays.asList("your", "name", "who", "call", "you"),
                Arrays.asList("I'm CodeAlpha Assistant, a Java-based chatbot built for this project!"));

        train("hours", Arrays.asList("hours", "open", "time", "working", "available"),
                Arrays.asList("We're available Monday to Friday, 9 AM to 6 PM.", "Our support hours are 9 AM - 6 PM, Mon-Fri."));

        train("pricing", Arrays.asList("price", "cost", "pricing", "fee", "charge", "expensive"),
                Arrays.asList("Our pricing plans start at $9.99/month. Would you like more details?"));

        train("contact", Arrays.asList("contact", "email", "phone", "reach", "support"),
                Arrays.asList("You can reach us at support@example.com or call +1-800-555-0199."));

        train("refund", Arrays.asList("refund", "return", "money", "back", "cancel"),
                Arrays.asList("We offer a full refund within 30 days of purchase, no questions asked."));

        train("shipping", Arrays.asList("shipping", "delivery", "ship", "deliver", "arrive"),
                Arrays.asList("Standard shipping takes 3-5 business days. Express shipping is available too."));

        train("account", Arrays.asList("account", "signup", "register", "login", "sign"),
                Arrays.asList("You can create an account by clicking 'Sign Up' on our homepage."));

        train("password", Arrays.asList("password", "forgot", "reset", "locked"),
                Arrays.asList("To reset your password, click 'Forgot Password' on the login page."));

        train("thanks", Arrays.asList("thanks", "thank", "appreciate", "helpful"),
                Arrays.asList("You're very welcome!", "Happy to help!", "Anytime!"));

        train("how_are_you", Arrays.asList("how", "are", "feeling", "doing"),
                Arrays.asList("I'm just a program, but I'm running smoothly! How can I help you?"));

        train("capabilities", Arrays.asList("what", "can", "do", "help", "features"),
                Arrays.asList("I can answer questions about hours, pricing, refunds, shipping, accounts, and more. Just ask!"));

        train("joke", Arrays.asList("joke", "funny", "laugh"),
                Arrays.asList("Why do programmers prefer dark mode? Because light attracts bugs!"));
    }

    private static void train(String tag, List<String> keywords, List<String> responses) {
        KNOWLEDGE_BASE.add(new FAQEntry(keywords, responses));
    }

       public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================================");
        System.out.println("        AI CHATBOT (CodeAlpha Task 3)");
        System.out.println("=================================================");
        System.out.println("Bot: Hi! I'm your assistant. Type 'help' to see what I can do,");
        System.out.println("     or 'bye' anytime to end our chat.");

        boolean chatting = true;
        while (chatting) {
            System.out.print("\nYou: ");
            String input = scanner.nextLine();

            if (input == null || input.trim().isEmpty()) {
                System.out.println("Bot: I didn't quite catch that. Could you say it again?");
                continue;
            }

            String trimmed = input.trim();
            List<String> tokens = tokenize(trimmed);

            if (tokens.contains("bye") || tokens.contains("exit") || tokens.contains("quit")) {
                String farewell = userName != null
                        ? "Bot: Goodbye, " + userName + "! Have a great day!"
                        : "Bot: Goodbye! Have a great day!";
                System.out.println(farewell);
                chatting = false;
                continue;
            }

            if (tokens.contains("help")) {
                printHelp();
                continue;
            }

            String capturedName = tryExtractName(trimmed);
            if (capturedName != null) {
                userName = capturedName;
                System.out.println("Bot: Nice to meet you, " + userName + "! How can I help you today?");
                continue;
            }

            String response = matchResponse(tokens);
            System.out.println("Bot: " + response);
        }

        scanner.close();
    }

    private static void printHelp() {
        System.out.println("Bot: I can chat about: greetings, my name, hours, pricing, contact info,");
        System.out.println("     refunds, shipping, accounts, password resets, and I even know a joke!");
        System.out.println("     Try asking something like \"What are your hours?\" or \"How do I reset my password?\"");
    }

    private static List<String> tokenize(String input) {
        String cleaned = input.toLowerCase().replaceAll("[^a-z0-9\\s']", " ");
        String[] rawTokens = cleaned.trim().split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String t : rawTokens) {
            if (!t.isEmpty()) tokens.add(t);
        }
        return tokens;
    }

    private static String tryExtractName(String input) {
        String lower = input.toLowerCase();
        String marker = null;
        if (lower.contains("my name is")) marker = "my name is";
        else if (lower.contains("i am ")) marker = "i am ";
        else if (lower.contains("i'm ")) marker = "i'm ";
        else if (lower.contains("call me")) marker = "call me";

        if (marker == null) return null;

        int idx = lower.indexOf(marker);
        String rest = input.substring(idx + marker.length()).trim();
        if (rest.isEmpty()) return null;

        String candidate = rest.split("\\s+")[0].replaceAll("[^a-zA-Z]", "");
        if (candidate.isEmpty()) return null;

        return Character.toUpperCase(candidate.charAt(0)) + candidate.substring(1).toLowerCase();
    }

    private static String matchResponse(List<String> tokens) {
        FAQEntry bestMatch = null;
        int bestScore = 0;

        for (FAQEntry entry : KNOWLEDGE_BASE) {
            int score = 0;
            for (String token : tokens) {
                for (String keyword : entry.keywords) {
                    if (token.equals(keyword)) {
                        score += 2; // exact match worth more
                    } else if (token.length() > 3 && levenshteinDistance(token, keyword) <= 1) {
                        score += 1; 
                    }
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestMatch = entry;
            }
        }

        if (bestMatch != null && bestScore > 0) {
            List<String> responses = bestMatch.responses;
            return responses.get(random.nextInt(responses.size()));
        }

        return "I'm not sure I understand. Try asking about hours, pricing, refunds, shipping, "
                + "accounts, or type 'help' to see topics I know about.";
    }

    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }
}

package com.agrocycle.agrocycle.assistant;

import com.agrocycle.agrocycle.entity.Listing;
import com.agrocycle.agrocycle.repository.ListingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final ListingRepository listingRepository;

    public AssistantController(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }
    private static final Map<String, String> FAQ = new LinkedHashMap<>();

    static {
        FAQ.put("how to sell", "To sell crop waste on AgroCycle:\n1. Register or log in\n2. Click 'Post a Listing' and select 'I HAVE'\n3. Fill in the product name, category, quantity, price and location\n4. Add photos of your material\n5. Submit — buyers can now find and request your listing!");
        FAQ.put("how to buy", "To buy crop waste:\n1. Register or log in\n2. Go to Marketplace and search for what you need\n3. Click on a listing and press 'Request'\n4. Enter your delivery location and quantity\n5. Wait for seller approval — you'll be notified!");
        FAQ.put("delivery", "AgroCycle automatically matches a nearby delivery person after the seller accepts your order. You'll receive notifications at each step: assigned → picked up → in transit → delivered. You'll also get a 6-digit OTP to confirm delivery.");
        FAQ.put("categories", "We support: Paddy Straw & Rice Husk, Wheat Straw, Sugarcane Leaves & Bagasse, Cotton Stalks, Groundnut Shells, Coconut Husk, Maize Stalks, Vegetable Waste, Fruit Waste, and Other Agricultural Residues.");
        FAQ.put("registration", "Registration is free! Click Register and fill in your name, email, mobile, password, and address. Farmer/Buyer accounts are approved instantly. Delivery person applications require admin approval.");
        FAQ.put("payment", "Payments are arranged directly between buyer and seller. AgroCycle facilitates the connection and delivery. Prices are shown per unit (kg, tonne, bag etc.) on each listing.");
        FAQ.put("negotiate", "Yes! Listings marked as 'Negotiable' allow you to discuss price with the seller. Contact them through the order request process.");
        FAQ.put("cancel", "You can cancel an order before it is picked up for delivery. Once picked up, cancellation is not available. Go to My Orders and click Cancel.");
        FAQ.put("environment", "AgroCycle helps reduce crop burning by connecting people who have agricultural waste with those who need it. Every completed transaction represents material that was reused instead of burned. Check our home page for estimated impact statistics.");
        FAQ.put("support", "Need help? Go to Support → Create Ticket. Describe your issue and our team will respond. You can also reference a specific order number.");
        FAQ.put("paddy straw", "Paddy straw and rice husk are widely used for: mushroom cultivation, animal feed (after processing), paper making, biofuel, and organic compost. Post your paddy straw listing and reach buyers in the mushroom and biogas sector!");
        FAQ.put("sugarcane", "Sugarcane bagasse and leaves are used for: paper production, biofuel, compost, and animal fodder. Bagasse has high energy content, making it ideal for biomass power plants.");
        FAQ.put("wheat straw", "Wheat straw is used for: animal bedding, mushroom growing substrate, composting, biofuel, and construction materials (straw bales).");
        FAQ.put("otp", "The delivery OTP is a 6-digit code shown in your order details after the delivery person is assigned. Share it only with the delivery person at the time of delivery to confirm receipt.");
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "").toLowerCase().trim();
        String response = findAnswer(message);
        return Map.of("reply", response);
    }

    @GetMapping("/suggestions")
    public List<String> suggestions(@RequestParam String q) {
        String query = q.toLowerCase();
        List<String> suggestions = new ArrayList<>();
        // From categories
        for (Listing.Category cat : Listing.Category.values()) {
            String name = cat.name().replace("_", " ").toLowerCase();
            if (name.contains(query)) suggestions.add(name);
        }
        // From recent listings
        listingRepository.findFeaturedListings(PageRequest.of(0, 20)).stream()
            .map(Listing::getProductName)
            .filter(n -> n.toLowerCase().contains(query))
            .distinct().limit(5)
            .forEach(suggestions::add);
        return suggestions.stream().limit(8).toList();
    }

    private String findAnswer(String message) {
        // Direct FAQ keyword matching
        for (Map.Entry<String, String> entry : FAQ.entrySet()) {
            if (message.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Intent detection
        if (message.contains("hello") || message.contains("hi") || message.contains("namaste")) {
            return "Hello! I'm the AgroCycle Assistant. I can help you learn how to buy/sell crop waste, understand delivery, navigate the marketplace, and more. What would you like to know?";
        }
        if (message.contains("price") || message.contains("cost")) {
            return "Prices are set by individual sellers per unit (kg, tonne, bags). Use the Price filter on the Marketplace to find listings in your budget. Listings marked 'Negotiable' allow price discussions.";
        }
        if (message.contains("register") || message.contains("sign up") || message.contains("account")) {
            return FAQ.get("registration");
        }
        if (message.contains("notification") || message.contains("alert")) {
            return "Notifications appear in the bell icon at the top of the page. You'll be notified for: new orders, seller acceptance/rejection, delivery updates, support replies, and admin announcements. Check your email too if configured.";
        }
        if (message.contains("contact") || message.contains("help") || message.contains("ticket")) {
            return "To contact support, go to Support → Create Support Ticket. Describe your issue, select a category, and optionally attach an order number. Our team typically responds within 24 hours.";
        }
        if (message.contains("image") || message.contains("photo") || message.contains("picture")) {
            return "You can add up to 5 photos per listing. Use your phone camera to take a live photo, or upload existing images. Supported formats: JPEG, PNG, WebP. Maximum size: 10MB per image.";
        }

        // Fallback
        return "I didn't quite understand that. I can help with:\n• How to buy or sell crop waste\n• Delivery process and OTP\n• Categories of agricultural materials\n• Registration and account help\n• Payment and pricing\n• Support tickets\n\nOr try creating a support ticket if you need personalized help!";
    }
}

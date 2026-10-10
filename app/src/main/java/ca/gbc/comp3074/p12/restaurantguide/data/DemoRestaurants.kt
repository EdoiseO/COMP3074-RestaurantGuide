package ca.gbc.comp3074.p12.restaurantguide.data

import ca.gbc.comp3074.p12.restaurantguide.R

/** In-memory wireframe records, enriched from official menus/contact pages on October 10, 2026.
 * Ratings are sample personal ratings, not public review scores. See docs/demo-restaurant-research.md.
 */
object DemoRestaurants {
    val restaurants = listOf(
        Restaurant(
            id = 1,
            name = "Two Brothers Shawarma",
            description = "Chicken and beef shawarma served as plates or sandwiches, " +
                "alongside falafel and kebabs. The menu also includes chicken souvlaki, " +
                "poutine and salads, with baklava and kazandibi for dessert.",
            address = "2324 Lake Shore Blvd W, Etobicoke, ON M8V 1B6",
            phone = "416-201-8280",
            tags = listOf("Shawarma", "Kebabs", "Falafel", "Souvlaki", "Poutine", "Baklava"),
            rating = 4,
            photoRes = R.drawable.two_brothers_meal,
            mapRes = R.drawable.two_brothers_map,
            fullMapRes = R.drawable.two_brothers_full_map,
            listSummary = "Shawarma • Kebabs • Falafel",
            // Existing Figma sample gallery; official menu images illustrate food, not personal visits.
            galleryRes = listOf(R.drawable.two_brothers_meal, R.drawable.two_brothers_chicken,
                R.drawable.two_brothers_falafel, R.drawable.two_brothers_baklava),
        ),
        Restaurant(
            id = 2,
            name = "Midyeci Riza",
            description = "Turkish street food on Wilson Avenue, featuring midye dolma " +
                "(rice-stuffed mussels), kokorec and meatball sandwiches. The menu also " +
                "includes chicken and roast meat over rice, lentil soup and handmade ayran.",
            address = "954 Wilson Ave, North York, ON M3K 1E7",
            phone = "416-398-3884",
            tags = listOf("Turkish", "Stuffed mussels", "Kokorec", "Meatballs", "Rice dishes", "Ayran"),
            rating = 5,
            photoRes = R.drawable.midyeci_riza,
            mapRes = R.drawable.midyeci_riza_map,
            fullMapRes = R.drawable.midyeci_riza_full_map,
            listSummary = "Turkish • Stuffed mussels • Kokorec",
        ),
        Restaurant(
            id = 3,
            name = "La Unica",
            description = "Mexican bakery and taqueria on Wilson Avenue, serving Mexican " +
                "breads and tacos with fillings such as al pastor, carnitas, steak and " +
                "chicken. The Wilson menu also includes birria, burritos, quesadillas, " +
                "mulitas and tamales.",
            address = "850 Wilson Ave, North York, ON M3K 1E5",
            phone = "437-216-6130",
            tags = listOf("Mexican", "Bakery", "Tacos", "Birria", "Quesadillas", "Tamales"),
            rating = 3,
            photoRes = R.drawable.la_unica,
            mapRes = R.drawable.la_unica_map,
            fullMapRes = R.drawable.la_unica_full_map,
            listSummary = "Mexican bakery • Tacos • Birria",
        ),
    )
}

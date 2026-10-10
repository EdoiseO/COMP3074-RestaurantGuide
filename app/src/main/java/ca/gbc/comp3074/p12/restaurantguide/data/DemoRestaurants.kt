package ca.gbc.comp3074.p12.restaurantguide.data

import ca.gbc.comp3074.p12.restaurantguide.R

/** In-memory prototype records transcribed from the exported wireframes. */
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
            listSummary = "Shawarma • Kebabs • Falafel",
        ),
        Restaurant(
            id = 2,
            name = "Midyeci Riza",
            description = "Turkish street food",
            address = "",
            phone = "",
            tags = listOf("Turkish"),
            rating = 5,
            photoRes = R.drawable.midyeci_riza,
        ),
        Restaurant(
            id = 3,
            name = "La Unica",
            description = "Mexican bakery and restaurant",
            address = "",
            phone = "",
            tags = listOf("Mexican"),
            rating = 3,
            photoRes = R.drawable.la_unica,
        ),
    )
}

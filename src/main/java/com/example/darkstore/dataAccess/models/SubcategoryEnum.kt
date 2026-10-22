package com.example.darkstore.dataAccess.models

enum class SubcategoryEnum(val category: CategoryEnum) {
    MILK(CategoryEnum.DIARY),
    FERMENTED(CategoryEnum.DIARY),
    CHEESE(CategoryEnum.DIARY),
    YOGURT(CategoryEnum.DIARY),
    BUTTER(CategoryEnum.DIARY),
    EGGS(CategoryEnum.DIARY),
    BABY_MILK(CategoryEnum.DIARY),

    BREAD(CategoryEnum.BAKERY),
    CRISPBREADS(CategoryEnum.BAKERY),
    PASTRY(CategoryEnum.BAKERY),

    WATER(CategoryEnum.DRINKS),
    SODA(CategoryEnum.DRINKS),
    JUICE(CategoryEnum.DRINKS),
    COFFEE_TEA(CategoryEnum.DRINKS),
    ENERGETIC(CategoryEnum.DRINKS),
    BEER_AND_WINE(CategoryEnum.DRINKS),
    KVAS(CategoryEnum.DRINKS),

    CHIPS(CategoryEnum.SNACKS),
    CHOCOLATE(CategoryEnum.SNACKS),
    COOKIES(CategoryEnum.SNACKS),
    NUTS(CategoryEnum.SNACKS),

    POULTRY(CategoryEnum.MEAT),
    BEEF_PORK(CategoryEnum.MEAT),
    SAUSAGES(CategoryEnum.MEAT),
    MINCED(CategoryEnum.MEAT),

    PASTA(CategoryEnum.GROCERY),
    CEREALS(CategoryEnum.GROCERY),
    CANNED(CategoryEnum.GROCERY),
    SAUCES(CategoryEnum.GROCERY),
    OILS(CategoryEnum.GROCERY),

    FRESH_VEGETABLES(CategoryEnum.VEGETABLES),
    FRUITS(CategoryEnum.VEGETABLES),
    GREENS(CategoryEnum.VEGETABLES),

    ICE_CREAM(CategoryEnum.FREEZINGS),
    FROZEN_SEMIFINISHED(CategoryEnum.FREEZINGS), // пельмени, вареники
    FROZEN_VEGETABLES(CategoryEnum.FREEZINGS),

    READY_MEALS(CategoryEnum.DISHES),
    SALADS(CategoryEnum.DISHES);

    fun belongsTo(category: CategoryEnum?): Boolean = this.category == category

    companion object {
        fun byCategory(category: CategoryEnum): List<SubcategoryEnum> =
            entries.filter { it.category == category }
    }
}

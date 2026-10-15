package br.caio.delivery.app

import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.RestaurantId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal fun RestaurantId.asRouteValue(): String = value.toString()

@OptIn(ExperimentalUuidApi::class)
internal fun MenuItemId.asRouteValue(): String = value.toString()

@OptIn(ExperimentalUuidApi::class)
internal fun String.toRestaurantIdOrNull(): RestaurantId? = runCatching { RestaurantId(Uuid.parse(this)) }.getOrNull()

@OptIn(ExperimentalUuidApi::class)
internal fun String.toMenuItemIdOrNull(): MenuItemId? = runCatching { MenuItemId(Uuid.parse(this)) }.getOrNull()

const val RESTAURANT_DEEP_LINK_BASE = "delivery://restaurantes"

package tests

import team.genki.chotto.core.*
import kotlin.test.Test


@Suppress("unused")
object PhoneNumberTest : JsonTest() {

	@Test
	fun testJson() = testJson(
		value = PhoneNumber("value"),
		json = """ "value" """
	)
}
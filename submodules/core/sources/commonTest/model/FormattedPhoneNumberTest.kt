package tests

import team.genki.chotto.core.*
import kotlin.test.Test


@Suppress("unused")
object FormattedPhoneNumberTest : JsonTest() {

	@Test
	fun testJson() = testJson(
		value = FormattedPhoneNumber("value"),
		json = """ "value" """
	)
}

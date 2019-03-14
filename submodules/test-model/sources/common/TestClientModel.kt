package tests

import team.genki.chotto.client.model.*


object TestClientModel : ClientModel<TestCommandRequestMeta, TestCommandResponseMeta>(
	commandDescriptors = listOf(TestCommand),
	commandRequestMetaClass = TestCommandRequestMeta::class,
	commandResponseMetaClass = TestCommandResponseMeta::class,
	entityTypes = listOf(TestId),
	jsonConfiguration = testJsonConfiguration
) {

	override fun createDefaultResponseMeta() = TestCommandResponseMeta(
		property = null
	)


	override fun createRequestMetaForCommand(command: Command<*>) = TestCommandRequestMeta(
		property = "value"
	)
}


expect val testJsonConfiguration: JsonConfiguration
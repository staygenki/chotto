package team.genki.chotto.server

import com.github.fluidsonic.fluid.json.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.util.pipeline.*
import team.genki.chotto.core.*


internal object CommandRequestFeature : ApplicationFeature<ApplicationCallPipeline, Unit, Unit> {

	override val key = AttributeKey<Unit>("Chotto: command request feature")


	@Suppress("UNCHECKED_CAST")
	override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit) {
		Unit.configure()

		pipeline.receivePipeline.intercept(ApplicationReceivePipeline.Transform) { subject ->
			val data = subject.value as? CommandRequestPipelineData
				?: error("unexpected value in receive pipeline: ${subject.value}")

			val request = parseRequest(body = receiveBody())

			chottoCall.transactionController.onRequestReceived(request)

			proceedWith(ApplicationReceiveRequest(
				type = subject.type,
				value = request
			))
		}
	}


	private fun parseRequest(body: String): CommandRequest<*, *> {
		TODO() // FIXME
	}
//		try {
//			jsonParser.parseValueOfType<CommandRequest<*, *>>(body)
//		}
//		catch (e: JSONException) {
//			if (e is JSONException.Schema || e is JSONException.Syntax) {
//				throw CommandFailure(
//					code = "invalidRequest",
//					developerMessage = e.message,
//					userMessage = CommandFailure.genericUserMessage,
//					cause = e
//				)
//			}
//
//			throw e
//		}


	private suspend fun PipelineContext<ApplicationReceiveRequest, ApplicationCall>.receiveBody(): String {
		val request = call.request
		if (!request.contentType().withoutParameters().match(ContentType.Application.Json))
			throw CommandFailure(
				code = "invalidRequest",
				developerMessage = "Expected content of type '${ContentType.Application.Json}'",
				userMessage = CommandFailure.genericUserMessage
			)

		return request.receiveChannel().toByteArray().toString(request.contentCharset() ?: Charsets.UTF_8)
	}
}

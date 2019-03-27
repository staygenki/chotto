package team.genki.chotto.server

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext


internal class TransactionFeature<Context : ChottoServerContext, Transaction : ChottoTransaction>(
	private val configuration: ServerConfiguration<Context, Transaction>
) : ApplicationFeature<ApplicationCallPipeline, Unit, Unit> {

	override val key = AttributeKey<Unit>("Chotto: transaction feature")


	@Suppress("UNCHECKED_CAST")
	override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit) {
		Unit.configure()

		pipeline.intercept(ApplicationCallPipeline.Setup) {
			call.attributes.put(AttributeKeys.call, configuration.createCall(call))
		}
	}


	object AttributeKeys {

		val call = AttributeKey<ChottoCall<*>>("Chotto: call")
	}
}


internal val ApplicationCall.chottoCall
	get() = attributes[TransactionFeature.AttributeKeys.call]


internal val PipelineContext<*, ApplicationCall>.chottoCall
	get() = call.chottoCall
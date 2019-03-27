package team.genki.chotto.server

import com.github.fluidsonic.fluid.json.*
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey
import java.io.StringWriter


internal object CommandResponseFeature : ApplicationFeature<ApplicationCallPipeline, Unit, Unit> {

	override val key = AttributeKey<Unit>("Chotto: command response feature")


	@Suppress("UNCHECKED_CAST")
	override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit) {
		Unit.configure()

		pipeline.sendPipeline.intercept(ApplicationSendPipeline.Render) { subject ->
			if (subject is OutgoingContent) return@intercept

			val data = subject as? CommandResponsePipelineData
				?: error("unexpected value in send pipeline: $subject")

			val call = chottoCall

			proceedWith(serializeResponse(
				data = data,
				entityResolver = call.entityResolver as EntityResolver<ChottoTransaction>,
				transaction = call.transaction
			))
		}
	}


	private suspend fun serializeResponse(
		data: CommandResponsePipelineData,
		entityResolver: EntityResolver<ChottoTransaction>,
		transaction: ChottoTransaction
	): OutgoingContent {
		// we can't use ChottoCommandResponseJsonCodec due to on-the-fly entity resolution

		val writer = StringWriter()

		EntityResolvingJsonEncoder(
			codecProvider = JSONCodecProvider(data.model.jsonConverter.codecProvider, JSONCodecProvider.extended),
			resolver = entityResolver,
			transaction = transaction,
			writer = writer
		).apply {
			writeMapStart()

			writeMapElement("meta", value = data.meta)
			writeMapElement("result", value = data.result)

			writeMapKey("entities")
			writeEntities()

			// FIXME add to JSON codec
			writeMapElement("status", string = "success")

			writeMapEnd()
		}

		return TextContent(
			text = writer.toString(),
			contentType = ContentType.Application.Json.withCharset(Charsets.UTF_8),
			status = null
		)
	}
}
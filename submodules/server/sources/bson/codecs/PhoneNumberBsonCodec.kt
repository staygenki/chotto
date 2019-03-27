package team.genki.chotto.server

import org.bson.BsonReader
import org.bson.BsonWriter
import team.genki.chotto.core.*


internal object PhoneNumberBsonCodec : AbstractBsonCodec<PhoneNumber, BsonCodingContext>() {

	override fun BsonReader.decode(context: BsonCodingContext) =
		PhoneNumber(readString())


	override fun BsonWriter.encode(value: PhoneNumber, context: BsonCodingContext) {
		writeString(value.value)
	}
}
package team.genki.chotto.core

import kotlinx.serialization.*
import kotlinx.serialization.internal.*


internal object EntityMapSerializer : KSerializer<Map<EntityId, Entity>> {

	private val baseValueSerializer = PolymorphicSerializer(Entity::class)
	private val keySerializer = EntityId.serializer()
	private val typeParams = arrayOf(keySerializer, baseValueSerializer)

	override val descriptor = LinkedHashMapClassDesc(keySerializer.descriptor, baseValueSerializer.descriptor)


	override fun deserialize(decoder: Decoder): Map<EntityId, Entity> {
		val map = mutableMapOf<EntityId, Entity>()

		decoder.beginStructure(descriptor, *typeParams).apply {
			val size = decodeCollectionSize(descriptor)

			mainLoop@ while (true) {
				when (val index = decodeElementIndex(descriptor)) {
					CompositeDecoder.READ_ALL -> {
						require(size >= 0) { "Size must be known in advance when using READ_ALL" }
						(0 until size).forEach { readEntry(decoder = this, index = it, target = map, checkIndex = false) }
						break@mainLoop
					}
					CompositeDecoder.READ_DONE -> break@mainLoop
					else -> readEntry(decoder = this, index = index, target = map)
				}

			}
			endStructure(descriptor)

			return map
		}
	}


	@UseExperimental(ImplicitReflectionSerializer::class)
	private fun readEntry(decoder: CompositeDecoder, index: Int, target: MutableMap<EntityId, Entity>, checkIndex: Boolean = true) {
		val key = decoder.decodeSerializableElement(descriptor, index, keySerializer)

		val valueIndex =
			if (checkIndex)
				decoder.decodeElementIndex(descriptor).also {
					require(it == index + 1) { "Value must follow key in a map, index for key: $index, returned index for value: $it" }
				}
			else
				index + 1

		@Suppress("UNCHECKED_CAST")
		val valueSerializer = key.type.entityClass.serializer() as KSerializer<Entity>

		val value = decoder.decodeSerializableElement(descriptor, valueIndex, valueSerializer)

		target[key] = value
	}


	@UseExperimental(ImplicitReflectionSerializer::class)
	override fun serialize(encoder: Encoder, obj: Map<EntityId, Entity>) {
		encoder.beginCollection(descriptor, obj.size, *typeParams).apply {
			var index = 0
			for ((key, value) in obj) {
				encodeSerializableElement(descriptor, index++, keySerializer, key)

				@Suppress("UNCHECKED_CAST")
				val valueSerializer = key.type.entityClass.serializer() as KSerializer<Entity>

				encodeSerializableElement(descriptor, index++, valueSerializer, value)
			}

			endStructure(descriptor)
		}
	}
}

package tool.generator.api.metadata

open class ApiArg(
    var typeJava: String = "",
    var typeNative: String = "",
    var name: String = "",
    var optional: Boolean = false,
    var default: String? = null,
    var inSignatureJava: String = "final $typeJava $name",
    var inBodyJava: String = name,
    var inSignatureNative: String = "$typeNative $name",
    var inBodyNative: String = name,
)

/**
 * Used to represent arguments with a default value in the native body.
 * When rendered, such args only represent the native in-body side of the method.
 * They are ignored in signatures of all types.
 */
interface ApiArgDefault

open class ApiArgDefaultValue(
    value: String,
) : ApiArg(
    inBodyNative = value
), ApiArgDefault

class ApiArgNull : ApiArg(
    inBodyNative = "NULL"
), ApiArgDefault

/**
 * To represent reference types.
 */
class ApiArgPrimitivePtr(
    typeJava: String,
    typeNative: String,
    name: String,
    optional: Boolean,
    default: String?
) : ApiArg(
    typeJava = typeJava,
    typeNative = "$typeNative[]",
    name = name,
    optional = optional,
    default = default,
    inBodyJava = "$name == null ? new $typeNative[1] : $name.getData()",
    inBodyNative = "&$name[0]",
)

class ApiArgArray(
    typeJava: String,
    typeNative: String,
    name: String,
    optional: Boolean,
    default: String?
) : ApiArg(
    typeJava = typeJava,
    typeNative = typeNative,
    name = name,
    optional = optional,
    default = default,
    inSignatureJava = "final $typeJava[] $name",
    inSignatureNative = "$typeNative[] $name",
    inBodyNative = "&$name[0]",
)

/**
 * Structs are converted into the native pointer which is represented as a "long" type.
 */
class ApiArgStruct(
    typeJava: String,
    name: String,
    optional: Boolean,
    default: String?
) : ApiArg(
    typeJava = typeJava,
    typeNative = "long",
    name = name,
    optional = optional,
    default = default,
    inBodyJava = "${name}.ptr",
    inBodyNative = "(${typeJava.substringAfterLast('.')}*)$name"
)

interface ApiArgAlternate {
    fun getAlternativeArgJava(): ApiArg
}

open class ApiArgImVec2(
    name: String,
    optional: Boolean,
    default: String?,
    inSignatureNative: String = "float ${name}X, float ${name}Y",
    inBodyNative: String = "ImVec2(${name}X, ${name}Y)",
) : ApiArg(
    typeJava = "ImVec2",
    typeNative = "ImVec2",
    name = name,
    optional = optional,
    default = default,
    inBodyJava = "$name.x, $name.y",
    inSignatureNative = inSignatureNative,
    inBodyNative = inBodyNative,
), ApiArgAlternate {
    override fun getAlternativeArgJava(): ApiArg {
        return object : ApiArg(
            typeJava = typeJava,
            typeNative = "$typeNative[]",
            name = name,
            optional = optional,
            default = default,
            inSignatureJava = "final float ${name}X, final float ${name}Y",
            inBodyJava = "${name}X, ${name}Y",
            inSignatureNative = inSignatureNative,
            inBodyNative = inBodyNative,
        ) {
        }
    }
}

open class ApiArgImVec4(
    name: String,
    optional: Boolean,
    default: String?,
    inSignatureNative: String = "float ${name}X, float ${name}Y, float ${name}Z, float ${name}W",
    inBodyNative: String = "ImVec4(${name}X, ${name}Y, ${name}Z, ${name}W)",
) : ApiArg(
    typeJava = "ImVec4",
    typeNative = "ImVec4",
    name = name,
    optional = optional,
    default = default,
    inBodyJava = "$name.x, $name.y, $name.z, $name.w",
    inSignatureNative = inSignatureNative,
    inBodyNative = inBodyNative,
), ApiArgAlternate {
    override fun getAlternativeArgJava(): ApiArg {
        return object : ApiArg(
            typeJava = typeJava,
            typeNative = "$typeNative[]",
            name = name,
            optional = optional,
            default = default,
            inSignatureJava = "final float ${name}X, final float ${name}Y, final float ${name}Z, final float ${name}W",
            inBodyJava = "${name}X, ${name}Y, ${name}Z, ${name}W",
            inSignatureNative = inSignatureNative,
            inBodyNative = inBodyNative,
        ) {
        }
    }
}

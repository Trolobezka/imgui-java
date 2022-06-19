package tool.generator.api.metadata

open class ApiArg(
    val typeJava: String,
    val typeNative: String,
    val name: String,
    val optional: Boolean,
    val default: String? = null,
) {
    open fun inSignatureJava(): String = "final $typeJava $name"
    open fun inBodyJava(): String = name
    open fun inSignatureNative(): String = "$typeNative $name"
    open fun inBodyNative(): String = name
}

/**
 * Used to represent arguments with a default value in the native body.
 * When rendered, such args only represent the native in-body side of the method.
 * They are ignored in signatures of all types.
 */
open class ApiArgDefault(default: String?) : ApiArg("", "", "", false, default) {
    override fun inBodyNative() = default!!
}

class ApiArgNull : ApiArgDefault(null)  {
    override fun inBodyNative() = "NULL"
}

class ApiArgString(
    name: String,
    optional: Boolean,
    default: String?
) : ApiArg("String", "String", name, optional, default)

/**
 * To represent reference types.
 */
class ApiArgPrimitivePtr(
    typeJava: String,
    private val typeNativeSimple: String,
    name: String,
    optional: Boolean,
    default: String?
) : ApiArg(typeJava, "$typeNativeSimple[]", name, optional, default) {
    override fun inBodyJava(): String = "$name == null ? new $typeNativeSimple[1] : $name.getData()"
    override fun inBodyNative(): String = "&$name[0]"
}

// Structs are converted into the native pointer which is represented as a "long" type.
class ApiArgStruct(
    typeJava: String,
    name: String,
    optional: Boolean,
    default: String?
) : ApiArg(typeJava, "long", name, optional, default) {
    override fun inBodyJava(): String = "${name}.ptr"
    override fun inBodyNative(): String = "(${typeJava.substringAfterLast('.')}*)$name"
}

interface ApiArgAlternate {
    fun getAlternativeArgJava(): ApiArg
}

open class ApiArgImVec2(
    name: String,
    optional: Boolean,
    default: String?
) : ApiArg("ImVec2", "ImVec2", name, optional, default), ApiArgAlternate {
    override fun inBodyJava() = "$name.x, $name.y"
    override fun inSignatureNative() = "float ${name}X, float ${name}Y"
    override fun inBodyNative() = "ImVec2(${name}X, ${name}Y)"

    override fun getAlternativeArgJava(): ApiArg {
        return object : ApiArgImVec2(name, optional, default) {
            override fun inSignatureJava() = "final float ${name}X, final float ${name}Y"
            override fun inBodyJava() = "${name}X, ${name}Y"
        }
    }
}

open class ApiArgImVec4(
    name: String,
    optional: Boolean,
    default: String?
) : ApiArg("ImVec4", "ImVec4", name, optional, default), ApiArgAlternate {
    override fun inBodyJava() = "$name.x, $name.y, $name.z, $name.w"
    override fun inSignatureNative() = "float ${name}X, float ${name}Y, float ${name}Z, float ${name}W"
    override fun inBodyNative() = "ImVec4(${name}X, ${name}Y, ${name}Z, ${name}W)"

    override fun getAlternativeArgJava(): ApiArg {
        return object : ApiArgImVec4(name, optional, default) {
            override fun inSignatureJava() =
                "final float ${name}X, final float ${name}Y, final float ${name}Z, final float ${name}W"

            override fun inBodyJava() = "${name}X, ${name}Y, ${name}Z, ${name}W"
        }
    }
}

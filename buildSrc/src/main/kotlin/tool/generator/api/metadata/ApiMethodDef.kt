package tool.generator.api.metadata

class ApiMethodDef(
    val receiver: String,
    val name: String,
    val static: Boolean,
    val result: ApiResult?,
) {
    val args = mutableListOf<ApiArg>()

    fun arg(typeJava: String, typeNative: String, name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg(typeJava, typeNative, name, optional, default)
    }

    fun argNull() {
        args += ApiArgNull()
    }

    fun argString(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg("String", "String", name, optional, default)
    }

    fun argInt(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg("int", "int", name, optional, default)
    }

    fun argIntCast(name: String, cast: String, optional: Boolean = false, default: String? = null) {
        args += object : ApiArg("int", "int", name, optional, default) {
            override fun inBodyNative() = "$cast$name"
        }
    }

    fun argBoolean(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg("boolean", "boolean", name, optional, default)
    }

    fun argFloat(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg("float", "float", name, optional, default)
    }

    fun argBooleanPtr(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgPrimitivePtr("ImBoolean", "boolean", name, optional, default)
    }

    fun argIntPtr(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgPrimitivePtr("ImInt", "int", name, optional, default)
    }

    fun argImVec2(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgImVec2(name, optional, default)
    }

    fun argImVec4(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgImVec4(name, optional, default)
    }

    fun argStruct(type: String, name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgStruct(type, name, optional, default)
    }
}

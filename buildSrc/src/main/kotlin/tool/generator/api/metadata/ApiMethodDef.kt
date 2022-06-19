package tool.generator.api.metadata

class ApiMethodDef(
    val receiver: String,
    val name: String,
    val static: Boolean,
    val result: ApiResult?,
) {
    val args = mutableListOf<ApiArg>()

    fun argStr(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg.Str(name, optional, default)
    }

    fun argInt(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg.Int(name, optional, default)
    }
    fun argBool(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg.Bool(name, optional, default)
    }
    fun argFloat(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg.Float(name, optional, default)
    }

    fun argBoolPtr(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg.ImBoolean(name, optional, default)
    }

    fun argImVec2(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg.ImVec2(name, optional, default)
    }
    fun argImVec4(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg.ImVec4(name, optional, default)
    }

    fun argStruct(type: String, name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg.Struct(type, name, optional, default)
    }
}

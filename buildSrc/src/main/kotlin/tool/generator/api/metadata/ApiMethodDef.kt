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

    fun argDefault(default: String) {
        args += ApiArgDefault(default)
    }

    fun argPrefix(arg: ApiArg, prefix: String) {
        args += object : ApiArg(arg.typeJava, arg.typeNative, arg.name, arg.optional, arg.default) {
            override fun inBodyNative() = "$prefix${arg.name}"
        }
    }

    fun argString(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArg("String", "String", name, optional, default)
    }

    fun argBoolean(name: String, optional: Boolean = false, default: String? = null) {
        args += argBooleanRaw(name, optional, default)
    }

    fun argBooleanRaw(name: String, optional: Boolean = false, default: String? = null): ApiArg {
        return ApiArg("boolean", "boolean", name, optional, default)
    }

    fun argBooleanPtr(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgPrimitivePtr("imgui.type.ImBoolean", "boolean", name, optional, default)
    }

    fun argShort(name: String, optional: Boolean = false, default: String? = null) {
        args += argShortRaw(name, optional, default)
    }

    fun argShortRaw(name: String, optional: Boolean = false, default: String? = null): ApiArg {
        return ApiArg("short", "short", name, optional, default)
    }

    fun argShortPtr(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgPrimitivePtr("imgui.type.ImShort", "short", name, optional, default)
    }

    fun argInt(name: String, optional: Boolean = false, default: String? = null) {
        args += argIntRaw(name, optional, default)
    }

    fun argIntRaw(name: String, optional: Boolean = false, default: String? = null): ApiArg {
        return ApiArg("int", "int", name, optional, default)
    }

    fun argIntPtr(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgPrimitivePtr("imgui.type.ImInt", "int", name, optional, default)
    }

    fun argIntArr(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgArray("int", "int", name, optional, default)
    }

    fun argFloat(name: String, optional: Boolean = false, default: String? = null) {
        args += argFloatRaw(name, optional, default)
    }

    fun argFloatRaw(name: String, optional: Boolean = false, default: String? = null): ApiArg {
        return ApiArg("float", "float", name, optional, default)
    }

    fun argFloatPtr(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgPrimitivePtr("imgui.type.ImFloat", "float", name, optional, default)
    }

    fun argFloatArr(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgArray("float", "float", name, optional, default)
    }

    fun argDouble(name: String, optional: Boolean = false, default: String? = null) {
        args += argDoubleRaw(name, optional, default)
    }

    fun argDoubleRaw(name: String, optional: Boolean = false, default: String? = null): ApiArg {
        return ApiArg("double", "double", name, optional, default)
    }

    fun argDoublePtr(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgPrimitivePtr("imgui.type.ImDouble", "double", name, optional, default)
    }

    fun argDoubleArr(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgArray("double", "double", name, optional, default)
    }

    fun argLong(name: String, optional: Boolean = false, default: String? = null) {
        args += argLongRaw(name, optional, default)
    }

    fun argLongRaw(name: String, optional: Boolean = false, default: String? = null): ApiArg {
        return ApiArg("long", "long", name, optional, default)
    }

    fun argLongPtr(name: String, optional: Boolean = false, default: String? = null) {
        args += ApiArgPrimitivePtr("imgui.type.ImLong", "long", name, optional, default)
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

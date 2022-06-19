package tool.generator.api.metadata

abstract class ApiResult(val type: String)

class ApiResultPrimitive(type: String) : ApiResult(type)

class ApiResultString : ApiResult("String")

class ApiResultStruct(type: String, val static: Boolean, val isRef: Boolean) : ApiResult(type)

abstract class ApiResultImVec(typeJava: String, val typeNative: String) : ApiResult(typeJava)

class ApiResultImVec2 : ApiResultImVec("imgui.ImVec2", "ImVec2")
class ApiResultImVec4 : ApiResultImVec("imgui.ImVec4", "ImVec4")


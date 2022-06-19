package tool.generator.api.metadata

class ApiMethodFactory {
    fun create(methodDef: ApiMethodDef): Collection<String> {
        val methods = mutableListOf<String>()

        collectArgGroups(methodDef.args).forEach { argGroup ->
            methods.add(createMethodsJava(methodDef, argGroup))
            createAltMethodsJava(methodDef, argGroup)?.let(methods::add)
            methods.add(createMethodsNative(methodDef, argGroup))
        }

        return methods
    }

    private fun createAltMethodsJava(methodDef: ApiMethodDef, argGroup: ArgGroup): String? {
        val subGroup = mutableListOf<ApiArg>()

        for (apiArg in argGroup.args) {
            subGroup += if (apiArg is ApiArgAlternate) {
                apiArg.getAlternativeArgJava()
            } else {
                apiArg
            }
        }

        return if (subGroup == argGroup.args) {
            return null
        } else {
            createMethodsJava(methodDef, ArgGroup(subGroup))
        }
    }

    private fun createMethodsJava(methodDef: ApiMethodDef, argGroup: ArgGroup): String {
        val argsInSign = argGroup.renderJavaInSignature()
        val argsInBody = argGroup.renderJavaInBody()

        val methodMods = if (methodDef.static) "public static" else "public"
        val methodName = methodDef.name.decapitalize()
        val methodSignCall = "$methodName($argsInSign)"
        val methodCallNative = "n${methodDef.name}($argsInBody)"

        if (methodDef.result == null) {
            return """
            |$TAB$methodMods void $methodSignCall {
            |$TAB$TAB$methodCallNative;
            |$TAB}
            """.trimMargin()
        }

        val methodSign = "$methodMods ${methodDef.result.type} $methodSignCall"

        if (methodDef.result is ApiResultStruct) {
            if (methodDef.result.static) {
                val staticFieldName = "_gen_${methodName}_${argGroup.size}"

                return """
                |${TAB}private static final ${methodDef.result.type} $staticFieldName = new ${methodDef.result.type}(0);
                |$TAB$methodSign {
                |$TAB$TAB${staticFieldName}.ptr = $methodCallNative;
                |${TAB}return ${staticFieldName};
                |$TAB}
                """.trimMargin()
            } else {
                return """
                |$TAB$methodSign {
                |$TAB${TAB}return new ${methodDef.result.type}($methodCallNative);
                |$TAB}
                """.trimMargin()
            }
        }

        if (methodDef.result is ApiResultImVec) {
            val dstWithArgsInSign = "dst${if (argsInSign.isNotEmpty()) ", $argsInSign" else ""}"
            val dstWithArgsInBody = "dst${if (argsInBody.isNotEmpty()) ", $argsInBody" else ""}"

            var methods = """
            |$TAB$methodSign {
            |$TAB${TAB}final ${methodDef.result.type} dst = new ${methodDef.result.type}();
            |$TAB${TAB}n${methodDef.name}($dstWithArgsInBody);
            |$TAB${TAB}return dst;
            |$TAB}
            |
            |$TAB$methodMods void $methodName(final ${methodDef.result.type} $dstWithArgsInSign) {
            |$TAB${TAB}n${methodDef.name}($dstWithArgsInBody);
            |$TAB}
            |
            |$TAB$methodMods float ${methodName}X($argsInSign) {
            |$TAB${TAB}return n${methodDef.name}X($argsInBody);
            |$TAB}
            |$TAB$methodMods float ${methodName}Y($argsInSign) {
            |$TAB${TAB}return n${methodDef.name}Y($argsInBody);
            |$TAB}
            """.trimMargin()

            if (methodDef.result is ApiResultImVec4) {
                methods += """
                |
                |$TAB$methodMods float ${methodName}Z($argsInSign) {
                |$TAB${TAB}return n${methodDef.name}Z($argsInBody);
                |$TAB}
                |$TAB$methodMods float ${methodName}W($argsInSign) {
                |$TAB${TAB}return n${methodDef.name}W($argsInBody);
                |$TAB}
                """.trimMargin()
            }

            return methods
        }

        return """
        |$TAB$methodMods ${methodDef.result.type} $methodSignCall {
        |$TAB${TAB}return $methodCallNative;
        |$TAB}
        """.trimMargin()
    }

    private fun createMethodsNative(methodDef: ApiMethodDef, argGroup: ArgGroup): String {
        val argsInSign = argGroup.renderNativeInSignature()
        val argsInBody = argGroup.renderNativeInBody()

        val methodMods = if (methodDef.static) "private static native" else "private native"
        val methodName = "n${methodDef.name}"
        val methodSignCall = "n${methodDef.name}($argsInSign)"
        val methodCall = "${methodDef.receiver}${methodDef.name}($argsInBody)"

        if (methodDef.result == null) {
            return """
            |$TAB$methodMods void $methodSignCall; /*
            |$TAB$TAB$methodCall;
            |$TAB*/
            """.trimMargin()
        }

        if (methodDef.result is ApiResultStruct) {
            return """
            |$TAB$methodMods long $methodSignCall; /*
            |$TAB${TAB}return (intptr_t)${if (methodDef.result.isRef) "&" else ""}${methodCall};
            |$TAB*/
            """.trimMargin()
        }

        if (methodDef.result is ApiResultImVec) {
            val dstWithArgsInSign = "dst${if (argsInSign.isNotEmpty()) ", $argsInSign" else ""}"
            val resultCpyMethodName = "Jni::${methodDef.result.typeNative}Cpy"

            var methods = """
            |$TAB$methodMods void $methodName(${methodDef.result.type} $dstWithArgsInSign); /*
            |$TAB$TAB$resultCpyMethodName(env, $methodCall, dst);
            |$TAB*/
            |
            |$TAB$methodMods float ${methodName}X($argsInSign); /*
            |$TAB${TAB}return $methodCall.x;
            |$TAB*/
            |$TAB$methodMods float ${methodName}Y($argsInSign); /*
            |$TAB${TAB}return $methodCall.y;
            |$TAB*/
            """.trimMargin()

            if (methodDef.result is ApiResultImVec4) {
                methods += """
                |
                |$TAB$methodMods float ${methodName}Z($argsInSign); /*
                |$TAB${TAB}return $methodCall.z;
                |$TAB*/
                |$TAB$methodMods float ${methodName}W($argsInSign); /*
                |$TAB${TAB}return $methodCall.w;
                |$TAB*/
                """.trimMargin()
            }

            return methods
        }

        if (methodDef.result is ApiResultString) {
            return """
            |$TAB$methodMods ${methodDef.result.type} $methodSignCall; /*
            |$TAB${TAB}return env->NewStringUTF($methodCall);
            |$TAB*/
            """.trimMargin()
        }

        return """
        |$TAB$methodMods ${methodDef.result.type} $methodSignCall; /*
        |$TAB${TAB}return $methodCall;
        |$TAB*/
        """.trimMargin()
    }

    /**
     * Collect all available args into subgroups. That's required to handle optional args,
     * which a basically create a new arguments group without them.
     */
    private fun collectArgGroups(args: Collection<ApiArg>): Collection<ArgGroup> {
        val argGroups = mutableListOf<ArgGroup>()
        val group = mutableListOf<ApiArg>()
        for (arg in args) {
            // When the argument is optional we add a new group, which ends when the optional arg starts.
            // For example, if the "arg2" is optional we'll have: (arg1, arg2) -> (arg1) and (arg1, arg2)
            if (arg.optional) {
                argGroups.add(ArgGroup(group.toList()))
            }
            group.add(arg)
        }
        argGroups.add(ArgGroup(group))

        // Empty arguments is also a group.
        // While rendering it we will iterate through the empty list and won't render any args at all.
        if (argGroups.isEmpty()) {
            argGroups.add(ArgGroup())
        }

        // Process default arguments.
        argGroups.toList().forEach { argGroup ->
            argGroup.args.forEachIndexed { idx, arg ->
                arg.default?.let { defaultValue ->
                    // Ignore the default arg if it is optional and the last in the args list.
                    if (arg.optional && idx == argGroup.size - 1) {
                        return@let
                    }

                    val argsWithDefault = argGroup.args.toMutableList()
                    argsWithDefault[idx] = ApiArgDefault(defaultValue)
                    argGroups.add(ArgGroup(argsWithDefault))
                }
            }
        }

        return argGroups
    }

    private class ArgGroup(val args: Collection<ApiArg> = emptyList()) {
        val size: Int
            get() = args.size

        fun renderJavaInSignature(): String {
            return args.filter { it !is ApiArgDefault }.joinToString(transform = ApiArg::inSignatureJava)
        }

        fun renderJavaInBody(): String {
            return args.filter { it !is ApiArgDefault }.joinToString(transform = ApiArg::inBodyJava)
        }

        fun renderNativeInSignature(): String {
            return args.filter { it !is ApiArgDefault }.joinToString(transform = ApiArg::inSignatureNative)
        }

        fun renderNativeInBody(): String {
            return args.joinToString(transform = ApiArg::inBodyNative)
        }
    }
}

package tool.generator.api.metadata.def.imgui

import tool.generator.api.metadata.ApiMetadata

class ImGui : ApiMetadata() {
    init {
        receiver = "ImGui::"
        static = true

        // Context creation and access
        method("CreateContext", resultStruct("imgui.internal.ImGuiContext")) {
            argStruct("imgui.ImFontAtlas", "sharedFontAtlas", optional = true)
        }
        method("DestroyContext") {
            argStruct("imgui.internal.ImGuiContext", "ctx", optional = true)
        }
        method("GetCurrentContext", resultStruct("imgui.internal.ImGuiContext", static = true))
        method("SetCurrentContext") {
            argStruct("imgui.internal.ImGuiContext", "ctx")
        }

        // Main
        method("GetIO", resultStruct("imgui.ImGuiIO", static = true, isRef = true))
        method("GetStyle", resultStruct("imgui.ImGuiStyle", static = true, isRef = true))
        method("NewFrame")
        method("EndFrame")
        method("Render")
        method("GetDrawData", resultStruct("imgui.ImDrawData", static = true))

        // Demo, Debug, Information
        method("ShowDemoWindow") { argBooleanPtr("open", optional = true) }
        method("ShowMetricsWindow") { argBooleanPtr("open", optional = true) }
        method("ShowStackToolWindow") { argBooleanPtr("open", optional = true) }
        method("ShowStyleEditor") { argStruct("imgui.ImGuiStyle", "ref", optional = true) }
        method("ShowStyleSelector", resultBoolean()) { argString("label") }
        method("ShowFontSelector") { argString("label") }
        method("ShowUserGuide")
        method("GetVersion", resultString())

        // Styles
        method("StyleColorsDark") { argStruct("imgui.ImGuiStyle", "dst", optional = true) }
        method("StyleColorsLight") { argStruct("imgui.ImGuiStyle", "dst", optional = true) }
        method("StyleColorsClassic") { argStruct("imgui.ImGuiStyle", "dst", optional = true) }

        // Windows
        method("Begin", resultBoolean()) {
            argString("name")
            argBooleanPtr("pOpen", optional = true, default = "NULL")
            argInt("flags", optional = true)
        }
        method("End")

        // Child Windows
        method("BeginChild", resultBoolean()) {
            argString("strId")
            argImVec2("size", optional = true, default = "ImVec2(0, 0)")
            argBoolean("border", optional = true, default = "false")
            argInt("flags", optional = true)
        }
        method("BeginChild", resultBoolean()) {
            argInt("id")
            argImVec2("size", optional = true)
            argBoolean("border", optional = true)
            argInt("flags", optional = true)
        }
        method("EndChild")

        // Windows Utilities
        method("IsWindowAppearing", resultBoolean())
        method("IsWindowCollapsed", resultBoolean())
        method("IsWindowFocused", resultBoolean()) { argInt("flags", optional = true) }
        method("IsWindowHovered", resultBoolean()) { argInt("flags", optional = true) }
        method("GetWindowDrawList", resultStruct("imgui.ImDrawList"))
        method("GetWindowDpiScale", resultFloat())
        method("GetWindowPos", resultImVec2())
        method("GetWindowSize", resultImVec2())
        method("GetWindowWidth", resultFloat())
        method("GetWindowHeight", resultFloat())
        method("GetWindowViewport", resultStruct("imgui.ImGuiViewport"))

        // Window manipulation
        method("SetNextWindowPos") {
            argImVec2("pos")
            argInt("cond", optional = true)
            argImVec2("pivot", optional = true)
        }
        method("SetNextWindowSize") {
            argImVec2("size")
            argInt("cond", optional = true)
        }
        method("SetNextWindowSizeConstraints") {
            argImVec2("sizeMin")
            argImVec2("sizeMax")
        }
        method("SetNextWindowContentSize") { argImVec2("size") }
        method("SetNextWindowCollapsed") {
            argBoolean("collapsed")
            argInt("cond", optional = true)
        }
        method("SetNextWindowFocus")
        method("SetNextWindowBgAlpha") { argFloat("alpha") }
        method("SetNextWindowViewport") { argInt("viewportId") }
        method("SetWindowPos") {
            argImVec2("pos")
            argInt("cond", optional = true)
        }
        method("SetWindowSize") {
            argImVec2("size")
            argInt("cond", optional = true)
        }
        method("SetWindowCollapsed") {
            argBoolean("collapsed")
            argInt("cond", default = "ImGuiCond_None")
        }
        method("SetWindowCollapsed") {
            argString("name")
            argBoolean("collapsed")
            argInt("cond", default = "ImGuiCond_None")
        }
        method("SetWindowFocus")
        method("SetWindowFontScale") { argFloat("scale") }
        method("SetWindowPos") {
            argString("name")
            argImVec2("pos")
            argInt("cond", optional = true)
        }
        method("SetWindowSize") {
            argString("name")
            argImVec2("size")
            argInt("cond", optional = true)
        }
        method("SetWindowFocus") {
            argString("name")
        }

        // Content region
        method("GetContentRegionAvail", resultImVec2())
        method("GetContentRegionMax", resultImVec2())
        method("GetWindowContentRegionMin", resultImVec2())
        method("GetWindowContentRegionMax", resultImVec2())

        // Windows Scrolling
        method("GetScrollX", resultFloat())
        method("GetScrollY", resultFloat())
        method("SetScrollX") { argFloat("scrollX") }
        method("SetScrollY") { argFloat("scrollY") }
        method("GetScrollMaxX", resultFloat())
        method("GetScrollMaxY", resultFloat())
        method("SetScrollHereX") { argFloat("centerRatioX", optional = true) }
        method("SetScrollHereY") { argFloat("centerRatioY", optional = true) }
        method("SetScrollFromPosX") {
            argFloat("localX")
            argFloat("centerRatioX", optional = true)
        }
        method("SetScrollFromPosY") {
            argFloat("localY")
            argFloat("centerRatioY", optional = true)
        }

        // Parameters stacks (shared)
        method("PushFont") { argStruct("imgui.ImFont", "font") }
        method("PopFont")
        method("PushStyleColor") {
            argInt("idx")
            argInt("col")
        }
        method("PushStyleColor") {
            argInt("idx")
            argImVec4("col")
        }
        method("PopStyleColor") { argInt("count", optional = true) }
        method("PushStyleVar") {
            argInt("idx")
            argInt("col")
        }
        method("PushStyleVar") {
            argInt("idx")
            argImVec2("val")
        }
        method("PopStyleVar") { argInt("count", optional = true) }
        method("PushAllowKeyboardFocus") { argBoolean("allowKeyboardFocus") }
        method("PopAllowKeyboardFocus")
        method("PushButtonRepeat") { argBoolean("repeat") }
        method("PopButtonRepeat")

        // Parameters stacks (current window)
        method("PushItemWidth") { argFloat("itemWidth") }
        method("PopItemWidth")
        method("SetNextItemWidth") { argFloat("itemWidth") }
        method("CalcItemWidth", resultFloat())
        method("PushTextWrapPos") { argFloat("wrapLocalPosX", optional = true) }
        method("PopTextWrapPos")

        // Style read access
        method("GetFont", resultStruct("imgui.ImFont"))
        method("GetFontSize", resultFloat())
        method("GetFontTexUvWhitePixel", resultImVec2())
        method("GetColorU32", resultInt()) {
            argInt("idx")
            argFloat("alphaMul")
        }
        method("GetColorU32", resultInt()) { argImVec4("col") }
        method("GetStyleColorVec4", resultImVec4()) { argInt("idx") }

        // Cursor / Layout
        method("Separator")
        method("SameLine") {
            argFloat("offsetFromStart", optional = true)
            argFloat("spacing", optional = true)
        }
        method("NewLine")
        method("Spacing")
        method("Dummy") { argImVec2("size") }
        method("Indent") { argFloat("indentW", optional = true) }
        method("Unindent") { argFloat("indentW", optional = true) }
        method("BeginGroup")
        method("EndGroup")
        method("GetCursorPos", resultImVec2())
        method("SetCursorPos") { argImVec2("localPos") }
        method("SetCursorPosX") { argFloat("localX") }
        method("SetCursorPosY") { argFloat("localY") }
        method("GetCursorStartPos", resultImVec2())
        method("GetCursorScreenPos", resultImVec2())
        method("SetCursorScreenPos") { argImVec2("localPos") }
        method("AlignTextToFramePadding")
        method("GetTextLineHeight", resultFloat())
        method("GetTextLineHeightWithSpacing", resultFloat())
        method("GetFrameHeight", resultFloat())
        method("GetFrameHeightWithSpacing", resultFloat())

        // ID stack/scopes
        method("PushID") { argString("id") }
        method("PushID") { argInt("id") }
        method("PopID")
        method("GetID", resultInt()) { argString("id") }

        // Widgets: Text
        method("TextUnformatted") { argString("text") }
        method("Text") {
            argString("text")
            argNull()
        }
        method("TextColored") {
            argImVec4("col")
            argString("text")
            argNull()
        }
        method("TextDisabled") {
            argString("text")
            argNull()
        }
        method("TextWrapped") {
            argString("text")
            argNull()
        }
        method("LabelText") {
            argString("text")
            argNull()
        }
        method("BulletText") {
            argString("text")
            argNull()
        }

        // Widgets: Main
        method("Button", resultBoolean()) {
            argString("label")
            argImVec2("size", optional = true)
        }
        method("SmallButton", resultBoolean()) { argString("label") }
        method("InvisibleButton", resultBoolean()) {
            argString("id")
            argImVec2("size")
            argInt("flags", optional = true)
        }
        method("ArrowButton", resultBoolean()) {
            argString("id")
            argInt("dir")
        }
        method("Image") {
            argPrefix(argIntRaw("userTextureId"), "(ImTextureID)(intptr_t)")
            argImVec2("size")
            argImVec2("uv0", optional = true)
            argImVec2("uv1", optional = true)
            argImVec4("tintCol", optional = true)
            argImVec4("borderCol", optional = true)
        }
        method("ImageButton", resultBoolean()) {
            argPrefix(argIntRaw("userTextureId"), "(ImTextureID)(intptr_t)")
            argImVec2("size")
            argImVec2("uv0", optional = true)
            argImVec2("uv1", optional = true)
            argFloat("framePadding", optional = true)
            argImVec4("bgCol", optional = true)
            argImVec4("tintCol", optional = true)
        }
        method("Checkbox", resultBoolean()) {
            argString("label")
            argBooleanPtr("v")
        }
        method("CheckboxFlags", resultBoolean()) {
            argString("label")
            argIntPtr("flags")
            argInt("flagsValue")
        }
        method("RadioButton", resultBoolean()) {
            argString("label")
            argBoolean("active")
        }
        method("RadioButton", resultBoolean()) {
            argString("label")
            argIntPtr("v")
            argInt("vButton")
        }
        method("ProgressBar") {
            argFloat("fraction")
            argImVec2("sizeArg", optional = true, default = "ImVec2(-FLT_MIN, 0)")
            argString("overlay", optional = true)
        }
        method("Bullet")

        // Widgets: Combo Box
        method("BeginCombo", resultBoolean()) {
            argString("label")
            argString("previewValue")
            argInt("flags", optional = true)
        }
        method("EndCombo")
        method("Combo", resultBoolean()) {
            argString("label")
            argIntPtr("currentItem")
            argString("itemsSeparatedByZeros")
            argInt("popupMaxHeightInItems", optional = true)
        }

        // Widgets: Drag Sliders
        method("DragFloat", resultBoolean()) {
            argString("label")
            argFloatPtr("value")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("DragFloat", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("DragFloat2", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("DragFloat3", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("DragFloat4", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("DragFloatRange2", resultBoolean()) {
            argString("label")
            argFloatPtr("vCurrentMin")
            argFloatPtr("vCurrentMax")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true)
            argString("formatMax", optional = true)
            argInt("flags", optional = true)
        }
        method("DragFloatRange2", resultBoolean()) {
            argString("label")
            argFloatArr("vCurrentMin")
            argFloatArr("vCurrentMax")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true)
            argString("formatMax", optional = true)
            argInt("flags", optional = true)
        }
        method("DragInt", resultBoolean()) {
            argString("label")
            argIntPtr("value")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("DragInt", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("DragInt2", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("DragInt3", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("DragInt4", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("DragIntRange2", resultBoolean()) {
            argString("label")
            argIntPtr("vCurrentMin")
            argIntPtr("vCurrentMax")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true)
            argString("formatMax", optional = true)
            argInt("flags", optional = true)
        }
        method("DragIntRange2", resultBoolean()) {
            argString("label")
            argIntArr("vCurrentMin")
            argIntArr("vCurrentMax")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true)
            argString("formatMax", optional = true)
            argInt("flags", optional = true)
        }
        method("DragScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_Float")
            argFloatPtr("pData")
            argFloat("vSpeed", optional = true)
            argPrefix(argFloatRaw("pMin", optional = true), "&")
            argPrefix(argFloatRaw("pMax", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("DragScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S16")
            argShortPtr("pData")
            argFloat("vSpeed", optional = true)
            argPrefix(argShortRaw("pMin", optional = true), "&")
            argPrefix(argShortRaw("pMax", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("DragScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S32")
            argIntPtr("pData")
            argFloat("vSpeed", optional = true)
            argPrefix(argIntRaw("pMin", optional = true), "&")
            argPrefix(argIntRaw("pMax", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("DragScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S64")
            argLongPtr("pData")
            argFloat("vSpeed", optional = true)
            argPrefix(argLongRaw("pMin", optional = true), "&")
            argPrefix(argLongRaw("pMax", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }

        // Widgets: Regular Sliders
        method("SliderFloat", resultBoolean()) {
            argString("label")
            argFloatPtr("value")
            argFloat("vMin")
            argFloat("vMax")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("SliderFloat", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vMin")
            argFloat("vMax")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("SliderFloat2", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vMin")
            argFloat("vMax")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("SliderFloat3", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vMin")
            argFloat("vMax")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("SliderFloat4", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vMin")
            argFloat("vMax")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("SliderAngle", resultBoolean()) {
            argString("label")
            argFloatPtr("vRad")
            argFloat("vDegreesMin")
            argFloat("vDegreesMax")
            argString("format", optional = true, default = "\"%.0f deg\"")
            argInt("flags", optional = true)
        }
        method("SliderInt", resultBoolean()) {
            argString("label")
            argIntPtr("value")
            argInt("vMin")
            argInt("vMax")
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("SliderInt", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("vMin")
            argInt("vMax")
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("SliderInt2", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("vMin")
            argInt("vMax")
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("SliderInt3", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("vMin")
            argInt("vMax")
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("SliderInt4", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("vMin")
            argInt("vMax")
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("SliderScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_Float")
            argFloatPtr("pData")
            argPrefix(argFloatRaw("pMin"), "&")
            argPrefix(argFloatRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("SliderScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S16")
            argShortPtr("pData")
            argPrefix(argShortRaw("pMin"), "&")
            argPrefix(argShortRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("SliderScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S32")
            argIntPtr("pData")
            argPrefix(argIntRaw("pMin"), "&")
            argPrefix(argIntRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("SliderScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S64")
            argLongPtr("pData")
            argPrefix(argLongRaw("pMin"), "&")
            argPrefix(argLongRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("VSliderFloat", resultBoolean()) {
            argString("label")
            argImVec2("size")
            argFloatPtr("value")
            argFloat("vMin")
            argFloat("vMax")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("VSliderInt", resultBoolean()) {
            argString("label")
            argImVec2("size")
            argIntPtr("value")
            argInt("vMin")
            argInt("vMax")
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("VSliderScalar", resultBoolean()) {
            argString("label")
            argImVec2("size")
            argDefault("ImGuiDataType_Float")
            argFloatPtr("pData")
            argPrefix(argFloatRaw("pMin"), "&")
            argPrefix(argFloatRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("VSliderScalar", resultBoolean()) {
            argString("label")
            argImVec2("size")
            argDefault("ImGuiDataType_S16")
            argShortPtr("pData")
            argPrefix(argFloatRaw("pMin"), "&")
            argPrefix(argFloatRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("VSliderScalar", resultBoolean()) {
            argString("label")
            argImVec2("size")
            argDefault("ImGuiDataType_S32")
            argIntPtr("pData")
            argPrefix(argFloatRaw("pMin"), "&")
            argPrefix(argFloatRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("VSliderScalar", resultBoolean()) {
            argString("label")
            argImVec2("size")
            argDefault("ImGuiDataType_S64")
            argLongPtr("pData")
            argPrefix(argFloatRaw("pMin"), "&")
            argPrefix(argFloatRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }

        // Widgets: Input with Keyboard
        method("InputFloat", resultBoolean()) {
            argString("label")
            argFloatPtr("value")
            argFloat("step", optional = true)
            argFloat("stepFast", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("InputFloat", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("step", optional = true)
            argFloat("stepFast", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("InputFloat2", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("InputFloat3", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("InputFloat4", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("InputInt", resultBoolean()) {
            argString("label")
            argIntPtr("value")
            argInt("step", optional = true)
            argInt("stepFast", optional = true)
            argInt("flags", optional = true)
        }
        method("InputInt", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("step", optional = true)
            argInt("stepFast", optional = true)
            argInt("flags", optional = true)
        }
        method("InputInt2", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("flags", optional = true)
        }
        method("InputInt3", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("flags", optional = true)
        }
        method("InputInt4", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("flags", optional = true)
        }
        method("InputDouble", resultBoolean()) {
            argString("label")
            argDoublePtr("value")
            argDouble("step")
            argDouble("stepFast")
            argString("format", optional = true, default = "\"%.6f\"")
            argInt("flags", optional = true)
        }
        method("InputDouble", resultBoolean()) {
            argString("label")
            argDoubleArr("value")
            argDouble("step")
            argDouble("stepFast")
            argString("format", optional = true, default = "\"%.6f\"")
            argInt("flags", optional = true)
        }
        method("InputScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_Float")
            argFloatPtr("pData")
            argPrefix(argFloatRaw("pStep", optional = true), "&")
            argPrefix(argFloatRaw("pStepFast", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("InputScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S16")
            argShortPtr("pData")
            argPrefix(argFloatRaw("pStep", optional = true), "&")
            argPrefix(argFloatRaw("pStepFast", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("InputScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S32")
            argIntPtr("pData")
            argPrefix(argFloatRaw("pStep", optional = true), "&")
            argPrefix(argFloatRaw("pStepFast", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("InputScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S64")
            argLongPtr("pData")
            argPrefix(argFloatRaw("pStep", optional = true), "&")
            argPrefix(argFloatRaw("pStepFast", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }

        // Widgets: Color Editor/Picker
        method("ColorEdit3", resultBoolean()) {
            argString("label")
            argFloatArr("col")
            argInt("flags", optional = true)
        }
        method("ColorEdit4", resultBoolean()) {
            argString("label")
            argFloatArr("col")
            argInt("flags", optional = true)
        }
        method("ColorPicker3", resultBoolean()) {
            argString("label")
            argFloatArr("col")
            argInt("flags", optional = true)
        }
        method("ColorPicker4", resultBoolean()) {
            argString("label")
            argFloatArr("col")
            argInt("flags", optional = true)
            argFloatPtr("refCol", optional = true)
        }
        method("ColorButton", resultBoolean()) {
            argString("descId")
            argImVec4("col")
            argInt("flags", optional = true, default = "ImGuiColorEditFlags_None")
            argImVec2("size", optional = true)
        }
        method("SetColorEditOptions") {
            argInt("flags")
        }

        // Widgets: Trees
        method("TreeNode", resultBoolean()) {
            argString("label")
        }
        method("TreeNodeEx", resultBoolean()) {
            argString("label")
            argInt("flags", optional = true)
        }
        method("TreePush") {
            argString("strId")
        }
        method("TreePop")
        method("GetTreeNodeToLabelSpacing", resultFloat())
        method("CollapsingHeader", resultBoolean()) {
            argString("label")
            argInt("flags", optional = true)
        }
        method("CollapsingHeader", resultBoolean()) {
            argString("label")
            argBooleanPtr("pVisible")
            argInt("flags", optional = true)
        }
        method("SetNextItemOpen") {
            argBoolean("isOpen")
            argInt("cont", optional = true)
        }

        // Widgets: Selectables
        method("Selectable", resultBoolean()) {
            argString("label")
            argBoolean("selected", optional = true, default = "false")
            argInt("flags", optional = true, default = "ImGuiSelectableFlags_None")
            argImVec2("size", optional = true)
        }
        method("Selectable", resultBoolean()) {
            argString("label")
            argBooleanPtr("selected")
            argInt("flags", optional = true, default = "ImGuiSelectableFlags_None")
            argImVec2("size", optional = true)
        }

        // Widgets: List Boxes
        method("BeginListBox", resultBoolean()) {
            argString("label")
            argImVec2("size", optional = true)
        }
        method("EndListBox")

        // Widgets: Data Plotting
        method("PlotLines") {
            argString("label")
            argFloatArr("values")
            argInt("valuesCount")
            argInt("valuesOffset", optional = true, default = "0")
            argString("overlayText", optional = true, default = "NULL")
            argFloat("scaleMin", optional = true)
            argFloat("scaleMax", optional = true)
            argImVec2("graphSize", optional = true, default = "ImVec2(0, 0)")
            argInt("stride", optional = true)
        }
        method("PlotHistogram") {
            argString("label")
            argFloatArr("values")
            argInt("valuesCount")
            argInt("valuesOffset", default = "0")
            argString("overlayText", default = "NULL")
            argFloat("scaleMin", optional = true)
            argFloat("scaleMax", optional = true)
            argImVec2("graphSize", default = "ImVec2(0, 0)")
            argInt("stride", optional = true)
        }

        // Widgets: Value() Helpers.
        method("Value") {
            argString("prefix")
            argPrefix(argBooleanRaw("value"), "(bool)")
        }
        method("Value") {
            argString("prefix")
            argPrefix(argIntRaw("value"), "(int)")
        }
        method("Value") {
            argString("prefix")
            argPrefix(argLongRaw("value"), "(unsigned int)")
        }
        method("Value") {
            argString("prefix")
            argPrefix(argFloatRaw("value"), "(float)")
            argString("floatFormat", optional = true)
        }

        // Widgets: Menus
        method("BeginMenuBar", resultBoolean())
        method("EndMenuBar")
        method("BeginMainMenuBar", resultBoolean())
        method("EndMainMenuBar")
        method("BeginMenu", resultBoolean()) {
            argString("label")
            argBoolean("enabled", optional = true)
        }
        method("EndMenu")
        method("MenuItem", resultBoolean()) {
            argString("label")
            argString("shortcut", optional = true, default = "NULL")
            argBoolean("selected", optional = true)
            argBoolean("enabled", optional = true)
        }
        method("MenuItem", resultBoolean()) {
            argString("label")
            argString("shortcut", optional = true, default = "NULL")
            argBooleanPtr("selected")
            argBoolean("enabled", optional = true)
        }

        // Tooltips
        method("BeginTooltip")
        method("EndTooltip")
        method("SetTooltip") {
            argString("tooltip")
            argNull()
        }

        // Popups, Modals
        method("BeginPopup", resultBoolean()) {
            argString("strId")
            argInt("flags", optional = true)
        }
        method("BeginPopupModal", resultBoolean()) {
            argString("name")
            argBooleanPtr("pOpen", optional = true, default = "NULL")
            argInt("flags", optional = true)
        }
        method("EndPopup")

        // Popups: open/close functions
        method("OpenPopup") {
            argString("strId")
            argInt("popupFlags", optional = true)
        }
        method("OpenPopup") {
            argInt("id")
            argInt("popupFlags", optional = true)
        }
        method("OpenPopupOnItemClick") {
            argString("strId", optional = true, default = "NULL")
            argInt("popupFlags", optional = true)
        }
        method("CloseCurrentPopup")

        // Popups: open+begin combined functions helpers
        method("BeginPopupContextItem", resultBoolean()) {
            argString("strId", optional = true, default = "NULL")
            argInt("popupFlags", optional = true)
        }
        method("BeginPopupContextWindow", resultBoolean()) {
            argString("strId", optional = true, default = "NULL")
            argInt("popupFlags", optional = true)
        }
        method("BeginPopupContextVoid", resultBoolean()) {
            argString("strId", optional = true, default = "NULL")
            argInt("popupFlags", optional = true)
        }

        // Popups: query functions
        method("IsPopupOpen", resultBoolean()) {
            argString("strId")
            argInt("flags", optional = true)
        }

        // Tables
        method("BeginTable", resultBoolean()) {
            argString("strId")
            argInt("column")
            argInt("flags", optional = true, default = "ImGuiTableFlags_None")
            argImVec2("outerSize", optional = true, default = "ImVec2(0, 0)")
            argFloat("innerWidth", optional = true)
        }
        method("EndTable")
        method("TableNextRow") {
            argInt("rowFlags", optional = true, default = "ImGuiTableRowFlags_None")
            argFloat("minRowHeight", optional = true)
        }
        method("TableNextColumn", resultBoolean())
        method("TableSetColumnIndex", resultBoolean()) {
            argInt("columnN")
        }

        // Tables: Headers & Columns declaration
        method("TableSetupColumn") {
            argString("label")
            argInt("flags", optional = true, default = "ImGuiTableColumnFlags_None")
            argFloat("initWidthOrWeight", optional = true, default = "0.0f")
            argInt("userId", optional = true)
        }
        method("TableSetupScrollFreeze") {
            argInt("cols")
            argInt("rows")
        }
        method("TableHeadersRow")
        method("TableHeader") {
            argString("label")
        }

        // Tables: Sorting
        // TODO: TableGetSortSpecs

        // Tables: Miscellaneous functions
        method("TableGetColumnCount", resultInt())
        method("TableGetColumnIndex", resultInt())
        method("TableGetRowIndex", resultInt())
        method("TableGetColumnName", resultString()) {
            argInt("columnN", optional = true)
        }
        method("TableGetColumnFlags", resultInt()) {
            argInt("columnN", optional = true)
        }
        method("TableSetColumnEnabled") {
            argInt("columnN")
            argBoolean("value")
        }
        method("TableSetBgColor") {
            argInt("target")
            argInt("color")
            argInt("columnN", optional = true)
        }

        // Legacy Columns API (prefer using Tables!)
        method("Columns") {
            argInt("count", optional = true, default = "1")
            argString("id", optional = true, default = "NULL")
            argBoolean("border", optional = true)
        }
        method("NextColumn")
        method("GetColumnIndex", resultInt())
        method("GetColumnWidth", resultFloat()) {
            argInt("columnIndex", optional = true)
        }
        method("SetColumnWidth") {
            argInt("columnIndex")
            argFloat("width")
        }
        method("GetColumnOffset", resultFloat()) {
            argInt("columnIndex", optional = true)
        }
        method("SetColumnOffset") {
            argInt("columnIndex")
            argFloat("offsetX")
        }
        method("GetColumnsCount", resultInt())

        // Tab Bars, Tabs
        method("BeginTabBar", resultBoolean()) {
            argString("strId")
            argInt("flags", optional = true)
        }
        method("EndTabBar")
        method("BeginTabItem", resultBoolean()) {
            argString("label")
            argBooleanPtr("pOpen", optional = true, default = "NULL")
            argInt("flags", optional = true)
        }
        method("EndTabItem")
        method("TabItemButton", resultBoolean()) {
            argString("label")
            argInt("flags", optional = true)
        }
        method("SetTabItemClosed") {
            argString("tabOrDockedWindowLabel")
        }

        // Docking
        method("DockSpace", resultInt()) {
            argInt("id")
            argImVec2("size", optional = true, default = "ImVec2(0, 0)")
            argInt("flags", optional = true, default = "ImGuiDockNodeFlags_None")
            argStruct("imgui.ImGuiWindowClass", "windowClass", optional = true)
        }
        method("DockSpaceOverViewport", resultInt()) {
            argStruct("imgui.ImGuiViewport", "viewport", optional = true, default = "NULL")
            argInt("flags", optional = true, default = "ImGuiDockNodeFlags_None")
            argStruct("imgui.ImGuiWindowClass", "windowClass", optional = true)
        }
        method("SetNextWindowDockID") {
            argInt("dockId")
            argInt("cond", optional = true)
        }
        method("SetNextWindowClass") {
            argStruct("imgui.ImGuiWindowClass", "windowClass")
        }
        method("GetWindowDockID", resultInt())
        method("IsWindowDocked", resultBoolean())

        // Logging/Capture
        method("LogToTTY") {
            argInt("autoOpenDepth", optional = true)
        }
        method("LogToFile") {
            argInt("autoOpenDepth", optional = true, default = "-1")
            argString("filename", optional = true)
        }
        method("LogToClipboard") {
            argInt("autoOpenDepth", optional = true)
        }
        method("LogFinish")
        method("LogButtons")
        method("LogText") {
            argString("text")
            argNull()
        }

        // Drag and Drop
        method("BeginDragDropSource", resultBoolean()) {
            argInt("flags", optional = true)
        }
        method("EndDragDropSource")
        method("BeginDragDropTarget", resultBoolean())
        method("EndDragDropTarget")

        // Disabling [BETA API]
        method("BeginDisabled") {
            argBoolean("disabled", optional = true)
        }
        method("EndDisabled")

        // Clipping
        method("PushClipRect") {
            argImVec2("clipRectMin")
            argImVec2("clipRectMax")
            argBoolean("intersectWithCurrentClipRect")
        }
        method("PopClipRect")

        // Focus, Activation
        method("SetItemDefaultFocus")
        method("SetKeyboardFocusHere") {
            argInt("offset", optional = true)
        }

        // Item/Widgets Utilities and Query Functions
        method("IsItemHovered", resultBoolean()) {
            argInt("flags", optional = true)
        }
        method("IsItemActive", resultBoolean())
        method("IsItemFocused", resultBoolean())
        method("IsItemClicked", resultBoolean()) {
            argInt("mouseButton", optional = true)
        }
        method("IsItemVisible", resultBoolean())
        method("IsItemEdited", resultBoolean())
        method("IsItemActivated", resultBoolean())
        method("IsItemDeactivated", resultBoolean())
        method("IsItemDeactivatedAfterEdit", resultBoolean())
        method("IsItemToggledOpen", resultBoolean())
        method("IsAnyItemHovered", resultBoolean())
        method("IsAnyItemActive", resultBoolean())
        method("IsAnyItemFocused", resultBoolean())
        method("GetItemRectMin", resultImVec2())
        method("GetItemRectMax", resultImVec2())
        method("GetItemRectSize", resultImVec2())
        method("SetItemAllowOverlap")

        // Viewports
        method("GetMainViewport", resultStruct("imgui.ImGuiViewport", static = true))

        // Miscellaneous Utilities
        method("IsRectVisible", resultBoolean()) {
            argImVec2("size")
        }
        method("IsRectVisible", resultBoolean()) {
            argImVec2("rectMin")
            argImVec2("rectMax")
        }
        method("GetTime", resultDouble())
        method("GetFrameCount", resultInt())
        method("GetBackgroundDrawList", resultStruct("imgui.ImDrawList"))
        method("GetForegroundDrawList", resultStruct("imgui.ImDrawList"))
        method("GetBackgroundDrawList", resultStruct("imgui.ImDrawList")) {
            argStruct("imgui.ImGuiViewport", "viewport")
        }
        method("GetForegroundDrawList", resultStruct("imgui.ImDrawList")) {
            argStruct("imgui.ImGuiViewport", "viewport")
        }
        // TODO: GetDrawListSharedData
        method("GetStyleColorName", resultString()) {
            argInt("idx")
        }
        method("SetStateStorage") {
            argStruct("imgui.ImGuiStorage", "storage")
        }
        method("GetStateStorage", resultStruct("imgui.ImGuiStorage"))
        method("BeginChildFrame", resultBoolean()) {
            argInt("id")
            argImVec2("size")
            argInt("flags", optional = true)
        }
        method("EndChildFrame")

        // Text Utilities
        method("CalcTextSize", resultImVec2()) {
            argString("text")
            argString("textEnd", optional = true, default = "NULL")
            argBoolean("hideTextAfterDoubleHas", optional = true, default = "false")
            argFloat("wrapWidth", optional = true)
        }

        // Color Utilities
        method("ColorConvertU32ToFloat4", resultImVec4()) {
            argInt("in")
        }
        method("ColorConvertFloat4ToU32", resultInt()) {
            argImVec4("in")
        }
        method("ColorConvertRGBtoHSV") {
            argFloat("r")
            argFloat("g")
            argFloat("b")
            argFloatPtr("outH")
            argFloatPtr("outS")
            argFloatPtr("outV")
        }
        method("ColorConvertHSVtoRGB") {
            argFloat("h")
            argFloat("s")
            argFloat("v")
            argFloatPtr("outR")
            argFloatPtr("outG")
            argFloatPtr("outB")
        }
    }
}
